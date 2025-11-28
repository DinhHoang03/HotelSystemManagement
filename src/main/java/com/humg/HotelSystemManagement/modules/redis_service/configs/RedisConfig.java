package com.humg.HotelSystemManagement.modules.redis_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplateObject(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 1. Tạo ObjectMapper chuyên dụng để xử lý Date/Time
        ObjectMapper objectMapper = new ObjectMapper();
        // Đăng ký module Java 8 Date/Time (Fix lỗi LocalDate của bạn)
        objectMapper.registerModule(new JavaTimeModule());
        // Cấu hình để nó lưu ngày tháng dạng chuỗi "yyyy-MM-dd" thay vì Array số [yyyy, MM, dd]
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Cấu hình để Redis lưu thêm thông tin Class (@class) vào JSON để lúc lấy ra nó map ngược lại được
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        // 2. Tạo Serializer dùng ObjectMapper vừa cấu hình
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. Set Serializer cho Template
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer); // Sử dụng serializer xịn

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer); // Sử dụng serializer xịn cho Hash Value luôn

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplateString(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}