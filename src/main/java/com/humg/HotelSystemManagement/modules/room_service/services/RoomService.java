package com.humg.HotelSystemManagement.modules.room_service.services;

import com.humg.HotelSystemManagement.modules.room_service.resources.requests.RoomRequest;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
import com.humg.HotelSystemManagement.utils.enums.RoomStatus;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomTypeRepository;
import com.humg.HotelSystemManagement.utils.interfaces.ISimpleCRUDService;
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
public class RoomService implements ISimpleCRUDService<RoomResponse, RoomRequest, Long> {
    RoomRepository roomRepository;
    RoomTypeRepository roomTypeRepository;

    @Override
    public RoomResponse create(RoomRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        if(roomRepository.existsByRoomNumber(request.getRoomNumber()))
            throw new AppException(AppErrorCode.OBJECT_EXISTED);

        var roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(roomType)
                .roomStatus(RoomStatus.AVAILABLE)
                // --- NEW FIELDS ---
                .floor(request.getFloor())
                .viewType(request.getViewType())
                .isClean(true) // Mặc định tạo mới là sạch
                .build();

        var result = roomRepository.save(room);
        return mapToResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Room> result = roomRepository.findAll(pageable);
        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return result.map(this::mapToResponse);
    }

    // API MỚI: Tìm kiếm theo tên loại phòng
    @Transactional(readOnly = true)
    public Page<RoomResponse> searchByRoomTypeName(String roomTypeName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Room> result = roomRepository.findByRoomTypeNameContaining(roomTypeName, pageable);

        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        return result.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        var room = roomRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );
        return mapToResponse(room);
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var room = roomRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        if (request.getRoomTypeId() != null) {
            var roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
            room.setRoomType(roomType);
        }

        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getFloor() != null) room.setFloor(request.getFloor());
        if (request.getViewType() != null) room.setViewType(request.getViewType());

        // Update trạng thái vệ sinh (cho tạp vụ)
        if (request.getIsClean() != null) room.setClean(request.getIsClean());

        var update = roomRepository.save(room);
        return mapToResponse(update);
    }

    @Override
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }
        roomRepository.deleteById(id);
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomStatus(room.getRoomStatus().toString())
                .floor(room.getFloor())
                .viewType(room.getViewType())
                .isClean(room.isClean())

                // Flatten RoomType info
                .roomTypeId(room.getRoomType().getRoomTypeId())
                .roomTypeName(room.getRoomType().getRoomTypes())
                .imageUrl(room.getRoomType().getImageUrl())
                .priceByDay(room.getRoomType().getFullDayPrice())
                .maxAdults(room.getRoomType().getMaxAdults())
                .build();
    }
}