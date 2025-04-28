// List of protected pages that require authentication
const protectedPages = [
    '/customer-dashboard.html',
    '/book-room.html',
    '/rooms.html',
    '/services.html',
    '/my-booking.html',
    '/my-account.html',
    '/admin-dashboard.html',
    '/employee-dashboard.html'
];

// Global state for authentication
window.AUTH_STATE = window.AUTH_STATE || {
    isCheckingAuth: false,
    hasCheckedAuth: false,
    isAuthenticated: false,
    verifying: false
};

// Logging helper (disable in production)
const isDev = true;
function log(...args) {
    if (isDev) {
        console.log('[AUTH]', ...args);
    }
}

// Check if current page requires authentication
function isProtectedPage() {
    const currentPath = window.location.pathname;
    return protectedPages.some(page => currentPath.endsWith(page));
}

// Main authentication check function - runs on all pages
function checkAuth() {
    // Don't run if we're already checking or have checked
    if (window.AUTH_STATE.isCheckingAuth || window.AUTH_STATE.hasCheckedAuth) {
        log('Auth check already in progress or completed, skipping');
        return;
    }
    
    window.AUTH_STATE.isCheckingAuth = true;
    log('Running authentication check');
    
    // Only enforce auth check for protected pages
    if (!isProtectedPage()) {
        log('Not a protected page, skipping auth check');
        window.AUTH_STATE.isCheckingAuth = false;
        window.AUTH_STATE.hasCheckedAuth = true;
        return;
    }

    // Get token from cookie
    const token = getTokenFromCookie();
    log('Auth token:', token ? 'Found' : 'Not found');

    if (!token) {
        log('No token found, redirecting to login');
        window.AUTH_STATE.isCheckingAuth = false;
        window.AUTH_STATE.hasCheckedAuth = true;
        window.location.href = '/login.html';
        return;
    }

    // Verify token with server
    verifyToken(token);
}

// Token verification with server
function verifyToken(token) {
    if (window.AUTH_STATE.verifying) {
        log('Token verification already in progress, skipping');
        return;
    }
    
    window.AUTH_STATE.verifying = true;
    log('Verifying token with server');
    
    $.ajax({
        url: '/auth/introspect',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ token: token }),
        xhrFields: {
            withCredentials: true
        },
        success: function(response) {
            log('Token verification response received');
            
            if (response && response.result && response.result.valid) {
                log('Token is valid');
                window.AUTH_STATE.isAuthenticated = true;
                
                // Set token for all future AJAX requests
                setupAjaxAuth(token);
                
                // Optionally load user profile on dashboard pages
                if (window.location.pathname.includes('dashboard')) {
                    // Only load profile once
                    if (!window.PROFILE_LOADED) {
                        window.PROFILE_LOADED = true;
                        loadUserProfile();
                    }
                }
            } else {
                log('Token is invalid');
                // Clear invalid token
                clearCookie('token');
                // Redirect to login
                window.location.href = '/login.html?session_expired=true';
            }
            
            window.AUTH_STATE.isCheckingAuth = false;
            window.AUTH_STATE.hasCheckedAuth = true;
            window.AUTH_STATE.verifying = false;
        },
        error: function(xhr) {
            log('Token verification failed:', xhr.status);
            // Handle server errors gracefully
            if (xhr.status === 401 || xhr.status === 403) {
                clearCookie('token');
                window.location.href = '/login.html?error=auth_error';
            }
            
            window.AUTH_STATE.isCheckingAuth = false;
            window.AUTH_STATE.hasCheckedAuth = true;
            window.AUTH_STATE.verifying = false;
        }
    });
}

// Setup AJAX auth headers for all future requests
function setupAjaxAuth(token) {
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        },
        xhrFields: {
            withCredentials: true
        }
    });
}

// Cookie management functions
function getTokenFromCookie() {
    return getCookie('token');
}

function getCookie(name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
}

function setSecureCookie(name, value, expiryDays = 7) {
    const date = new Date();
    date.setTime(date.getTime() + (expiryDays * 24 * 60 * 60 * 1000));
    const expires = "expires=" + date.toUTCString();
    // Use secure flag when on HTTPS
    const secure = location.protocol === 'https:' ? '; secure' : '';
    document.cookie = name + "=" + value + "; " + expires + "; path=/; samesite=strict" + secure;
    log('Cookie set:', name, 'with expiry:', expiryDays, 'days');
}

function clearCookie(name) {
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; samesite=strict";
    log('Cookie cleared:', name);
}

// JWT decoding helper (for client-side use only, not for security decisions)
function decodeJWT(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        log('Error decoding JWT:', e);
        return null;
    }
}

// Limit profile loading to once per page
window.PROFILE_LOADED = false;

// Load user profile information
function loadUserProfile() {
    log('Loading user profile');
    
    // Only proceed if not already loaded
    if (window.PROFILE_LOADED !== true) {
        window.PROFILE_LOADED = true;
    } else {
        log('Profile already loaded, skipping');
        return;
    }
    
    $.ajax({
        url: '/customer/profile',
        type: 'GET',
        success: function(response) {
            log('Profile response received');
            
            if (response && response.id) {
                updateUIWithUserInfo(response);
            } else {
                log('Invalid profile response format');
                setDefaultUserInfo();
            }
        },
        error: function(xhr, status, error) {
            log('Error loading profile:', status);
            setDefaultUserInfo();
            
            // Only redirect for auth errors, not for other profile loading errors
            if (xhr.status === 401 || xhr.status === 403) {
                log('Authentication error while loading profile');
                // Don't redirect immediately to prevent redirect loops
            }
        }
    });
}

// Update UI with user information
function updateUIWithUserInfo(user) {
    // Update username in header
    if ($('#usernameDisplay').length) {
        $('#usernameDisplay').text(user.name || 'User');
    }
    
    // Update profile menu if it exists
    if ($('#menuUsername').length) {
        $('#menuUsername').text(user.name || 'User');
    }
    
    if ($('#menuUserEmail').length) {
        $('#menuUserEmail').find('span').text(user.email || 'user@example.com');
    }
    
    if ($('#menuUserPhone').length) {
        $('#menuUserPhone').find('span').text(user.phone || '0123456789');
    }
    
    if ($('#menuUserAddress').length) {
        $('#menuUserAddress').find('span').text(user.address || 'Hanoi, Vietnam');
    }
    
    // Update avatar with user's name
    const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name || 'User')}&background=4F46E5&color=fff`;
    
    if ($('#userAvatar').length) {
        $('#userAvatar').attr('src', avatarUrl);
    }
    
    if ($('#menuUserAvatar').length) {
        $('#menuUserAvatar').attr('src', avatarUrl);
    }
    
    // Update member type if it exists
    const memberType = user.memberType || 'Standard Member';
    if ($('#menuUsername').next('span').length) {
        $('#menuUsername').next('span').text(memberType);
    }
}

// Set default user information when profile can't be loaded
function setDefaultUserInfo() {
    // Update username in header
    if ($('#usernameDisplay').length) {
        $('#usernameDisplay').text('User');
    }
    
    // Update profile menu if it exists
    if ($('#menuUsername').length) {
        $('#menuUsername').text('User');
    }
    
    if ($('#menuUserEmail').length) {
        $('#menuUserEmail').find('span').text('user@example.com');
    }
    
    if ($('#menuUserPhone').length) {
        $('#menuUserPhone').find('span').text('0123456789');
    }
    
    if ($('#menuUserAddress').length) {
        $('#menuUserAddress').find('span').text('Hanoi, Vietnam');
    }
    
    // Update avatar with default
    const defaultAvatarUrl = 'https://ui-avatars.com/api/?name=User&background=4F46E5&color=fff';
    
    if ($('#userAvatar').length) {
        $('#userAvatar').attr('src', defaultAvatarUrl);
    }
    
    if ($('#menuUserAvatar').length) {
        $('#menuUserAvatar').attr('src', defaultAvatarUrl);
    }
    
    // Update member type if it exists
    if ($('#menuUsername').next('span').length) {
        $('#menuUsername').next('span').text('Standard Member');
    }
}

// Handle logout
function logout() {
    log('Logging out');
    
    const token = getTokenFromCookie();
    if (token) {
        // Call logout API if token exists
        $.ajax({
            url: '/auth/logout',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ token: token }),
            xhrFields: {
                withCredentials: true
            },
            complete: function() {
                // Always clear cookie and redirect regardless of API response
                clearCookie('token');
                window.location.href = '/login.html?logged_out=true';
            }
        });
    } else {
        // If no token, just redirect to login
        window.location.href = '/login.html';
    }
}

// Global AJAX error handling for auth errors
$(document).ajaxError(function(event, jqXHR, settings, error) {
    // Don't handle errors for auth endpoints to prevent redirect loops
    if (settings.url.includes('/auth/')) {
        return;
    }
    
    // Handle 401/403 errors for non-auth endpoints
    if (jqXHR.status === 401) {
        log('Unauthorized access detected:', settings.url);
        // Don't redirect from profile endpoint to prevent loops
        if (!settings.url.includes('/customer/profile')) {
            clearCookie('token');
            window.location.href = '/login.html?session_expired=true';
        }
    } else if (jqXHR.status === 403) {
        log('Forbidden access detected:', settings.url);
        alert('You do not have permission to access this resource.');
    }
});

// Debug cookie helper
function debugCookies() {
    log("All cookies:", document.cookie);
    
    // List all cookies individually
    const cookies = document.cookie.split(';');
    log("Individual cookies:");
    cookies.forEach(cookie => {
        log(cookie.trim());
    });
    
    // Check for specific cookies
    log("Token cookie:", getCookie('token'));
}

// We only want to run auth check once when DOM is ready
// Use an immediately invoked function to ensure it runs exactly once
(function() {
    if (window.AUTH_CHECK_INITIALIZED) return;
    window.AUTH_CHECK_INITIALIZED = true;
    
    $(document).ready(function() {
        // Only run checkAuth once
        setTimeout(checkAuth, 100);
    });
})(); 