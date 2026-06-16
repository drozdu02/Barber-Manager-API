package com.barber_manager.appointment_service.booking.port.in;

import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
import com.barber_manager.appointment_service.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentController {

    AppointmentResponse create(CreateAppointmentRequest request);

    void cancelByBookingToken(String bookingToken);

    StaffAppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status);

    StaffAppointmentResponse getStaffDetails(Long appointmentId);

    List<StaffAppointmentResponse> getBarberCalendar(Long barberId, LocalDateTime from, LocalDateTime to);
}
