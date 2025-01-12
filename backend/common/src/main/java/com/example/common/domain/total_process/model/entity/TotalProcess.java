package com.example.common.domain.total_process.model.entity;

import com.example.common.domain.alarm.model.entity.Alarm;
import com.example.common.domain.announcement.model.entity.Announcement;
import com.example.common.domain.auth.model.entity.Seeker;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private Boolean resumeResult;
    private Boolean interviewOneResult;
    private Boolean interviewTwoResult;
    private Boolean finalResult;

    @ManyToOne
    @JoinColumn(name = "announcement_idx")
    private Announcement announcement;

    @ManyToOne
    @JoinColumn(name = "seeker_idx")
    private Seeker seeker;

    // 알람 테이블과 1:n
    @OneToMany(mappedBy = "totalProcess")
    private List<Alarm> alarm;
}
