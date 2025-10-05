// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Check if user is logged in
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Load user profile data
    loadUserProfile();
});

// Load user profile data
function loadUserProfile() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    $.ajax({
        url: '/customer/profile',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            console.log('Profile response:', response);
            if (response) {
                // Update form fields with user data
                $('#name').val(response.name || '');
                $('#email').val(response.email || '');
                $('#phone').val(response.phone || '');
                $('#address').val(response.address || '');

                // Update profile information
                $('#profileName').text(response.name || 'User');
                $('#memberType').text(response.memberType || 'Standard Member');

                // Update avatar
                const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(response.name || 'User')}&background=4F46E5&color=fff&size=128`;
                $('#profileAvatar').attr('src', avatarUrl);
                $('#userAvatar').attr('src', avatarUrl);

                // Update username in navigation
                $('#usernameDisplay').text(response.name || 'User');

                // Store user ID
                localStorage.setItem('userId', response.id);
            } else {
                console.error('Invalid response format:', response);
                showError('Error loading profile data');
            }
        },
        error: function(xhr) {
            console.error('Error loading profile:', xhr);
            if (xhr.status === 401) {
                localStorage.removeItem('token');
                window.location.href = '/login.html';
            } else {
                showError('Failed to load profile information. Please try again later.');
            }
        }
    });
}

// Submit form handler
$('#profileEditForm').on('submit', function(e) {
    e.preventDefault();

    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
        showError('User ID not found. Please log in again.');
        return;
    }

    // Get form data
    const formData = {
        name: $('#name').val(),
        email: $('#email').val(),
        phone: $('#phone').val(),
        address: $('#address').val()
    };

    // Validate form data
    if (!formData.email || !formData.phone) {
        showError('Please fill in all required fields (Email and Phone).');
        return;
    }

    // Show loading indicator
    Swal.fire({
        title: 'Updating profile...',
        text: 'Please wait',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // Send update request
    $.ajax({
        url: `/customer/update/${userId}`,
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(formData),
        contentType: 'application/json',
        success: function(response) {
            console.log('Update response:', response);
            Swal.fire({
                icon: 'success',
                title: 'Profile Updated!',
                text: 'Your profile has been successfully updated.',
                showConfirmButton: true,
                confirmButtonText: 'Continue',
                confirmButtonColor: '#4F46E5'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/customer-dashboard.html';
                }
            });
        },
        error: function(xhr) {
            console.error('Error updating profile:', xhr);
            let errorMessage = 'Failed to update profile.';

            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }

            showError(errorMessage);
        }
    });
});

// Show error message
function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Error',
        text: message,
        confirmButtonColor: '#4F46E5'
    });
}

// Logout function
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    window.location.href = '/login.html';
}

// Generate initials from name
function generateInitials(name) {
    if (!name) return 'U';
    return name
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase()
        .substring(0, 3);
}

// Avatar Selection Functions
function openAvatarModal() {
    const modal = document.getElementById('avatarModal');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    // Update initials in the default avatar option
    const name = document.getElementById('usernameDisplay').textContent;
    document.getElementById('initialsAvatar').textContent = generateInitials(name);
}

function closeAvatarModal() {
    const modal = document.getElementById('avatarModal');
    modal.classList.add('hidden');
    document.body.style.overflow = 'auto';
}

function selectAvatar(avatarUrl) {
    const profileAvatar = document.getElementById('profileAvatar');
    const userAvatar = document.getElementById('userAvatar');

    if (avatarUrl === 'default') {
        const name = document.getElementById('usernameDisplay').textContent;
        const initials = generateInitials(name);
        const defaultAvatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(initials)}&background=4F46E5&color=fff&size=128`;
        profileAvatar.src = defaultAvatarUrl;
        userAvatar.src = defaultAvatarUrl;
        localStorage.setItem('userAvatar', 'default');
    } else {
        profileAvatar.src = avatarUrl;
        userAvatar.src = avatarUrl;
        localStorage.setItem('userAvatar', avatarUrl);
    }

    closeAvatarModal();
}

// Contact Modal Functions with Improved Animation
function openContactModal() {
    const modal = document.getElementById('contactModal');
    const modalContent = modal.querySelector('.modal-content');
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    // Reset animation classes
    modalContent.classList.remove('closing');
    modalContent.classList.add('modal-content');
}

function closeContactModal() {
    const modal = document.getElementById('contactModal');
    const modalContent = modal.querySelector('.modal-content');

    // Add closing animation
    modalContent.classList.add('closing');

    // Wait for animation to complete
    setTimeout(() => {
        modal.classList.add('hidden');
        document.body.style.overflow = 'auto';
    }, 300);
}

// Load saved avatar on page load
document.addEventListener('DOMContentLoaded', function() {
    const savedAvatar = localStorage.getItem('userAvatar');
    const name = document.getElementById('usernameDisplay').textContent;

    if (savedAvatar === 'default' || !savedAvatar) {
        const initials = generateInitials(name);
        const defaultAvatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(initials)}&background=4F46E5&color=fff&size=128`;
        document.getElementById('profileAvatar').src = defaultAvatarUrl;
        document.getElementById('userAvatar').src = defaultAvatarUrl;
    } else {
        document.getElementById('profileAvatar').src = savedAvatar;
        document.getElementById('userAvatar').src = savedAvatar;
    }
});