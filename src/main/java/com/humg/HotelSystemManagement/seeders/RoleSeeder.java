package com.humg.HotelSystemManagement.seeders;

import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Permission;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.PermissionRepository;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2) // Chạy sau PermissionSeeder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleSeeder implements CommandLineRunner {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info("Seeding Roles (ADMIN & CUSTOMER)...");

            // === 1. ROLE: CUSTOMER ===
            // Quyền hạn: Xem phòng/dịch vụ, Đặt phòng, Chat AI, Sửa hồ sơ, Thanh toán
            var customerPerms = getPerms(List.of(
                    "ROOM_VIEW",
                    "OFFER_VIEW",
                    "BOOKING_CREATE",
                    "BOOKING_VIEW",
                    "BOOKING_CANCEL",
                    "BOOKING_UPDATE", // Gọi đồ ăn/dịch vụ thêm
                    "CUSTOMER_UPDATE",
                    "CUSTOMER_PROFILE",
                    "PAYMENT_EXECUTE",
                    "AI_CHAT",
                    "FILES_UPLOAD", // Up avatar
                    "BILL_VIEW" // Xem hóa đơn của mình
            ));

            Role customerRole = Role.builder()
                    .name("CUSTOMER")
                    .description("Khách hàng sử dụng dịch vụ")
                    .permissions(customerPerms)
                    .build();

            // === 2. ROLE: ADMIN ===
            // Quyền hạn: Full quyền (Lấy tất cả permission trong DB)
            var allPerms = new HashSet<>(permissionRepository.findAll());

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Quản trị viên hệ thống")
                    .permissions(allPerms)
                    .build();

            roleRepository.saveAll(List.of(customerRole, adminRole));

            log.info("Roles ADMIN and CUSTOMER seeded successfully!");
        }
    }

    // Helper: Lấy tập hợp Permission theo tên
    private Set<Permission> getPerms(List<String> names) {
        return new HashSet<>(permissionRepository.findAllById(names));
    }
}