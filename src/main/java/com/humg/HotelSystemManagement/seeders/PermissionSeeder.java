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
@Order(1) // QUAN TRỌNG: Chạy đầu tiên
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionSeeder implements CommandLineRunner {
    PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (permissionRepository.count() == 0) {
            log.info("Seeding Permissions...");

            List<Permission> permissions = List.of(
                    // 1. Room Management
                    build("ROOM_CREATE", "Create new rooms, types, statuses"),
                    build("ROOM_VIEW", "View room lists and details"),
                    build("ROOM_DELETE", "Delete rooms"),
                    build("OFFER_CREATE", "Create hotel offers"),
                    build("OFFER_DELETE", "Delete hotel offers"),

                    // 2. Booking & Payment
                    build("BOOKING_CREATE", "Create new bookings"),
                    build("BOOKING_VIEW", "View booking details"),
                    build("BOOKING_CANCEL", "Cancel bookings"),
                    build("BOOKING_UPDATE", "Add items/services to booking"),
                    build("PAYMENT_EXECUTE", "Execute payments (Zalo/PayPal)"),

                    // 3. Bill Management
                    build("BILL_CREATE", "Create bills (Check-out)"),
                    build("BILL_VIEW", "View bills"),
                    build("BILL_DELETE", "Delete bills (Sensitive)"),

                    // 4. Customer Management
                    build("CUSTOMER_UPDATE", "Update customer info"),
                    build("CUSTOMER_DELETE", "Delete customer accounts"),
                    build("CUSTOMER_PROFILE", "View own profile"),
                    build("CUSTOMER_LIST_VIEW", "View list of customers"),

                    // 5. Admin & Employee Management
                    build("EMPLOYEE_VIEW", "View employee list"),
                    build("EMPLOYEE_APPROVE", "Approve/Reject employees"),
                    build("ATTENDANCE_VIEW_ALL", "View all attendance records"),
                    build("DASHBOARD_STATS", "View revenue and statistics"),

                    // 6. System
                    build("SYSTEM_MANAGE", "Manage Roles and Permissions"),
                    build("AI_CHAT", "Use Gemini AI Chatbot")
            );

            permissionRepository.saveAll(permissions);
            log.info("Permissions seeded successfully!");
        }
    }

    private Permission build(String name, String desc) {
        return Permission.builder().name(name).description(desc).build();
    }
}