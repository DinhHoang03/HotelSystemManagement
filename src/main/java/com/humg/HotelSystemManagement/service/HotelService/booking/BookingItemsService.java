package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.service.SystemService.NormalizeString;
import com.humg.HotelSystemManagement.dto.request.booking.bookingItems.BookingItemRequest;
import com.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.BookingItemsMapper;
import com.humg.HotelSystemManagement.repository.HotelOffersRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemsService {
    BookingItemsRepository bookingItemsRepository;
    HotelOffersRepository hotelOffersRepository;
    BookingItemsMapper bookingItemsMapper;
    NormalizeString normalizeString;

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

    @Transactional
    public List<BookingItemResponse> createOrders(List<BookingItemRequest> requests, String userId){
        if(requests == null || requests.isEmpty()){
            return new ArrayList<>();
        }

        List<BookingItems> bookingItemsList = requests
                .stream()
                .map(
                        request -> {
                            var normalizeHotelOffer = normalizeString.normalizedString(request.getHotelOffer());
                            var hotelOffer = hotelOffersRepository.findByServiceTypes(normalizeHotelOffer)
                                    .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

                            var quantity = request.getQuantity();
                            var totalItemsPrice = hotelOffer.getPrice() * quantity;

                            return BookingItems.builder()
                                    .booking(null)
                                    .username(userId)
                                    .quantity(quantity)
                                    .hotelOffers(hotelOffer)
                                    .totalItemsPrice(totalItemsPrice)
                                    .build();
                        }
                )
                .collect(Collectors.toList());

        var result = bookingItemsRepository.saveAll(bookingItemsList);

        return result
                .stream()
                .map(bookingItems -> BookingItemResponse.builder()
                        .bookingItemId(bookingItems.getBookingItemId())
                        .hotelOffer(bookingItems.getHotelOffers().getServiceTypes())
                        .quantity(bookingItems.getQuantity())
                        .totalItemsPrice(bookingItems.getTotalItemsPrice())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteBookingItems(String bookingItemId){
        var bookingItems = bookingItemsRepository.findById(bookingItemId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        bookingItemsRepository.delete(bookingItems);
    }
}
