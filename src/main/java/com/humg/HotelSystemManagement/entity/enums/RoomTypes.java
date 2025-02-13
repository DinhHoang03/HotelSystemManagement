package com.humg.HotelSystemManagement.entity.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum RoomTypes {
    STANDARD(""),
    SUPERIOR(""),
    DELUXE("");

    String description;
}
