package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "barber_breaks", indexes = {
        @Index(name = "idx_barber_breaks_barber_start", columnList = "barberId,startTime"),
        @Index(name = "idx_barber_breaks_start_end", columnList = "startTime,endTime")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarberBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long barberId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime endTime;
}

