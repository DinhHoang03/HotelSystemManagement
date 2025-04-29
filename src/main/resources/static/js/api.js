// API Handler for Hotel Management System
const API = {
    // Base URL with context path
    baseUrl: document.querySelector('meta[name="context-path"]')?.content || '',
    
    // Token storage
    token: localStorage.getItem('token'),
    refreshToken: localStorage.getItem('refreshToken'),
    
    // Set token
    setToken: function(token) {
        this.token = token;
        localStorage.setItem('token', token);
    },
    
    // Set refresh token
    setRefreshToken: function(refreshToken) {
        this.refreshToken = refreshToken;
        localStorage.setItem('refreshToken', refreshToken);
    },
    
    // Remove tokens
    removeToken: function() {
        this.token = null;
        this.refreshToken = null;
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
    },
    
    // Get headers with token
    getHeaders: function() {
        const headers = {
            'Content-Type': 'application/json'
        };
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    },
    
    // Login
    login: function(username, password) {
        return $.ajax({
            url: `${this.baseUrl}/auth/login`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ username, password })
        });
    },
    
    // Verify token
    verifyToken: function() {
        if (!this.token) {
            return Promise.reject('No token found');
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/introspect`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ token: this.token })
        });
    },
    
    // Refresh token
    refreshToken: function() {
        if (!this.refreshToken) {
            return Promise.reject('No refresh token found');
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/refresh`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ refreshToken: this.refreshToken })
        });
    },
    
    // Logout
    logout: function() {
        if (!this.token) {
            return Promise.resolve();
        }
        return $.ajax({
            url: `${this.baseUrl}/auth/logout`,
            method: 'POST',
            headers: this.getHeaders(),
            data: JSON.stringify({ token: this.token })
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