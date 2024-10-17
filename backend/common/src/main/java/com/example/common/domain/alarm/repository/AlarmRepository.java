package com.example.common.domain.alarm.repository;

import com.example.common.domain.alarm.model.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Optional<List<Alarm>> findBySeekerIdx(Long seekerIdx);

    Optional<Alarm> findByResumeIdx(Long resumeIdx);

    Optional<Alarm> findByInterviewScheduleIdx(Long interviewScheduleIdx);

    Optional<Alarm> findByTotalProcessIdx(Long totalProcessIdx);
}
