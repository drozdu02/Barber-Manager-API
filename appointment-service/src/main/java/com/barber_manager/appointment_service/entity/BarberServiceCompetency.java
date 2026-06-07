package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "barber_service_competencies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barber_id", "service_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarberServiceCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barber_id", nullable = false)
    private Long barberId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;
}
