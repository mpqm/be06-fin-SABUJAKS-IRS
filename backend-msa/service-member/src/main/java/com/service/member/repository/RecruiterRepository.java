package com.service.member.repository;

import com.service.member.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    @Query("SELECT r FROM Recruiter r WHERE r.email = :recruiterEmail")
    Optional<Recruiter> findByRecruiterEmail(String recruiterEmail);

    @Query("SELECT r FROM Recruiter r WHERE r.idx = :recruiterIdx")
    Optional<Recruiter> findByRecruiterIdx(Long recruiterIdx);
}