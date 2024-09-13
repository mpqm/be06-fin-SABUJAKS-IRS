package com.sabujaks.irs.domain.video_interview.service;

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
import com.sabujaks.irs.global.utils.JwtUtil;
import io.openvidu.java.client.*;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoInterviewService {
    private final OpenVidu openVidu;
    private final VideoInterviewRepository videoInterviewRepository;
    public VideoInterviewCreateRes create(VideoInterviewCreateReq dto) throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = SessionProperties.fromJson(dto.getParams()).build();
        Session session = openVidu.createSession(properties);
        VideoInterview videoInterviewRoom = VideoInterview.builder()
                .announceUUID(dto.getAnnounceUUID())
                .videoInterviewRoomUUID(session.getSessionId())
                .build();
        videoInterviewRepository.save(videoInterviewRoom);
        return VideoInterviewCreateRes.builder()
                .idx(videoInterviewRoom.getIdx())
                .build();
    }

    public List<VideoInterviewSearchRes> searchAll(String announceUUID) throws BaseException{
        List<VideoInterview> result =  videoInterviewRepository.findAllByAnnounceUUID(announceUUID)
        .orElseThrow( () -> new BaseException(BaseResponseMessage.VIDEO_INTERVIEW_SEARCH_ALL_FAIL_NOT_FOUND));
        List<VideoInterviewSearchRes> videoInterviewSearchResList = new ArrayList<>();
        for (VideoInterview videoInterview : result) {
            VideoInterviewSearchRes videoInterviewSearchRes = VideoInterviewSearchRes.builder()
                    .idx(videoInterview.getIdx())
                    .announceUUID(videoInterview.getAnnounceUUID())
                    .videoInterviewRoomUUID(videoInterview.getVideoInterviewRoomUUID())
                    .build();
            videoInterviewSearchResList.add(videoInterviewSearchRes);
        }
        return videoInterviewSearchResList;
    }

    public VideoInterviewTokenGetRes getSessionToken(VideoInterviewTokenGetReq dto, CustomUserDetails customUserDetails) throws BaseException, OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(dto.getVideoInterviewRoomUUID());
        if (session == null) { throw new BaseException(BaseResponseMessage.VIDEO_INTERVIEW_JOIN_FAIL);}
        ConnectionProperties properties = ConnectionProperties.fromJson(dto.getParams()).build();
        Connection connection = session.createConnection(properties);
        return VideoInterviewTokenGetRes.builder()
                .sessionToken(connection.getToken())
                .build();
    }

}
