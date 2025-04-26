package com.humg.HotelSystemManagement.utils;

import org.springframework.stereotype.Component;

@Component
public class NormalizeString {
    public String normalizedString(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            // Nếu gặp chữ in hoa không phải ký tự đầu tiên, thêm dấu "_"
            if (i > 0 && Character.isUpperCase(c)) {
                result.append('_');
            }

            // Chuyển thành in hoa và thêm vào kết quả
            result.append(Character.toUpperCase(c));
        }

        // Thay thế khoảng trắng và dấu "-" thành "_"
        return result.toString().replaceAll("[\\s-]+", "_");
    }
}
