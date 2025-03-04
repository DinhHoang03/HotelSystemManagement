package com.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ValidationErrorCode {
    UNKNOWN_ERROR(100 ,"Unknown error"),
    INVALID_IDENTITY_ID(101, "Please enter right format of your identity ID!"),
    INVALID_PHONE_NUMBER(102, "Please enter your phone number valid!"),
    INVALID_EMAIL(103, "Please enter the valid email!"),
    INVALID_PASSWORD(104, "Please enter the valid password(Must be between 8 to 16 characters and required at least a lowercase letter, a uppercase letter and at least a special character"),
    REQUEST_NULL(105, "This field does not accept null request! Please enter your information here!"),
    INVALID_ROLE(106, "The role is invalid, please choose back again!"),
    INVALID_DOB(107, "Invalid birthdate, please choose again"),
    INVALID_GENDER(108, "Invalid gender, please choose again"),
    INVALID_USERNAME(109, "Invalid user name, username must be longer than 4 characters, please choose again"),
    ;

    int code;
    String message;
}
