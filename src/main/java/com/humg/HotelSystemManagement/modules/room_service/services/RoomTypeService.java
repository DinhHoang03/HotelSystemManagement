package com.humg.HotelSystemManagement.modules.room_service.services;

import com.humg.HotelSystemManagement.modules.room_service.resources.requests.RoomTypeRequest;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomTypeResponse;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.RoomType;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomTypeRepository;
import com.humg.HotelSystemManagement.utils.interfaces.ISimpleCRUDService;
import com.humg.HotelSystemManagement.utils.NormalizeString;
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

        if(roomTypeRepository.existsByRoomTypes(roomTypeNormalized))
            throw new AppException(AppErrorCode.OBJECT_EXISTED);

        RoomType roomType = RoomType.builder()
                .roomTypes(roomTypeNormalized)
                // Map fields mới
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .halfDayPrice(request.getHalfDayPrice())
                .fullDayPrice(request.getFullDayPrice())
                .fullWeekPrice(request.getFullWeekPrice())
                .maxAdults(request.getMaxAdults() != null ? request.getMaxAdults() : 2) // Default 2
                .maxChildren(request.getMaxChildren() != null ? request.getMaxChildren() : 1) // Default 1
                .area(request.getArea())
                .amenities(request.getAmenities())
                .build();

        var result = roomTypeRepository.save(roomType);
        return mapToResponse(result);
    }

    @Override
    public Page<RoomTypeResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomType> result = roomTypeRepository.findAll(pageable);
        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return result.map(this::mapToResponse);
    }

    public RoomTypeResponse getById(Long id) {
        var roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );
        return mapToResponse(roomType);
    }

    @Override
    public RoomTypeResponse update(Long id, RoomTypeRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        // Update nếu có dữ liệu gửi lên
        if (request.getRoomTypes() != null) roomType.setRoomTypes(normalizeString.normalizedString(request.getRoomTypes()));
        if (request.getImageUrl() != null) roomType.setImageUrl(request.getImageUrl());
        if (request.getDescription() != null) roomType.setDescription(request.getDescription());

        if (request.getHalfDayPrice() != null) roomType.setHalfDayPrice(request.getHalfDayPrice());
        if (request.getFullDayPrice() != null) roomType.setFullDayPrice(request.getFullDayPrice());
        if (request.getFullWeekPrice() != null) roomType.setFullWeekPrice(request.getFullWeekPrice());

        if (request.getMaxAdults() != null) roomType.setMaxAdults(request.getMaxAdults());
        if (request.getMaxChildren() != null) roomType.setMaxChildren(request.getMaxChildren());
        if (request.getArea() != null) roomType.setArea(request.getArea());
        if (request.getAmenities() != null) roomType.setAmenities(request.getAmenities());

        var update = roomTypeRepository.save(roomType);
        return mapToResponse(update);
    }

    @Override
    public void delete(Long id) {
        if(!roomTypeRepository.existsById(id)) throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        roomTypeRepository.deleteById(id);
    }

    private RoomTypeResponse mapToResponse(RoomType entity) {
        return RoomTypeResponse.builder()
                .roomTypeId(entity.getRoomTypeId())
                .roomTypes(entity.getRoomTypes())
                .imageUrl(entity.getImageUrl())
                .description(entity.getDescription())
                .halfDayPrice(entity.getHalfDayPrice())
                .fullDayPrice(entity.getFullDayPrice())
                .fullWeekPrice(entity.getFullWeekPrice())
                .maxAdults(entity.getMaxAdults())
                .maxChildren(entity.getMaxChildren())
                .area(entity.getArea())
                .amenities(entity.getAmenities())
                .build();
    }
}