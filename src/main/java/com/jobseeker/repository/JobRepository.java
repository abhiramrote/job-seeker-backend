package com.jobseeker.repository;

import com.jobseeker.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByActiveTrue();

    List<Job> findByCompany(String company);

    List<Job> findByExperienceLevel(String experienceLevel);

    List<Job> findByLocationContainingIgnoreCase(String location);

    List<Job> findByScrapedAtAfter(LocalDateTime dateTime);

    Optional<Job> findByJobUrl(String jobUrl);

    @Query("SELECT j FROM Job j WHERE j.active = true " +
           "AND (:company IS NULL OR LOWER(j.company) = LOWER(:company)) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:experienceLevel IS NULL OR LOWER(j.experienceLevel) = LOWER(:experienceLevel)) " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> findByFilters(@Param("company") String company,
                            @Param("location") String location,
                            @Param("experienceLevel") String experienceLevel,
                            @Param("keyword") String keyword);
}
