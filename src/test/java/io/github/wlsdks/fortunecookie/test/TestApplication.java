package io.github.wlsdks.fortunecookie.test;

import io.github.wlsdks.fortunecookie.config.FortuneCookieAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(FortuneCookieAutoConfiguration.class)
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}