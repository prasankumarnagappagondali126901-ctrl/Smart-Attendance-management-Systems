package com.smartatt.desktop.ui;

import com.smartatt.core.model.Student;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Dialog for adding / editing students with a calendar popup for DOB.
 * Requires JCalendar (toedter) jar on the Desktop project classpath.
 */
public class EditStudentDialog extends JDialog {
    private final JTextField enrollField = new JTextField(20);
    private final JTextField nameField = new JTextField(20);
    private final JDateChooser dobChooser;          // calendar popup
    private final JTextField deptField = new JTextField(15);
    private final JSpinner semSpinner;
    private final JTextField emailField = new JTextField(25);

    private Student studentResult = null;

    public EditStudentDialog(Window owner, Student existing) {
        super(owner, existing == null ? "Add Student" : "Edit Student", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;

        // Enroll
        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Enroll:"), gbc);
        gbc.gridx = 1; form.add(enrollField, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy++; form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; form.add(nameField, gbc);

        // DOB - JDateChooser
        gbc.gridx = 0; gbc.gridy++; form.add(new JLabel("DOB:"), gbc);
        dobChooser = new JDateChooser();
        dobChooser.setDateFormatString("yyyy-MM-dd"); // ensures same format as DB
        dobChooser.setPreferredSize(enrollField.getPreferredSize());
        gbc.gridx = 1; form.add(dobChooser, gbc);

        // Dept
        gbc.gridx = 0; gbc.gridy++; form.add(new JLabel("Dept:"), gbc);
        gbc.gridx = 1; form.add(deptField, gbc);

        // Semester
        gbc.gridx = 0; gbc.gridy++; form.add(new JLabel("Semester:"), gbc);
        semSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        gbc.gridx = 1; form.add(semSpinner, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy++; form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; form.add(emailField, gbc);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        // Populate if editing
        if (existing != null) {
            enrollField.setText(existing.getEnroll());
            nameField.setText(existing.getName());
            if (existing.getDob() != null) {
                dobChooser.setDate(localDateToDate(existing.getDob()));
            }
            deptField.setText(existing.getDept());
            semSpinner.setValue(existing.getSemester());
            emailField.setText(existing.getEmail());
        }

        ok.addActionListener(e -> onOK());
        cancel.addActionListener(e -> onCancel());

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void onOK() {
        String enroll = enrollField.getText().trim();
        String name = nameField.getText().trim();
        Date date = dobChooser.getDate();
        String dept = deptField.getText().trim();
        int sem = (Integer) semSpinner.getValue();
        String email = emailField.getText().trim();

        if (enroll.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enroll and Name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate dob = null;
        if (date != null) {
            dob = dateToLocalDate(date);
        }

        Student s = new Student();
        s.setEnroll(enroll);
        s.setName(name);
        s.setDob(dob);
        s.setDept(dept);
        s.setSemester(sem);
        s.setEmail(email);

        studentResult = s;
        setVisible(false);
        dispose();
    }

    private void onCancel() {
        studentResult = null;
        setVisible(false);
        dispose();
    }

    public Student getStudentResult() {
        return studentResult;
    }

    // -----------------------
    // Helper conversions
    // -----------------------
    private static LocalDate dateToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date localDateToDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
