// Store email globally
let currentEmail = '';

// Initialize AOS
document.addEventListener('DOMContentLoaded', function() {
    AOS.init({
        duration: 1000,
        once: true
    });
    setTimeout(function() {
        document.getElementById('loadscreen').classList.add('hide');
    }, 500);
});

// Show/Hide Error Message
function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorAlert.classList.remove('hidden');
}

function hideError() {
    document.getElementById('errorAlert').classList.add('hidden');
}

// Show/Hide Success Message
function showSuccess(message) {
    const successAlert = document.getElementById('successAlert');
    const successMessage = document.getElementById('successMessage');
    successMessage.textContent = message;
    successAlert.classList.remove('hidden');
}

function hideSuccess() {
    document.getElementById('successAlert').classList.add('hidden');
}

// Send OTP
function sendOTP() {
    const email = document.getElementById('email').value;
    if (!email) {
        showError('Please enter your email');
        return;
    }

    // Hide any existing alerts
    hideError();
    hideSuccess();

    $.ajax({
        url: '/email/forgot-password/send-otp',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            email: email
        }),
        success: function(response) {
            console.log('Send OTP Response:', response);
            showSuccess('OTP has been sent to your email');
            // Lưu email vào localStorage
            localStorage.setItem('resetEmail', email);
            // Redirect to verify-otp page first
            window.location.href = '/verify-otp.html?email=' + encodeURIComponent(email);
        },
        error: function(xhr) {
            console.log('Send OTP Error:', xhr);
            try {
                const errorResponse = JSON.parse(xhr.responseText);
                showError(errorResponse.message || 'Failed to send OTP');
            } catch (e) {
                showError('An error occurred. Please try again.');
            }
        }
    });
}

// Verify OTP
function verifyOTP() {
    const otp = document.getElementById('otp').value;
    const email = localStorage.getItem('resetEmail');

    if (!otp) {
        showError('Please enter OTP');
        return;
    }

    if (!email) {
        showError('Email not found. Please try again from the beginning.');
        return;
    }

    // Hide any existing alerts
    hideError();
    hideSuccess();

    $.ajax({
        url: '/email/verify-otp',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            email: email,
            otp: otp
        }),
        success: function(response) {
            console.log('Verify OTP Response:', response);
            if (response.result) {
                showSuccess('OTP verified successfully');
                document.getElementById('otpForm').classList.add('hidden');
                document.getElementById('resetPasswordForm').classList.remove('hidden');
            } else {
                showError('Invalid OTP');
            }
        },
        error: function(xhr) {
            console.log('Verify OTP Error:', xhr);
            try {
                const errorResponse = JSON.parse(xhr.responseText);
                showError(errorResponse.message || 'Invalid OTP');
            } catch (e) {
                showError('An error occurred. Please try again.');
            }
        }
    });
}

// Reset Password
function resetPassword() {
    const username = document.getElementById('username').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!username || !newPassword || !confirmPassword) {
        showError('Please fill in all fields');
        return;
    }

    if (newPassword !== confirmPassword) {
        showError('Passwords do not match');
        return;
    }

    // Hide any existing alerts
    hideError();
    hideSuccess();

    $.ajax({
        url: '/email/forgot-password/update-password',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            newPassword: newPassword
        }),
        success: function(response) {
            showSuccess('Password has been reset successfully');
            // Clean up OTP
            $.ajax({
                url: '/email/delete-otp',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    email: currentEmail
                })
            });
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 2000);
        },
        error: function(xhr) {
            console.log('Reset Password Error:', xhr);
            try {
                const errorResponse = JSON.parse(xhr.responseText);
                showError(errorResponse.message || 'Failed to reset password');
            } catch (e) {
                showError('An error occurred. Please try again.');
            }
        }
    });
}
