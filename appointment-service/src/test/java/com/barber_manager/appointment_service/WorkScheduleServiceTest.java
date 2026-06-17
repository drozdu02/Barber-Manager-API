package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.dto.admin.CreateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.WorkScheduleResponse;
import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.service.WorkScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceTest {

    @InjectMocks
    private WorkScheduleService workScheduleService;

    @Mock
    private BarberWorkScheduleRepository workScheduleRepository;

    @Mock
    private BarberTimeOffRepository timeOffRepository;

    @Mock
    private BarberBreakRepository barberBreakRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Test
    void shouldCreateWorkSchedule() {
        CreateWorkScheduleRequest request = new CreateWorkScheduleRequest(
                10L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        when(workScheduleRepository.findByBarberIdAndDayOfWeek(10L, DayOfWeek.MONDAY)).thenReturn(Optional.empty());
        when(appointmentRepository.findAllByBarberIdAndCanceledFalseAndStartTimeAfter(eq(10L), any()))
                .thenReturn(List.of());
        when(workScheduleRepository.save(any(BarberWorkSchedule.class))).thenAnswer(invocation -> {
            BarberWorkSchedule schedule = invocation.getArgument(0);
            schedule.setId(1L);
            return schedule;
        });

        WorkScheduleResponse response = workScheduleService.createWorkSchedule(request);

        assertEquals(10L, response.barberId());
        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
        verify(workScheduleRepository).save(any(BarberWorkSchedule.class));
    }

    @Test
    void shouldRejectDuplicateWorkSchedule() {
        CreateWorkScheduleRequest request = new CreateWorkScheduleRequest(
                10L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        when(workScheduleRepository.findByBarberIdAndDayOfWeek(10L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(new BarberWorkSchedule()));

        assertThrows(BusinessRuleException.class, () -> workScheduleService.createWorkSchedule(request));
        verify(workScheduleRepository, never()).save(any());
    }
}
