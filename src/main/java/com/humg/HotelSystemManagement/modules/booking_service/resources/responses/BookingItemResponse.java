package com.humg.HotelSystemManagement.modules.booking_service.resources.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemResponse {
    String bookingItemId;
    String hotelOfferName; // Trả về tên món: "Phở Bò", "Coca Cola"
    String imageUrl;       // Kèm ảnh cho đẹp
    int quantity;
    Long unitPrice;        // Giá đơn vị
    Long totalItemsPrice;  // Tổng tiền (Giá x Số lượng)
}