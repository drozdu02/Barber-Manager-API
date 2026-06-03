package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "barber_time_off", indexes = {
        @Index(name = "idx_time_off_barber_dates", columnList = "barberId,startDate,endDate")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarberTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long barberId;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 512)
    private String reason;
}
