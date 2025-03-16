package io.github.wlsdks.fortunecookie.test.controller;

import io.github.wlsdks.fortunecookie.annotation.FortuneCookie;
import io.github.wlsdks.fortunecookie.properties.FortuneMode;
import io.github.wlsdks.fortunecookie.properties.GameType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/secure")
@FortuneCookie
public class SecureTestController {

    @GetMapping("/test")
    public Map<String, Object> secureTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        if (auth != null) {
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities().toString());
        } else {
            response.put("message", "Not authenticated");
        }

        return response;
    }

}