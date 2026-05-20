package roomescape.domain;

import java.time.LocalDateTime;
import roomescape.dto.member.MemberSummary;

public class Token {

    private final Long id;
    private final String tokenValue;
    private final LocalDateTime expiredAt;
    private final MemberSummary memberSummary;

    public Token(Long id, String tokenValue, LocalDateTime expiredAt, MemberSummary memberSummary) {
        this.id = id;
        this.tokenValue = tokenValue;
        this.expiredAt = expiredAt;
        this.memberSummary = memberSummary;
    }

    public Token(String tokenValue, LocalDateTime expiredAt, Long memberId, MemberRole memberRole) {
        this(null, tokenValue, expiredAt, new MemberSummary(memberId, memberRole));
    }

    public Long getId() {
        return id;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public MemberSummary getLoginMember() {
        return memberSummary;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isAdminMember() {
        return memberSummary.isAdmin();
    }

    public boolean isSameTokenValue(String tokenValue) {
        return this.tokenValue.equals(tokenValue);
    }

    public Token withId(long id) {
        return new Token(id, tokenValue, expiredAt, memberSummary);
    }
}
