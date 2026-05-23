package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateRequest(
    @NotNull(message = "날짜는 필수 입력값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @NotNull(message = "시간 ID는 필수 입력값입니다.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,

    @NotBlank(message = "테마는 필수 입력값입니다.")
    String theme,

    @NotNull(message = "매장 아이디는 필수 입력값입니다.")
    Long storeId
) {
}
