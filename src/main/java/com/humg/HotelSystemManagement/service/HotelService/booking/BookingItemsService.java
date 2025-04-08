package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.entity.booking.Booking;
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

    public BookingItemResponse createOrder(BookingItemRequest request) {
        BookingItems bookingItems;

        if(request != null) {
            var normalizeHotelOffer = normalizeString.normalizedString(request.getHotelOffer());

            var hotelOffer = hotelOffersRepository.findByServiceTypes(normalizeHotelOffer)
                    .orElseThrow(
                            () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
            );

            var quantity = request.getQuantity();

            bookingItems = BookingItems.builder()
                    .quantity(quantity)
                    .hotelOffers(hotelOffer)
                    .build();

            var caculated = hotelOffer.getPrice() * quantity;
            bookingItems.setTotalItemsPrice(caculated);

        }else {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }
        var result = bookingItemsRepository.save(bookingItems);

        return BookingItemResponse.builder()
                .hotelOffer(result.getHotelOffers().getServiceTypes())
                .quantity(request.getQuantity())
                .totalItemsPrice(result.getTotalItemsPrice())
                .build();
    }

    public List<BookingItemResponse> createOrders(List<BookingItemRequest> requests, Booking booking){
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
                                    .booking(booking)
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
}
