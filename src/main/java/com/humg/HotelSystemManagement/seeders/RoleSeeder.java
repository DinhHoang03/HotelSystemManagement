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
@Order(2) // QUAN TRỌNG: Chạy sau khi Permission đã có
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleSeeder implements CommandLineRunner {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info("Seeding Roles and assigning Permissions...");

            // 1. ROLE: CUSTOMER
            // Quyền: Đặt phòng, Xem phòng, Chat AI, Sửa hồ sơ, Thanh toán
            var customerPerms = getPerms(List.of(
                    "BOOKING_CREATE", "BOOKING_VIEW", "BOOKING_CANCEL",
                    "CUSTOMER_UPDATE", "CUSTOMER_PROFILE", "PAYMENT_EXECUTE",
                    "AI_CHAT", "ROOM_VIEW"
            ));
            Role customerRole = buildRole("CUSTOMER", "Hotel Guest", customerPerms);

            // 2. ROLE: RECEPTIONIST (Lễ tân)
            // Quyền: Quản lý khách, đặt phòng hộ, check-in/out (tạo bill)
            var receptionistPerms = getPerms(List.of(
                    "ROOM_VIEW",
                    "BOOKING_CREATE", "BOOKING_VIEW", "BOOKING_CANCEL", "BOOKING_UPDATE",
                    "BILL_CREATE", "BILL_VIEW",
                    "CUSTOMER_LIST_VIEW", "CUSTOMER_UPDATE"
            ));
            Role receptionistRole = buildRole("RECEPTIONIST", "Front Desk", receptionistPerms);

            // 3. ROLE: ACCOUNTANT (Kế toán)
            // Quyền: Xem tiền, Xóa hóa đơn sai, Xem nhân viên (tính lương)
            var accountantPerms = getPerms(List.of(
                    "BILL_VIEW", "BILL_DELETE", "DASHBOARD_STATS", "EMPLOYEE_VIEW"
            ));
            Role accountantRole = buildRole("ACCOUNTANT", "Financial Staff", accountantPerms);

            // 4. ROLE: DEPARTMENT_HEAD (Trưởng bộ phận)
            // Quyền: Lễ tân + Tạo phòng + Quản lý nhân viên + Xem báo cáo
            var headPerms = new HashSet<>(receptionistPerms); // Kế thừa quyền lễ tân
            headPerms.addAll(getPerms(List.of(
                    "ROOM_CREATE", "OFFER_CREATE",
                    "EMPLOYEE_VIEW", "EMPLOYEE_APPROVE",
                    "ATTENDANCE_VIEW_ALL", "DASHBOARD_STATS"
            )));
            Role headRole = buildRole("DEPARTMENT_HEAD", "Manager", headPerms);

            // 5. ROLE: ADMIN (Trùm cuối)
            // Quyền: Lấy TẤT CẢ quyền trong database
            var allPerms = new HashSet<>(permissionRepository.findAll());
            Role adminRole = buildRole("ADMIN", "Super Administrator", allPerms);

            // 6. ROLE: CLEANER / WAITER (Nhân viên thường)
            // Quyền: Cơ bản (Giả sử chỉ xem phòng để biết mà dọn)
            var staffPerms = getPerms(List.of("ROOM_VIEW"));
            Role cleanerRole = buildRole("CLEANER", "Housekeeping", staffPerms);
            Role waiterRole = buildRole("WAITER", "Restaurant Staff", staffPerms);

            roleRepository.saveAll(List.of(
                    customerRole, receptionistRole, accountantRole,
                    headRole, adminRole, cleanerRole, waiterRole
            ));

            log.info("Roles seeded successfully!");
        }
    }

    // Helper: Lấy danh sách Permission từ DB theo tên
    private Set<Permission> getPerms(List<String> names) {
        return new HashSet<>(permissionRepository.findAllById(names));
    }

    // Helper: Build Role Object
    private Role buildRole(String name, String desc, Set<Permission> perms) {
        return Role.builder()
                .name(name)
                .description(desc)
                .permissions(perms)
                .build();
    }
}