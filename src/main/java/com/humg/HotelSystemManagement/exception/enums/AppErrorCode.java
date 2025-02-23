package com.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public enum AppErrorCode {
    USER_EXISTED(1001, "This account currently exists!"),
    USER_NOT_EXISTED(1002, "This account does not exist!"),
    REQUEST_NULL(1003, "Request is null!");


    int code;
    String message;
}
