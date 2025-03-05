package com.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public enum AppErrorCode {
    OBJECT_IS_NULL(1000, "Object is null!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "This account currently exists!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "This account does not exist!", HttpStatus.NOT_FOUND),
    LIST_EMPTY(1003, "List is empty!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Password is incorrect! Please try again", HttpStatus.UNAUTHORIZED),
    SIGN_TOKEN_ERROR(1005, "Cannot create token", HttpStatus.),
    INVALID_STATUS(1006, "Status currently failed!"),
    ADMIN_CREATION_NOT_ALLOWED(1007, "Only one admin account only in system management!")
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
