package com.example.backend.dto;

import com.example.backend.entity.CourseSchedule;
import java.time.LocalTime;

public class ScheduleDTO {
    private Long id;
    private Long courseId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;
    private Integer weekStart;
    private Integer weekEnd;
    private String weekType;

    public static ScheduleDTO fromEntity(CourseSchedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setCourseId(schedule.getCourseId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setClassroom(schedule.getClassroom());
        dto.setWeekStart(schedule.getWeekStart());
        dto.setWeekEnd(schedule.getWeekEnd());
        dto.setWeekType(schedule.getWeekType());
        return dto;
    }

    public CourseSchedule toEntity() {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(this.id);
        schedule.setCourseId(this.courseId);
        schedule.setDayOfWeek(this.dayOfWeek);
        schedule.setStartTime(this.startTime);
        schedule.setEndTime(this.endTime);
        schedule.setClassroom(this.classroom);
        schedule.setWeekStart(this.weekStart);
        schedule.setWeekEnd(this.weekEnd);
        schedule.setWeekType(this.weekType);
        return schedule;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public Integer getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(Integer weekStart) {
        this.weekStart = weekStart;
    }

    public Integer getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(Integer weekEnd) {
        this.weekEnd = weekEnd;
    }

    public String getWeekType() {
        return weekType;
    }

    public void setWeekType(String weekType) {
        this.weekType = weekType;
    }
}
