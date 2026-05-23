package roomescape.repository.store;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Store;

@Repository
public class JdbcStoreRepository implements StoreRepository {

    private static final RowMapper<Store> STORE_ROW_MAPPER = (rs, rowNum) ->
        new Store(
            rs.getLong("id"),
            rs.getString("st_name"),
            new Member(
                rs.getLong("m_id"),
                rs.getString("username"),
                rs.getString("password"),
                MemberRole.valueOf(rs.getString("member_role"))
            )
        );

    private final JdbcTemplate jdbcTemplate;

    public JdbcStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Store save(Store store) {
        final String sql = """
            INSERT INTO store(st_name, member_id)
            VALUES(?, ?) 
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, store.getName());
            ps.setLong(2, store.getMemberId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("매장 생성에 실패했습니다.");
        }
        return store.withId(key.longValue());
    }

    @Override
    public Optional<Store> findById(long id) {
        return jdbcTemplate.query("""
                    SELECT s.id, s.st_name, 
                           m.id as m_id, m.username, m.password, m.member_role
                    FROM store s
                    JOIN member m ON s.member_id = m.id
                    WHERE s.id = ?
                    """,
                STORE_ROW_MAPPER,
                id).stream()
            .findFirst();
    }

    @Override
    public List<Store> findAllByMemberId(Long memberId) {
        return jdbcTemplate.query("""
            SELECT s.id, s.st_name, 
                   m.id as m_id, m.username, m.password, m.member_role
            FROM store s 
            JOIN member m ON s.member_id = m.id
            WHERE s.member_id = ?
            """,
            STORE_ROW_MAPPER,
            memberId);
    }
}
