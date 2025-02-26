package com.humg.HotelSystemManagement.entity.enums;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RoomStatus {
    AVAILABLE("Phòng trống, có thể đặt"),
    BOOKED("Đã được đặt trước, nhưng khách chưa check-in"),
    OCCUPIED("Khách đã check-in và đang sử dụng"),
    CHECKOUT_PENDING("Khách đã check-out nhưng chưa dọn dẹp xong"),
    CLEANING("Đang dọn dẹp, tạm thời không thể đặt"),
    UNDER_MAINTENANCE("Phòng đang sửa chữa, không thể đặt");

    String description;
}
