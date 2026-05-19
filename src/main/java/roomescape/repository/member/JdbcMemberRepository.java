package roomescape.repository.member;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) ->
        new Member(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password"),
            MemberRole.valueOf(rs.getString("member_role"))
        );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Member save(Member member) {
        final String sql = """
            INSERT INTO member(username, password, member_role)
            VALUES (?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getUsername());
            ps.setString(2, member.getPassword());
            ps.setString(3, member.getRole().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("회원 생성에 실패했습니다.");
        }
        return member.withId(key.longValue());
    }

    @Override
    public Member findById(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT id, username, password, member_role
                FROM member
                WHERE id = ?
                """,
            MEMBER_ROW_MAPPER,
            id);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return jdbcTemplate.query("""
                    SELECT id, username, password, member_role
                    FROM member
                    WHERE username = ?
                    """,
                MEMBER_ROW_MAPPER,
                username)
            .stream()
            .findFirst();
    }
}
