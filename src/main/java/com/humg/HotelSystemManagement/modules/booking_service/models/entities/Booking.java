package com.humg.HotelSystemManagement.modules.booking_service.models.entities;

import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    String bookingId;

    @Column(name = "booking_date", nullable = false)
    LocalDate bookingDate; // Ngày tạo đơn

    // --- QUẢN LÝ TRẠNG THÁI ---
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    PaymentStatus paymentStatus;

    // --- THÔNG TIN THANH TOÁN (Thay thế BookingBill) ---
    @Column(name = "payment_date")
    LocalDateTime paymentDate; // Thời điểm thanh toán xong

    @Column(name = "payment_transaction_id")
    String paymentTransactionId; // Mã giao dịch từ cổng thanh toán (Zalo/Paypal)

    // --- QUẢN LÝ TÀI CHÍNH ---
    @Column(name = "total_room_price")
    @Builder.Default
    Long totalRoomPrice = 0L;

    @Column(name = "total_service_price")
    @Builder.Default
    Long totalServicePrice = 0L; // Tiền ăn uống, minibar

    @Column(name = "grand_total")
    @Builder.Default
    Long grandTotal = 0L; // Tổng tiền phải trả

    // --- QUAN HỆ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    // Cascade ALL: Khi lưu Booking, tự động lưu luôn Room và Items. OrphanRemoval: Xóa booking sẽ xóa luôn items con.
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingRoom> bookingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingItems> bookingItems = new ArrayList<>();

    // --- BUSINESS METHODS (Logic tính toán nằm ngay trong Entity) ---
    public void calculateTotals() {
        this.totalRoomPrice = bookingRooms.stream()
                .mapToLong(BookingRoom::getTotalRoomAmount)
                .sum();

        this.totalServicePrice = bookingItems.stream()
                .mapToLong(BookingItems::getTotalItemsPrice)
                .sum();

        this.grandTotal = this.totalRoomPrice + this.totalServicePrice;
    }

    public void addBookingRoom(BookingRoom room) {
        if (this.bookingRooms == null) this.bookingRooms = new ArrayList<>();
        this.bookingRooms.add(room);
        room.setBooking(this);
    }

    public void addBookingItem(BookingItems item) {
        if (this.bookingItems == null) this.bookingItems = new ArrayList<>();
        this.bookingItems.add(item);
        item.setBooking(this);
    }
}