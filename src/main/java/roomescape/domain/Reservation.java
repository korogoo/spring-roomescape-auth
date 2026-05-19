package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation {

    private final Long id;
    private final String username;
    private final LocalDate date;
    private final LocalTime time;
    private final String theme;

    public Reservation(Long id, String username, LocalDate date, LocalTime time, String theme) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(String username, LocalDate date, LocalTime time, String theme) {
        this(null, username, date, time, theme);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public Reservation withId(Long key) {
        return new Reservation(key, username, date, time, theme);
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
