package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.dto.AvailabilitySlotResponse;
import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.entity.ServiceOffering;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final int SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BarberBreakRepository barberBreakRepository;
    private final BarberWorkScheduleRepository workScheduleRepository;
    private final BarberTimeOffRepository timeOffRepository;

    public AvailabilityResponse getAvailability(
            LocalDate date,
            Long serviceId,
            Long barberId,
            boolean any,
            List<Long> barberIds
    ) {
        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service offering not found."));

        int durationMinutes = service.getSlotCount() * SLOT_MINUTES;

        if (any) {
            if (barberIds == null || barberIds.isEmpty()) {
                throw new BusinessRuleException("When any=true you must provide barberIds.");
            }
            List<AvailabilitySlotResponse> slots = buildMergedSlots(barberIds, date, durationMinutes);
            return new AvailabilityResponse(date, serviceId, null, true, slots);
        }

        if (barberId == null) {
            throw new BusinessRuleException("barberId is required when any=false.");
        }

        List<AvailabilitySlotResponse> slots = buildSlotsForBarber(barberId, date, durationMinutes);
        return new AvailabilityResponse(date, serviceId, barberId, false, slots);
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
