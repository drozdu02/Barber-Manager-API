package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.dto.AvailabilitySlotResponse;
import com.barber_manager.appointment_service.entity.ServiceOffering;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final int SLOT_MINUTES = 30;
    private static final LocalTime OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(17, 0);

    private final AppointmentRepository appointmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BarberBreakRepository barberBreakRepository;

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
        LocalDateTime dayStart = date.atTime(OPEN_TIME);
        LocalDateTime dayEnd = date.atTime(CLOSE_TIME);

        if (any) {
            if (barberIds == null || barberIds.isEmpty()) {
                throw new BusinessRuleException("When any=true you must provide barberIds.");
            }
            List<AvailabilitySlotResponse> slots = new ArrayList<>();
            LocalDateTime cursor = dayStart;
            while (!cursor.plusMinutes(durationMinutes).isAfter(dayEnd)) {
                LocalDateTime start = cursor;
                LocalDateTime end = cursor.plusMinutes(durationMinutes);
                List<Long> available = barberIds.stream()
                        .filter(id -> isFree(id, start, end))
                        .sorted(Comparator.naturalOrder())
                        .toList();
                if (!available.isEmpty()) {
                    slots.add(new AvailabilitySlotResponse(start, end, available));
                }
                cursor = cursor.plusMinutes(SLOT_MINUTES);
            }
            return new AvailabilityResponse(date, serviceId, null, true, slots);
        }

        if (barberId == null) {
            throw new BusinessRuleException("barberId is required when any=false.");
        }

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

        return new AvailabilityResponse(date, serviceId, barberId, false, slots);
    }

    private boolean isFree(Long barberId, LocalDateTime start, LocalDateTime end) {
        boolean noAppointments = appointmentRepository.findOverlapping(barberId, start, end).isEmpty();
        boolean noBreaks = barberBreakRepository.findOverlapping(barberId, start, end).isEmpty();
        return noAppointments && noBreaks;
    }
}

