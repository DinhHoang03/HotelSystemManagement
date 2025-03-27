package com.humg.HotelSystemManagement;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public class JasyptDecryptorTest {
    public static void main(String[] args) {
        // Tạo đối tượng mã hóa/giải mã
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // Thiết lập cấu hình giống trong JasyptConfiguration.java
        config.setPassword("whatsthesecretpassword");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);

        // Giá trị cần giải mã
        String encryptedValue = "rFpGwv/uscX+l9y5QCe6X5H`3Y825wsLAAuXHT2WgsBiibbVkUj/hrr9GFT+xoEDE";

        // Thử giải mã
        try {
            String decryptedValue = encryptor.decrypt(encryptedValue);
            System.out.println("Giá trị gốc: " + decryptedValue);
        } catch (Exception e) {
            System.out.println("Không thể giải mã: " + e.getMessage());
        }
    }
}
