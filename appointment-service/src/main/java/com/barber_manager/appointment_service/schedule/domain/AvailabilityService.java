package com.barber_manager.appointment_service.schedule.domain;

import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.BarberServiceCompetencyRepository;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.dto.AvailabilitySlotResponse;
import com.barber_manager.appointment_service.dto.BarberAssignment;
import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.schedule.port.in.IAvailabilityController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AvailabilityService implements IAvailabilityController {

    private static final int SLOT_MINUTES = 30;
    private static final int SEARCH_HORIZON_DAYS = 60;

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final BarberBreakRepository barberBreakRepository;
    private final BarberWorkScheduleRepository workScheduleRepository;
    private final BarberTimeOffRepository timeOffRepository;
    private final BarberServiceCompetencyRepository competencyRepository;

    @Override
    public AvailabilityResponse getAvailability(
            LocalDate date,
            Long serviceId,
            Long barberId,
            boolean anyAvailable,
            List<Long> anyAvailableBarberIds
    ) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found."));

        int durationMinutes = service.getSlotCount() * SLOT_MINUTES;

        if (anyAvailable) {
            if (anyAvailableBarberIds == null || anyAvailableBarberIds.isEmpty()) {
                throw new BusinessRuleException(
                        "When anyAvailable=true you must provide anyAvailableBarberIds."
                );
            }
            List<Long> competentBarbers = filterCompetentBarbers(serviceId, anyAvailableBarberIds);
            List<AvailabilitySlotResponse> slots = buildMergedSlots(competentBarbers, date, durationMinutes);
            return new AvailabilityResponse(date, serviceId, null, true, slots);
        }

        if (barberId == null) {
            throw new BusinessRuleException("barberId is required when anyAvailable=false.");
        }

        ensureBarberCompetent(serviceId, barberId);
        List<AvailabilitySlotResponse> slots = buildSlotsForBarber(barberId, date, durationMinutes);
        return new AvailabilityResponse(date, serviceId, barberId, false, slots);
    }

    public List<Long> filterCompetentBarbers(Long serviceId, List<Long> barberIds) {
        if (barberIds == null || barberIds.isEmpty()) {
            return List.of();
        }
        if (!competencyRepository.existsByServiceId(serviceId)) {
            return barberIds;
        }

        Set<Long> competent = new HashSet<>(competencyRepository.findBarberIdsByServiceId(serviceId));
        return barberIds.stream()
                .filter(competent::contains)
                .toList();
    }

    public Optional<Long> resolveBarberForSlot(
            Long serviceId,
            List<Long> barberIds,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<Long> competentBarbers = filterCompetentBarbers(serviceId, barberIds);
        for (Long candidate : competentBarbers) {
            if (isFree(candidate, start, end)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    public Optional<BarberAssignment> findEarliestAssignment(
            Long serviceId,
            List<Long> barberIds,
            LocalDateTime searchFrom
    ) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found."));

        List<Long> competentBarbers = filterCompetentBarbers(serviceId, barberIds);
        if (competentBarbers.isEmpty()) {
            return Optional.empty();
        }

        int durationMinutes = service.getSlotCount() * SLOT_MINUTES;
        LocalDateTime anchor = searchFrom != null ? searchFrom : LocalDateTime.now();

        for (int dayOffset = 0; dayOffset < SEARCH_HORIZON_DAYS; dayOffset++) {
            LocalDate date = anchor.toLocalDate().plusDays(dayOffset);
            for (AvailabilitySlotResponse slot : buildMergedSlots(competentBarbers, date, durationMinutes)) {
                if (!slot.startTime().isBefore(anchor) && !slot.availableBarberIds().isEmpty()) {
                    Long assignedBarberId = slot.availableBarberIds().getFirst();
                    return Optional.of(new BarberAssignment(assignedBarberId, slot.startTime(), slot.endTime()));
                }
            }
        }
        return Optional.empty();
    }

    private void ensureBarberCompetent(Long serviceId, Long barberId) {
        if (!competencyRepository.existsByServiceId(serviceId)) {
            return;
        }
        if (!competencyRepository.existsByBarberIdAndServiceId(barberId, serviceId)) {
            throw new BusinessRuleException("Barber cannot perform this service.");
        }
    }

    private List<AvailabilitySlotResponse> buildMergedSlots(
            List<Long> barberIds,
            LocalDate date,
            int durationMinutes
    ) {
        Map<LocalDateTime, List<Long>> slotToBarbers = new LinkedHashMap<>();

        for (Long id : barberIds) {
            for (AvailabilitySlotResponse slot : buildSlotsForBarber(id, date, durationMinutes)) {
                slotToBarbers.merge(
                        slot.startTime(),
                        new ArrayList<>(List.of(id)),
                        (existing, added) -> {
                            existing.addAll(added);
                            existing.sort(Comparator.naturalOrder());
                            return existing;
                        }
                );
            }
        }

        return slotToBarbers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDateTime start = entry.getKey();
                    LocalDateTime end = start.plusMinutes(durationMinutes);
                    return new AvailabilitySlotResponse(start, end, entry.getValue());
                })
                .toList();
    }

    private List<AvailabilitySlotResponse> buildSlotsForBarber(
            Long barberId,
            LocalDate date,
            int durationMinutes
    ) {
        Optional<WorkingWindow> window = resolveWorkingWindow(barberId, date);
        if (window.isEmpty()) {
            return List.of();
        }

        LocalDateTime dayStart = window.get().start();
        LocalDateTime dayEnd = window.get().end();

        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        LocalDateTime cursor = dayStart;
        while (!cursor.plusMinutes(durationMinutes).isAfter(dayEnd)) {
            LocalDateTime start = cursor;
            LocalDateTime end = cursor.plusMinutes(durationMinutes);
            if (isFree(barberId, start, end)) {
                slots.add(new AvailabilitySlotResponse(start, end, List.of(barberId)));
            }
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }

    private Optional<WorkingWindow> resolveWorkingWindow(Long barberId, LocalDate date) {
        if (!timeOffRepository.findActiveOnDate(barberId, date).isEmpty()) {
            return Optional.empty();
        }

        Optional<BarberWorkSchedule> schedule = workScheduleRepository.findByBarberIdAndDayOfWeek(
                barberId,
                date.getDayOfWeek()
        );
        return schedule.map(s -> new WorkingWindow(date.atTime(s.getOpenTime()), date.atTime(s.getCloseTime())));
    }

    private boolean isFree(Long barberId, LocalDateTime start, LocalDateTime end) {
        boolean noAppointments = appointmentRepository.findOverlapping(barberId, start, end).isEmpty();
        boolean noBreaks = barberBreakRepository.findOverlapping(barberId, start, end).isEmpty();
        return noAppointments && noBreaks;
    }

    private record WorkingWindow(LocalDateTime start, LocalDateTime end) {
    }
}
