package com.smartatt.desktop.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    // panels (cards)
    private final StudentPanel studentPanel = new StudentPanel(); // refactor to JPanel if needed
    private final AttendancePanel attendancePanel = new AttendancePanel();
    private final AttendanceReportPanel reportPanel = new AttendanceReportPanel();

    public MainFrame() {
        setTitle("Smart Attendance - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        sidebar.setBackground(new Color(240, 240, 240));

        JLabel title = new JLabel("<html><b>SmartAttendance</b></html>");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        sidebar.add(title);

        JButton btnStudents = new JButton("Manage Students");
        JButton btnAttendance = new JButton("Take Attendance");
        JButton btnReports = new JButton("Attendance Reports");
        JButton btnLogout = new JButton("Logout");

        Dimension btnSize = new Dimension(180, 36);
        for (JButton b : new JButton[]{btnStudents, btnAttendance, btnReports, btnLogout}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(b);
            sidebar.add(Box.createRigidArea(new Dimension(0,8)));
        }

        add(sidebar, BorderLayout.WEST);

        // Card area
        cardPanel.add(wrapPanel(studentPanel), "students");
        cardPanel.add(wrapPanel(attendancePanel), "attendance");
        cardPanel.add(wrapPanel(reportPanel), "reports");
        add(cardPanel, BorderLayout.CENTER);

        // Button actions
        btnStudents.addActionListener(e -> showCard("students"));
        btnAttendance.addActionListener(e -> showCard("attendance"));
        btnReports.addActionListener(e -> showCard("reports"));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        // start on students page
        showCard("students");
    }

    private JPanel wrapPanel(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void showCard(String name) {
        cards.show(cardPanel, name);
    }
}
