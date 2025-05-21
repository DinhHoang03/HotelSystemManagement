package com.humg.HotelSystemManagement.service.HotelService.hotel;

import com.humg.HotelSystemManagement.dto.request.room.RoomRequest;
import com.humg.HotelSystemManagement.dto.response.room.RoomResponse;
import com.humg.HotelSystemManagement.entity.enums.RoomStatus;
import com.humg.HotelSystemManagement.entity.roomManagerment.Room;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomTypeRepository;
import com.humg.HotelSystemManagement.utils.Interfaces.ISimpleCRUDService;
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
public class RoomService implements ISimpleCRUDService<RoomResponse, RoomRequest, Long> {
    RoomRepository roomRepository;
    RoomTypeRepository roomTypeRepository;
    //RoomStatusRepository roomStatusRepository;
    NormalizeString normalizeString;

    @Override
    public RoomResponse create(RoomRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        String roomTypeNormalized = normalizeString.normalizedString(request.getRoomType());
        //String roomStatusNormalized = normalizeString.normalizedString(request.getRoomStatus());

        var status = RoomStatus.AVAILABLE;
        if(roomRepository.existsByRoomNumber(request.getRoomNumber()))
            throw new AppException(AppErrorCode.OBJECT_EXISTED);

        var roomType = roomTypeRepository.findByRoomTypes(request.getRoomType())
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        //var roomStatus = roomStatusRepository.findByRoomStatus(roomStatusNormalized);

        String roomTypeString = roomType.getRoomTypes();

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(roomType)
                .roomStatus(status)
                .build();

        var result = roomRepository.save(room);

        return RoomResponse.builder()
                .roomId(result.getRoomId())
                .roomNumber(result.getRoomNumber())
                .roomType(roomTypeString)
                .roomStatus(result.getRoomStatus().toString())
                .build();
    }

    @Override
    public Page<RoomResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Room> result = roomRepository.findAll(pageable);

        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<RoomResponse> response = result.map(room -> {
            return RoomResponse.builder()
                    .roomId(room.getRoomId())  // Đảm bảo trả về roomId
                    .roomNumber(room.getRoomNumber())
                    .roomType(room.getRoomType().getRoomTypes())
                    .roomStatus(room.getRoomStatus().toString())
                    .build();
        });

        return response;
    }


    public RoomResponse getById(Long id) {
        var room = roomRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        RoomResponse result = RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType().getRoomTypes())
                .roomStatus(room.getRoomStatus().toString())
                .build();

        return result;
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var room = roomRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        var roomTypeNormalized = normalizeString.normalizedString(request.getRoomType());
        //var roomStatusNormalized = normalizeString.normalizedString(request.getRoomStatus());

        //var roomStatus = roomStatusRepository.findByRoomStatus(roomStatusNormalized);
        var roomType = roomTypeRepository.findByRoomTypes(roomTypeNormalized)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        //room.setRoomStatus(roomStatus.get());
        room.setRoomType(roomType);

        var update = roomRepository.save(room);

        RoomResponse result = RoomResponse.builder()
                .roomId(update.getRoomId())
                .roomNumber(update.getRoomNumber())
                .roomStatus(update.getRoomStatus().toString())
                .roomType(update.getRoomType().getRoomTypes())
                .build();

        return result;
    }

    @Override
    public void delete(Long id) {
        var roomType = roomRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        roomRepository.deleteById(id);
    }
}
