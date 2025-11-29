package com.humg.HotelSystemManagement.utils.enums;

public enum BookingStatus {
    IN_CART,      // Mới thêm vào, chưa tạo đơn (Đang đi chợ)
    PENDING,      // Đã tạo đơn, chờ thanh toán
    CONFIRMED,    // Đã thanh toán/xác nhận
    CHECKED_IN,   // Đã nhận phòng
    CHECKED_OUT,  // Đã trả phòng
    CANCELLED,    // Đã hủy
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

