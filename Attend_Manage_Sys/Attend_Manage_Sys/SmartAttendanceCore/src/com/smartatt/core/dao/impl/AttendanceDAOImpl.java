package com.smartatt.core.dao.impl;

import com.smartatt.core.dao.AttendanceDAO;
import com.smartatt.core.db.DBUtil;
import com.smartatt.core.model.AttendanceRecord;
import com.smartatt.core.model.ClassSession;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {

    @Override
    public int createSession(ClassSession cs) throws Exception {
        String sql = "INSERT INTO class_session(subject_id, session_date, created_by) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cs.getSubjectId());
            ps.setDate(2, java.sql.Date.valueOf(cs.getSessionDate()));
            ps.setInt(3, cs.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new SQLException("Failed to create session");
        }
    }

    @Override
    public void markAttendanceBatch(int sessionId, List<AttendanceRecord> records) throws Exception {
        String sql = "INSERT INTO attendance(session_id, student_id, status, remarks) VALUES(?,?,?,?)";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = DBUtil.getConnection();
            c.setAutoCommit(false);
            ps = c.prepareStatement(sql);
            for (AttendanceRecord r : records) {
                ps.setInt(1, sessionId);
                ps.setInt(2, r.getStudentId());
                ps.setString(3, r.getStatus());
                ps.setString(4, r.getRemarks());
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
        } catch (Exception ex) {
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public List<AttendanceRecord> getAttendanceBySession(int sessionId) throws Exception {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE session_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();
                    r.setAttendanceId(rs.getInt("attendance_id"));
                    r.setSessionId(rs.getInt("session_id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setStatus(rs.getString("status"));
                    Timestamp t = rs.getTimestamp("marked_at");
                    if (t != null) r.setMarkedAt(t.toLocalDateTime());
                    r.setRemarks(rs.getString("remarks"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    @Override
    public double getStudentAttendancePercentage(int studentId, int subjectId) throws Exception {
        // simple approach: count student's present marks for subject / total sessions for that subject
        String presentSql = "SELECT COUNT(*) FROM attendance a JOIN class_session s ON a.session_id = s.session_id "
                + "WHERE a.student_id = ? AND s.subject_id = ? AND a.status = 'present'";
        String totalSql = "SELECT COUNT(*) FROM class_session WHERE subject_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement pp = c.prepareStatement(presentSql);
             PreparedStatement pt = c.prepareStatement(totalSql)) {
            pp.setInt(1, studentId); pp.setInt(2, subjectId);
            pt.setInt(1, subjectId);
            int present = 0, total = 0;
            try (ResultSet rs = pp.executeQuery()) { if (rs.next()) present = rs.getInt(1); }
            try (ResultSet rs2 = pt.executeQuery()) { if (rs2.next()) total = rs2.getInt(1); }
            if (total == 0) return 0.0;
            return (present * 100.0) / total;
        }
    }
}
