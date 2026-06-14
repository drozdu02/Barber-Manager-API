/**
 * Testy pisane metodą TDD (Test-Driven Development).
 * <p>
 * Każda klasa opisuje jedną funkcjonalność biznesową. Kolejność metod testowych
 * ({@link org.junit.jupiter.api.Order}) odzwierciedla cykl:
 * <ol>
 *   <li><b>Red</b> — najpierw test definiujący oczekiwane zachowanie (na początku pada),</li>
 *   <li><b>Green</b> — implementacja minimalnego kodu spełniającego test,</li>
 *   <li><b>Refactor</b> — kolejne testy rozszerzają reguły bez zmiany wcześniejszego kontraktu.</li>
 * </ol>
 * Struktura Given / When / Then w każdej metodzie.
 */
package com.barber_manager.appointment_service.tdd;
