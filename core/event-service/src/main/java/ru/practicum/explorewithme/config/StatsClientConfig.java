package ru.practicum.explorewithme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.explorewithme.StatsClient;

@Configuration
public class StatsClientConfig {
    @Value("${stats.server.id:stats-server}")
    private String statsServiceId;

    @Bean
    public StatsClient statsClient(DiscoveryClient discoveryClient, RestTemplateBuilder builder) {
        return new StatsClient(discoveryClient, statsServiceId, builder);
    }
}
