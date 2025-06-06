package com.humg.HotelSystemManagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class APIResponse<T> {
    @Builder.Default
    int code = 1000; //Success API Code(Other error code will be defined in enum class)
    String message;
    T result;
}
