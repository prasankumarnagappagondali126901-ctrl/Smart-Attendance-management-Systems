package com.smartatt.core.dao;

import com.smartatt.core.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {
    int create(Student s) throws Exception;
    Optional<Student> findById(int id) throws Exception;
    Optional<Student> findByEnroll(String enroll) throws Exception;
    List<Student> findAll() throws Exception;
    boolean update(Student s) throws Exception;
    boolean delete(int id) throws Exception;
}
