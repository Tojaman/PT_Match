package com.solo.ptmatch.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

        @Bean
        public RedisTemplate<String, List<TrainerSummaryResponse>> trainerCacheTemplate(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper objectMapper) {
                RedisTemplate<String, List<TrainerSummaryResponse>> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key는 String으로 직렬화
                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                // Value는 Jackson2JsonRedisSerializer로 자동 직렬화
                // JavaType을 사용하여 List<TrainerSummaryResponse> 타입 지정
                CollectionType listType = objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, TrainerSummaryResponse.class);

                Jackson2JsonRedisSerializer<List<TrainerSummaryResponse>> serializer = new Jackson2JsonRedisSerializer<>(
                                objectMapper, listType);

                template.setValueSerializer(serializer);
                template.setHashValueSerializer(serializer);

                return template;
        }
}
