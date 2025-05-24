package com.hotel.humg.HotelSystemManagement.crypto;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;

@Slf4j
public class HMACUtil {

    //Formatter: off
    public final static String HMACMD5 = "HmacMD5";
    public final static String HMACSHA1 = "HmacSHA1";
    public final static String HMACSHA256 = "HmacSHA256";
    public final static String HMACSHA512 = "HmacSHA512";
    public final static Charset UTF8CHARSET = Charset.forName("UTF-8");

    //Formatter: on
    public final static LinkedList<String> HMACS = new LinkedList<>(
            Arrays.asList("UnSupport", "HmacSHA256", "HmacMD5", "HmacSHA384", "HmacSHA1", "HmacSHA512")
    );

    public static String computeHMac(String data, String key) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hmacBytes = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate HMAC", e);
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    public static byte[] HMacEncode(final String algorithm, final String key, final String data) {
        Mac macGenerator = null;

        try {
            macGenerator = Mac.getInstance(algorithm);
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), algorithm);
            macGenerator.init(signingKey);
        } catch (Exception e) {
            log.error("HMAC encoding failed: algorithm={}, key={}, data={}, error={}", algorithm, key, data, e.getMessage());
            throw new RuntimeException("HMAC encoding failed", e);
        }

        if (macGenerator == null) {
            return null;
        }

        byte[] dataByte =  null;
        try {
            dataByte = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        return macGenerator.doFinal(dataByte);
    }

    public static String HMacBase64Encode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        if(hmacEncodeBytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(hmacEncodeBytes);
    }

    public static String HMacHexStringEncode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        if(hmacEncodeBytes == null) {
            return null;
        }
        return HexStringUtil.byteArrayToHexString(hmacEncodeBytes);
    }
}
