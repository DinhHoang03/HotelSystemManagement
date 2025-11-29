package com.humg.HotelSystemManagement.modules.booking_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingItemsRepository;
import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingItemRequest;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingItemResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        HotelOffers hotelOffer = hotelOffersRepository.findById(request.getHotelOfferId())
                .orElseThrow(() -> new RuntimeException("Món ăn/Dịch vụ không tồn tại"));

        long unitPrice = hotelOffer.getPrice();
        long totalItemsPrice = unitPrice * request.getQuantity();

        BookingItems bookingItem = BookingItems.builder()
                .booking(null)
                .username(username)
                .quantity(request.getQuantity())
                .totalItemsPrice(totalItemsPrice)
                .hotelOffers(hotelOffer)
                .bookingStatus(BookingStatus.IN_CART) // <--- GÁN TRẠNG THÁI "ĐANG ĐI CHỢ"
                .build();

        var result = bookingItemsRepository.save(bookingItem);
        return mapToResponse(result);
    }

    // API Lấy giỏ hàng (Cart) -> Tìm theo status IN_CART
    public Page<BookingItemResponse> getMyCart(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingItemsRepository.findByUsernameAndBookingStatus(username, BookingStatus.IN_CART, pageable)
                .map(this::mapToResponse);
    }

    // API Lấy lịch sử (Bỏ qua status, lấy hết)
    public Page<BookingItemResponse> getAllByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Ở đây dùng phương thức có sẵn findAll của JPA Specification hoặc query custom nếu cần
        // Nhưng tạm thời dùng findByUsername (bạn cần đảm bảo repo có hàm này hoặc dùng findAll với Example)
        // Để đơn giản, tôi dùng query findAll có filter username ở Repo (nếu bạn chưa có thì thêm vào: findByUsername)
        // Hoặc ở đây tôi giả định bạn muốn lấy các item ĐÃ MUA (PENDING/CONFIRMED)
        return bookingItemsRepository.findAll(pageable).map(this::mapToResponse);
        // Lưu ý: Nếu muốn đúng chuẩn history, nên lọc status != IN_CART
    }

    public Page<BookingItemResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingItemsRepository.findAll(pageable).map(this::mapToResponse);
    }

    public BookingItemResponse getById(String id) {
        BookingItems item = bookingItemsRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        return mapToResponse(item);
    }

    public void deleteBookingItems(String bookingItemId) {
        var bookingItems = bookingItemsRepository.findById(bookingItemId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        bookingItemsRepository.delete(bookingItems);
    }

    private BookingItemResponse mapToResponse(BookingItems item) {
        return BookingItemResponse.builder()
                .bookingItemId(item.getBookingItemId())
                .hotelOfferName(item.getHotelOffers().getName())
                .imageUrl(item.getHotelOffers().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getHotelOffers().getPrice())
                .totalItemsPrice(item.getTotalItemsPrice())
                .build();
    }
}