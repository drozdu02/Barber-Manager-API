package com.barber_manager.appointment_service.schedule.port.in;

import com.barber_manager.appointment_service.dto.admin.CreateBreakRequest;
import com.barber_manager.appointment_service.dto.admin.CreateTimeOffRequest;
import com.barber_manager.appointment_service.dto.admin.CreateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.ReplaceWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.TimeOffResponse;
import com.barber_manager.appointment_service.dto.admin.UpdateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.WorkScheduleResponse;
import com.barber_manager.appointment_service.entity.BarberBreak;

import java.util.List;

public interface IWorkScheduleController {

    List<WorkScheduleResponse> listWorkSchedules(Long barberId);

    WorkScheduleResponse createWorkSchedule(CreateWorkScheduleRequest request);

    WorkScheduleResponse updateWorkSchedule(Long id, UpdateWorkScheduleRequest request);

    List<WorkScheduleResponse> replaceWorkSchedules(ReplaceWorkScheduleRequest request);

    void deleteWorkSchedule(Long id);

    List<TimeOffResponse> listTimeOff(Long barberId);

    TimeOffResponse createTimeOff(CreateTimeOffRequest request);

    void deleteTimeOff(Long id);

    BarberBreak createBreak(CreateBreakRequest request);

    void deleteBreak(Long id);
}
