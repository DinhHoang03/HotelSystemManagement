package com.hotel.humg.HotelSystemManagement.entity.enums;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ServiceTypes {
    LAUNDRY("Tính theo mỗi lần giặt 5kg"),
    BREAKFAST("Suất ăn tiêu chuẩn cho 1 người"),
    SPA_AND_MASSAGE("Gói 60 phút"),
    MINIBAR("Trọn gói cho minibar trong phòng"),
    LATE_CHECK_OUT("Áp dụng cho check-out sau 12h trưa");

    String description;

}

//LAUNDRY(Giặt giũ),
//    BREAKFAST(Bữa sáng),
//    SPA_AND_MASSAGE,
//    MINIBAR(Đồ uống trong phòng),
//    LATE_CHECK_OUT(Trả phòng muộn)
