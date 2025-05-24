package com.hotel.humg.HotelSystemManagement.entity.enums;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    WAITING_PAYMENT,
    CANCELLED,
    FAILED,
    IN_PROGRESS
}

/**
 *     PENDING(0, "Chờ xác nhận"),
 *     CONFIRMED(1, "Đã xác nhận"),
 *     CHECKED_IN(2, "Đã nhận phòng"),
 *     CHECKED_OUT(3, "Đã trả phòng"),
 *     WAITING_PAYMENT(4, "Chờ thanh toán"),
 *     CANCELLED(5, "Đã hủy"),
 *     FAILED(6, "Thất bại"),
 *     IN_PROGRESS(7, "Đang xử lý");
 */

