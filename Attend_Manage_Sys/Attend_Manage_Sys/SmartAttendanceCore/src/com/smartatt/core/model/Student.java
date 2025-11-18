package com.smartatt.core.model;

import java.time.LocalDate;

public class Student {
    private int studentId;
    private String enroll;
    private String name;
    private LocalDate dob;
    private String dept;
    private int semester;
    private String email;

    public Student() {}

    public Student(String enroll, String name, LocalDate dob, String dept, int semester, String email) {
        this.enroll = enroll;
        this.name = name;
        this.dob = dob;
        this.dept = dept;
        this.semester = semester;
        this.email = email;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getEnroll() { return enroll; }
    public void setEnroll(String enroll) { this.enroll = enroll; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Student[" + studentId + ", " + enroll + ", " + name + "]";
    }
}
