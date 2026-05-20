package roomescape.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "JSON 파싱 실패 혹은 URL 경로 변수 타입 오류가 발생했습니다"),
    INVALID_REQUEST_URI_VARIABLE_TYPE(HttpStatus.BAD_REQUEST, "요청 URI 형식이 올바르지 않습니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "필수값이 누락되었거나 필드 유효성 검증에 실패했습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다"),
    INVALID_USERNAME_AND_PASSWORD(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 올바르지 않습니다"),
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 서버 오류가 발생했습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    ;

    private final HttpStatus code;
    private final String message;

    ErrorCode(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
    }

    public HttpStatus getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
