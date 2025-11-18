package com.smartatt.core.model;

import java.time.LocalDateTime;

public class AttendanceRecord {
    private int attendanceId;
    private int sessionId;
    private int studentId;
    private String status; // "present" / "absent" / "late"
    private LocalDateTime markedAt;
    private String remarks;

    public AttendanceRecord() {}

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    @Override
    public String toString() {
        return "AttendanceRecord[" + attendanceId + ", session=" + sessionId +
               ", student=" + studentId + ", status=" + status + "]";
    }
}
