package com.barber_manager.appointment_service.entity;

import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEvent;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client_phone_profiles", indexes = {
        @Index(name = "idx_client_phone_profile_number", columnList = "phoneNumber", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientPhoneProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 9, max = 9)
    @Pattern(regexp = "^[0-9]*$", message = "Phone number must contain only digits.")
    @Column(nullable = false, unique = true, length = 16)
    private String phoneNumber;

    @Column(nullable = false)
    private int noShowCount = 0;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(length = 512)
    private String blockReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void registerNoShow() {
        this.noShowCount++;
    }

    public boolean shouldAutoBlock(int threshold) {
        return !blocked && noShowCount >= threshold;
    }

    public void block(
            UUID eventId,
            LocalDateTime occurredAt,
            String reason,
            boolean automatic,
            List<Long> futureAppointmentIds
    ) {
        if (blocked) {
            return;
        }
        this.blocked = true;
        this.blockReason = reason;
        registerEvent(new ClientBlockedEvent(
                eventId,
                occurredAt,
                id,
                phoneNumber,
                reason,
                automatic,
                futureAppointmentIds
        ));
    }

    public void unblock() {
        this.blocked = false;
        this.blockReason = null;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }
}
