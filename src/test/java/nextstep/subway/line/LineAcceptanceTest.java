package nextstep.subway.line;

import static nextstep.subway.AcceptanceApi.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationRequest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    LineRequest 신분당선;
    LineRequest 분당선;

    @BeforeEach
    void setStations() {
        //지하철_역_생성_요청
        지하철_역_생성_요청(makeParam("강남역"));
        지하철_역_생성_요청(makeParam("광교역"));
        지하철_역_생성_요청(makeParam("왕십리역"));
        지하철_역_생성_요청(makeParam("수원역"));
        신분당선 = makeParam("bg-red-600", "신분당선", 1L,2L, 10);
        분당선 = makeParam("bg-red-600", "분당선", 3L,4L, 10);
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(신분당선);

        // then
        // 지하철_노선_생성됨
        지하철_노선_생성됨(response);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성할 경우, 에러가 발생한다")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        지하철_노선_생성_요청(신분당선);

        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(신분당선);

        // then
        // 지하철_노선_생성_실패됨
        지하철_노선_생성_실패함(response);
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given

        // 지하철_노선_등록되어_있음
        지하철_노선_생성_요청(신분당선);
        지하철_노선_생성_요청(분당선);

        // when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();
        // then
        // 지하철_노선_목록_응답됨
        지하철_노선_목록_응답됨(response);

        // 지하철_노선_목록_포함됨
        지하철_노선에_지하철역이_포함됨(response, Arrays.asList(신분당선.getName(), 분당선.getName()));
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(신분당선);
        // when
        // 지하철_노선_조회_요청
        Long id = Long.parseLong(createResponse.header("Location").split("/")[2]);
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(id);
        // then
        // 지하철_노선_응답됨
        지하철_노선_응답됨(response);
    }

    @DisplayName("존재하지 않는 지하철 노선을 조회한다.")
    @Test
    void getLineNoData() {
        // given

        // when
        // 지하철_노선_조회_요청
        Long id = 1L;
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(id);
        // then
        // 지하철_노선_응답됨
        지하철_노선이_조회_실패함(response);
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(신분당선);

        // when
        // 지하철_노선_수정_요청
        Long id = Long.parseLong(createResponse.header("Location").split("/")[2]);
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(id, makeParam("bg-red-600", "신분당선"));
        // then
        // 지하철_노선_수정됨
        지하철_노선이_수정됨(response);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(신분당선);

        // when
        // 지하철_노선_제거_요청
        Long id = Long.parseLong(createResponse.header("Location").split("/")[2]);
        ExtractableResponse<Response> response = 지하철_노선_제거_요청(id);

        // then
        // 지하철_노선_삭제됨
        지하철_노선이_삭제됨(response);
    }

    public static void 지하철_노선이_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static void 지하철_노선이_수정됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 지하철_노선이_조회_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    public static void 지하철_노선_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> resultStationIds = response.jsonPath().getList("stations", StationResponse.class).stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());
        assertThat(resultStationIds).containsExactly(1L,2L);
    }

    public static void 지하철_노선에_지하철역이_포함됨(ExtractableResponse<Response> response, List<String> expectedLineNames) {
        List<String> resultLineNames = response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getName)
            .collect(Collectors.toList());
        assertThat(resultLineNames).containsAll(expectedLineNames);
    }

    public static void 지하철_노선_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 지하철_노선_생성_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    public static void 지하철_노선_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    private LineRequest makeParam(String color, String name, Long upStationId, Long downStationId, int distance) {
        return new LineRequest(name, color, upStationId, downStationId, distance);
    }

    private LineRequest makeParam(String color, String name) {
        return new LineRequest(name, color);
    }

    private StationRequest makeParam(String name) {
        return new StationRequest(name);
    }
}
