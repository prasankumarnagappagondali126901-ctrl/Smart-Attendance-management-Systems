package com.smartatt.desktop.ui;

import com.smartatt.core.db.DBUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;

/**
 * Simple report panel: pick subject and date range, list sessions and stats.
 */
public class AttendanceReportPanel extends JPanel {
    private final JComboBox<SubjectItem> subjectCombo = new JComboBox<>();
    private final JDateChooser fromDate = new JDateChooser();
    private final JDateChooser toDate = new JDateChooser();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Session ID", "Date", "Present", "Absent", "Percent Present"}, 0);
    private final JTable table = new JTable(model);

    public AttendanceReportPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Subject:"));
        top.add(subjectCombo);
        top.add(new JLabel("From:"));
        fromDate.setDateFormatString("yyyy-MM-dd");
        top.add(fromDate);
        top.add(new JLabel("To:"));
        toDate.setDateFormatString("yyyy-MM-dd");
        top.add(toDate);
        JButton load = new JButton("Load Sessions");
        top.add(load);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        
        table.removeColumn(table.getColumnModel().getColumn(0));

        load.addActionListener(e -> loadSessions());
        loadSubjects();
    }

    private void loadSubjects() {
        try (Connection c = DBUtil.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT subject_id, name FROM subjects ORDER BY name")) {
            subjectCombo.removeAllItems();
            while (rs.next()) {
                subjectCombo.addItem(new SubjectItem(rs.getInt("subject_id"), rs.getString("name")));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + ex.getMessage());
        }
    }

    private void loadSessions() {
        SubjectItem subj = (SubjectItem) subjectCombo.getSelectedItem();
        if (subj == null) return;
        LocalDate from = dateToLocal(fromDate.getDate());
        LocalDate to = dateToLocal(toDate.getDate());
        // default to wide range
        if (from == null) from = LocalDate.of(2000,1,1);
        if (to == null) to = LocalDate.now();

        model.setRowCount(0);
        String sql = "SELECT s.session_id, s.session_date, " +
                "SUM(CASE WHEN a.status='present' THEN 1 ELSE 0 END) as present_count, " +
                "SUM(CASE WHEN a.status='absent' THEN 1 ELSE 0 END) as absent_count, " +
                "COUNT(a.attendance_id) as total " +
                "FROM class_session s LEFT JOIN attendance a ON s.session_id = a.session_id " +
                "WHERE s.subject_id = ? AND s.session_date BETWEEN ? AND ? " +
                "GROUP BY s.session_id, s.session_date ORDER BY s.session_date DESC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, subj.id);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sid = rs.getInt("session_id");
                    Date d = rs.getDate("session_date");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");
                    int total = rs.getInt("total");
                    double pct = total == 0 ? 0.0 : (present * 100.0) / total;
                    model.addRow(new Object[]{sid, d.toString(), present, absent, String.format("%.1f%%", pct)});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading sessions: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static LocalDate dateToLocal(java.util.Date d) {
        if (d == null) return null;
        return new java.sql.Date(d.getTime()).toLocalDate();
    }

    private static class SubjectItem {
        int id; String name;
        SubjectItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
