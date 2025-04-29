// List of protected pages that require authentication
const protectedPages = [
    '/customer-dashboard.html',
    '/book-room.html',
    '/rooms.html',
    '/services.html',
    '/my-booking.html',
    '/my-account.html'
];

// Check if current page requires authentication
function isProtectedPage() {
    const currentPath = window.location.pathname;
    return protectedPages.some(page => currentPath.endsWith(page));
}

// Check if user is authenticated
function checkAuth() {
    // Only check auth for protected pages
    if (!isProtectedPage()) {
        return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Token is valid, set it in headers for all AJAX requests
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }
    });

    // Load user profile
    loadUserProfile();
}

// Load user profile information
function loadUserProfile() {
    $.ajax({
        url: '/customer/profile',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            console.log('Profile Response:', response);
            
            if (response && response.id) {
                const user = response;
                console.log('User data:', user);
                
                // Update username display in navigation
                $('#usernameDisplay').text(user.name || 'User');
                
                // Update profile menu
                $('#menuUsername').text(user.name || 'User');
                $('#menuUserEmail').find('span').text(user.email || 'user@example.com');
                $('#menuUserPhone').find('span').text(user.phone || '0123456789');
                $('#menuUserAddress').find('span').text(user.address || 'Hanoi, Vietnam');
                
                // Update avatar with user's name
                const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name || 'User')}&background=4F46E5&color=fff`;
                $('#userAvatar').attr('src', avatarUrl);
                $('#menuUserAvatar').attr('src', avatarUrl);
                
                // Update member type
                const memberType = user.memberType || 'Standard Member';
                $('#menuUsername').next('span').text(memberType);
                
                // Store user ID for booking
                localStorage.setItem('userId', user.id);
            } else {
                console.error('Invalid profile response format:', response);
                setDefaultUserInfo();
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading profile:', {
                status: status,
                error: error,
                response: xhr.responseText
            });
            setDefaultUserInfo();
        }
    });
}

// Set default user information
function setDefaultUserInfo() {
    $('#usernameDisplay').text('User');
    $('#menuUsername').text('User');
    $('#menuUserEmail').find('span').text('user@example.com');
    $('#menuUserPhone').find('span').text('0123456789');
    $('#menuUserAddress').find('span').text('Hanoi, Vietnam');
    
    const defaultAvatarUrl = 'https://ui-avatars.com/api/?name=User&background=4F46E5&color=fff';
    $('#userAvatar').attr('src', defaultAvatarUrl);
    $('#menuUserAvatar').attr('src', defaultAvatarUrl);
    
    $('#menuUsername').next('span').text('Standard Member');
}

// Handle logout
function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}

// Add token to all AJAX requests and handle errors
$(document).ajaxError(function(event, jqXHR, settings, error) {
    if (jqXHR.status === 401) {
        // Unauthorized - token invalid or expired
        localStorage.removeItem('token');
        window.location.href = '/login.html';
    } else if (jqXHR.status === 403) {
        // Forbidden - user doesn't have permission
        alert('You do not have permission to access this resource.');
    } else if (jqXHR.status === 404) {
        // Not found
        alert('The requested resource was not found.');
    } else if (jqXHR.status >= 500) {
        // Server error
        alert('An error occurred on the server. Please try again later.');
    }
}); 