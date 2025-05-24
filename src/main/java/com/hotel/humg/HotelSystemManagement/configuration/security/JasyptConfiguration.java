package com.hotel.humg.HotelSystemManagement.configuration.security;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfiguration {
    @Bean
    public StringEncryptor jasyptStringEncryptor(){
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword("whatsthesecretpassword");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000"); //Số lần lặp lại để tạo khóa
        config.setPoolSize("1"); //Kích thước pool cho encryptor
        config.setProviderName("SunJCE"); // Nhà cung cấp mã hóa
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator"); // Salt generator
        config.setStringOutputType("base64"); // Định dạng đầu ra

        encryptor.setConfig(config);
        return encryptor;
    }
}
