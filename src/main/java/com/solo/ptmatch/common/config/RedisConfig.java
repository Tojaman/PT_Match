package com.solo.ptmatch.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.cache.pubsub.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

        @Bean
        public RedisTemplate<String, Object> cacheRedisTemplate(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper objectMapper) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                StringRedisSerializer keySerializer = new StringRedisSerializer();
                // 객체 타입 정보(@class)를 포함하여 직렬화
                GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                template.setKeySerializer(keySerializer);
                template.setHashKeySerializer(keySerializer);
                template.setValueSerializer(valueSerializer);
                template.setHashValueSerializer(valueSerializer);

                return template;
        }

        @Bean
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
                return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                        RedisConnectionFactory connectionFactory,
                        RedisMessageSubscriber subscriber) {

                RedisMessageListenerContainer container = new RedisMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.addMessageListener(subscriber, new ChannelTopic("cache:evict"));
                return container;
        }
}
