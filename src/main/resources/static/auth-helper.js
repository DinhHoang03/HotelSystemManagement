// Hàm đăng xuất
function logout() {
    console.log('Logging out...');
    
    // Xóa cookie với các domain và path khác nhau để đảm bảo xóa hết
    const cookiesToClear = ["token", "user_role", "role", "scope", "userId"];
    const domains = ["", "localhost", ".localhost", window.location.hostname];
    const paths = ["/", "", "/customer-dashboard.html", "/my-bookings.html"];
    
    cookiesToClear.forEach(cookieName => {
        // Xóa với tất cả các domain và path có thể
        domains.forEach(domain => {
            paths.forEach(path => {
                // Xóa cookie với domain
                if (domain) {
                    document.cookie = `${cookieName}=; domain=${domain}; path=${path}; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax`;
                }
                // Xóa cookie không có domain
                document.cookie = `${cookieName}=; path=${path}; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax`;
            });
        });
    });
    
    // Xóa cả dữ liệu trong localStorage
    try {
        localStorage.removeItem('user_role');
        localStorage.removeItem('token'); // Xóa cả token nếu có
        console.log('Cleared localStorage authentication data');
    } catch (e) {
        console.log('Error clearing localStorage:', e);
    }
    
    console.log('Logged out and cleared cookies');
    window.location.href = '/login.html?logged_out=true';
}

// Helper function to get token from cookie
function getAuthToken() {
    return getCookie('token');
}

// Helper function to get auth headers
function getAuthHeaders() {
    const token = getAuthToken();
    if (!token) {
        console.error('No auth token found');
        return {};
    }
    return {
        'Authorization': `Bearer ${token}`
    };
}

// Helper function for jQuery AJAX calls
function ajaxWithAuth(settings) {
    return $.ajax({
        ...settings,
        headers: {
            ...settings.headers,
            ...getAuthHeaders()
        },
        xhrFields: {
            withCredentials: true
        }
    });
}

// Helper function for Fetch API calls
async function fetchWithAuth(url, options = {}) {
    const headers = {
        ...options.headers,
        ...getAuthHeaders()
    };
    
    const response = await fetch(url, {
        ...options,
        headers,
        credentials: 'include'
    });
    
    if (!response.ok) {
        if (response.status === 401) {
            // Token expired or invalid
            clearAllAuthCookies();
            window.location.href = '/login.html?session_expired=true';
            return;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response;
} 