package com.humg.HotelSystemManagement.modules.hotel_offer_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests.HotelOfferRequest;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses.HotelOfferResponse;
import com.humg.HotelSystemManagement.modules.minio_service.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelOfferService {
    private final HotelOffersRepository hotelOffersRepository;
    private final MinioService minioService; // Inject service upload ảnh

    // 1. CREATE: Tạo dịch vụ mới kèm ảnh
    @Transactional
    public HotelOfferResponse create(HotelOfferRequest request, MultipartFile image) {
        if (request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        // Kiểm tra trùng tên (nếu cần)
        // if (hotelOffersRepository.existsByName(request.getName())) ...

        String imageUrl = null;
        // Nếu có gửi ảnh thì upload lên MinIO
        if (image != null && !image.isEmpty()) {
            imageUrl = minioService.uploadFile(image);
        }

        // Map từ Request -> Entity
        HotelOffers offer = HotelOffers.builder()
                .serviceCategory(request.getServiceCategory()) // SPA, FOOD...
                .name(request.getName())                       // Phở Bò...
                .description(request.getDescription())
                .price(request.getPrice())
                .unitInfo(request.getUnitInfo())               // Bát/Suất...
                .imageUrl(imageUrl)                            // URL từ MinIO
                .build();

        var savedOffer = hotelOffersRepository.save(offer);
        return mapToResponse(savedOffer);
    }

    // 2. GET ALL (Phân trang)
    public Page<HotelOfferResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HotelOffers> result = hotelOffersRepository.findAll(pageable);
        return result.map(this::mapToResponse);
    }

    // 3. GET BY CATEGORY (Để hiển thị menu riêng biệt)
    public List<HotelOfferResponse> getByCategory(String category) {
        return hotelOffersRepository.findByServiceCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 4. DELETE
    public void delete(String id) {
        HotelOffers offer = hotelOffersRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        hotelOffersRepository.delete(offer);
    }

    public HotelOfferResponse getById(String id) {
        HotelOffers offer = hotelOffersRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        return mapToResponse(offer);
    }

    // 6. UPDATE (Cập nhật thông tin & Thay đổi ảnh nếu có)
    @Transactional
    public HotelOfferResponse update(String id, HotelOfferRequest request, MultipartFile image) {
        // 1. Tìm bản ghi cũ
        HotelOffers offer = hotelOffersRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        // 2. Cập nhật thông tin văn bản
        offer.setServiceCategory(request.getServiceCategory());
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        offer.setUnitInfo(request.getUnitInfo());

        // 3. Xử lý ảnh: Chỉ cập nhật nếu người dùng gửi file mới lên
        if (image != null && !image.isEmpty()) {
            // (Option) Nếu muốn tiết kiệm dung lượng, có thể xóa ảnh cũ trên MinIO ở đây trước khi up ảnh mới
            // minioService.deleteFile(offer.getImageUrl());

            String newImageUrl = minioService.uploadFile(image);
            offer.setImageUrl(newImageUrl);
        }

        // 4. Lưu và trả về
        var updatedOffer = hotelOffersRepository.save(offer);
        return mapToResponse(updatedOffer);
    }

    // Helper: Map Entity -> Response DTO
    private HotelOfferResponse mapToResponse(HotelOffers entity) {
        return HotelOfferResponse.builder()
                .id(entity.getHotelServiceId())
                .category(entity.getServiceCategory())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .unitInfo(entity.getUnitInfo())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}