package com.humg.HotelSystemManagement.seeders;

import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Permission;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(1) // Chạy trước RoleSeeder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionSeeder implements CommandLineRunner {
    PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (permissionRepository.count() == 0) {
            log.info("Seeding Permissions for ADMIN & CUSTOMER model...");

            List<Permission> permissions = List.of(
                    // === 1. QUẢN LÝ PHÒNG (Room) ===
                    build("ROOM_CREATE", "Tạo phòng và loại phòng mới (Admin)"),
                    build("ROOM_VIEW", "Xem danh sách và chi tiết phòng (Public)"),
                    build("ROOM_DELETE", "Xóa phòng (Admin)"),

                    // === 2. DỊCH VỤ KHÁCH SẠN (Offer/Menu) ===
                    build("OFFER_CREATE", "Tạo dịch vụ ăn uống/spa (Admin)"),
                    build("OFFER_VIEW", "Xem menu dịch vụ (Public)"),
                    build("OFFER_DELETE", "Xóa dịch vụ (Admin)"),

                    // === 3. ĐẶT PHÒNG (Booking) ===
                    build("BOOKING_CREATE", "Đặt phòng mới (Customer)"),
                    build("BOOKING_VIEW", "Xem lịch sử/chi tiết đặt phòng"),
                    build("BOOKING_CANCEL", "Hủy đặt phòng"),
                    build("BOOKING_UPDATE", "Thêm dịch vụ vào đơn đặt phòng"),

                    // === 4. THANH TOÁN (Payment & Bill) ===
                    build("PAYMENT_EXECUTE", "Thực hiện thanh toán Online (Zalo/PayPal)"),
                    build("BILL_CREATE", "Tạo hóa đơn/Check-out (Admin)"),
                    build("BILL_VIEW", "Xem hóa đơn"),
                    build("BILL_DELETE", "Xóa hóa đơn (Admin)"),

                    // === 5. QUẢN LÝ TÀI KHOẢN (Customer & User) ===
                    build("USER_UPDATE", "Cập nhật thông tin cá nhân"),
                    build("USER_PROFILE", "Xem profile bản thân"),
                    build("USER_DELETE", "Xóa tài khoản khách hàng (Admin)"),
                    build("USER_LIST_VIEW", "Xem danh sách khách hàng (Admin)"),
                    build("USER_CREATE", "Tạo khách hàng (Admin)"),

                    // === 6. HỆ THỐNG & TIỆN ÍCH KHÁC ===
                    build("SYSTEM_MANAGE", "Quản lý Role/Permission (Admin)"),
                    build("DASHBOARD_STATS", "Xem thống kê doanh thu (Admin)"),
                    build("AI_CHAT", "Chat với Gemini AI"),
                    build("FILES_UPLOAD", "Upload ảnh lên hệ thống")
            );

            permissionRepository.saveAll(permissions);
            log.info("Permissions seeded successfully!");
        }
    }

    private Permission build(String name, String desc) {
        return Permission.builder().name(name).description(desc).build();
    }
}