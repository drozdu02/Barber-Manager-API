package com.barber_manager.appointment_service.events.handlers;

import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.events.AppointmentCanceledEvent;
import com.barber_manager.appointment_service.events.CancellationSource;
import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientBlockedPolicy {

    private final IAppointmentRepository appointmentRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClientBlocked(ClientBlockedEvent event) {
        for (Long appointmentId : event.futureAppointmentIds()) {
            appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                if (appointment.isCanceled()) {
                    return;
                }
                appointment.setCanceled(true);
                appointment.setStatus(AppointmentStatus.CANCELED);
                Appointment saved = appointmentRepository.save(appointment);
                publishCanceled(saved);
            });
        }
        log.info(
                "Canceled {} future appointments after client block phoneNumber={} automatic={}",
                event.futureAppointmentIds().size(),
                event.phoneNumber(),
                event.automatic()
        );
    }

    private void publishCanceled(Appointment appointment) {
        domainEventPublisher.publish(new AppointmentCanceledEvent(
                domainEventPublisher.newEventId(),
                domainEventPublisher.now(),
                appointment.getId(),
                appointment.getBarberId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getPhoneNumber(),
                appointment.getBookingToken(),
                CancellationSource.CLIENT_BLOCK_CASCADE
        ));
    }
}
