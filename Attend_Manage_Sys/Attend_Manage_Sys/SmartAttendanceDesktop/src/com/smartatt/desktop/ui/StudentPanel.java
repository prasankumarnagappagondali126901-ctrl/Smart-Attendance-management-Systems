package com.smartatt.desktop.ui;

import com.smartatt.core.dao.StudentDAO;
import com.smartatt.core.dao.impl.StudentDAOImpl;
import com.smartatt.core.model.Student;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * JPanel-based Student manager (list, add, edit, delete).
 * Lightweight, intended to be shown inside a CardLayout.
 */
public class StudentPanel extends JPanel {
    private final StudentDAO dao = new StudentDAOImpl();
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel statusLabel = new JLabel(" ");

    public StudentPanel() {
        setLayout(new BorderLayout(6,6));
        model = new DefaultTableModel(new Object[]{"ID","Enroll","Name","DOB","Dept","Sem","Email"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        table.removeColumn(table.getColumnModel().getColumn(0));

        // Toolbar
        JPanel top = new JPanel(new BorderLayout(6,6));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(24);
        left.add(new JLabel("Search:"));
        left.add(search);
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        right.add(refresh); right.add(add); right.add(edit); right.add(delete);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        bottom.add(statusLabel, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        // actions
        refresh.addActionListener(e -> loadStudents());
        add.addActionListener(e -> addStudent());
        edit.addActionListener(e -> editStudent());
        delete.addActionListener(e -> deleteStudent());

        // live search
        search.getDocument().addDocumentListener(new DocumentListener() {
            void f() {
                String t = search.getText();
                if (t == null || t.trim().isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
            }
            public void insertUpdate(DocumentEvent e) { f(); }
            public void removeUpdate(DocumentEvent e) { f(); }
            public void changedUpdate(DocumentEvent e) { f(); }
        });

        // initial load
        loadStudents();
    }

    public void loadStudents() {
        SwingUtilities.invokeLater(() -> {
            try {
                model.setRowCount(0);
                List<Student> list = dao.findAll();
                for (Student s : list) {
                    model.addRow(new Object[]{
                            s.getStudentId(),
                            s.getEnroll(),
                            s.getName(),
                            s.getDob() != null ? s.getDob().toString() : "",
                            s.getDept(),
                            s.getSemester(),
                            s.getEmail()
                    });
                }
                statusLabel.setText("Loaded " + list.size() + " students");
            } catch (Exception e) {
                showError("Error loading students: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Optional<Integer> getSelectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return Optional.empty();
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object idObj = model.getValueAt(modelRow, 0);
        if (idObj == null) return Optional.empty();
        return Optional.of(((Number) idObj).intValue());
    }

    private void addStudent() {
        EditStudentDialog dlg = new EditStudentDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        var res = dlg.getStudentResult();
        if (res == null) return;
        try {
            int id = dao.create(res);
            res.setStudentId(id);
            loadStudents();
            statusLabel.setText("Added " + res.getEnroll());
        } catch (Exception ex) {
            showError("Add failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void editStudent() {
        Optional<Integer> maybe = getSelectedId();
        if (maybe.isEmpty()) { JOptionPane.showMessageDialog(this, "Select a student to edit"); return; }
        int id = maybe.get();
        try {
            var opt = dao.findById(id);
            if (opt.isEmpty()) { showError("Student not found"); loadStudents(); return; }
            Student existing = opt.get();
            EditStudentDialog dlg = new EditStudentDialog(SwingUtilities.getWindowAncestor(this), existing);
            dlg.setVisible(true);
            Student edited = dlg.getStudentResult();
            if (edited == null) return;
            edited.setStudentId(existing.getStudentId());
            boolean ok = dao.update(edited);
            if (ok) {
                loadStudents();
                statusLabel.setText("Updated " + edited.getEnroll());
            } else showError("No rows updated");
        } catch (Exception ex) {
            showError("Edit failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {
        Optional<Integer> maybe = getSelectedId();
        if (maybe.isEmpty()) { JOptionPane.showMessageDialog(this, "Select a student to delete"); return; }
        int id = maybe.get();
        int c = JOptionPane.showConfirmDialog(this, "Delete selected student?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            boolean ok = dao.delete(id);
            if (ok) { loadStudents(); statusLabel.setText("Deleted id=" + id); }
            else showError("Delete returned 0");
        } catch (Exception ex) {
            showError("Delete failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + m);
    }
}
