package roomescape.domain;

public enum MemberRole {
    ADMIN, NORMAL;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
