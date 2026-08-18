package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EventServiceApplication {
    public static final String STATS_APP_NAME = "ewm-main-service";

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
