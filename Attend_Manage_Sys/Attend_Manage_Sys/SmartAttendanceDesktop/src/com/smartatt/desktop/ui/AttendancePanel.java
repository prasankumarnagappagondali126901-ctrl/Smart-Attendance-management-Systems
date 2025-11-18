package com.smartatt.desktop.ui;

import com.smartatt.core.dao.AttendanceDAO;
import com.smartatt.core.dao.StudentDAO;
import com.smartatt.core.dao.impl.AttendanceDAOImpl;
import com.smartatt.core.dao.impl.StudentDAOImpl;
import com.smartatt.core.model.AttendanceRecord;
import com.smartatt.core.model.ClassSession;
import com.smartatt.core.model.Student;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPanel-based Attendance UI: pick subject, date, load students, mark present/absent, save as a session.
 */
public class AttendancePanel extends JPanel {
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();

    private final JComboBox<SubjectItem> subjectCombo = new JComboBox<>();
    private final JDateChooser dateChooser = new JDateChooser();
    private final AttendanceTableModel tableModel = new AttendanceTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel status = new JLabel(" ");

    public AttendancePanel() {
        setLayout(new BorderLayout(6,6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Subject:"));
        top.add(subjectCombo);
        top.add(new JLabel("Date:"));
        dateChooser.setDateFormatString("yyyy-MM-dd");
        top.add(dateChooser);
        JButton load = new JButton("Load Students");
        JButton save = new JButton("Save Attendance");
        top.add(load); top.add(save);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(status, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        load.addActionListener(e -> loadStudents());
        save.addActionListener(e -> saveAttendance());

        // load subjects list (reuse DB read)
        loadSubjects();

        // default to today
        dateChooser.setDate(java.sql.Date.valueOf(LocalDate.now()));
        table.removeColumn(table.getColumnModel().getColumn(0));
    }

    private void loadSubjects() {
        subjectCombo.removeAllItems();
        try (var conn = com.smartatt.core.db.DBUtil.getConnection();
             var st = conn.createStatement();
             var rs = st.executeQuery("SELECT subject_id, name FROM subjects ORDER BY name")) {
            while (rs.next()) {
                subjectCombo.addItem(new SubjectItem(rs.getInt("subject_id"), rs.getString("name")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load subjects: " + e.getMessage());
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentDAO.findAll();
            tableModel.setStudents(students);
            status.setText("Loaded " + students.size() + " students");
        } catch (Exception ex) {
            showError("Load failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void saveAttendance() {
    SubjectItem subj = (SubjectItem) subjectCombo.getSelectedItem();
    if (subj == null) { JOptionPane.showMessageDialog(this, "Select subject"); return; }
    java.util.Date d = dateChooser.getDate();
    if (d == null) { JOptionPane.showMessageDialog(this, "Select date"); return; }

    ClassSession cs = new ClassSession();
    cs.setSubjectId(subj.id);
    cs.setSessionDate(LocalDate.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault()));
    cs.setCreatedBy(1); // demo

    try {
        int sessionId = attendanceDAO.createSession(cs);
        List<AttendanceRecord> recs = new ArrayList<>();
        for (AttendanceTableModel.Row r : tableModel.getRows()) {
            AttendanceRecord ar = new AttendanceRecord();
            ar.setSessionId(sessionId);
            ar.setStudentId(r.student.getStudentId());
            ar.setStatus(r.present ? "present" : "absent");
            ar.setRemarks("");
            recs.add(ar);
        }
        attendanceDAO.markAttendanceBatch(sessionId, recs);
        status.setText("Saved attendance for session " + sessionId);
        JOptionPane.showMessageDialog(this, "Attendance saved (session " + sessionId + ").");
    } catch (Exception ex) {
        showError("Save failed: " + ex.getMessage());
        ex.printStackTrace();
    }
}


    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg);
        status.setText("Error: " + msg);
    }

    // Table model and row holder
    private static class AttendanceTableModel extends AbstractTableModel {
        private final java.util.List<Row> rows = new ArrayList<>();
        private final String[] cols = {"ID","Enroll","Name","Present"};

        public void setStudents(List<Student> students) {
            rows.clear();
            for (Student s : students) rows.add(new Row(s));
            fireTableDataChanged();
        }
        public java.util.List<Row> getRows() { return rows; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Row row = rows.get(r);
            return switch (c) {
                case 0 -> row.student.getStudentId();
                case 1 -> row.student.getEnroll();
                case 2 -> row.student.getName();
                case 3 -> row.present;
                default -> null;
            };
        }
        @Override public Class<?> getColumnClass(int c) { return c == 3 ? Boolean.class : Object.class; }
        @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        @Override public void setValueAt(Object val, int r, int c) {
            if (c == 3) {
                rows.get(r).present = (Boolean) val;
                fireTableCellUpdated(r, c);
            }
        }

        private static class Row {
            final Student student;
            boolean present;
            Row(Student s) { this.student = s; present = false; }
        }

    }
    
    private static class SubjectItem {
        int id;
        String name;
        SubjectItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
