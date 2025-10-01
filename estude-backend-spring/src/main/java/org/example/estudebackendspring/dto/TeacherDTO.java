package org.example.estudebackendspring.dto;

import java.util.Date;

public class TeacherDTO {
    private Long userId;
    private String teacherCode;
    private String fullName;
    private Date hireDate;
    private Date endDate;
    private boolean isAdmin;
    private boolean isHomeroomTeacher;

    public TeacherDTO(Long UserId,String teacherCode, String fullName, Date hireDate, Date endDate,
                      boolean isAdmin, boolean isHomeroomTeacher) {
        this.userId = UserId;
        this.teacherCode = teacherCode;
        this.fullName = fullName;
        this.hireDate = hireDate;
        this.endDate = endDate;
        this.isAdmin = isAdmin;
        this.isHomeroomTeacher = isHomeroomTeacher;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isHomeroomTeacher() {
        return isHomeroomTeacher;
    }

    public void setHomeroomTeacher(boolean homeroomTeacher) {
        isHomeroomTeacher = homeroomTeacher;
    }
}