package com.humg.HotelSystemManagement.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private String SECRET_KEY = "whatdadogdoing"; //Khóa bí mật:)
    private long JWT_EXPIRATION = 1000 * 60 * 60; //Token hết hạn sau 1 giờ(1000ms * 60s * 60m)

    //Hàm private tạo JWT từ claims và subject
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder() //Khởi tạo đối tượng Jwts builder để build một token mới
                .setClaims(claims) //Đăng ký dữ liệu tùy chỉnh ở payload để nhúng vào JWT(Ví dụ: vai trò user, email, sdt, ...)
                .setSubject(subject) //Chỉ đích danh người dùng truyền tải(Thường là username, ở đây là email để đăng nhập)
                .setIssuedAt(new Date(System.currentTimeMillis())) //Thiết lập thời điểm tạo Token
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) //Định nghĩa thời điểm hết hạn token, giá trị enum trên là thời gian sống của token(tính theo ms)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) //Ký JWT bằng thuật toán HS256 và SECRET_KEY/ chữ ký này sẽ giúp xác minh token có hợp lệ hay không
                .compact(); //Biến mọi thứ thành một chuỗi JWT hoàn chỉnh
        //Chuỗi ví dụ: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.SIGNATURE
    }

    //Hàm công khai tạo JWT từ email và role
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>(); //Tạo HashMap lưu trữ cặp key value cho payLoad (Ví dụ trong payload: "email" : xxxx@gmail.com)
        claims.put("role", role); //Thêm vai trò vào payload
        return createToken(claims, email); //Gọi hàm private để tạo token
    }

    //Hàm lấy toàn bộ claims từ token
    private Claims extractAllClaims(String token){
        return Jwts.parser() //Tạo parser để đọc JWT
                .setSigningKey(SECRET_KEY)//Đặt khóa bí mật để kiểm tra signature
                .parseClaimsJwt(token)//Parse token thành đối tượng JWS
                .getBody();//Lấy payload (claims)
    }

    //Hàm generic để trích xuất thông tin bất kỳ từ token
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token); //Lấy toàn bộ Payload
        return claimResolver.apply(claims); //Áp dụng lambda để lấy giá trị cụ thể (email, role, vv)
    }

    //Lấy Email từ token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject); //Trích xuất "sub" (email) từ payload
    }

    //Lấy Role từ token
    public String extractRole(String token){
        return extractClaim(token, claims -> claims.get("role", String.class)); //Trích xuất "role" từ payload
    }

    //Lấy thời gian hết hạn từ token
    private Date extractExpiration(String token){
        return extractClaim(token , Claims::getExpiration); //Trích xuất "exp" từ payload
    }

    //Kiểm tra token đã hết hạn chưa
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date()); //So sánh thời gian hết hạn với hiện tại
    }

    //Kiểm tra token đã hết hạn chưa
    public Boolean isTokenValid(String token, String email){
        final String extractedEmail = extractEmail(token); //Lấy email từ token
        Boolean conditionResult = extractedEmail.equals(email) && !isTokenExpired(token); //Kiểm tra email xem có khớp không và token đã hết hạn chưa
        return conditionResult;
    }
}
