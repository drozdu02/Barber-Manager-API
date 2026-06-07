package com.barber_manager.appointment_service.communication.handlers;

import com.barber_manager.appointment_service.communication.NotificationDispatchService;
import com.barber_manager.appointment_service.events.AppointmentBookedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentBookedNotificationHandler {

    private final NotificationDispatchService notificationDispatchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentBooked(AppointmentBookedEvent event) {
        try {
            notificationDispatchService.dispatchBookingConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to dispatch booking confirmation for appointment id={}", event.appointmentId(), e);
        }
    }
}
