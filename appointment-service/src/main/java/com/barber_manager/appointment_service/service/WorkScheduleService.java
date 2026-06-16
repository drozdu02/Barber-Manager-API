package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import com.barber_manager.appointment_service.dto.admin.CreateBreakRequest;
import com.barber_manager.appointment_service.dto.admin.CreateTimeOffRequest;
import com.barber_manager.appointment_service.dto.admin.CreateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.ReplaceWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.TimeOffResponse;
import com.barber_manager.appointment_service.dto.admin.UpdateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.WorkScheduleResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.BarberBreak;
import com.barber_manager.appointment_service.entity.BarberTimeOff;
import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.schedule.port.in.IWorkScheduleController;
import com.barber_manager.appointment_service.schedule.port.out.IBarberBreakRepository;
import com.barber_manager.appointment_service.schedule.port.out.IBarberTimeOffRepository;
import com.barber_manager.appointment_service.schedule.port.out.IWorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService implements IWorkScheduleController {

    private final IWorkScheduleRepository workScheduleRepository;
    private final IBarberTimeOffRepository timeOffRepository;
    private final IBarberBreakRepository barberBreakRepository;
    private final IAppointmentRepository appointmentRepository;

    @Override
    public List<WorkScheduleResponse> listWorkSchedules(Long barberId) {
        return workScheduleRepository.findAllByBarberIdOrderByDayOfWeekAsc(barberId).stream()
                .map(this::toWorkScheduleResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkScheduleResponse createWorkSchedule(CreateWorkScheduleRequest request) {
        validateHours(request.openTime(), request.closeTime());
        if (workScheduleRepository.findByBarberIdAndDayOfWeek(request.barberId(), request.dayOfWeek()).isPresent()) {
            throw new BusinessRuleException("Work schedule already exists for this barber and day.");
        }
        ensureNoAppointmentsOutsideHours(request.barberId(), request.dayOfWeek(), request.openTime(), request.closeTime());

        BarberWorkSchedule schedule = new BarberWorkSchedule();
        schedule.setBarberId(request.barberId());
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setOpenTime(request.openTime());
        schedule.setCloseTime(request.closeTime());
        return toWorkScheduleResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public WorkScheduleResponse updateWorkSchedule(Long id, UpdateWorkScheduleRequest request) {
        validateHours(request.openTime(), request.closeTime());
        BarberWorkSchedule schedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work schedule not found."));
        ensureNoAppointmentsOutsideHours(
                schedule.getBarberId(),
                schedule.getDayOfWeek(),
                request.openTime(),
                request.closeTime()
        );

        schedule.setOpenTime(request.openTime());
        schedule.setCloseTime(request.closeTime());
        return toWorkScheduleResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public List<WorkScheduleResponse> replaceWorkSchedules(ReplaceWorkScheduleRequest request) {
        long distinctDays = request.entries().stream()
                .map(ReplaceWorkScheduleRequest.DayScheduleEntry::dayOfWeek)
                .distinct()
                .count();
        if (distinctDays != request.entries().size()) {
            throw new BusinessRuleException("Duplicate dayOfWeek in work schedule entries.");
        }

        List<BarberWorkSchedule> existing = workScheduleRepository.findAllByBarberIdOrderByDayOfWeekAsc(request.barberId());
        Set<DayOfWeek> newDays = request.entries().stream()
                .map(ReplaceWorkScheduleRequest.DayScheduleEntry::dayOfWeek)
                .collect(Collectors.toSet());

        for (BarberWorkSchedule old : existing) {
            if (!newDays.contains(old.getDayOfWeek())) {
                ensureNoAppointmentsOnDay(request.barberId(), old.getDayOfWeek());
            }
        }

        for (ReplaceWorkScheduleRequest.DayScheduleEntry entry : request.entries()) {
            validateHours(entry.openTime(), entry.closeTime());
            ensureNoAppointmentsOutsideHours(
                    request.barberId(),
                    entry.dayOfWeek(),
                    entry.openTime(),
                    entry.closeTime()
            );
        }

        workScheduleRepository.deleteAllByBarberId(request.barberId());
        return request.entries().stream()
                .map(entry -> {
                    BarberWorkSchedule schedule = new BarberWorkSchedule();
                    schedule.setBarberId(request.barberId());
                    schedule.setDayOfWeek(entry.dayOfWeek());
                    schedule.setOpenTime(entry.openTime());
                    schedule.setCloseTime(entry.closeTime());
                    return toWorkScheduleResponse(workScheduleRepository.save(schedule));
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteWorkSchedule(Long id) {
        BarberWorkSchedule schedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work schedule not found."));
        ensureNoAppointmentsOnDay(schedule.getBarberId(), schedule.getDayOfWeek());
        workScheduleRepository.delete(schedule);
    }

    @Override
    public List<TimeOffResponse> listTimeOff(Long barberId) {
        return timeOffRepository.findAllByBarberIdOrderByStartDateAsc(barberId).stream()
                .map(this::toTimeOffResponse)
                .toList();
    }

    @Override
    @Transactional
    public TimeOffResponse createTimeOff(CreateTimeOffRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessRuleException("endDate must be on or after startDate.");
        }
        ensureNoAppointmentsInDateRange(request.barberId(), request.startDate(), request.endDate());

        BarberTimeOff timeOff = new BarberTimeOff();
        timeOff.setBarberId(request.barberId());
        timeOff.setStartDate(request.startDate());
        timeOff.setEndDate(request.endDate());
        timeOff.setReason(request.reason());
        return toTimeOffResponse(timeOffRepository.save(timeOff));
    }

    @Override
    @Transactional
    public void deleteTimeOff(Long id) {
        if (!timeOffRepository.existsById(id)) {
            throw new NotFoundException("Time off entry not found.");
        }
        timeOffRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BarberBreak createBreak(CreateBreakRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessRuleException("endTime must be after startTime.");
        }
        BarberBreak barberBreak = new BarberBreak(null, request.barberId(), request.startTime(), request.endTime());
        return barberBreakRepository.save(barberBreak);
    }

    @Override
    @Transactional
    public void deleteBreak(Long id) {
        if (barberBreakRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Break not found.");
        }
        barberBreakRepository.deleteById(id);
    }

    private void validateHours(LocalTime openTime, LocalTime closeTime) {
        if (!closeTime.isAfter(openTime)) {
            throw new BusinessRuleException("closeTime must be after openTime.");
        }
    }

    private void ensureNoAppointmentsOutsideHours(
            Long barberId,
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        List<Appointment> futureAppointments = appointmentRepository
                .findAllByBarberIdAndCanceledFalseAndStartTimeAfter(barberId, LocalDateTime.now());

        for (Appointment appointment : futureAppointments) {
            if (appointment.getStartTime().getDayOfWeek() != dayOfWeek) {
                continue;
            }
            LocalTime start = appointment.getStartTime().toLocalTime();
            LocalTime end = appointment.getEndTime().toLocalTime();
            if (start.isBefore(openTime) || end.isAfter(closeTime)) {
                throw new BusinessRuleException(
                        "Cannot change work schedule: active appointments exist outside the new hours."
                );
            }
        }
    }

    private void ensureNoAppointmentsOnDay(Long barberId, DayOfWeek dayOfWeek) {
        List<Appointment> futureAppointments = appointmentRepository
                .findAllByBarberIdAndCanceledFalseAndStartTimeAfter(barberId, LocalDateTime.now());

        boolean hasConflict = futureAppointments.stream()
                .anyMatch(a -> a.getStartTime().getDayOfWeek() == dayOfWeek);
        if (hasConflict) {
            throw new BusinessRuleException(
                    "Cannot remove work schedule: active appointments exist on this day."
            );
        }
    }

    private void ensureNoAppointmentsInDateRange(Long barberId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(LocalTime.of(23, 59, 59));

        List<Appointment> appointments = appointmentRepository
                .findAllByBarberIdAndCanceledFalseAndStartTimeBetween(barberId, rangeStart, rangeEnd);

        if (!appointments.isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot add time off: active appointments exist in the selected date range."
            );
        }
    }

    private WorkScheduleResponse toWorkScheduleResponse(BarberWorkSchedule schedule) {
        return new WorkScheduleResponse(
                schedule.getId(),
                schedule.getBarberId(),
                schedule.getDayOfWeek(),
                schedule.getOpenTime(),
                schedule.getCloseTime()
        );
    }

    private TimeOffResponse toTimeOffResponse(BarberTimeOff timeOff) {
        return new TimeOffResponse(
                timeOff.getId(),
                timeOff.getBarberId(),
                timeOff.getStartDate(),
                timeOff.getEndDate(),
                timeOff.getReason()
        );
    }
}
