package io.github.wlsdks;

import io.github.wlsdks.fortunecookie.properties.FortuneCookieProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FortuneCookieProperties.class)
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}