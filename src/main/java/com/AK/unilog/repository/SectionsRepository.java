package com.AK.unilog.repository;

import com.AK.unilog.entity.Course;
import com.AK.unilog.entity.Section;
import com.AK.unilog.service.Semester;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionsRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByCourseAndYearAndSemester(Course course, int year, Semester semester );

    Optional<List<Section>> findByCourse(Course course);

    Optional<List<Section>> findByCourseAndDisabledIsFalse(Course course);

    Optional<List<Section>> findByDisabledFalse();

    //student section search
    //disabled is false and ignore case courseNumber search
    Optional<List<Section>> findByDisabledFalseAndCourseCourseNumberContainingIgnoreCase(String courseNumber, Sort sort);

    Optional<List<Section>> findByCourseCourseNumberContainingIgnoreCase(String courseNumber, Sort sort);


}

