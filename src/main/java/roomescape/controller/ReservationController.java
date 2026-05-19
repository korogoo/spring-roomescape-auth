package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponse> getReservations(
        HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Member member = (Member) session.getAttribute(MEMBER_SESSION_KEY);
        if (member == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
        if (!member.isAdmin()) {
            throw new RoomEscapeException(ErrorCode.FORBIDDEN);
        }

        return reservationService.getReservations().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse save(
        HttpServletRequest httpRequest,
        @Valid @RequestBody ReservationCreateRequest request
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Member member = (Member) session.getAttribute(MEMBER_SESSION_KEY);
        if (member == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Reservation reservation = reservationService.save(request, member);
        return new ResourceIdResponse(reservation.getId());
    }
}
