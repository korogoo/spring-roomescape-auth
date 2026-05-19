package roomescape.domain;

public class Member {

    private final Long id;
    private final String username;
    private final String password;
    private final MemberRole role;

    public Member(Long id, String username, String password, MemberRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Member(String username, String password, MemberRole role) {
        this(null, username, password, role);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    public Member withId(long id) {
        return new Member(id, username, password, role);
    }

    public boolean isSamePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }
}
