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
    private Integer sectionStart;
    private Integer sectionEnd;

    // 节次对应的时间映射
    private static final LocalTime[] SECTION_START_TIMES = {
        null, // 占位，索引从1开始
        LocalTime.of(8, 0),   // 第1节
        LocalTime.of(8, 50),  // 第2节
        LocalTime.of(10, 0),  // 第3节
        LocalTime.of(10, 50), // 第4节
        LocalTime.of(14, 0),  // 第5节
        LocalTime.of(14, 50), // 第6节
        LocalTime.of(16, 0),  // 第7节
        LocalTime.of(16, 50), // 第8节
        LocalTime.of(19, 0),  // 第9节
        LocalTime.of(19, 50), // 第10节
        LocalTime.of(21, 0),  // 第11节
        LocalTime.of(21, 50)  // 第12节
    };

    private static final LocalTime[] SECTION_END_TIMES = {
        null,
        LocalTime.of(8, 50),
        LocalTime.of(9, 40),
        LocalTime.of(10, 50),
        LocalTime.of(11, 40),
        LocalTime.of(14, 50),
        LocalTime.of(15, 40),
        LocalTime.of(16, 50),
        LocalTime.of(17, 40),
        LocalTime.of(19, 50),
        LocalTime.of(20, 40),
        LocalTime.of(21, 50),
        LocalTime.of(22, 40)
    };

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
        dto.setSectionStart(schedule.getSectionStart());
        dto.setSectionEnd(schedule.getSectionEnd());
        return dto;
    }

    public CourseSchedule toEntity() {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(this.id);
        schedule.setCourseId(this.courseId);
        schedule.setDayOfWeek(this.dayOfWeek);
        schedule.setClassroom(this.classroom);
        schedule.setWeekStart(this.weekStart);
        schedule.setWeekEnd(this.weekEnd);
        schedule.setWeekType(this.weekType);
        schedule.setSectionStart(this.sectionStart);
        schedule.setSectionEnd(this.sectionEnd);

        // 根据节次自动计算时间
        if (this.sectionStart != null && this.sectionStart >= 1 && this.sectionStart <= 12) {
            schedule.setStartTime(SECTION_START_TIMES[this.sectionStart]);
        } else if (this.startTime != null) {
            schedule.setStartTime(this.startTime);
        } else {
            schedule.setStartTime(LocalTime.of(8, 0)); // 默认
        }

        if (this.sectionEnd != null && this.sectionEnd >= 1 && this.sectionEnd <= 12) {
            schedule.setEndTime(SECTION_END_TIMES[this.sectionEnd]);
        } else if (this.endTime != null) {
            schedule.setEndTime(this.endTime);
        } else {
            schedule.setEndTime(LocalTime.of(9, 40)); // 默认
        }

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

    public Integer getSectionStart() {
        return sectionStart;
    }

    public void setSectionStart(Integer sectionStart) {
        this.sectionStart = sectionStart;
    }

    public Integer getSectionEnd() {
        return sectionEnd;
    }

    public void setSectionEnd(Integer sectionEnd) {
        this.sectionEnd = sectionEnd;
    }
}
