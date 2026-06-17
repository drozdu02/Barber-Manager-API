package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import com.barber_manager.appointment_service.repository.NoShowIncidentRepository;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "eureka.client.enabled=false",
        "appointment.reminder.enabled=false",
        "email.username=test@example.com",
        "email.password=test-password",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class AppointmentServiceApplicationTests {

    @MockitoBean
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private ServiceRepository serviceRepository;

    @MockitoBean
    private BarberBreakRepository barberBreakRepository;

    @MockitoBean
    private BarberWorkScheduleRepository barberWorkScheduleRepository;

    @MockitoBean
    private BarberTimeOffRepository barberTimeOffRepository;

    @MockitoBean
    private NoShowIncidentRepository noShowIncidentRepository;

    @MockitoBean
    private ClientPhoneProfileRepository clientPhoneProfileRepository;

    @Test
    void contextLoads() {
    }
}
