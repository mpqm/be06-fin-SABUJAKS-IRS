package com.example.api.global.utils.email.service;

import com.example.api.domain.alarm.service.AlarmService;
import com.example.api.domain.auth.model.response.AuthSignupRes;
import com.example.api.domain.auth.model.response.SeekerInfoGetRes;
import com.example.api.domain.interview_schedule.model.response.InterviewScheduleRes;
import com.example.api.domain.video_interview.model.response.VideoInterviewCreateRes;
import com.example.api.global.common.exception.BaseException;
import com.example.api.global.common.responses.BaseResponse;
import com.example.api.global.common.responses.BaseResponseMessage;
import com.example.api.global.utils.email.model.response.ResumeResultRes;
import com.example.common.domain.alarm.model.entity.Alarm;
import com.example.common.domain.alarm.repository.AlarmRepository;
import com.example.common.domain.auth.model.entity.Seeker;
import com.example.common.domain.auth.repository.SeekerRepository;
import com.example.common.domain.interview_schedule.model.entity.InterviewSchedule;
import com.example.common.domain.resume.model.entity.Resume;
import com.example.common.domain.total_process.model.entity.TotalProcess;
import com.example.common.domain.total_process.repository.TotalProcessRepository;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Async
public class EmailSenderSeeker {

    private final JavaMailSender mailSender;
    private final FreeMarkerConfigurer freemarkerConfigurer;
    private final AlarmRepository alarmRepository;
    private final SeekerRepository seekerRepository;
    private final TotalProcessRepository totalProcessRepository;
    private final AlarmService alarmService; // AlarmService 추가


    public EmailSenderSeeker(JavaMailSender mailSender, FreeMarkerConfigurer freemarkerConfigurer, AlarmRepository alarmRepository, SeekerRepository seekerRepository, TotalProcessRepository totalProcessRepository, AlarmService alarmService) {
        this.mailSender = mailSender;
        this.freemarkerConfigurer = freemarkerConfigurer;
        this.alarmRepository = alarmRepository;
        this.seekerRepository = seekerRepository;
        this.totalProcessRepository = totalProcessRepository;
        this.alarmService = alarmService;
    }

    public void signupEmail(AuthSignupRes response, String uuid) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(response.getEmail());

            if(Objects.equals(response.getRole(),"ROLE_RECRUITER")){
                if(!response.getEmail_auth() && !response.getInactive()){
                    helper.setSubject("IRS - 채용 담당자로 가입하신걸 환영합니다.");
                } else {
                    helper.setSubject("IRS - 채용 담당자 계정 복구 이메일 검증");
                }
            } else if (Objects.equals(response.getRole(), "ROLE_SEEKER")) {
                if(!response.getEmail_auth() && !response.getInactive()){
                    helper.setSubject("IRS - 지원자로 가입하신걸 환영합니다.");
                } else {
                    helper.setSubject("IRS - 지원자 계정 복구 이메일 검증");
                }
            } else {
                throw new BaseException(BaseResponseMessage.AUTH_EMAIL_VERIFY_FAIL_INVALID_ROLE);
            }

            // 템플릿 내부에서 처리한 변수값 매핑
            Map<String, Object> model = new HashMap<>();
            model.put("name", response.getEmail());
            model.put("emailVerifyUrl", "https://www.sabujaks-irs.kro.kr/api/api/auth/email-verify?email="+response.getEmail()+"&role="+response.getRole()+"&uuid="+uuid);

            Template template = freemarkerConfigurer.getConfiguration().getTemplate("SignupEmail.html");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true); // Set HTML content
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    public void sendSubmitResumeEmail() throws RuntimeException {
        try {

        } catch (Exception e) {
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    public void sendResumeResultEmail(List<ResumeResultRes> getInfo) throws RuntimeException {
        try {
            for(ResumeResultRes dto : getInfo) {
                Optional<Alarm> optionalAlarm = alarmRepository.findByResumeIdx(dto.getResumeIdx());
                if (optionalAlarm.isPresent()) {
                    continue; // checkAlarm이 존재하면 continue
                } else {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                    helper.setTo(dto.getSeekerEmail());
                    helper.setSubject("[IRS] 서류전형 결과 안내");

                    // 템플릿 내부에서 처리한 변수값 매핑
                    Map<String, Object> model = new HashMap<>();
                    model.put("name", dto.getSeekerName());
                    model.put("companyName", dto.getCompanyName());
                    model.put("announcementTitle", dto.getAnnouncementTitle());

                    // 메일로 전송할 템플릿 렌더링
                    // 디렉토리 지정한 configure파일에서 객체 얻어와서 해당 객체로 템플릿 찾아서 얻어온다.
                    TotalProcess totalProcess = totalProcessRepository.findByAnnouncementIdxAndSeekerIdx(dto.getAnnouncementIdx(), dto.getSeekerIdx()).get();
                    Template template = null;
                    if(totalProcess != null) {
                        if(totalProcess.getResumeResult()) {
                            template = freemarkerConfigurer.getConfiguration().getTemplate("ResumeAcceptEmail.html");
                        } else {
                            template = freemarkerConfigurer.getConfiguration().getTemplate("ResumeRejectEmail.html");
                        }
                    }

                    String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                    helper.setText(html, true); // Set HTML content

                    // Alarm 저장 로직
                    Seeker seeker = seekerRepository.findBySeekerIdx(dto.getSeekerIdx())
                            .orElseThrow(() -> new BaseException(BaseResponseMessage.MEMBER_NOT_FOUND));

                    Alarm alarm = Alarm.builder()
                            .type("서류전형 결과 안내")
                            .status(false)
                            .message(html)
                            .seeker(seeker)
                            .createdAt(LocalDateTime.now())
                            .resume(Resume.builder()
                                    .idx(dto.getResumeIdx())
                                    .build())
                            .build();

                    alarmRepository.save(alarm);


                    mailSender.send(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    public void sendNotiInterviewScheduleEmail(InterviewScheduleRes dto) throws RuntimeException {
        try {
            for(SeekerInfoGetRes seekerInfoGetRes : dto.getSeekerList()) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                helper.setTo(seekerInfoGetRes.getEmail());
                helper.setSubject("[IRS] 인터뷰 일정 안내");

                // 템플릿 내부에서 처리한 변수값 매핑
                Map<String, Object> model = new HashMap<>();
                model.put("name", seekerInfoGetRes.getName());
                model.put("interviewDate", dto.getInterviewDate());
                model.put("interviewStart", dto.getInterviewStart());
                model.put("interviewEnd", dto.getInterviewEnd());
                model.put("companyName", dto.getCompanyName());
                model.put("announcementTitle", dto.getAnnouncementTitle());

                if(dto.getIsOnline()) {
                    model.put("isOnline", "온라인");
                } else {
                    model.put("isOnline", "오프라인");
                }

                // 메일로 전송할 템플릿 렌더링
                // 디렉토리 지정한 configure파일에서 객체 얻어와서 해당 객체로 템플릿 찾아서 얻어온다.
                Template template;
                if(dto.getCareerBase().equals("경력")) {
                    template = freemarkerConfigurer.getConfiguration().getTemplate("InterviewExpEmail.html");
                } else {
                    template = freemarkerConfigurer.getConfiguration().getTemplate("InterviewNewEmail.html");
                }

                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                helper.setText(html, true); // Set HTML content

                // Alarm 저장 로직
                Seeker seeker = seekerRepository.findBySeekerIdx(seekerInfoGetRes.getIdx())
                        .orElseThrow(() -> new BaseException(BaseResponseMessage.MEMBER_NOT_FOUND));

                Alarm alarm = Alarm.builder()
                        .type("인터뷰 일정 안내")
                        .status(false)
                        .message(html)
                        .seeker(seeker)
                        .interviewSchedule(InterviewSchedule.builder()
                                .idx(dto.getIdx())
                                .isOnline(dto.getIsOnline())
                                .interviewDate(dto.getInterviewDate())
                                .interviewStart(dto.getInterviewStart())
                                .interviewEnd(dto.getInterviewEnd())
                                .uuid(dto.getUuid())
                                .careerBase(dto.getCareerBase())
                                .interviewNum(dto.getInterviewNum())
                                .build())
                        .createdAt(LocalDateTime.now())
                        .build();

                alarmRepository.save(alarm);

                // 알람 전송
                alarmService.sendAlarm(seekerInfoGetRes.getIdx(), "면접 일정 안내 알림 전송");

                mailSender.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    public void sendConfirmInterviewScheduleEmail(VideoInterviewCreateRes dto) throws RuntimeException {
        try {
            for(SeekerInfoGetRes seekerInfoGetRes : dto.getInterviewScheduleRes().getSeekerList()) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                helper.setTo(seekerInfoGetRes.getEmail());
                helper.setSubject("[IRS] 인터뷰 일정 상세 안내");

                // 템플릿 내부에서 처리한 변수값 매핑
                Map<String, Object> model = new HashMap<>();
                model.put("name", seekerInfoGetRes.getName());
                model.put("interviewDate", dto.getInterviewScheduleRes().getInterviewDate());
                model.put("interviewStart", dto.getInterviewScheduleRes().getInterviewStart());
                model.put("interviewEnd", dto.getInterviewScheduleRes().getInterviewEnd());
                model.put("companyName", dto.getInterviewScheduleRes().getCompanyName());
                model.put("announcementTitle", dto.getInterviewScheduleRes().getAnnouncementTitle());
                model.put("videoInterviewUrl", "https://sabujaks-irs.kro.kr/video-interview/" + dto.getAnnouncementUuid());

                if(dto.getInterviewScheduleRes().getIsOnline()) {
                    model.put("isOnline", "온라인");
                } else {
                    model.put("isOnline", "오프라인");
                }

                // 메일로 전송할 템플릿 렌더링
                // 디렉토리 지정한 configure파일에서 객체 얻어와서 해당 객체로 템플릿 찾아서 얻어온다.
                Template template;
                if(dto.getInterviewScheduleRes().getCareerBase().equals("경력")) {
                    template = freemarkerConfigurer.getConfiguration().getTemplate("InterviewConfirmOnlineEmail.html");
                } else {
                    template = freemarkerConfigurer.getConfiguration().getTemplate("InterviewNewEmail.html");
                }

                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                helper.setText(html, true); // Set HTML content

                // Alarm 저장 로직
                Seeker seeker = seekerRepository.findBySeekerIdx(seekerInfoGetRes.getIdx())
                        .orElseThrow(() -> new BaseException(BaseResponseMessage.MEMBER_NOT_FOUND));

                Alarm alarm = Alarm.builder()
                        .type("인터뷰 일정 상세 안내")
                        .status(false)
                        .message(html)
                        .seeker(seeker)
                        .interviewSchedule(InterviewSchedule.builder()
                                .idx(dto.getIdx())
                                .isOnline(dto.getInterviewScheduleRes().getIsOnline())
                                .interviewDate(dto.getInterviewScheduleRes().getInterviewDate())
                                .interviewStart(dto.getInterviewScheduleRes().getInterviewStart())
                                .interviewEnd(dto.getInterviewScheduleRes().getInterviewEnd())
                                .uuid(dto.getInterviewScheduleRes().getUuid())
                                .careerBase(dto.getInterviewScheduleRes().getCareerBase())
                                .interviewNum(dto.getInterviewScheduleRes().getInterviewNum())
                                .build())
                        .createdAt(LocalDateTime.now())
                        .build();

                alarmRepository.save(alarm);

                mailSender.send(message);
            }
        } catch (Exception e) {
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    public void sendResultEmail() throws RuntimeException {
        try {

        } catch (Exception e) {
            new BaseResponse<>(BaseResponseMessage.EMAIL_SEND_FAIL);
        }
    }

    // 클라이언트별로 SseEmitter를 저장하는 Map
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void sendAlarm(Long seekerIdx, String message) {
        SseEmitter emitter = emitters.get(seekerIdx);
        System.out.println("seekerIdx: " + seekerIdx);
        System.out.println("message: " + message);
        if (emitter != null) {
            try {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("send Alarm start");
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(message));
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@");
            } catch (IOException e) {
                emitters.remove(seekerIdx);
            }
        }
    }
}