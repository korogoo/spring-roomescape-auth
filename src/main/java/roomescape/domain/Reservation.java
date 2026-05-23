package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation {

    private final Long id;
    private final Member member;
    private final Store store;
    private final LocalDate date;
    private final LocalTime time;
    private final String theme;

    public Reservation(Long id, Member member, Store store, LocalDate date, LocalTime time, String theme) {
        this.id = id;
        this.member = member;
        this.store = store;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(long memberId, long storeId, LocalDate date, LocalTime time, String theme) {
        this(
            null,
            new Member(memberId, null, null, null),
            new Store(storeId, null, null),
            date,
            time,
            theme);
    }

    public Long getId() {
        return id;
    }

    public long getMemberId() {
        return member.getId();
    }

    public String getMemberUsername() {
        return member.getUsername();
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getTheme() {
        return theme;
    }

    public long getStoreId() {
        return store.getId();
    }

    public Reservation withId(long id) {
        return new Reservation(id, member, store, date, time, theme);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id != null
            && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
