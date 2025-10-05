// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

function togglePassword(inputId) {
    const passwordInput = document.getElementById(inputId);
    const icon = document.querySelector(`#${inputId} + label + button i`);

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

function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    errorMessage.textContent = message;
    errorAlert.classList.remove('hidden');
}

function hideError() {
    document.getElementById('errorAlert').classList.add('hidden');
}

// Tab switching
document.getElementById('customerTab').addEventListener('click', function() {
    this.classList.add('active');
    document.getElementById('employeeTab').classList.remove('active');
    document.getElementById('customerForm').classList.remove('hidden');
    document.getElementById('employeeForm').classList.add('hidden');
});

document.getElementById('employeeTab').addEventListener('click', function() {
    this.classList.add('active');
    document.getElementById('customerTab').classList.remove('active');
    document.getElementById('employeeForm').classList.remove('hidden');
    document.getElementById('customerForm').classList.add('hidden');
});

// Customer Registration
$('#customerForm').submit(function(e) {
    e.preventDefault();
    hideError();

    // Validate required fields
    const requiredFields = [
        'customerIdentityId',
        'customerUsername',
        'customerName',
        'customerPhone',
        'customerEmail',
        'customerDob',
        'customerGender',
        'customerAddress',
        'customerPassword',
        'customerConfirmPassword'
    ];

    for (const fieldId of requiredFields) {
        if (!$(`#${fieldId}`).val()) {
            showError(`Please fill in all required fields`);
            return;
        }
    }

    // Validate password match
    if ($('#customerPassword').val() !== $('#customerConfirmPassword').val()) {
        showError('Passwords do not match');
        return;
    }

    // Validate password strength
    const password = $('#customerPassword').val();
    if (password.length < 8) {
        showError('Password must be at least 8 characters long');
        return;
    }

    // Show loading state
    const submitButton = $(this).find('button[type="submit"]');
    const originalText = submitButton.text();
    submitButton.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-2"></i>Processing...');

    // Prepare form data
    const formData = {
        identityId: $('#customerIdentityId').val(),
        username: $('#customerUsername').val(),
        name: $('#customerName').val(),
        phone: $('#customerPhone').val(),
        email: $('#customerEmail').val(),
        dob: $('#customerDob').val(),
        gender: $('#customerGender').val(),
        address: $('#customerAddress').val(),
        role: 'CUSTOMER',
        password: $('#customerPassword').val()
    };

    // Send registration request
    $.ajax({
        url: '/customer/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            window.location.href = '/registration-success.html';
        },
        error: function(xhr) {
            console.error('Registration error:', xhr);
            let errorMessage = 'An error occurred. Please try again.';

            if (xhr.status === 400) {
                const response = xhr.responseJSON;
                if (response && response.message) {
                    errorMessage = response.message;
                } else {
                    errorMessage = 'Invalid input data. Please check your information.';
                }
            } else if (xhr.status === 401) {
                errorMessage = 'Registration service is currently unavailable. Please try again later.';
            } else if (xhr.status === 403) {
                errorMessage = 'Access denied. Please try again.';
            } else if (xhr.status === 404) {
                errorMessage = 'Service not found. Please try again later.';
            } else if (xhr.status === 500) {
                errorMessage = 'Server error. Please try again later.';
            }

            showError(errorMessage);
        },
        complete: function() {
            // Reset button state
            submitButton.prop('disabled', false).text(originalText);
        }
    });
});

// Employee Registration
$('#employeeForm').submit(function(e) {
    e.preventDefault();
    hideError();

    // Validate required fields
    const requiredFields = [
        'employeeName',
        'employeeUsername',
        'employeeEmail',
        'employeePhone',
        'employeeIdentityId',
        'employeeDob',
        'employeeGender',
        'employeeAddress',
        'employeeRole',
        'employeePassword',
        'employeeConfirmPassword'
    ];

    for (const fieldId of requiredFields) {
        if (!$(`#${fieldId}`).val()) {
            showError(`Please fill in all required fields`);
            return;
        }
    }

    // Validate password match
    if ($('#employeePassword').val() !== $('#employeeConfirmPassword').val()) {
        showError('Passwords do not match');
        return;
    }

    // Validate password strength
    const password = $('#employeePassword').val();
    if (password.length < 8) {
        showError('Password must be at least 8 characters long');
        return;
    }

    // Show loading state
    const submitButton = $(this).find('button[type="submit"]');
    const originalText = submitButton.text();
    submitButton.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-2"></i>Processing...');

    // Prepare form data
    const formData = {
        name: $('#employeeName').val(),
        username: $('#employeeUsername').val(),
        email: $('#employeeEmail').val(),
        phone: $('#employeePhone').val(),
        identityId: $('#employeeIdentityId').val(),
        dob: $('#employeeDob').val(),
        gender: $('#employeeGender').val(),
        address: $('#employeeAddress').val(),
        role: $('#employeeRole').val(),
        password: $('#employeePassword').val()
    };

    // Send registration request
    $.ajax({
        url: '/employee/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            window.location.href = '/registration-success.html';
        },
        error: function(xhr) {
            console.error('Registration error:', xhr);
            let errorMessage = 'An error occurred. Please try again.';

            if (xhr.status === 400) {
                const response = xhr.responseJSON;
                if (response && response.message) {
                    errorMessage = response.message;
                } else {
                    errorMessage = 'Invalid input data. Please check your information.';
                }
            } else if (xhr.status === 401) {
                errorMessage = 'Registration service is currently unavailable. Please try again later.';
            } else if (xhr.status === 403) {
                errorMessage = 'Access denied. Please try again.';
            } else if (xhr.status === 404) {
                errorMessage = 'Service not found. Please try again later.';
            } else if (xhr.status === 500) {
                errorMessage = 'Server error. Please try again later.';
            }

            showError(errorMessage);
        },
        complete: function() {
            // Reset button state
            submitButton.prop('disabled', false).text(originalText);
        }
    });
});