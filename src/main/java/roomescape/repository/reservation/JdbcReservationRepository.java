package roomescape.repository.reservation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
        new Reservation(
            rs.getLong("id"),
            new Member(
                rs.getLong("m_id"),
                rs.getString("username"),
                rs.getString("password"),
                MemberRole.valueOf(rs.getString("member_role"))
            ),
            rs.getDate("res_date").toLocalDate(),
            rs.getTime("res_time").toLocalTime(),
            rs.getString("theme")
        );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query("""
                SELECT r.id, r.res_date, r.res_time, r.theme,
                       m.id as m_id, m.username, m.password, m.member_role
                FROM reservation r
                JOIN member m ON r.member_id = m.id  
                """,
            RESERVATION_ROW_MAPPER);
    }

    @Override
    public Reservation save(Reservation reservation) {
        final String sql = """
            INSERT INTO reservation(res_date, res_time, theme, member_id)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(reservation.getDate()));
            ps.setTime(2, Time.valueOf(reservation.getTime()));
            ps.setString(3, reservation.getTheme());
            ps.setLong(4, reservation.getMemberId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException();
        }
        return reservation.withId(key.longValue());
    }
}
