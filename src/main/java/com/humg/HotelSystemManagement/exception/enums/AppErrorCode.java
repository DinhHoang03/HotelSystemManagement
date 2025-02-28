package com.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public enum AppErrorCode {
    OBJECT_IS_NULL(1000, "Object is null!"),
    USER_EXISTED(1001, "This account currently exists!"),
    USER_NOT_EXISTED(1002, "This account does not exist!"),
    LIST_EMPTY(1003, "List is empty!"),
    UNAUTHENTICATED(1004, "Password is incorrect! Please try again"),
    SIGN_TOKEN_ERROR(1005, "Cannot create token")
    ;

    int code;
    String message;
}
