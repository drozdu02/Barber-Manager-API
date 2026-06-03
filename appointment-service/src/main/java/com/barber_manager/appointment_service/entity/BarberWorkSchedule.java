package com.barber_manager.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "barber_work_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"barberId", "dayOfWeek"})
}, indexes = {
        @Index(name = "idx_work_schedules_barber", columnList = "barberId")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarberWorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long barberId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(nullable = false)
    private LocalTime openTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime closeTime;
}
