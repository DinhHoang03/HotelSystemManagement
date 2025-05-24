package com.hotel.humg.HotelSystemManagement.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ValidationErrorCode {
    UNKNOWN_ERROR(100 ,"Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_IDENTITY_ID(101, "Please enter right format of your identity ID!", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(102, "Please enter your phone number valid!", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(103, "Please enter the valid email!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(104, "Please enter the valid password(Must be between 8 to 16 characters and required at least a lowercase letter, a uppercase letter and at least a special character", HttpStatus.BAD_REQUEST),
    REQUEST_NULL(105, "This field does not accept null request! Please enter your information here!", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(106, "The role is invalid, please choose back again!", HttpStatus.BAD_REQUEST),
    INVALID_DOB(107, "Invalid birthdate, please choose again", HttpStatus.BAD_REQUEST),
    INVALID_GENDER(108, "Invalid gender, please choose again", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(109, "Invalid user name, username must be longer than 4 characters, please choose again", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(110, "Invalid status, please choose again", HttpStatus.BAD_REQUEST)
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
