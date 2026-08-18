package ru.practicum.ewm.stats.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.avro.serialization.EventSimilarityAvroDeserializer;
import ru.practicum.ewm.stats.avro.serialization.UserActionAvroDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Bean
    public ConsumerFactory<Long, UserActionAvro> analyzerUserActionConsumerFactory(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.user-actions-group-id}") String groupId) {
        Map<String, Object> properties = baseProperties(bootstrapServers, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro>
    analyzerUserActionKafkaListenerContainerFactory(
            ConsumerFactory<Long, UserActionAvro> analyzerUserActionConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(analyzerUserActionConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> analyzerSimilarityConsumerFactory(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.similarity-group-id}") String groupId) {
        Map<String, Object> properties = baseProperties(bootstrapServers, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityAvroDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro>
    analyzerSimilarityKafkaListenerContainerFactory(
            ConsumerFactory<String, EventSimilarityAvro> analyzerSimilarityConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(analyzerSimilarityConsumerFactory);
        return factory;
    }

    private Map<String, Object> baseProperties(String bootstrapServers, String groupId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return properties;
    }
}
