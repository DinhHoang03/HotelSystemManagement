package com.humg.HotelSystemManagement.modules.booking_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingItemsRepository;
import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingItemRequest;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingItemResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemsService {
    BookingItemsRepository bookingItemsRepository;
    HotelOffersRepository hotelOffersRepository;

    @Transactional
    public BookingItemResponse createOrder(BookingItemRequest request, String username) {
        if (request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        // 1. Tìm món ăn/dịch vụ theo ID (Thay vì theo tên như cũ)
        HotelOffers hotelOffer = hotelOffersRepository.findById(request.getHotelOfferId())
                .orElseThrow(() -> new RuntimeException("Món ăn/Dịch vụ không tồn tại"));

        // 2. Tính tổng tiền
        long unitPrice = hotelOffer.getPrice();
        long totalItemsPrice = unitPrice * request.getQuantity();

        // 3. Tạo Item (Lúc này chưa gắn vào Booking nào, coi như giỏ hàng tạm của User)
        BookingItems bookingItem = BookingItems.builder()
                .booking(null)
                .username(username)
                .quantity(request.getQuantity())
                .totalItemsPrice(totalItemsPrice)
                .hotelOffers(hotelOffer)
                .build();

        var result = bookingItemsRepository.save(bookingItem);

        return mapToResponse(result);
    }

    public void deleteBookingItems(String bookingItemId) {
        var bookingItems = bookingItemsRepository.findById(bookingItemId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        bookingItemsRepository.delete(bookingItems);
    }

    // Helper map response
    private BookingItemResponse mapToResponse(BookingItems item) {
        return BookingItemResponse.builder()
                .bookingItemId(item.getBookingItemId())
                .hotelOfferName(item.getHotelOffers().getName()) // Lấy tên món (Phở bò)
                .imageUrl(item.getHotelOffers().getImageUrl())   // Lấy ảnh món
                .quantity(item.getQuantity())
                .unitPrice(item.getHotelOffers().getPrice())
                .totalItemsPrice(item.getTotalItemsPrice())
                .build();
    }
}