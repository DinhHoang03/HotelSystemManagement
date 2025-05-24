package com.hotel.humg.HotelSystemManagement.service.HotelService.hotel;

import com.hotel.humg.HotelSystemManagement.dto.request.room.roomType.RoomTypeRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.room.roomType.RoomTypeResponse;
import com.hotel.humg.HotelSystemManagement.entity.roomManagerment.RoomType;
import com.hotel.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.hotel.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.hotel.humg.HotelSystemManagement.repository.roomManagerment.RoomTypeRepository;
import com.hotel.humg.HotelSystemManagement.utils.Interfaces.ISimpleCRUDService;
import com.hotel.humg.HotelSystemManagement.utils.NormalizeString;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomTypeService implements ISimpleCRUDService<RoomTypeResponse, RoomTypeRequest, Long> {
    RoomTypeRepository roomTypeRepository;
    NormalizeString normalizeString;

    @Override
    public RoomTypeResponse create(RoomTypeRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        String roomTypeNormalized = normalizeString.normalizedString(request.getRoomTypes());

        if(roomTypeRepository.existsByRoomTypes(request.getRoomTypes()))
            throw new AppException(AppErrorCode.OBJECT_EXISTED);

        RoomType roomType = RoomType.builder()
                .roomTypes(roomTypeNormalized)
                .halfDayPrice(request.getHalfDayPrice())
                .fullDayPrice(request.getFullDayPrice())
                .fullWeekPrice(request.getFullWeekPrice())
                .build();

        var result = roomTypeRepository.save(roomType);

        return RoomTypeResponse.builder()
                .roomTypeId(result.getRoomTypeId())
                .roomTypes(result.getRoomTypes())
                .halfDayPrice(result.getHalfDayPrice())
                .fullDayPrice(result.getFullDayPrice())
                .fullWeekPrice(result.getFullWeekPrice())
                .build();
    }

    @Override
    public Page<RoomTypeResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RoomType> result = roomTypeRepository.findAll(pageable);

        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<RoomTypeResponse> response = result.map(roomType -> {
            return RoomTypeResponse.builder()
                    .roomTypeId(roomType.getRoomTypeId())
                    .roomTypes(roomType.getRoomTypes())
                    .halfDayPrice(roomType.getHalfDayPrice())
                    .fullDayPrice(roomType.getFullDayPrice())
                    .fullWeekPrice(roomType.getFullWeekPrice())
                    .build();
        });

        return response;
    }


    public RoomTypeResponse getById(Long id) {
        var roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        RoomTypeResponse result = RoomTypeResponse.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .roomTypes(roomType.getRoomTypes())
                .halfDayPrice(roomType.getHalfDayPrice())
                .fullDayPrice(roomType.getFullDayPrice())
                .fullWeekPrice(roomType.getFullWeekPrice())
                .build();

        return result;
    }

    @Override
    public RoomTypeResponse update(Long id, RoomTypeRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        roomType.setHalfDayPrice(request.getHalfDayPrice());
        roomType.setFullDayPrice(request.getFullDayPrice());
        roomType.setFullWeekPrice(request.getFullWeekPrice());

        var update = roomTypeRepository.save(roomType);

        RoomTypeResponse result = RoomTypeResponse.builder()
                .roomTypeId(update.getRoomTypeId())
                .roomTypes(update.getRoomTypes())
                .halfDayPrice(update.getHalfDayPrice())
                .fullDayPrice(update.getFullDayPrice())
                .fullWeekPrice(update.getFullWeekPrice())
                .build();

        return result;
    }

    @Override
    public void delete(Long id) {
        var roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        roomTypeRepository.deleteById(id);
    }
}
