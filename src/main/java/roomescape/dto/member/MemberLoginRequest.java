package roomescape.dto.member;

public record MemberLoginRequest(
    String username,
    String password
) {
}
