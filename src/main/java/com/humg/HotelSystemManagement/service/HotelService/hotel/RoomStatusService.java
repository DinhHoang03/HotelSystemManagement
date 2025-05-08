package com.humg.HotelSystemManagement.service.HotelService.hotel;

import com.humg.HotelSystemManagement.dto.request.room.roomServiceStatus.RoomStatusRequest;
import com.humg.HotelSystemManagement.dto.response.room.roomServiceStatus.RoomStatusResponse;
import com.humg.HotelSystemManagement.entity.roomManagerment.RoomStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomStatusRepository;
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
public class RoomStatusService implements ISimpleCRUDService<RoomStatusResponse, RoomStatusRequest, Long> {
    RoomStatusRepository roomServiceStatusRepository;
    RoomRepository roomRepository;
    NormalizeString normalizeString;

    @Override
    public RoomStatusResponse create(RoomStatusRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        String normalizedRoomServiceStatus = normalizeString.normalizedString(request.getRoomStatus());

        if(roomServiceStatusRepository.existsByRoomStatus(normalizedRoomServiceStatus)) throw new AppException(AppErrorCode.OBJECT_EXISTED);

        var room = roomRepository.findByRoomNumber(request.getRoomNumber())
                .orElseThrow(
                        () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
                );

        RoomStatus roomServiceStatus = RoomStatus.builder()
                .roomStatus(normalizedRoomServiceStatus)
                .description(request.getDescription())
                .room(room)
                .build();

        var result = roomServiceStatusRepository.save(roomServiceStatus);

        return RoomStatusResponse.builder()
                .roomStatus(result.getRoomStatus())
                .description(result.getDescription())
                .roomNumber(result.getRoom().getRoomNumber())
                .build();
    }

    @Override
    public Page<RoomStatusResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomStatus> listPages = roomServiceStatusRepository.findAll(pageable);

        if(listPages.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<RoomStatusResponse> result = listPages.map(roomServiceStatus -> {
            return RoomStatusResponse.builder()
                    .roomStatus(roomServiceStatus.getRoomStatus())
                    .description(roomServiceStatus.getDescription())
                    .roomNumber(roomServiceStatus.getRoom().getRoomNumber())
                    .build();
        });

        return result;
    }

    @Override
    public RoomStatusResponse getById(Long id) {
        var roomServiceStatus = roomServiceStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        return RoomStatusResponse.builder()
                .roomStatus(roomServiceStatus.getRoomStatus())
                .description(roomServiceStatus.getDescription())
                .roomNumber(roomServiceStatus.getRoom().getRoomNumber())
                .build();
    }

    @Override
    public RoomStatusResponse update(Long id, RoomStatusRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {
        var result = roomServiceStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        roomServiceStatusRepository.deleteById(result.getRoomStatusId());
    }
}
