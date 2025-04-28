/**
 * Authentication helper for DinhRise Hotel system
 * Include this script in all protected pages
 */

(function() {
    // Debug flag - set to false in production
    const isDev = true;
    
    function log(...args) {
        if (isDev) console.log('[Auth]', ...args);
    }
    
    log('Auth helper loaded');
    
    // Get auth token from cookie only
    function getToken() {
        const tokenCookie = document.cookie.match(/(?:^|; )token=([^;]*)/);
        if (tokenCookie && tokenCookie[1]) {
            log('Token found in cookie');
            return tokenCookie[1];
        }
        
        log('No token found');
        return null;
    }
    
    // Get user role from cookie only
    function getUserRole() {
        // Try from role cookie first
        const roleCookie = document.cookie.match(/(?:^|; )role=([^;]*)/);
        if (roleCookie && roleCookie[1]) {
            log('Role found in cookie:', roleCookie[1]);
            return roleCookie[1];
        }
        
        // Then try from user_role cookie (frontend naming)
        const userRoleCookie = document.cookie.match(/(?:^|; )user_role=([^;]*)/);
        if (userRoleCookie && userRoleCookie[1]) {
            log('Role found in user_role cookie:', userRoleCookie[1]);
            return userRoleCookie[1];
        }
        
        // Try extracting from token if available
        const token = getToken();
        if (token) {
            const payload = parseJwt(token);
            if (payload && payload.scope) {
                log('Role found in JWT payload:', payload.scope);
                return payload.scope;
            }
        }
        
        log('No role found');
        return null;
    }
    
    // Parse JWT token
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            log('Error parsing JWT:', e);
            return null;
        }
    }
    
    // Logout user and redirect to login page
    function logout() {
        log('Logging out user');
        document.cookie = "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax";
        document.cookie = "user_role=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax";
        document.cookie = "role=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax";
        window.location.href = '/login.html?logged_out=true';
    }
    
    // Verify token with server
    function verifyToken(callback) {
        const token = getToken();
        
        if (!token) {
            log('No token found, redirecting to login');
            window.location.href = '/login.html?session_expired=true';
            return;
        }
        
        log('Verifying token with server');
        
        // Backend code chỉ ra rằng nó sẽ tự động lấy token từ cookie nếu không được cung cấp
        // Nhưng để chắc chắn, chúng ta vẫn gửi token trong body
        $.ajax({
            url: '/auth/introspect',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ token: token }),
            xhrFields: {
                withCredentials: true
            },
            success: function(response) {
                log('Token verification response:', response);
                
                if (response && response.result && response.result.valid) {
                    log('Token is valid');
                    
                    // Nếu token hợp lệ nhưng không có thông tin về vai trò, lấy từ JWT
                    if (token) {
                        const payload = parseJwt(token);
                        if (payload && payload.scope) {
                            log('Setting role from JWT payload:', payload.scope);
                            const cookieExpiry = new Date();
                            cookieExpiry.setDate(cookieExpiry.getDate() + 7);
                            document.cookie = `user_role=${payload.scope}; path=/; expires=${cookieExpiry.toUTCString()}; SameSite=Lax`;
                        }
                    }
                    
                    if (callback && typeof callback === 'function') {
                        callback(true);
                    }
                } else {
                    log('Token is invalid, logging out');
                    logout();
                }
            },
            error: function(xhr) {
                log('Token verification error:', xhr.status);
                
                // Debugging info
                log('XHR Status:', xhr.status);
                log('XHR Response Text:', xhr.responseText);
                try {
                    const errorObj = JSON.parse(xhr.responseText);
                    log('Error Object:', errorObj);
                } catch (e) {
                    log('Cannot parse response as JSON');
                }
                
                // If it's an authentication error, redirect to login
                if (xhr.status === 401 || xhr.status === 403) {
                    window.location.href = '/login.html?session_expired=true';
                } else {
                    // For other errors, just log them
                    console.error('Error verifying token:', xhr.responseText);
                    
                    // Call callback with error status
                    if (callback && typeof callback === 'function') {
                        callback(false);
                    }
                }
            }
        });
    }
    
    // Check current page and roles
    function checkAuthForCurrentPage() {
        const currentPath = window.location.pathname;
        const userRole = getUserRole();
        
        log('Current path:', currentPath);
        log('User role:', userRole);
        
        // Skip check for login page
        if (currentPath === '/login.html' || currentPath === '/') {
            return;
        }
        
        // If no token, redirect to login
        if (!getToken()) {
            log('No token found, redirecting to login');
            window.location.href = '/login.html?session_expired=true';
            return;
        }
        
        // Check if user has access to this page
        if (currentPath.includes('admin') && !userRole?.includes('ROLE_ADMIN')) {
            log('User does not have admin role, redirecting');
            window.location.href = '/login.html?error=unauthorized';
            return;
        }
        
        if (currentPath.includes('employee') && 
            !(userRole?.includes('ROLE_ACCOUNTANT') || 
              userRole?.includes('ROLE_DEPARTMENT_HEAD') || 
              userRole?.includes('ROLE_RECEPTIONIST') || 
              userRole?.includes('ROLE_CLEANER') || 
              userRole?.includes('ROLE_WAITER'))) {
            log('User does not have employee role, redirecting');
            window.location.href = '/login.html?error=unauthorized';
            return;
        }
        
        if (currentPath.includes('customer') && !userRole?.includes('ROLE_CUSTOMER')) {
            log('User does not have customer role, redirecting');
            window.location.href = '/login.html?error=unauthorized';
            return;
        }
        
        log('User has access to current page');
    }
    
    // Setup AJAX defaults to include token cookie
    function setupAjaxDefaults() {
        // Set default settings for all AJAX requests
        $.ajaxSetup({
            xhrFields: {
                withCredentials: true // This ensures cookies are sent with requests
            }
        });
        
        // Add error handler for 401/403 responses
        $(document).ajaxError(function(event, xhr, settings) {
            if (xhr.status === 401 || xhr.status === 403) {
                log('Received unauthorized response, logging out');
                logout();
            }
        });
        
        log('AJAX defaults configured');
    }
    
    // Initialize auth helper
    function initialize() {
        setupAjaxDefaults();
        
        // For debugging
        log('All cookies:', document.cookie);
        
        checkAuthForCurrentPage();
        
        // Verify token and force reload if invalid
        verifyToken(function(isValid) {
            if (!isValid) {
                log('Token validation failed, logging out');
                logout();
            }
        });
    }
    
    // Expose auth API globally
    window.authHelper = {
        getToken: getToken,
        getUserRole: getUserRole,
        logout: logout,
        verifyToken: verifyToken,
        initialize: initialize
    };
    
    // Auto-initialize if jQuery is available
    if (typeof $ !== 'undefined') {
        $(document).ready(function() {
            initialize();
        });
    } else {
        log('jQuery not found, manual initialization required');
        document.addEventListener('DOMContentLoaded', function() {
            if (typeof $ !== 'undefined') {
                initialize();
            } else {
                console.error('jQuery is required for auth-helper.js');
            }
        });
    }
})(); 