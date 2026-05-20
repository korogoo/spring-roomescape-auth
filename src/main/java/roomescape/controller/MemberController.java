package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.AuthConstants;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Token;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.repository.token.TokenRepository;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final TokenRepository tokenRepository;

    public MemberController(MemberService memberService, TokenRepository tokenRepository) {
        this.memberService = memberService;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/admin/join")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse joinAdmin(
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.save(request, MemberRole.ADMIN);
        return new ResourceIdResponse(member.getId());
    }

    @PostMapping("/normal/join")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse joinNormal(
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.save(request, MemberRole.NORMAL);
        return new ResourceIdResponse(member.getId());
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(
        HttpServletResponse httpResponse,
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.login(request);

        String uuid = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(10);
        Token token = new Token(uuid, expiredAt, member.getId(), member.getRole());
        tokenRepository.save(token);

        httpResponse.setHeader(AuthConstants.SESSION_HEADER_KEY, uuid);
    }
}
