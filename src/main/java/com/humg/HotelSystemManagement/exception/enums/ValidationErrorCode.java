package com.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ValidationErrorCode {
    INVALID_IDENTITY_ID(101, "Please enter right format of your identity ID!"),
    INVALID_PHONE_NUMBER(102, "Please enter your phone number valid!"),
    INVALID_EMAIL(103, "Please enter the valid email!"),
    INVALID_PASSWORD(104, "Please enter the valid password(Must be between 8 to 16 characters and required at least a lowercase letter, a uppercase letter and at least a special character"),
    UNKNOWN_ERROR(100 ,"Unknown error");

    int code;
    String message;
}
