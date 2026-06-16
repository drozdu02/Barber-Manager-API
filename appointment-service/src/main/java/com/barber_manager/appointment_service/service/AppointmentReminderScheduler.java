package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import com.barber_manager.appointment_service.communication.NotificationDispatchService;
import com.barber_manager.appointment_service.config.ReminderProperties;
import com.barber_manager.appointment_service.entity.Appointment;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "appointment.reminder.enabled", havingValue = "true", matchIfMissing = true)
public class AppointmentReminderScheduler {

    private final IAppointmentRepository appointmentRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final ReminderProperties reminderProperties;

    @Scheduled(fixedDelayString = "${appointment.reminder.fixed-delay-ms:900000}")
    @Transactional
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sendBefore = now.plusHours(reminderProperties.getHoursBefore());

        List<Appointment> due = appointmentRepository.findDueForReminder(now, sendBefore);
        for (Appointment appointment : due) {
            try {
                notificationDispatchService.dispatchAppointmentReminder(
                        appointment.getEmail(),
                        appointment.getFirstName(),
                        appointment.getService().getName(),
                        appointment.getBookingToken(),
                        appointment.getStartTime().toString()
                );
                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);
            } catch (MessagingException e) {
                log.error("Failed to send reminder for appointment id={}", appointment.getId(), e);
            }
        }
    }
}
