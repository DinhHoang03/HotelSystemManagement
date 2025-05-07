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
    OBJECT_EXISTED(1011, "This object is existed!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "This account currently exists!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "This account does not exist!", HttpStatus.NOT_FOUND),
    LIST_EMPTY(1003, "List is empty!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "Access denied! You have no permission to access this service!", HttpStatus.FORBIDDEN),
    SIGN_TOKEN_ERROR(1006, "Cannot create token", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_STATUS(1007, "Status currently failed!", HttpStatus.BAD_REQUEST),
    REQUEST_IS_NULL(1008, "This request is null!", HttpStatus.BAD_REQUEST),
    NO_ROLES_ASSIGNED(1009, "No such role found on system!", HttpStatus.BAD_REQUEST),
    USERNAME_CONFLICT(1010, "Invalid duplicate user data!", HttpStatus.BAD_REQUEST),
    INVALID_DATE(1011, "Date is invalid, try again!", HttpStatus.BAD_REQUEST),
    ROOM_ALREADY_BOOKED(1012, "Room is already booked!", HttpStatus.BAD_REQUEST),
    ROOM_NOT_AVAILABLE(1012, "Room is not avaiable now!", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_ROOM_ID(1013, "Invalid booking room id!", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_ITEM_ID(1014, "Invalid booking item id!", HttpStatus.BAD_REQUEST),
    PAYMENT_CREATION_FAILED(1015, "Payment creation failed!", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(1016, "Invalid amount", HttpStatus.BAD_REQUEST),
    ZALOPAY_ERROR(1017, "ZaloPay API Error", HttpStatus.BAD_REQUEST),
    USER_NOT_APPROVE(1018, "You need admin approve to grand access to our system, please contact to admin to unlock your account", HttpStatus.BAD_REQUEST),
    ORDER_CREATE_FAILED(1019, "Create order failed!", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
