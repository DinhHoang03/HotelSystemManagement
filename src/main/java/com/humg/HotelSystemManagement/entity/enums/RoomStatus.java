package com.humg.HotelSystemManagement.entity.enums;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RoomStatus {
    AVAILABLE("Phòng trống, có thể đặt"),
    OCCUPIED("Phòng đang có khách lưu trú"),
    CLEANING("Phòng đang được dọn dẹp"),
    NEED_CLEANING("Phòng vừa có khách trả, cần dọn dẹp"),
    UNDER_MAINTENANCE("Phòng đang bảo trì, không thể đặt"),
    OUT_OF_SERVICE("Phòng bị lỗi, không sử dụng được");

    String description;

    @Override
    public String toString() {
        return name();
    }
}




