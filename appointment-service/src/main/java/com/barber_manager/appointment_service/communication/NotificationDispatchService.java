package com.barber_manager.appointment_service.communication;

import com.barber_manager.appointment_service.events.AppointmentBookedEvent;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Communication context entry point. Booking publishes {@link AppointmentBookedEvent};
 * this service dispatches notifications without Booking calling mail infrastructure directly.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final MailService mailService;

    public void dispatchBookingConfirmation(AppointmentBookedEvent event) throws MessagingException {
        mailService.sendConfirmationEmail(
                event.email(),
                event.firstName(),
                event.bookingToken(),
                event.startTime().toString()
        );
    }

    public void dispatchAppointmentReminder(
            String email,
            String customerName,
            String serviceName,
            String bookingToken,
            String startTime
    ) throws MessagingException {
        mailService.sendReminderEmail(email, customerName, serviceName, bookingToken, startTime);
    }
}
