package com.sabujaks.irs.domain.video_interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabujaks.irs.domain.auth.repository.EstimatorRepository;
import com.sabujaks.irs.domain.auth.repository.SeekerRepository;
import com.sabujaks.irs.domain.interview_schedule.model.entity.InterviewSchedule;
import com.sabujaks.irs.domain.interview_schedule.repository.InterviewScheduleRepository;
import com.sabujaks.irs.domain.video_interview.model.entity.VideoInterview;
import com.sabujaks.irs.domain.video_interview.model.request.VideoInterviewCreateReq;
import com.sabujaks.irs.domain.video_interview.model.request.VideoInterviewTokenGetReq;
import com.sabujaks.irs.domain.video_interview.model.response.VideoInterviewCreateRes;
import com.sabujaks.irs.domain.video_interview.model.response.VideoInterviewSearchRes;
import com.sabujaks.irs.domain.video_interview.model.response.VideoInterviewTokenGetRes;
import com.sabujaks.irs.domain.video_interview.repository.VideoInterviewRepository;
import com.sabujaks.irs.global.common.exception.BaseException;
import com.sabujaks.irs.global.common.responses.BaseResponseMessage;
import com.sabujaks.irs.global.security.CustomUserDetails;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VideoInterviewService {
    private final OpenVidu openVidu;
    private final VideoInterviewRepository videoInterviewRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;

    public VideoInterviewCreateRes create(VideoInterviewCreateReq dto) throws OpenViduJavaClientException, OpenViduHttpException, BaseException {
        SessionProperties properties = SessionProperties.fromJson(dto.getParams()).build();
        Session session = openVidu.createSession(properties);
        VideoInterview videoInterviewRoom = VideoInterview.builder()
                .announceUUID(dto.getAnnounceUUID())
                .videoInterviewRoomUUID(session.getSessionId())
                .build();
        videoInterviewRepository.save(videoInterviewRoom);

        return VideoInterviewCreateRes.builder()
                .idx(videoInterviewRoom.getIdx())
                .interviewScheduleRes(dto.getInterviewScheduleInfo())
                .build();
    }

    public List<VideoInterviewSearchRes> searchAll(String announceUUID, CustomUserDetails userDetails) throws BaseException {
        if(Objects.equals(userDetails.getRole(), "ROLE_ESTIMATOR")){
            List<InterviewSchedule> interviewScheduleList = interviewScheduleRepository.findByUuidAndEstimatorIdx(announceUUID, userDetails.getEstimator().getIdx());
            List<VideoInterviewSearchRes> videoInterviewSearchResList = new ArrayList<>();
            for(InterviewSchedule interviewSchedule : interviewScheduleList){
                VideoInterviewSearchRes videoInterviewSearchRes = VideoInterviewSearchRes.builder()
                        .announceUUID(announceUUID)
                        .videoInterviewUUID(interviewSchedule.getUuid())
                        .interviewDate(interviewSchedule.getInterviewDate())
                        .interviewStart(interviewSchedule.getInterviewStart())
                        .interviewEnd(interviewSchedule.getInterviewEnd())
                        .build();
                videoInterviewSearchResList.add(videoInterviewSearchRes);
            }
            return videoInterviewSearchResList;
        } else if (Objects.equals(userDetails.getRole(), "ROLE_SEEKER")) {
            List<InterviewSchedule> interviewScheduleList = interviewScheduleRepository.findByUuidAndSeekerIdx(announceUUID, userDetails.getSeeker().getIdx());
            List<VideoInterviewSearchRes> videoInterviewSearchResList = new ArrayList<>();
            for(InterviewSchedule interviewSchedule : interviewScheduleList){
                VideoInterviewSearchRes videoInterviewSearchRes = VideoInterviewSearchRes.builder()
                        .announceUUID(announceUUID)
                        .videoInterviewUUID(interviewSchedule.getUuid())
                        .interviewDate(interviewSchedule.getInterviewDate())
                        .interviewStart(interviewSchedule.getInterviewStart())
                        .interviewEnd(interviewSchedule.getInterviewEnd())
                        .build();
                videoInterviewSearchResList.add(videoInterviewSearchRes);
            }
            return videoInterviewSearchResList;
        } else {
            throw new BaseException(BaseResponseMessage.VIDEO_INTERVIEW_SEARCH_ALL_FAIL);
        }
    }

    public VideoInterviewTokenGetRes sessionToken(VideoInterviewTokenGetReq dto, CustomUserDetails userDetails) throws BaseException, OpenViduJavaClientException, OpenViduHttpException {
        boolean result = checkUserAuthorities(userDetails, dto);
        if(!result){
            throw new BaseException(BaseResponseMessage.VIDEO_INTERVIEW_JOIN_FAIL_NOT_TIME);
        }
        Session session = openVidu.getActiveSession(dto.getVideoInterviewUUID());
        if (session == null) { throw new BaseException(BaseResponseMessage.VIDEO_INTERVIEW_JOIN_FAIL);}
        ConnectionProperties properties = ConnectionProperties.fromJson(dto.getParams()).build();

        try{
            Connection connection = session.createConnection(properties);
            return VideoInterviewTokenGetRes.builder()
                    .sessionToken(connection.getToken())
                    .userEmail(userDetails.getEmail())
                    .userType(userDetails.getRole())
                    .build();
        } catch (Exception e){
            System.out.println("Error creating connection: " + e.getMessage());
            openVidu.createSession(SessionProperties.fromJson(dto.getParams()).build());
            Session activeSession = openVidu.getActiveSession(dto.getVideoInterviewUUID());
            ConnectionProperties reCreateProperties = ConnectionProperties.fromJson(dto.getParams()).build();
            activeSession.getConnection(dto.getVideoInterviewUUID());
            Connection connection = activeSession.createConnection(reCreateProperties);
            return VideoInterviewTokenGetRes.builder()
                    .sessionToken(connection.getToken())
                    .userEmail(userDetails.getEmail())
                    .userType(userDetails.getRole())
                    .build();
        }
    }


        public boolean checkUserAuthorities(CustomUserDetails userDetails, VideoInterviewTokenGetReq dto) {
            // 현재 시스템 시간
            LocalDateTime currentTime = LocalDateTime.now();

            // 권한 스트링 형식: "ROLE_SEEKER|id1|id2|날짜|시작시간|종료시간"
            Collection< ? extends GrantedAuthority> authorities = userDetails.getVideoInterviewAuthorities();

            for (GrantedAuthority authority : authorities) {
                String authorityStr = authority.getAuthority();
                String[] parts = authorityStr.split("\\|");
                if (parts.length == 6) {
                    String role = parts[0];
                    System.out.println(role);
                    String id1 = parts[1];
                    System.out.println(id1);
                    String id2 = parts[2];
                    System.out.println(id2);
                    String date = parts[3];
                    System.out.println(date);
                    String startTime = parts[4];
                    System.out.println(startTime);
                    String endTime = parts[5];
                    System.out.println(endTime);
                    if(Objects.equals(id1, dto.getAnnounceUUID()) && Objects.equals(id2, dto.getVideoInterviewUUID())){
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
                        LocalDateTime startDateTime = LocalDateTime.parse(date + " " + startTime, dateFormatter);
                        LocalDateTime endDateTime = LocalDateTime.parse(date + " " + endTime, dateFormatter);
                        LocalDateTime startDateTimeWithBuffer = startDateTime.minusMinutes(3);
                        if (currentTime.isAfter(startDateTimeWithBuffer) && currentTime.isBefore(endDateTime)) {
                            System.out.println("권한이 유효합니다: " + role);
                            return true;
                        }
                    }

                }
            }
            return false;
        }
}

