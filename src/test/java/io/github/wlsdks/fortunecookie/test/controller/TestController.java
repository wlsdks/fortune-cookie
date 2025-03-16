package io.github.wlsdks.fortunecookie.test.controller;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.properties.FortuneMode;
import io.github.wlsdks.fortunecookie.properties.GameType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@FortuneCookie
public class TestController {

    @GetMapping("/joke")
    @FortuneCookie(mode = FortuneMode.JOKE)
    public Map<String, Object> jokeTest() {
        return Map.of("message", "Joke mode test");
    }

    @GetMapping("/quote")
    @FortuneCookie(mode = FortuneMode.QUOTE)
    public Map<String, Object> quoteTest() {
        return Map.of("message", "Quote mode test");
    }

    @GetMapping("/game")
    @FortuneCookie(gameEnabled = true, gameType = GameType.NUMBER)
    public Map<String, Object> gameTest() {
        return Map.of("message", "Game test");
    }

    @GetMapping("/placeholder")
    @FortuneCookie
    public Map<String, Object> placeholderTest() {
        return Map.of("message", "Placeholder test");
    }

    @GetMapping("/dto")
    @FortuneCookie
    public TestDto dtoTest() {
        return new TestDto("Hello", "World");
    }

}
