package com.humg.HotelSystemManagement.crypto;

import org.springframework.security.core.parameters.P;

import java.util.Locale;

public class HexStringUtil {
    static final byte[] HEX_CHAR_TABLE = {
            (byte) '0', (byte) '1', (byte) '2', (byte) '3',
            (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
            (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    public static String byteArrayToHexString(byte[] raw) {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for(byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex);
    }

    public static byte[] hexStringToByteArray(String hex) {
        String hexStandard = hex.toLowerCase(Locale.ENGLISH);
        int size = hexStandard.length() / 2;
        byte[] bytesResult = new byte[size];

        int index = 0;
        for(int i = 0; i < size; i++) {
            bytesResult[i] = (byte) (hexStandard.charAt(index));
            ++index;
            byte tmp = (byte) (hexStandard.charAt(index));
            ++index;

            if(bytesResult[i] > HEX_CHAR_TABLE[9]) {
                bytesResult[i] -= ((byte) ('a') - 10);
            } else {
                bytesResult[i] -= (byte) ('0');
            }
            if(tmp > HEX_CHAR_TABLE[9]) {
                tmp -= ((byte) ('a') - 10);
            }else {
                tmp -= (byte) ('0');
            }
            bytesResult[i] = (byte) (bytesResult[i] * 16 + tmp);
        }
        return bytesResult;
    }
}
