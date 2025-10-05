package com.humg.HotelSystemManagement.utils.enums;

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
