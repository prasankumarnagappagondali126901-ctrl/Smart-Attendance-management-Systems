package com.smartatt.core.dao.impl;

import com.smartatt.core.dao.StudentDAO;
import com.smartatt.core.db.DBUtil;
import com.smartatt.core.model.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public int create(Student s) throws Exception {
        String sql = "INSERT INTO students(enroll,name,dob,dept,semester,email) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getEnroll());
            ps.setString(2, s.getName());
            ps.setDate(3, java.sql.Date.valueOf(s.getDob()));
            ps.setString(4, s.getDept());
            ps.setInt(5, s.getSemester());
            ps.setString(6, s.getEmail());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Creating student failed, no rows affected.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    s.setStudentId(id);
                    return id;
                } else {
                    throw new SQLException("Creating student failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Optional<Student> findById(int id) throws Exception {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Student> findByEnroll(String enroll) throws Exception {
        String sql = "SELECT * FROM students WHERE enroll = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, enroll);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Student> findAll() throws Exception {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Student s) throws Exception {
        String sql = "UPDATE students SET enroll=?, name=?, dob=?, dept=?, semester=?, email=? WHERE student_id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getEnroll());
            ps.setString(2, s.getName());
            ps.setDate(3, java.sql.Date.valueOf(s.getDob()));
            ps.setString(4, s.getDept());
            ps.setInt(5, s.getSemester());
            ps.setString(6, s.getEmail());
            ps.setInt(7, s.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String delAttend = "DELETE FROM attendance WHERE student_id = ?";
        String delStudent = "DELETE FROM students WHERE student_id = ?";
        Connection c = null;
        try {
            c = DBUtil.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(delAttend)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }
            int affected;
            try (PreparedStatement ps2 = c.prepareStatement(delStudent)) {
                ps2.setInt(1, id);
                affected = ps2.executeUpdate();
            }
            c.commit();
            return affected > 0;
        } catch (Exception ex) {
            if (c != null) try { c.rollback(); } catch (Exception ignored) {}
            throw ex;
        } finally {
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (Exception ignored) {}
        }
    }


    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setEnroll(rs.getString("enroll"));
        s.setName(rs.getString("name"));
        Date d = rs.getDate("dob");
        if (d != null) s.setDob(d.toLocalDate());
        s.setDept(rs.getString("dept"));
        s.setSemester(rs.getInt("semester"));
        s.setEmail(rs.getString("email"));
        return s;
    }
}
