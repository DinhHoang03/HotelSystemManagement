package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.utils.NormalizeString;
import com.humg.HotelSystemManagement.dto.request.booking.bookingItems.BookingItemRequest;
import com.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.HotelOffersRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
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
    NormalizeString normalizeString;

    @Transactional
    public BookingItemResponse createOrder(BookingItemRequest request, String username) {
        BookingItems bookingItems;

        if(request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }
            var normalizeHotelOffer = normalizeString.normalizedString(request.getHotelOffer());

            var hotelOffer = hotelOffersRepository.findByServiceTypes(normalizeHotelOffer)
                    .orElseThrow(
                            () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
            );

            var totalItemsPrice = hotelOffer.getPrice() * request.getQuantity();
            var quantity = request.getQuantity();

            BookingItems bookingItem = BookingItems.builder()
                    .booking(null)
                    .username(username) // Gắn userId từ token
                    .quantity(quantity)
                    .totalItemsPrice(totalItemsPrice)
                    .hotelOffers(hotelOffer)
                    .build();


        var result = bookingItemsRepository.save(bookingItem);

        return BookingItemResponse.builder()
                .bookingItemId(result.getBookingItemId())
                .hotelOffer(result.getHotelOffers().getServiceTypes())
                .quantity(result.getQuantity())
                .totalItemsPrice(result.getTotalItemsPrice())
                .build();
    }

    public void deleteBookingItems(String bookingItemId){
        var bookingItems = bookingItemsRepository.findById(bookingItemId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        bookingItemsRepository.delete(bookingItems);
    }
}
