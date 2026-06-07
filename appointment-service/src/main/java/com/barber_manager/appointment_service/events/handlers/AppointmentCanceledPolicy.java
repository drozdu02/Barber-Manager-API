package com.barber_manager.appointment_service.events.handlers;

import com.barber_manager.appointment_service.events.AppointmentCanceledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class AppointmentCanceledPolicy {

    /**
     * Slots become available again because the appointment aggregate is marked canceled
     * before this event is published (overlap queries exclude canceled appointments).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentCanceled(AppointmentCanceledEvent event) {
        log.info(
                "Slots released for appointment id={} barberId={} window=[{} - {}] source={}",
                event.appointmentId(),
                event.barberId(),
                event.startTime(),
                event.endTime(),
                event.source()
        );
    }
}
