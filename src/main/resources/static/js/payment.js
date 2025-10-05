// Check authentication
$(document).ready(function() {
    checkAuth();
    loadBookingDetails();
});

// Load booking details from URL parameters
function loadBookingDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const bookingId = urlParams.get('bookingId');

    if (!bookingId) {
        alert('No booking ID provided');
        window.location.href = '/customer-dashboard.html';
        return;
    }

    // Show loading overlay
    $('#loadingOverlay').addClass('active');

    // Get booking details
    $.ajax({
        url: `/booking/${bookingId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            if (response && response.result) {
                const booking = response.result;
                $('#roomType').text(booking.roomType);
                $('#checkInDate').text(new Date(booking.checkInDate).toLocaleDateString());
                $('#checkOutDate').text(new Date(booking.checkOutDate).toLocaleDateString());
                $('#nights').text(booking.nights);
                $('#roomPrice').text(formatCurrency(booking.roomPrice));
                $('#tax').text(formatCurrency(booking.tax));
                $('#totalAmount').text(formatCurrency(booking.grandTotal));

                // Enable pay button
                $('#payButton').prop('disabled', false);
            } else {
                alert('Failed to load booking details');
                window.location.href = '/customer-dashboard.html';
            }
        },
        error: function(xhr) {
            alert('Error loading booking details');
            window.location.href = '/customer-dashboard.html';
        },
        complete: function() {
            $('#loadingOverlay').removeClass('active');
        }
    });
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Handle paymentBill method selection
$('#zaloPayMethod').click(function() {
    $('.paymentBill-method').removeClass('selected');
    $(this).addClass('selected');
});

// Handle paymentBill
$('#payButton').click(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const bookingId = urlParams.get('bookingId');

    if (!bookingId) {
        alert('No booking ID provided');
        return;
    }

    // Show loading overlay
    $('#loadingOverlay').addClass('active');

    // Create booking bill
    $.ajax({
        url: '/bill/create',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        data: {
            bookingId: bookingId
        },
        success: function(response) {
            if (response && response.result) {
                const bill = response.result;
                // Redirect to ZaloPay paymentBill page
                window.location.href = bill.paymentUrl;
            } else {
                alert('Failed to create paymentBill');
            }
        },
        error: function(xhr) {
            alert('Error creating paymentBill');
        },
        complete: function() {
            $('#loadingOverlay').removeClass('active');
        }
    });
});