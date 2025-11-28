package com.humg.HotelSystemManagement.modules.customer_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.configs.SecurityConfig;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.RoleRepository;
import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.customer_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.UserUpdateRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.utils.NormalizeString;
import com.humg.HotelSystemManagement.utils.enums.Gender;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import com.humg.HotelSystemManagement.utils.interfaces.IGeneralCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IGeneralCRUDService<UserResponse, UserCreationRequest, UserUpdateRequest, String> {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    NormalizeString normalizeString;


    // --- 1. CREATE ---
    public UserResponse create(UserCreationRequest request) {
        if (request == null) throw new AppException(AppErrorCode.OBJECT_IS_NULL);

        // Validate unique fields
        if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }

        // Manual Mapping: Request -> Entity
        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .identityId(request.getIdentityId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .dob(request.getDob())
                .address(request.getAddress())
                // Xử lý Enum Gender an toàn
                .gender(Gender.valueOf(request.getGender()))
                .userStatus(UserStatus.ENABLED) // Mặc định enable khi đăng ký
                .build();

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // Assign Role CUSTOMER
        var customerRole = roleRepository.findById("CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("CUSTOMER", "Customer Role", new HashSet<>())));
        user.setRoles(new HashSet<>(Set.of(customerRole)));

        // Save
        user = userRepository.save(user);

        // Manual Mapping: Entity -> Response
        return mapToUserResponse(user);
    }

    // --- 2. UPDATE ---
    public UserResponse update(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request == null) throw new AppException(AppErrorCode.OBJECT_IS_NULL);

        // Manual Mapping Update: Chỉ update các trường không null
        if (request.getName() != null && !request.getName().isEmpty()) user.setName(request.getName());
        if (request.getEmail() != null && !request.getEmail().isEmpty()) user.setEmail(request.getEmail());
        if (request.getPhone() != null && !request.getPhone().isEmpty()) user.setPhone(request.getPhone());
        if (request.getAddress() != null && !request.getAddress().isEmpty()) user.setAddress(request.getAddress());

        // Nếu muốn cho update cả username (thường thì không nên)
        if (request.getUsername() != null && !request.getUsername().isEmpty()) user.setUsername(request.getUsername());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    // --- 3. GET MY INFO ---
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return mapToUserResponse(user);
    }

    // --- 4. GET ALL (LIST) ---
    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        return users.stream()
                .map(this::mapToUserResponse) // Gọi hàm map thủ công
                .collect(Collectors.toList());
    }

    // --- 5. GET ALL (PAGE) ---
    public Page<UserResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> customerPage = userRepository.findAll(pageable);

        return customerPage.map(this::mapToUserResponse);
    }

    // --- 6. GET BY ID ---
    public UserResponse getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return mapToUserResponse(user);
    }

    // --- 7. DELETE ---
    public void delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // --- BƯỚC QUAN TRỌNG: Gỡ bỏ Roles trước ---
        // Hành động này sẽ khiến Hibernate tự động xóa các dòng liên quan trong bảng user_roles
        user.getRoles().clear();
        userRepository.save(user); // Lưu lại để Hibernate thực thi lệnh xóa trong user_roles
        // ------------------------------------------

        // Sau đó mới xóa User
        userRepository.delete(user);
    }

    // ==================================================================
    // HELPER METHODS (Dùng để thay thế Mapper)
    // ==================================================================

    // Hàm chuyển từ Entity sang Response DTO
    private UserResponse mapToUserResponse(User user) {
        if (user == null) return null;

        // Lấy Role đầu tiên để hiển thị (hoặc join chuỗi nếu muốn)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("UNKNOWN");

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dob(user.getDob())
                .identityId(user.getIdentityId())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .userStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null)
                .role(roleName)
                .build();
    }

    // (Optional) Nếu bạn cần convert UserStatus từ request
    private UserStatus convertToStatus(String statusStr) {
        if (statusStr == null) return UserStatus.ENABLED; // Mặc định

        String normalized = normalizeString.normalizedString(statusStr);
        try {
            return UserStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }
    }
}