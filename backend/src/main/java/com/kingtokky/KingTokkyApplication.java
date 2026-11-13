package com.kingtokky;

import com.kingtokky.infrastructure.config.KingTokkyProperties;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(KingTokkyProperties.class)
public class KingTokkyApplication {

    public static void main(String[] args) {
        // Toda la lógica de turnos, reservas y horarios trabaja en hora de La Paz
        TimeZone.setDefault(TimeZone.getTimeZone("America/La_Paz"));
        SpringApplication.run(KingTokkyApplication.class, args);
    }
}
