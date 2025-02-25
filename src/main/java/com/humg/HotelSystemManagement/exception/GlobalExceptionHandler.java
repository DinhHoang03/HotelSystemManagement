package com.humg.HotelSystemManagement.exception;

import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.enums.ValidationErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Hàm bắt lỗi sử lý validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<APIResponse<Object>> handleValidationError(MethodArgumentNotValidException e){
        //Tạo map chứa nhiều lỗi validation để xử lý
        Map<String, String> errors = new HashMap<>();
        int code = HttpStatus.BAD_REQUEST.value();

        //Duyệt danh sách với các lỗi validation được lưu trong FieldError bằng hàm getbindingResult và fieldError
        for(FieldError error : e.getBindingResult().getFieldErrors()){
            //Lấy nội dung message trong các annotation validation
            String errorCode = error.getDefaultMessage();
            //Lấy Enum errorCode để lấy code và message của lỗi đó
            ValidationErrorCode validationErrorCode = getCustomerErrorCode(errorCode);

            //Nếu biến này có chứa enum thì sẽ được setup vào hashmap để print lỗi, chứa cặp code và message
            if(validationErrorCode != null){
                errors.put(error.getField(), validationErrorCode.getMessage());
                code = validationErrorCode.getCode();
            }else{
                //nếu không được thì sẽ lấy hẳn lỗi và message trong annotation validation luôn
                errors.put(error.getField(), "Invalid input: " + error.getDefaultMessage());
            }
        }

        //Trả về giá trị thành công của APIResponse
        return ResponseEntity.badRequest().body(APIResponse.builder()
                .result(errors)
                .message("Validation failed!")
                .code(code)
                .build());
    }

    //Hàm này sẽ lấy giá trị message của FieldError để xử lý so sánh và ánh xạ đổi sang dạng Enum
    private ValidationErrorCode getCustomerErrorCode(String code){
        for(ValidationErrorCode errorCode : ValidationErrorCode.values()){
            if(errorCode.name().equals(code)){
                return errorCode;
            }
        }
        //Nếu không tìm thấy chuỗi tương ứng với Enum thì sẽ ra là unknown error(Lỗi kĩ thuật của hệ thống do báo sai)
        return ValidationErrorCode.UNKNOWN_ERROR;
    }
    
    //Hàm xủ lý App Exception của hệ thống
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse> handlingAppException(AppException e){
        AppErrorCode appErrorCode = e.getAppErrorCode();

        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(appErrorCode.getCode());
        apiResponse.setMessage((appErrorCode.getMessage()));

        return ResponseEntity.badRequest().body(apiResponse);
    }


}
