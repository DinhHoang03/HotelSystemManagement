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

// Function to move to next input
function moveToNext(input) {
    if (input.value.length === 1) {
        const nextInput = input.nextElementSibling;
        if (nextInput) {
            nextInput.focus();
        }
    }
}

// Verify OTP
function verifyOTP() {
    const otpInputs = document.querySelectorAll('.otp-input');
    let otp = '';
    otpInputs.forEach(input => {
        otp += input.value;
    });
    const email = localStorage.getItem('resetEmail');

    if (otp.length !== 6) {
        showError('Please enter all 6 digits of OTP');
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
                // Redirect to reset-password.html
                window.location.href = '/reset-password.html?email=' + encodeURIComponent(email);
            } else {
                showError('Invalid OTP');
                // Xóa email khỏi localStorage nếu OTP thất bại
                localStorage.removeItem('resetEmail');
            }
        },
        error: function(xhr) {
            console.log('Verify OTP Error:', xhr);
            try {
                const errorResponse = JSON.parse(xhr.responseText);
                showError(errorResponse.message || 'Invalid OTP');
                // Xóa email khỏi localStorage nếu OTP thất bại
                localStorage.removeItem('resetEmail');
            } catch (e) {
                showError('An error occurred. Please try again.');
                // Xóa email khỏi localStorage nếu OTP thất bại
                localStorage.removeItem('resetEmail');
            }
        }
    });
}