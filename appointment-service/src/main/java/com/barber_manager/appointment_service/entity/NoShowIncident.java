package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "no_show_incidents", indexes = {
        @Index(name = "idx_no_show_incident_phone", columnList = "phoneNumber"),
        @Index(name = "idx_no_show_incident_appointment", columnList = "appointmentId", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoShowIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 9, max = 9)
    @Pattern(regexp = "^[0-9]*$", message = "Phone number must contain only digits.")
    @Column(nullable = false, length = 16)
    private String phoneNumber;

    @NotNull
    @Column(nullable = false, unique = true)
    private Long appointmentId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime appointmentStartTime;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;
}
