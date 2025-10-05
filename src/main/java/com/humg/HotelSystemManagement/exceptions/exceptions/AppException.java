package com.humg.HotelSystemManagement.exceptions.exceptions;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
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
