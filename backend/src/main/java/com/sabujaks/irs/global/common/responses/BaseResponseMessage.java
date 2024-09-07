package com.sabujaks.irs.global.common.responses;

public enum BaseResponseMessage {
    REQUEST_SUCCESS(true, 200, "요청이 정상적으로 처리되었습니다"),
    REQUEST_FAIL(false, 404, "요청을 실패했습니다."),
    DATABASE_SERVER_ERROR(false, 500, "DATABASE 오류"),
    // AUTH 1000~1999
    // MEMBER 회원가입 1000
    MEMBER_REGISTER_SUCCESS(true, 1000, "회원가입에 성공했습니다. 이메일 인증 후 로그인 해주세요"),
    MEMBER_REGISTER_FAIL_MEMBER_ALREADY_EXITS(false, 1001, "이미 다른 계정이 존재합니다." ),
    MEMBER_REGISTER_FAIL_INVALID_ROLE(false, 1002, "유효하지 않은 회원가입입니다. "),
    MEMBER_REGISTER_FAIL_NOT_COMPANY_AUTH(false, 1003, "기업 인증을 하지 않은 사용자는 가입할 수 없습니다."),

    // EMAIL_VERIFY 이메일 검증 1100

    EMAIL_VERIFY_SUCCESS(true, 1100, "이메일 검증을 완료했습니다."),
    EMAIL_VERIFY_FAIL(false, 1101, "이메일 검증을 실패했습니다."),
    EMAIL_VERIFY_FAIL_NOT_FOUND(false, 1102, "해당 유저를 찾을 수 없습니다."),
    EMAIL_VERIFY_FAIL_INVALID_ROLE(false, 1102, "유효하지 않은 이메일 검증입니다."),
    EMAIL_SEND_FAIL(false, 1103, "이메일 전송에 실패했습니다."),

    // COMPANY_VERIFY 기업 인증 1200
    COMPANY_VERIFY_SUCCESS(true, 1200, "기업 인증을 완료했습니다."),
    COMPANY_VERIFY_FAIL(false, 1201, "기업 인증에 실패했습니다."),
    COMPANY_VERIFY_FAIL_INVALID_REQUEST(false, 1202, "유효하지 않은 요청입니다.")

    ;

    private Boolean success;
    private Integer code;
    private String message;

    BaseResponseMessage(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
    public static BaseResponseMessage findByCode(Integer code) {
        for (BaseResponseMessage message : values()) { if (message.getCode().equals(code)) { return message; }}
        return null;
    }
    public Boolean getSuccess() { return success; }

    public Integer getCode() { return code; }

    public String getMessage() { return message; }
}