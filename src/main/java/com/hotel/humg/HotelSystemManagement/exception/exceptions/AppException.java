package com.hotel.humg.HotelSystemManagement.exception.exceptions;

import com.hotel.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppException extends RuntimeException {
    AppErrorCode appErrorCode;
}
