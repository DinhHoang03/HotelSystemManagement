/**
 * Login Page functionality
 */

// Initialize AOS
document.addEventListener('DOMContentLoaded', function() {
    AOS.init({
        duration: 1000,
        once: true
    });
});

/**
 * Toggle password visibility
 */
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const icon = document.querySelector('#password + i');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}

/**
 * Show error message
 * @param {string} message - Error message to display
 */
function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorAlert.classList.remove('hidden');
}

/**
 * Hide error message
 */
function hideError() {
    document.getElementById('errorAlert').classList.add('hidden');
}

// Handle form submission
$(document).ready(function() {
    $('#loginForm').submit(function(e) {
        e.preventDefault();
        hideError();

        const username = $('#username').val();
        const password = $('#password').val();

        if (!username || !password) {
            showError('Please fill in all fields');
            return;
        }

        $.ajax({
            url: '/auth/login',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                username: username,
                password: password
            }),
            success: function(response) {
                if (response.result && response.result.token) {
                    localStorage.setItem('token', response.result.token);
                    
                    // Decode token to get user role
                    const payload = JSON.parse(atob(response.result.token.split('.')[1]));
                    const role = payload.scope; // Scope là một chuỗi đơn, không phải mảng
                    
                    // Redirect based on role
                    if (role === 'ROLE_ADMIN') {
                        window.location.href = '/admin-dashboard.html';
                    } else if (role === 'ROLE_ACCOUNTANT' || role === 'ROLE_DEPARTMENT_HEAD' || 
                              role === 'ROLE_RECEPTIONIST' || role === 'ROLE_CLEANER' || 
                              role === 'ROLE_WAITER') {
                        window.location.href = '/employee-dashboard.html';
                    } else {
                        window.location.href = '/customer-dashboard.html';
                    }
                } else {
                    showError('Invalid response from server');
                }
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    try {
                        // Cố gắng đọc phản hồi lỗi từ backend
                        const errorResponse = JSON.parse(xhr.responseText);
                        if (errorResponse && errorResponse.message) {
                            showError(errorResponse.message);
                        } else {
                            showError('Invalid username or password');
                        }
                    } catch (e) {
                        // Nếu không thể parse JSON, hiển thị thông báo lỗi mặc định
                        showError('Invalid username or password');
                    }
                } else {
                    try {
                        // Cố gắng đọc phản hồi lỗi từ backend cho các lỗi khác
                        const errorResponse = JSON.parse(xhr.responseText);
                        if (errorResponse && errorResponse.message) {
                            showError(errorResponse.message);
                        } else {
                            showError('An error occurred. Please try again.');
                        }
                    } catch (e) {
                        // Nếu không thể parse JSON, hiển thị thông báo lỗi mặc định
                        showError('An error occurred. Please try again.');
                    }
                }
            }
        });
    });
}); 