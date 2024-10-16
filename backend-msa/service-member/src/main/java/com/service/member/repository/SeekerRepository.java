package com.service.member.repository;

import com.service.member.entity.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SeekerRepository extends JpaRepository<Seeker, Long> {

    @Query("SELECT s FROM Seeker s WHERE s.email = :seekerEmail")
    Optional<Seeker> findBySeekerEmail(String seekerEmail);

    @Query("SELECT s FROM Seeker s WHERE s.idx = :seekerIdx")
    Optional<Seeker> findBySeekerIdx(Long seekerIdx);
}