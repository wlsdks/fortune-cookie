package io.github.wlsdks;

import io.github.wlsdks.fortunecookie.config.FortuneCookieAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(FortuneCookieAutoConfiguration.class)
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}