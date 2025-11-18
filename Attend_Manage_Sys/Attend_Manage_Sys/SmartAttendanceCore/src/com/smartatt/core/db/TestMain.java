package com.smartatt.core.db;

import com.smartatt.core.dao.StudentDAO;
import com.smartatt.core.dao.impl.StudentDAOImpl;
import com.smartatt.core.model.Student;

import java.time.LocalDate;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        try {
            // Quick DB version check (optional)
            try (var conn = DBUtil.getConnection();
                 var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) System.out.println("MySQL version: " + rs.getString(1));
            }

            StudentDAO dao = new StudentDAOImpl();

            // list existing students
            List<Student> students = dao.findAll();
            System.out.println("Found students: " + students.size());
            for (Student s : students) System.out.println(s);

            // optionally insert a new test student if none exist
            if (students.isEmpty()) {
                Student ts = new Student("ENR_TEST", "Auto Test", LocalDate.of(2003,1,1), "CSE", 3, "test@uni.edu");
                int id = dao.create(ts);
                System.out.println("Inserted test student id=" + id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
