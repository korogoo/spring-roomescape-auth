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
import roomescape.domain.Store;

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
            new Store(
                rs.getLong("st_id"),
                rs.getString("st_name"),
                new Member(
                    rs.getLong("sm_id"),
                    rs.getString("sm_username"),
                    rs.getString("sm_password"),
                    MemberRole.valueOf(rs.getString("sm_member_role"))
                )
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
                       m.id as m_id, m.username, m.password, m.member_role,
                       st.id as st_id, st.st_name as st_name,
                       sm.id as sm_id, sm.username as sm_username, sm.password as sm_password, sm.member_role as sm_member_role
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN store st ON r.store_id = st.id
                JOIN member sm ON st.member_id = sm.id
                """,
            RESERVATION_ROW_MAPPER);
    }

    @Override
    public Reservation save(Reservation reservation) {
        final String sql = """
            INSERT INTO reservation(res_date, res_time, theme, member_id, store_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(reservation.getDate()));
            ps.setTime(2, Time.valueOf(reservation.getTime()));
            ps.setString(3, reservation.getTheme());
            ps.setLong(4, reservation.getMemberId());
            ps.setLong(5, reservation.getStoreId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException();
        }
        return reservation.withId(key.longValue());
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return jdbcTemplate.query("""
                SELECT r.id, r.res_date, r.res_time, r.theme,
                       m.id as m_id, m.username, m.password, m.member_role,
                       st.id as st_id, st.st_name as st_name,
                       sm.id as sm_id, sm.username as sm_username, sm.password as sm_password, sm.member_role as sm_member_role
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN store st ON r.store_id = st.id
                JOIN member sm ON st.member_id = sm.id
                WHERE r.member_id = ?
                """,
            RESERVATION_ROW_MAPPER,
            memberId);
    }

    @Override
    public List<Reservation> findAllByStoreMemberId(long managerId) {
        return jdbcTemplate.query("""
                SELECT r.id, r.res_date, r.res_time, r.theme,
                       m.id as m_id, m.username, m.password, m.member_role,
                       st.id as st_id, st.st_name as st_name,
                       sm.id as sm_id, sm.username as sm_username, sm.password as sm_password, sm.member_role as sm_member_role
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN store st ON r.store_id = st.id
                JOIN member sm ON st.member_id = sm.id
                WHERE st.member_id = ?
                """,
            RESERVATION_ROW_MAPPER,
            managerId);
    }

    @Override
    public boolean existsByIdAndStoreMemberId(long reservationId, long managerId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM reservation r
                JOIN store st ON r.store_id = st.id
                WHERE r.id = ? AND st.member_id = ?
                """,
            Integer.class,
            reservationId,
            managerId);
        return count != null && count == 1;
    }

    @Override
    public void deleteByIdAndStoreMemberId(long reservationId, long managerId) {
        jdbcTemplate.update("""
            DELETE FROM reservation
            WHERE id = ? AND store_id IN (SELECT id FROM store WHERE member_id = ?)
            """, reservationId, managerId);
    }
}
