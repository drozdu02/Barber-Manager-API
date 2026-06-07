package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
