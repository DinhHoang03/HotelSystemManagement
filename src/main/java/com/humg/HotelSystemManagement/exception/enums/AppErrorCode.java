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
    UNAUTHENTICATED(1004, "Password or username is incorrect! Please try again", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "Access denined! You have no permission to access this service!", HttpStatus.FORBIDDEN),
    SIGN_TOKEN_ERROR(1006, "Cannot create token", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_STATUS(1007, "Status currently failed!", HttpStatus.BAD_REQUEST),
    ADMIN_CREATION_NOT_ALLOWED(1008, "Only one admin account only in system management!", HttpStatus.FORBIDDEN),
    NO_ROLES_ASSIGNED(1009, "No such role found on system!", HttpStatus.BAD_REQUEST),
    USERNAME_CONFLICT(1010, "Invalid duplicate user data!", HttpStatus.BAD_REQUEST)
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
