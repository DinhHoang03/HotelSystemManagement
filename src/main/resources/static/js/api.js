// API Handler for Hotel Management System
const API = {
    // Base URL with context path
    baseUrl: document.querySelector('meta[name="context-path"]')?.content || '',
    
    // Get token from cookie
    getToken: function() {
        return this.getCookie('auth_token');
    },
    
    // Get refresh token from cookie
    getRefreshToken: function() {
        return this.getCookie('refresh_token');
    },
    
    // Get cookie by name
    getCookie: function(name) {
        const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
        return match ? match[2] : null;
    },
    
    // Set secure cookie
    setSecureCookie: function(name, value, expiryDays = 1) {
        const date = new Date();
        date.setTime(date.getTime() + (expiryDays * 24 * 60 * 60 * 1000));
        const expires = "expires=" + date.toUTCString();
        // Use secure flags when in production (HTTPS)
        const secure = location.protocol === 'https:' ? '; secure' : '';
        document.cookie = name + "=" + value + "; " + expires + "; path=/; samesite=strict" + secure;
    },
    
    // Clear cookie
    clearCookie: function(name) {
        document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    },
    
    // Set token
    setToken: function(token) {
        this.setSecureCookie('auth_token', token, 1);
    },
    
    // Set refresh token
    setRefreshToken: function(refreshToken) {
        this.setSecureCookie('refresh_token', refreshToken, 7); // Refresh token lasts longer
    },
    
    // Remove tokens
    removeToken: function() {
        this.clearCookie('auth_token');
        this.clearCookie('refresh_token');
    },
    
    // Get headers with token
    getHeaders: function() {
        const headers = {
            'Content-Type': 'application/json'
        };
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    },
    
    // Login
    login: function(username, password) {
        return $.ajax({
            url: `${this.baseUrl}/auth/login`,
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            data: JSON.stringify({ username, password })
        });
    },
    
    // Verify token
    verifyToken: function() {
        const token = this.getToken();
        if (!token) {
            return Promise.reject('No token found');
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/introspect`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ token: token })
        });
    },
    
    // Refresh token
    refreshToken: function() {
        const token = this.getToken();
        if (!token) {
            return Promise.reject('No token found');
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/refresh`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ token: token })
        });
    },
    
    // Logout
    logout: function() {
        const token = this.getToken();
        if (!token) {
            this.removeToken();
            return Promise.resolve();
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/logout`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ token: token })
        }).finally(() => {
            this.removeToken();
        });
    },
    
    // Error handler
    handleError: function(error) {
        if (error.status === 401) {
            this.removeToken();
            window.location.href = `${this.baseUrl}/login.html`;
        }
        throw error;
    }
};

// Add token refresh interceptor
$(document).ajaxError(function(event, jqXHR, settings, error) {
    if (jqXHR.status === 401 && settings.url !== `${API.baseUrl}/auth/login`) {
        API.refreshToken()
            .then(response => {
                if (response.result && response.result.token) {
                    API.setToken(response.result.token);
                    // Retry the original request
                    return $.ajax(settings);
                }
                throw new Error('Token refresh failed');
            })
            .catch(() => {
                API.removeToken();
                window.location.href = `${API.baseUrl}/login.html`;
            });
    }
}); 