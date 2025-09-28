package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.ScheduleDTO;
import org.example.estudebackendspring.entity.Schedule;
import org.example.estudebackendspring.repository.ScheduleRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public ScheduleDTO createSchedule(Schedule schedule) {
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return mapToDTO(savedSchedule);
    }

    public ScheduleDTO updateSchedule(Long id, Schedule scheduleDetails) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch học với id này: " + id));

        // Update fields (only non-null fields or as per requirement)
        if (scheduleDetails.getWeek() != null) {
            schedule.setWeek(scheduleDetails.getWeek());
        }
        if (scheduleDetails.getDetails() != null) {
            schedule.setDetails(scheduleDetails.getDetails());
        }
        if (scheduleDetails.getDate() != null) {
            schedule.setDate(scheduleDetails.getDate());
        }
        if (scheduleDetails.getStartPeriod() != null) {
            schedule.setStartPeriod(scheduleDetails.getStartPeriod());
        }
        if (scheduleDetails.getEndPeriod() != null) {
            schedule.setEndPeriod(scheduleDetails.getEndPeriod());
        }
        if (scheduleDetails.getRoom() != null) {
            schedule.setRoom(scheduleDetails.getRoom());
        }
        if (scheduleDetails.getStatus() != null) {
            schedule.setStatus(scheduleDetails.getStatus());
        }
        if (scheduleDetails.getTerm() != null) {
            schedule.setTerm(scheduleDetails.getTerm());
        }
        if (scheduleDetails.getClassSubject() != null) {
            schedule.setClassSubject(scheduleDetails.getClassSubject());
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return mapToDTO(updatedSchedule);
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new NoSuchElementException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    public List<ScheduleDTO> getSchedulesForTeacher(Long teacherId) {
        List<Schedule> schedules = scheduleRepository.findSchedulesForTeacher(teacherId);
        return schedules.stream().map(schedule -> mapToDTO(schedule)).collect(Collectors.toList());
    }

    public List<ScheduleDTO> getSchedulesForStudent(Long studentId) {
        List<Schedule> schedules = scheduleRepository.findSchedulesForStudent(studentId);
        return schedules.stream().map(schedule -> mapToDTO(schedule)).collect(Collectors.toList());

    }

    private ScheduleDTO mapToDTO(Schedule schedule) {
        // Initialize lazy-loaded relationships
        Hibernate.initialize(schedule.getTerm());
        Hibernate.initialize(schedule.getClassSubject());
        if (schedule.getClassSubject() != null) {
            Hibernate.initialize(schedule.getClassSubject().getSubject());
            Hibernate.initialize(schedule.getClassSubject().getTeacher());
            Hibernate.initialize(schedule.getClassSubject().getTerm());
            if (schedule.getClassSubject().getTerm() != null) {
                Hibernate.initialize(schedule.getClassSubject().getTerm().getClazz());
            }
        }

        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setWeek(schedule.getWeek());
        dto.setDetails(schedule.getDetails());
        dto.setDate(schedule.getDate());
        dto.setStartPeriod(schedule.getStartPeriod());
        dto.setEndPeriod(schedule.getEndPeriod());
        dto.setRoom(schedule.getRoom());
        dto.setStatus(schedule.getStatus());

        // Map Term
        ScheduleDTO.TermDTO termDTO = new ScheduleDTO.TermDTO();
        termDTO.setTermId(schedule.getTerm() != null ? schedule.getTerm().getTermId() : null);
        termDTO.setName(schedule.getTerm() != null ? schedule.getTerm().getName() : null);
        termDTO.setBeginDate(schedule.getTerm() != null ? schedule.getTerm().getBeginDate() : null);
        termDTO.setEndDate(schedule.getTerm() != null ? schedule.getTerm().getEndDate() : null);
        dto.setTerm(termDTO);

        // Map ClassSubject
        ScheduleDTO.ClassSubjectDTO classSubjectDTO = new ScheduleDTO.ClassSubjectDTO();
        classSubjectDTO.setClassSubjectId(schedule.getClassSubject() != null ? schedule.getClassSubject().getClassSubjectId() : null);
        classSubjectDTO.setSubjectId(schedule.getClassSubject() != null && schedule.getClassSubject().getSubject() != null
                ? schedule.getClassSubject().getSubject().getSubjectId() : null);
        classSubjectDTO.setSubjectName(schedule.getClassSubject() != null && schedule.getClassSubject().getSubject() != null
                ? schedule.getClassSubject().getSubject().getName() : null);
        classSubjectDTO.setClassId(schedule.getClassSubject() != null && schedule.getClassSubject().getTerm() != null
                && schedule.getClassSubject().getTerm().getClazz() != null
                ? schedule.getClassSubject().getTerm().getClazz().getClassId() : null);
        classSubjectDTO.setClassName(schedule.getClassSubject() != null && schedule.getClassSubject().getTerm() != null
                && schedule.getClassSubject().getTerm().getClazz() != null
                ? schedule.getClassSubject().getTerm().getClazz().getName() : null);
        classSubjectDTO.setTeacherId(schedule.getClassSubject() != null && schedule.getClassSubject().getTeacher() != null
                ? schedule.getClassSubject().getTeacher().getUserId() : null);
        classSubjectDTO.setTeacherName(schedule.getClassSubject() != null && schedule.getClassSubject().getTeacher() != null
                ? schedule.getClassSubject().getTeacher().getFullName() : null);
        dto.setClassSubject(classSubjectDTO);

        return dto;
    }
}