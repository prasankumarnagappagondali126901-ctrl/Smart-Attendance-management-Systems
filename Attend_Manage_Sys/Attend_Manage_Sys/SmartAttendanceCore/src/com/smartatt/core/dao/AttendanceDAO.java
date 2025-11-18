package com.smartatt.core.dao;

import com.smartatt.core.model.AttendanceRecord;
import com.smartatt.core.model.ClassSession;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceDAO {
    int createSession(ClassSession cs) throws Exception; // returns generated session_id
    void markAttendanceBatch(int sessionId, List<AttendanceRecord> records) throws Exception; // transactional
    List<AttendanceRecord> getAttendanceBySession(int sessionId) throws Exception;
    double getStudentAttendancePercentage(int studentId, int subjectId) throws Exception; // optional calc
}
