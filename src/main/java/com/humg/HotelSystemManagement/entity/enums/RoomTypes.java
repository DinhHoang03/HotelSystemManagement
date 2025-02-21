package com.humg.HotelSystemManagement.entity.enums;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RoomTypes {
    STANDARD(""),
    SUPERIOR(""),
    DELUXE("");

    String description;
}
