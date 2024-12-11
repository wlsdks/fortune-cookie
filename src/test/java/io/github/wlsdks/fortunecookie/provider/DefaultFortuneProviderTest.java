package io.github.wlsdks.fortunecookie.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultFortuneProvider 테스트")
class DefaultFortuneProviderTest {

    @Mock
    private MessageSource messageSource;

    private DefaultFortuneProvider fortuneProvider;


    @BeforeEach
    void setUp() {
        fortuneProvider = new DefaultFortuneProvider(messageSource);
    }

    @DisplayName("한국어 로케일로 포춘 메시지를 정상적으로 가져온다.")
    @Test
    void getFortuneWithKoreanLocale() {
        // given
        Locale koreanLocale = Locale.KOREAN;
        String expectedMessage = "행운이 찾아올 것입니다!";
        given(messageSource.getMessage(anyString(), any(), eq(koreanLocale)))
                .willReturn(expectedMessage);

        // when
        String actualMessage = fortuneProvider.getFortune(koreanLocale);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
        verify(messageSource).getMessage(startsWith("fortune."), any(), eq(koreanLocale));

    }

    @DisplayName("영어 로케일로 포춘 메시지를 정상적으로 가져온다")
    @Test
    void getFortuneWithEnglishLocale() {
        // given
        Locale englishLocale = Locale.ENGLISH;
        String expectedMessage = "Good luck will come to you!";
        given(messageSource.getMessage(anyString(), any(), eq(englishLocale)))
                .willReturn(expectedMessage);

        // when
        String actualMessage = fortuneProvider.getFortune(englishLocale);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
        verify(messageSource).getMessage(startsWith("fortune."), any(), eq(englishLocale));
    }

    @DisplayName("메시지가 없을 경우 기본 메시지를 반환한다")
    @Test
    void getFortuneWithDefaultMessage() {
        // given
        Locale locale = Locale.getDefault();
        String defaultMessage = "Today is your lucky day!";
        given(messageSource.getMessage(anyString(), any(), eq(locale)))
                .willReturn(defaultMessage);

        // when
        String actualMessage = fortuneProvider.getFortune();

        // then
        assertThat(actualMessage).isEqualTo(defaultMessage);
        verify(messageSource).getMessage(startsWith("fortune."), any(), eq(locale));
    }

    @DisplayName("메시지 키가 1에서 100 사이의 값으로 생성된다")
    @Test
    void getFortuneMessageKeyRange() {
        // given
        Locale locale = Locale.getDefault();

        // when
        fortuneProvider.getFortune(locale);

        // then
        verify(messageSource).getMessage(matches("fortune\\.[1-9][0-9]?|100"), any(), eq(locale));
    }

}