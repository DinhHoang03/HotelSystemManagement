// Check if user is authenticated
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        // Decode token to check expiration
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiration = payload.exp * 1000; // Convert to milliseconds
        
        if (Date.now() >= expiration) {
            // Token expired
            localStorage.removeItem('token');
            window.location.href = '/login.html';
            return;
        }

        // Token is valid, set it in headers for all AJAX requests
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
        });

        return payload;
    } catch (error) {
        // Invalid token format
        localStorage.removeItem('token');
        window.location.href = '/login.html';
    }
}

// Handle logout
function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}

// Add token to all AJAX requests
$(document).ajaxError(function(event, jqXHR, settings, error) {
    if (jqXHR.status === 401) {
        // Unauthorized - token invalid or expired
        localStorage.removeItem('token');
        window.location.href = '/login.html';
    }
}); 