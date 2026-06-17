package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.dto.BarberAssignment;
import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.BarberServiceCompetencyRepository;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.schedule.domain.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @InjectMocks
    private AvailabilityService availabilityService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BarberBreakRepository barberBreakRepository;

    @Mock
    private BarberWorkScheduleRepository workScheduleRepository;

    @Mock
    private BarberTimeOffRepository timeOffRepository;

    @Mock
    private BarberServiceCompetencyRepository competencyRepository;

    @Test
    void shouldReturnAvailabilityForSpecificBarber() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        Service service = service(1L, 1);
        BarberWorkSchedule schedule = new BarberWorkSchedule(null, 10L, date.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(12, 0));

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(competencyRepository.existsByServiceId(1L)).thenReturn(false);
        when(timeOffRepository.findActiveOnDate(10L, date)).thenReturn(List.of());
        when(workScheduleRepository.findByBarberIdAndDayOfWeek(10L, date.getDayOfWeek())).thenReturn(Optional.of(schedule));
        when(appointmentRepository.findOverlapping(eq(10L), any(), any())).thenReturn(List.of());
        when(barberBreakRepository.findOverlapping(eq(10L), any(), any())).thenReturn(List.of());

        AvailabilityResponse response = availabilityService.getAvailability(date, 1L, 10L, false, null);

        assertEquals(date, response.date());
        assertEquals(10L, response.barberId());
        assertFalse(response.any());
        assertFalse(response.slots().isEmpty());
        assertEquals(LocalDateTime.of(2026, 6, 10, 9, 0), response.slots().getFirst().startTime());
    }

    @Test
    void shouldReturnMergedAvailabilityForAnyBarber() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        Service service = service(1L, 1);
        BarberWorkSchedule schedule = new BarberWorkSchedule(null, 10L, date.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(10, 0));

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(competencyRepository.existsByServiceId(1L)).thenReturn(false);
        when(timeOffRepository.findActiveOnDate(any(), eq(date))).thenReturn(List.of());
        when(workScheduleRepository.findByBarberIdAndDayOfWeek(any(), eq(date.getDayOfWeek()))).thenReturn(Optional.of(schedule));
        when(appointmentRepository.findOverlapping(any(), any(), any())).thenReturn(List.of());
        when(barberBreakRepository.findOverlapping(any(), any(), any())).thenReturn(List.of());

        AvailabilityResponse response = availabilityService.getAvailability(date, 1L, null, true, List.of(10L, 11L));

        assertTrue(response.any());
        assertNull(response.barberId());
        assertFalse(response.slots().isEmpty());
    }

    @Test
    void shouldFilterBarbersByCompetency() {
        when(competencyRepository.existsByServiceId(1L)).thenReturn(true);
        when(competencyRepository.findBarberIdsByServiceId(1L)).thenReturn(List.of(10L));

        List<Long> competent = availabilityService.filterCompetentBarbers(1L, List.of(10L, 11L));

        assertEquals(List.of(10L), competent);
    }

    @Test
    void shouldResolveBarberForSlot() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 10, 10, 0);
        LocalDateTime end = start.plusMinutes(30);

        when(competencyRepository.existsByServiceId(1L)).thenReturn(false);
        when(appointmentRepository.findOverlapping(10L, start, end)).thenReturn(List.of());
        when(barberBreakRepository.findOverlapping(10L, start, end)).thenReturn(List.of());

        Optional<Long> barberId = availabilityService.resolveBarberForSlot(1L, List.of(10L, 11L), start, end);

        assertEquals(Optional.of(10L), barberId);
    }

    @Test
    void shouldFindEarliestAssignment() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        LocalDateTime anchor = LocalDateTime.of(2026, 6, 10, 8, 0);
        Service service = service(1L, 1);
        BarberWorkSchedule schedule = new BarberWorkSchedule(null, 10L, date.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(12, 0));

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(competencyRepository.existsByServiceId(1L)).thenReturn(false);
        when(timeOffRepository.findActiveOnDate(10L, date)).thenReturn(List.of());
        when(workScheduleRepository.findByBarberIdAndDayOfWeek(10L, date.getDayOfWeek())).thenReturn(Optional.of(schedule));
        when(appointmentRepository.findOverlapping(eq(10L), any(), any())).thenReturn(List.of());
        when(barberBreakRepository.findOverlapping(eq(10L), any(), any())).thenReturn(List.of());

        Optional<BarberAssignment> assignment = availabilityService.findEarliestAssignment(1L, List.of(10L), anchor);

        assertTrue(assignment.isPresent());
        assertEquals(10L, assignment.get().barberId());
        assertEquals(LocalDateTime.of(2026, 6, 10, 9, 0), assignment.get().startTime());
    }

    @Test
    void shouldRejectAvailabilityWhenServiceMissing() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> availabilityService.getAvailability(LocalDate.now(), 99L, 10L, false, null)
        );
    }

    @Test
    void shouldRejectAvailabilityWhenBarberMissingForSpecificMode() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service(1L, 1)));

        assertThrows(
                BusinessRuleException.class,
                () -> availabilityService.getAvailability(LocalDate.now(), 1L, null, false, null)
        );
    }

    @Test
    void shouldRejectAvailabilityWhenBarberIdsMissingForAnyMode() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service(1L, 1)));

        assertThrows(
                BusinessRuleException.class,
                () -> availabilityService.getAvailability(LocalDate.now(), 1L, null, true, List.of())
        );
    }

    @Test
    void shouldRejectIncompetentBarberForSpecificAvailability() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service(1L, 1)));
        when(competencyRepository.existsByServiceId(1L)).thenReturn(true);
        when(competencyRepository.existsByBarberIdAndServiceId(11L, 1L)).thenReturn(false);

        assertThrows(
                BusinessRuleException.class,
                () -> availabilityService.getAvailability(date, 1L, 11L, false, null)
        );
    }

    private Service service(Long id, int slotCount) {
        Service service = new Service();
        service.setId(id);
        service.setSlotCount(slotCount);
        service.setName("Haircut");
        return service;
    }
}
