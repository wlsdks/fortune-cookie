package io.github.wlsdks.fortunecookie.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class FortuneCookieSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("권한이 있는 사용자가 접근하는 경우")
    @Test
    @WithMockUser(username = "testUser", roles = {"USER", "ADMIN"})
    public void testSecurityPlaceholders() throws Exception {
        mockMvc.perform(get("/api/secure/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @DisplayName("기본 포춘 쿠키 테스트")
    @Test
    public void testBasicFortuneCookie() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print()); // 응답 출력
    }

    @DisplayName("유머 모드 테스트")
    @Test
    public void testJokeMode() throws Exception {
        // fortune-cookie.mode=joke 설정을 사용
        mockMvc.perform(get("/api/test/joke")
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());
    }

    @DisplayName("명언 모드 테스트")
    @Test
    public void testQuoteMode() throws Exception {
        // fortune-cookie.mode=quote 설정을 사용
        mockMvc.perform(get("/api/test/quote")
                        .header("Accept-Language", "ko"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());
    }

    @DisplayName("숫자 맞추기 게임 테스트")
    @Test
    public void testNumberGuessGame() throws Exception {
        // 첫 번째 요청 - 안내 메시지 확인
        mockMvc.perform(get("/api/test/game"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());

        // 두 번째 요청 - 게임 시도
        mockMvc.perform(get("/api/test/game")
                        .header("X-Guess", "5"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());
    }

    @DisplayName("플레이스홀더 테스트")
    @Test
    public void testPlaceholders() throws Exception {
        mockMvc.perform(get("/api/test/placeholder")
                        .header("X-User-Name", "John")
                        .header("X-User-Email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());
    }

    @DisplayName("테스트용 DTO 응답 래핑 테스트")
    @Test
    public void testDtoResponseWrapping() throws Exception {
        mockMvc.perform(get("/api/test/dto"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Fortune-Cookie"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.fortune").exists())
                .andDo(print());
    }

}