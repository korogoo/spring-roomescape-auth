package roomescape.dto.member;

import roomescape.domain.MemberRole;

public record MemberSummary(
    Long id,
    MemberRole role
) {

    public boolean isAdmin() {
        return role.isAdmin();
    }
}
