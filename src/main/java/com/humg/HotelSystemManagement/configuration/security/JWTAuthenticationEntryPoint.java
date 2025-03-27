package com.humg.HotelSystemManagement.configuration.security; // Khai báo package để tổ chức mã nguồn theo cấu trúc thư mục

import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper để chuyển đổi đối tượng Java thành JSON
import com.humg.HotelSystemManagement.dto.response.APIResponse; // Import APIResponse, một DTO dùng để định dạng phản hồi API
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode; // Import enum AppErrorCode chứa các mã lỗi ứng dụng
import jakarta.servlet.ServletException; // Import ServletException để xử lý ngoại lệ từ servlet
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest để xử lý thông tin yêu cầu HTTP
import jakarta.servlet.http.HttpServletResponse; // Import HttpServletResponse để gửi phản hồi HTTP
import org.springframework.http.MediaType; // Import MediaType để chỉ định kiểu nội dung phản hồi (application/json)
import org.springframework.security.core.AuthenticationException; // Import AuthenticationException cho lỗi xác thực
import org.springframework.security.web.AuthenticationEntryPoint; // Import AuthenticationEntryPoint để xử lý yêu cầu không xác thực

import java.io.IOException; // Import IOException để xử lý lỗi khi ghi dữ liệu vào phản hồi

// Định nghĩa class JWTAuthenticationEntryPoint triển khai giao diện AuthenticationEntryPoint để xử lý lỗi xác thực JWT
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    // Phương thức commence được gọi khi xác thực thất bại, nhận yêu cầu, phản hồi và ngoại lệ xác thực
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // Gán mã lỗi UNAUTHENTICATED (401) từ enum AppErrorCode
        AppErrorCode appErrorCode = AppErrorCode.UNAUTHENTICATED;

        // Thiết lập mã trạng thái HTTP của phản hồi (ví dụ: 401) từ AppErrorCode
        response.setStatus(appErrorCode.getHttpStatusCode().value());

        // Thiết lập kiểu nội dung phản hồi là JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Tạo đối tượng APIResponse sử dụng Builder pattern để định dạng phản hồi lỗi
        APIResponse<?> apiResponse = APIResponse.builder()
                .code(appErrorCode.getCode()) // Gán mã lỗi (ví dụ: "401")
                .message(appErrorCode.getMessage()) // Gán thông điệp lỗi (ví dụ: "UNAUTHENTICATED")
                .build(); // Hoàn thành xây dựng đối tượng APIResponse

        // Tạo instance của ObjectMapper để chuyển đổi APIResponse thành chuỗi JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // Ghi chuỗi JSON vào phản hồi bằng cách sử dụng PrintWriter từ response
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // Đẩy toàn bộ dữ liệu trong bộ đệm của response ra để gửi ngay lập tức tới client
        response.flushBuffer();
    }
}
