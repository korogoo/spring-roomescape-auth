package roomescape.domain;

public class Store {

    private final Long id;
    private final String name;
    private final Member member;

    public Store(Long id, String name, Member member) {
        this.id = id;
        this.name = name;
        this.member = member;
    }

    public Store(String name, Member member) {
        this(null, name, member);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Member getMember() {
        return member;
    }

    public long getMemberId() {
        return member.getId();
    }

    public Store withId(long id) {
        return new Store(id, name, member);
    }
}
