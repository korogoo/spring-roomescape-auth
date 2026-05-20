package roomescape.dto.reservation;

import java.time.format.DateTimeFormatter;
import roomescape.domain.Reservation;

public record ReservationResponse(
    Long id,
    Long memberId,
    String username,
    String date,
    String startAt,
    String theme
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getMemberId(),
            reservation.getMemberUsername(),
            reservation.getDate().format(DATE_FORMATTER),
            reservation.getTime().format(TIME_FORMATTER),
            reservation.getTheme()
        );
    }
}
