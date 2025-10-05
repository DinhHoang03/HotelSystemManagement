// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Handle header shrink on scroll
$(window).scroll(function() {
    if ($(window).scrollTop() > 50) {
        $('#navContainer').addClass('shrink');
        $('.nav-link').css('color', '#374151');
        $('.nav-link:hover').css('color', '#4F46E5');
        $('#userMenuButton').css('color', '#374151');
        $('#userMenuButton:hover').css('color', '#4F46E5');
        $('.nav-text').css('color', '#374151');
    } else {
        $('#navContainer').removeClass('shrink');
        $('.nav-link').css('color', 'white');
        $('.nav-link:hover').css('color', '#E5E7EB');
        $('#userMenuButton').css('color', 'white');
        $('#userMenuButton:hover').css('color', '#E5E7EB');
        $('.nav-text').css('color', 'white');
    }
});

// Check authentication
$(document).ready(function() {
    checkAuth();
    loadBookings();
});

// Toggle user menu
$('#userMenuButton').click(function() {
    const menu = $('#userMenu');
    if (menu.hasClass('hidden')) {
        menu.removeClass('hidden scale-95 opacity-0')
            .addClass('scale-100 opacity-100');
    } else {
        menu.removeClass('scale-100 opacity-100')
            .addClass('scale-95 opacity-0');
        setTimeout(() => {
            menu.addClass('hidden');
        }, 200);
    }
});

// Close menu when clicking outside
$(document).click(function(event) {
    if (!$(event.target).closest('#userMenuButton, #userMenu').length) {
        const menu = $('#userMenu');
        if (!menu.hasClass('hidden')) {
            menu.removeClass('scale-100 opacity-100')
                .addClass('scale-95 opacity-0');
            setTimeout(() => {
                menu.addClass('hidden');
            }, 200);
        }
    }
});

// Pagination variables
let currentPage = 0;
let totalPages = 1;
const pageSize = 10;

// Load bookings
function loadBookings() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // First, get user profile to ensure we have userId
    $.ajax({
        url: '/customer/profile',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            console.log('Profile response:', response);
            if (response && response.id) {
                const userId = response.id;
                localStorage.setItem('userId', userId);

                // Update profile menu with user information
                updateProfileMenu(response);

                // Now load bookings with the userId
                const apiUrl = `/booking/list/${userId}?page=${currentPage}&size=${pageSize}`;
                console.log('Loading bookings from:', apiUrl);

                $.ajax({
                    url: apiUrl,
                    type: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    success: function(response) {
                        console.log('Bookings response:', response);
                        if (response && response.result) {
                            const pageData = response.result;
                            const bookings = pageData.content || [];
                            totalPages = pageData.totalPages || 1;

                            displayBookings(bookings);
                            updatePaginationUI();
                        } else {
                            $('#bookingsList').html('<p class="text-center text-gray-500">No bookings found</p>');
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('Error loading bookings:', {
                            status: xhr.status,
                            statusText: xhr.statusText,
                            responseText: xhr.responseText,
                            error: error
                        });

                        if (xhr.status === 401) {
                            // Token expired or invalid
                            localStorage.removeItem('token');
                            localStorage.removeItem('userId');
                            window.location.href = '/login.html';
                        } else if (xhr.status === 500) {
                            // Server error
                            $('#bookingsList').html(`
                                <div class="text-center p-4 bg-red-50 rounded-lg">
                                    <p class="text-red-600 font-medium">Server error occurred</p>
                                    <p class="text-gray-600 mt-2">Please try again later or contact support if the problem persists.</p>
                                </div>
                            `);
                        } else {
                            $('#bookingsList').html(`
                                <div class="text-center p-4 bg-red-50 rounded-lg">
                                    <p class="text-red-600 font-medium">Error loading bookings</p>
                                    <p class="text-gray-600 mt-2">Please try again later.</p>
                                </div>
                            `);
                        }
                    }
                });
            } else {
                console.error('Invalid profile response:', response);
                window.location.href = '/login.html';
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading profile:', {
                status: xhr.status,
                statusText: xhr.statusText,
                responseText: xhr.responseText,
                error: error
            });

            if (xhr.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('userId');
                window.location.href = '/login.html';
            } else {
                $('#bookingsList').html(`
                    <div class="text-center p-4 bg-red-50 rounded-lg">
                        <p class="text-red-600 font-medium">Error loading profile</p>
                        <p class="text-gray-600 mt-2">Please try again later.</p>
                    </div>
                `);
            }
        }
    });
}

// Display bookings
function displayBookings(bookings) {
    const bookingsList = $('#bookingsList');
    bookingsList.empty();

    if (!bookings || bookings.length === 0) {
        bookingsList.html('<p class="text-gray-500 text-center py-8">No bookings found.</p>');
        return;
    }

    bookings.forEach(booking => {
        // Ensure booking status and paymentBill status exist and convert to lowercase, or use default values
        const bookingStatus = (booking.bookingStatus || 'PENDING').toLowerCase();
        const paymentStatus = (booking.paymentStatus || 'PENDING').toLowerCase();

        // Debug: Log status values
        console.log('Booking #' + booking.bookingId + ' - bookingStatus:', bookingStatus, '- paymentStatus:', paymentStatus);

        // Get appropriate status colors
        const bookingStatusColor = getStatusColor(bookingStatus);
        const paymentStatusColor = getStatusColor(paymentStatus);

        const formattedDate = formatDate(booking.bookingDate);
        const formattedRoomPrice = formatCurrency(booking.totalRoomPrice);
        const formattedServicePrice = formatCurrency(booking.totalBookingServicePrice);
        const formattedGrandTotal = formatCurrency(booking.grandTotal);

        // Show Cancel button when booking is not cancelled or completed
        const showCancelButton = bookingStatus !== 'cancelled' && bookingStatus !== 'completed';

        // Show Create Bill button only when booking is in PENDING status
        const showCreateBillButton = bookingStatus === 'pending';

        // Show Display Booking button when booking is CONFIRMED
        const showDisplayButton = bookingStatus === 'confirmed';

        const bookingHtml = `
            <div class="bg-white rounded-xl border border-gray-200 shadow-sm mb-8 flex flex-col md:flex-row overflow-hidden">
                <!-- Left: Logo and status -->
                <div class="flex flex-col items-center justify-between bg-gray-50 px-6 py-8 md:w-56 w-full">
                    <img src="https://cdn-icons-png.flaticon.com/512/2111/2111320.png" alt="Hotel Logo" class="w-14 h-14 mb-4 rounded-full border border-gray-200 shadow">
                    <span class="mt-2 px-3 py-1 rounded text-xs font-semibold ${bookingStatusColor} uppercase tracking-wide">${booking.bookingStatus}</span>
                </div>
                <!-- Right: Info -->
                <div class="flex-1 flex flex-col justify-between p-6">
                    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-2">
                        <div class="flex-1 min-w-0">
                            <div class="flex flex-wrap items-center gap-3 mb-1">
                                <span class="text-base font-bold text-gray-900 break-all">Đơn đặt #${booking.bookingId}</span>
                            </div>
                            <div class="flex flex-wrap gap-6 text-sm text-gray-500 mb-1">
                                <span>Ngày đặt: <span class="font-medium text-gray-700">${formattedDate}</span></span>
                            </div>
                        </div>
                        <div class="flex-shrink-0 flex flex-col items-end gap-2 min-w-[120px]">
                            <span class="text-2xl font-bold text-indigo-800 tracking-tight">${formattedGrandTotal} <span class="text-base font-medium text-gray-500">đ</span></span>
                        </div>
                    </div>
                    <div class="border-t border-dashed border-gray-300 my-4"></div>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
                        <div class="flex flex-col gap-1">
                            <span class="text-gray-500 text-xs">Room Total</span>
                            <span class="text-lg font-semibold text-gray-800">${formattedRoomPrice}</span>
                        </div>
                        <div class="flex flex-col gap-1">
                            <span class="text-gray-500 text-xs">Services Total</span>
                            <span class="text-lg font-semibold text-gray-800">${formattedServicePrice}</span>
                        </div>
                    </div>
                    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mt-2">
                        <div>
                            <span class="text-gray-500 text-xs">Grand Total</span>
                            <span class="text-xl font-bold text-indigo-700 ml-2">${formattedGrandTotal} <span class="text-base font-medium text-gray-500">đ</span></span>
                        </div>
                        <div class="flex flex-wrap gap-2 mt-2 md:mt-0">
                            ${showCreateBillButton ? `
                                <button onclick="createBill('${booking.bookingId}')" class="flex items-center gap-2 px-4 py-2 rounded border border-indigo-500 text-indigo-700 font-semibold bg-white hover:bg-indigo-50 transition"><i class="fas fa-file-invoice-dollar"></i> Create Bill</button>
                            ` : ''}
                            ${showDisplayButton ? `
                                <button onclick="displayBookingDetails('${booking.bookingId}')" class="flex items-center gap-2 px-4 py-2 rounded border border-green-500 text-green-700 font-semibold bg-white hover:bg-green-50 transition"><i class="fas fa-info-circle"></i> View Details</button>
                            ` : ''}
                            ${showCancelButton ? `
                                <button onclick="cancelBookingWithConfirmation('${booking.bookingId}')" class="flex items-center gap-2 px-4 py-2 rounded border border-red-500 text-red-700 font-semibold bg-white hover:bg-red-50 transition"><i class="fas fa-times"></i> Cancel</button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
        bookingsList.append(bookingHtml);
    });
}

function getStatusColor(status) {
    const statusColors = {
        'pending': 'bg-yellow-100 text-yellow-800',
        'confirmed': 'bg-green-100 text-green-800',
        'cancelled': 'bg-red-100 text-red-800',
        'completed': 'bg-blue-100 text-blue-800',
        'paid': 'bg-green-100 text-green-800',
        'unpaid': 'bg-red-100 text-red-800'
    };
    return statusColors[status] || 'bg-gray-100 text-gray-800';
}

function capitalizeFirstLetter(string) {
    if (!string) return 'Unknown';
    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
}

// Update pagination UI
function updatePaginationUI() {
    $('#pageInfo').text(`Page ${currentPage + 1} of ${totalPages}`);
    $('#prevPage').prop('disabled', currentPage === 0);
    $('#nextPage').prop('disabled', currentPage === totalPages - 1);
}

// Handle pagination
$('#prevPage').click(function() {
    if (currentPage > 0) {
        currentPage--;
        loadBookings();
    }
});

$('#nextPage').click(function() {
    if (currentPage < totalPages - 1) {
        currentPage++;
        loadBookings();
    }
});

// Add a new function to update the profile menu
function updateProfileMenu(userData) {
    // Update profile menu with user information
    $('#userMenuName').text(userData.name || 'Michael Man');
    $('#userMenuEmail').text(userData.email || 'microb@example.com');
    $('#userMenuPhone').text(userData.phone || '092236789');
    $('#userMenuAddress').text(userData.address || 'Hanoi');

    // Update avatar with user's name
    const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(userData.name || 'MM')}&background=4F46E5&color=fff`;
    $('#userAvatar').attr('src', avatarUrl);
    $('#userMenuAvatar').attr('src', avatarUrl);
}

// Add cancelBooking function
function cancelBooking(bookingId) {
    if (!bookingId) {
        console.error('No booking ID provided');
        return;
    }

    // Show confirmation dialog
    Swal.fire({
        title: 'Cancel Booking?',
        text: 'Are you sure you want to cancel this booking? This action cannot be undone.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#EF4444',
        cancelButtonColor: '#6B7280',
        confirmButtonText: 'Yes, cancel it',
        cancelButtonText: 'No, keep it'
    }).then((result) => {
        if (result.isConfirmed) {
            const token = localStorage.getItem('token');

            if (!token) {
                Swal.fire({
                    title: 'Error!',
                    text: 'Please login again to cancel your booking.',
                    icon: 'error'
                });
                return;
            }

            // Send cancel request
            $.ajax({
                url: `/booking/del/${bookingId}`,
                type: 'DELETE',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                success: function(response) {
                    console.log('Cancel booking response:', response);

                    Swal.fire({
                        title: 'Cancelled!',
                        text: 'Your booking has been cancelled successfully.',
                        icon: 'success'
                    }).then(() => {
                        // Reload bookings
                        loadBookings();
                    });
                },
                error: function(xhr) {
                    console.error('Error cancelling booking:', xhr);

                    let errorMessage = 'An error occurred while cancelling your booking. Please try again.';

                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMessage = xhr.responseJSON.message;
                    }

                    Swal.fire({
                        title: 'Error!',
                        text: errorMessage,
                        icon: 'error'
                    });
                }
            });
        }
    });
}

function handlePayment(bookingId) {
    // Get token
    const token = localStorage.getItem('token');

    if (!token) {
        Swal.fire({
            title: 'Error',
            text: 'Your session has expired. Please log in again.',
            icon: 'error'
        }).then(() => {
            window.location.href = '/login.html';
        });
        return;
    }

    // Prepare request data
    const requestData = {
        bookingId: bookingId
    };

    console.log('Creating booking bill for booking ID:', bookingId);

    // Show loading indicator
    Swal.fire({
        title: 'Creating Bill',
        text: 'Please wait while we prepare your bill...',
        icon: 'info',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // Step 1: Create a bill
    $.ajax({
        url: '/bill/create',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(requestData),
        success: function(response) {
            console.log('Bill created successfully:', response);

            if (response && response.result) {
                const billResponse = response.result;
                const bookingBillId = billResponse.bookingBillId;
                const amount = billResponse.grandTotal;

                // Show confirmation dialog
                Swal.fire({
                    title: 'Proceed to Payment?',
                    html: `
                        <div class="text-left">
                            <p>Your booking bill has been created with the following details:</p>
                            <div class="mt-4 p-4 bg-gray-100 rounded-lg">
                                <p><strong>Bill ID:</strong> ${bookingBillId}</p>
                                <p><strong>Amount:</strong> ${formatCurrency(amount)} VND</p>
                            </div>
                            <p class="mt-4">Do you want to proceed with the paymentBill via ZaloPay?</p>
                        </div>
                    `,
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#4F46E5',
                    cancelButtonColor: '#EF4444',
                    confirmButtonText: 'Yes, pay now',
                    cancelButtonText: 'No, cancel paymentBill'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // User wants to proceed with the paymentBill
                        createZaloPayOrder(bookingBillId);
                    } else {
                        // User wants to cancel the paymentBill
                        deleteBookingBill(bookingBillId);
                    }
                });
            } else {
                Swal.fire({
                    title: 'Error',
                    text: 'Failed to create bill. Please try again.',
                    icon: 'error'
                });
            }
        },
        error: function(xhr, status, error) {
            console.error('Error creating bill:', xhr.responseText);
            let errorMessage = 'An error occurred while creating the bill. Please try again.';

            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }

            // Kiểm tra nếu lỗi là duplicate entry cho booking bill
            if (xhr.responseText && xhr.responseText.includes('Duplicate entry') && xhr.responseText.includes('booking_bills')) {
                errorMessage = 'A paymentBill has already been initiated for this booking. Please check your pending payments or try again later.';

                Swal.fire({
                    title: 'Payment Already Initiated',
                    text: errorMessage,
                    icon: 'info',
                    confirmButtonColor: '#4F46E5',
                    confirmButtonText: 'OK'
                }).then(() => {
                    // Reload bookings to get updated status
                    loadBookings();
                });
            } else {
                Swal.fire({
                    title: 'Error',
                    text: errorMessage,
                    icon: 'error'
                });
            }
        }
    });
}

function deleteBookingBill(bookingBillId) {
    const token = localStorage.getItem('token');

    // Show loading indicator
    Swal.fire({
        title: 'Cancelling Bill',
        text: 'Please wait while we cancel your bill...',
        icon: 'info',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // Call API to delete the booking bill
    $.ajax({
        url: `/bill/delete/${bookingBillId}`,
        type: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        success: function(response) {
            console.log('Bill deleted successfully:', response);

            Swal.fire({
                title: 'Payment Cancelled',
                text: 'Your bill has been cancelled successfully.',
                icon: 'info'
            }).then(() => {
                // Reload the bookings to refresh the status
                loadBookings();
            });
        },
        error: function(xhr, status, error) {
            console.error('Error deleting bill:', xhr.responseText);
            let errorMessage = 'An error occurred while cancelling the bill. Please try again.';

            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }

            Swal.fire({
                title: 'Error',
                text: errorMessage,
                icon: 'error'
            });
        }
    });
}

function showZaloPayErrorDetails(errorCode, errorMessage) {
    let detailedMessage = errorMessage;

    // Provide more specific guidance based on error code
    switch(errorCode) {
        case 1015:
            detailedMessage = "Payment creation failed! This error typically occurs when there is an issue with the ZaloPay configuration or connection. Please check:";

            Swal.fire({
                title: 'ZaloPay Configuration Error',
                html: `
                    <div class="text-left">
                        <p>${detailedMessage}</p>
                        <ul class="mt-4 space-y-2 text-sm">
                            <li>• ZaloPay app ID, key1, and key2 in application.yaml</li>
                            <li>• Callback URL configuration in ZaloPayConfig.java</li>
                            <li>• Network connectivity to ZaloPay API servers</li>
                            <li>• Possible sandbox/production mode mismatch</li>
                        </ul>
                        <p class="mt-4">Please contact customer support if the issue persists.</p>
                    </div>
                `,
                icon: 'error',
                confirmButtonText: 'OK',
                confirmButtonColor: '#4F46E5'
            });
            break;

        default:
            Swal.fire({
                title: 'Payment Error',
                text: detailedMessage,
                icon: 'error',
                confirmButtonText: 'OK',
                confirmButtonColor: '#4F46E5'
            });
    }
}

function createZaloPayOrder(bookingBillId) {
    const token = localStorage.getItem('token');

    // Check if there's an active internet connection
    if (!navigator.onLine) {
        Swal.fire({
            title: 'No Internet Connection',
            text: 'Please check your internet connection and try again.',
            icon: 'warning',
            confirmButtonText: 'OK',
            confirmButtonColor: '#4F46E5'
        });
        return;
    }

    // Show loading indicator
    Swal.fire({
        title: 'Processing Payment',
        text: 'Please wait while we redirect you to the paymentBill gateway...',
        icon: 'info',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // Lưu bookingBillId vào localStorage để sử dụng sau khi thanh toán thành công
    localStorage.setItem('currentBookingBillId', bookingBillId);

    // Prepare paymentBill request data
    const requestData = {
        bookingBillId: bookingBillId  // ZaloPayOrderRequest uses bookingBillId field
    };

    console.log('Sending ZaloPay paymentBill request:', requestData);

    // Debug note
    console.log('Debug: If paymentBill fails with code 1015, check ZaloPay configuration in application.yaml file and ZaloPayConfig.java');

    $.ajax({
        url: '/zalopay/order',  // Updated endpoint
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(requestData),
        success: function(response) {
            console.log('ZaloPay order created successfully:', response);

            if (response && response.result) {
                try {
                    // Parse JSON string response
                    const zaloPayResponse = JSON.parse(response.result);
                    console.log('Parsed ZaloPay response:', zaloPayResponse);

                    if (zaloPayResponse.return_code === 1 && zaloPayResponse.order_url) {
                        // Redirect to ZaloPay paymentBill page
                        window.open(zaloPayResponse.order_url, '_blank');

                        Swal.fire({
                            title: 'Payment Initiated',
                            text: 'You have been redirected to ZaloPay. After completing paymentBill, please return to this page to check your booking status.',
                            icon: 'success'
                        }).then(() => {
                            // Reload the bookings
                            loadBookings();
                        });
                    } else if (zaloPayResponse.error) {
                        Swal.fire({
                            title: 'Error',
                            text: 'ZaloPay error: ' + zaloPayResponse.error,
                            icon: 'error'
                        });
                    } else {
                        Swal.fire({
                            title: 'Error',
                            text: 'Failed to create paymentBill order. Error code: ' + (zaloPayResponse.return_code || 'Unknown') +
                                  ', Message: ' + (zaloPayResponse.return_message || ''),
                            icon: 'error'
                        });
                    }
                } catch (parseError) {
                    console.error('Error parsing ZaloPay response:', parseError);
                    Swal.fire({
                        title: 'Error',
                        text: 'Could not process ZaloPay response. Please try again.',
                        icon: 'error'
                    });
                }
            } else {
                Swal.fire({
                    title: 'Error',
                    text: 'Failed to create paymentBill order. Please try again.',
                    icon: 'error'
                });
            }
        },
        error: function(xhr, status, error) {
            console.error('Error creating ZaloPay order:', xhr.responseText);

            // Log detailed information about the error
            console.log({
                status: xhr.status,
                statusText: xhr.statusText,
                responseText: xhr.responseText,
                error: error
            });

            let errorMessage = 'An error occurred while creating the paymentBill order. Please try again.';
            let errorCode = null;

            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
                errorCode = xhr.responseJSON.code;
            } else if (xhr.responseText) {
                try {
                    const errorObj = JSON.parse(xhr.responseText);
                    errorMessage = errorObj.message || errorMessage;
                    errorCode = errorObj.code;
                } catch (e) {
                    console.error('Error parsing error response:', e);
                }
            }

            // Show detailed error based on error code
            showZaloPayErrorDetails(errorCode, errorMessage);
        }
    });
}

// Thêm hàm mới để xác nhận việc hủy đặt phòng với cảnh báo không hoàn tiền
function cancelBookingWithConfirmation(bookingId) {
    if (!bookingId) {
        console.error('No booking ID provided');
        return;
    }

    // Show confirmation dialog with warning about no refund
    Swal.fire({
        title: 'Hủy đặt phòng?',
        html: `
            <div class="text-left">
                <p>Bạn có chắc chắn muốn hủy đặt phòng này?</p>
                <div class="mt-4 p-4 bg-red-50 rounded-lg">
                    <p class="text-red-600 font-bold">Lưu ý quan trọng:</p>
                    <p class="text-red-600">Việc hủy đặt phòng sẽ không được hoàn tiền.</p>
                </div>
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#EF4444',
        cancelButtonColor: '#6B7280',
        confirmButtonText: 'Đồng ý, hủy đặt phòng',
        cancelButtonText: 'Không, giữ lại'
    }).then((result) => {
        if (result.isConfirmed) {
            // Người dùng đã xác nhận, tiến hành hủy đặt phòng
            cancelBooking(bookingId);
        }
    });
}

// Function to create bill
async function createBill(bookingId) {
    try {
        // Show loading indicator
        Swal.fire({
            title: 'Creating Bill',
            text: 'Please wait while we create your bill...',
            icon: 'info',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login.html';
            return;
        }

        const response = await fetch('https://localhost:8443/bill/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ bookingId }),
            credentials: 'include'
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to create bill');
        }

        if (data.result) {
            // Show success message
            Swal.fire({
                title: 'Success!',
                text: 'Bill created successfully!',
                icon: 'success',
                confirmButtonText: 'View Bills'
            }).then(() => {
                // Redirect to my-bills page
                window.location.href = '/my-bills.html';
            });
        } else {
            throw new Error(data.message || 'Unknown error creating bill');
        }
    } catch (error) {
        console.error('Error creating bill:', error);

        Swal.fire({
            title: 'Error',
            text: error.message || 'Failed to create bill. Please try again.',
            icon: 'error',
            confirmButtonText: 'OK'
        });
    }
}

// Add function to display booking details
async function displayBookingDetails(bookingId) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login.html';
            return;
        }

        // Save bookingId to localStorage
        localStorage.setItem('currentBookingId', bookingId);

        // Show loading indicator
        Swal.fire({
            title: 'Loading Booking Details',
            text: 'Please wait...',
            icon: 'info',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        const response = await fetch(`https://localhost:8443/booking/info/${bookingId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        const data = await response.json();
        console.log('Booking response:', data); // Debug log

        if (!response.ok) {
            throw new Error(data.message || 'Failed to fetch booking details');
        }

        if (data.result) {
            const booking = data.result;
            console.log('Booking rooms:', booking.bookingRooms); // Debug log
            console.log('Booking items:', booking.bookingItems); // Debug log

            // Format the booking details for display
            const detailsHtml = `
                <div class="text-left">
                    <div class="mb-4">
                        <h3 class="text-lg font-bold text-gray-900 mb-2 flex items-center">
                            <i class="fas fa-info-circle text-indigo-500 mr-2"></i>
                            Booking Information
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-hashtag text-gray-400 mr-2"></i>
                                    Booking ID:
                                </p>
                                <p class="font-semibold">${booking.bookingId || 'N/A'}</p>
                            </div>
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-user text-gray-400 mr-2"></i>
                                    Customer Name:
                                </p>
                                <p class="font-semibold">${booking.customerName || 'N/A'}</p>
                            </div>
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-calendar text-gray-400 mr-2"></i>
                                    Booking Date:
                                </p>
                                <p class="font-semibold">${booking.bookingDate ? new Date(booking.bookingDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                            </div>
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-clock text-gray-400 mr-2"></i>
                                    Booking Status:
                                </p>
                                <p class="font-semibold">${booking.bookingStatus || 'N/A'}</p>
                            </div>
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-credit-card text-gray-400 mr-2"></i>
                                    Payment Status:
                                </p>
                                <p class="font-semibold">${booking.paymentStatus || 'N/A'}</p>
                            </div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <h3 class="text-lg font-bold text-gray-900 mb-2 flex items-center">
                            <i class="fas fa-bed text-indigo-500 mr-2"></i>
                            Room Details
                        </h3>
                        <div class="space-y-2">
                            ${Array.isArray(booking.bookingRooms) && booking.bookingRooms.length > 0 ?
                                booking.bookingRooms.map(room => {
                                    console.log('Processing room:', room);
                                    return `
                                        <div class="p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors duration-200">
                                            <p class="font-semibold flex items-center">
                                                <i class="fas fa-door-open text-indigo-500 mr-2"></i>
                                                Room ${room.rooms?.[0] || 'N/A'}
                                            </p>
                                            <p class="text-gray-600 flex items-center">
                                                <i class="fas fa-sign-in-alt text-gray-400 mr-2"></i>
                                                Check-in: ${room.checkInDate ? new Date(room.checkInDate[0], room.checkInDate[1]-1, room.checkInDate[2]).toLocaleDateString('vi-VN') : 'N/A'}
                                            </p>
                                            <p class="text-gray-600 flex items-center">
                                                <i class="fas fa-sign-out-alt text-gray-400 mr-2"></i>
                                                Check-out: ${room.checkOutDate ? new Date(room.checkOutDate[0], room.checkOutDate[1]-1, room.checkOutDate[2]).toLocaleDateString('vi-VN') : 'N/A'}
                                            </p>
                                            <p class="text-gray-600 flex items-center">
                                                <i class="fas fa-money-bill-wave text-gray-400 mr-2"></i>
                                                Amount: ${room.totalRoomAmount ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(room.totalRoomAmount) : 'N/A'}
                                            </p>
                                        </div>
                                    `;
                                }).join('') :
                                '<p class="text-gray-500 italic">No room details available</p>'
                            }
                        </div>
                    </div>

                    <div class="mb-4">
                        <h3 class="text-lg font-bold text-gray-900 mb-2 flex items-center">
                            <i class="fas fa-concierge-bell text-indigo-500 mr-2"></i>
                            Service Details
                        </h3>
                        <div class="space-y-2">
                            ${Array.isArray(booking.bookingItems) && booking.bookingItems.length > 0 ?
                                booking.bookingItems.map(item => {
                                    console.log('Processing item:', item);
                                    return `
                                        <div class="p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors duration-200">
                                            <p class="font-semibold flex items-center">
                                                <i class="fas fa-utensils text-indigo-500 mr-2"></i>
                                                ${item.hotelOffer || 'N/A'}
                                            </p>
                                            <p class="text-gray-600 flex items-center">
                                                <i class="fas fa-hashtag text-gray-400 mr-2"></i>
                                                Quantity: ${item.quantity || 'N/A'}
                                            </p>
                                            <p class="text-gray-600 flex items-center">
                                                <i class="fas fa-money-bill-wave text-gray-400 mr-2"></i>
                                                Price: ${item.totalItemsPrice ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.totalItemsPrice) : 'N/A'}
                                            </p>
                                        </div>
                                    `;
                                }).join('') :
                                '<p class="text-gray-500 italic">No service details available</p>'
                            }
                        </div>
                    </div>

                    <div class="mt-4 p-4 bg-indigo-50 rounded-lg">
                        <h3 class="text-lg font-bold text-indigo-900 mb-2 flex items-center">
                            <i class="fas fa-calculator text-indigo-500 mr-2"></i>
                            Payment Summary
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-bed text-gray-400 mr-2"></i>
                                    Room Total:
                                </p>
                                <p class="font-semibold">${booking.totalRoomPrice ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(booking.totalRoomPrice) : 'N/A'}</p>
                            </div>
                            <div>
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-concierge-bell text-gray-400 mr-2"></i>
                                    Services Total:
                                </p>
                                <p class="font-semibold">${booking.totalBookingServicePrice ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(booking.totalBookingServicePrice) : 'N/A'}</p>
                            </div>
                            <div class="col-span-2">
                                <p class="text-gray-600 flex items-center">
                                    <i class="fas fa-money-bill-wave text-gray-400 mr-2"></i>
                                    Grand Total:
                                </p>
                                <p class="text-xl font-bold text-indigo-600">${booking.grandTotal ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(booking.grandTotal) : 'N/A'}</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Show the details in a modal
            const modal = Swal.fire({
                title: 'Booking Details',
                html: detailsHtml,
                showCloseButton: true,
                showConfirmButton: false,
                allowOutsideClick: true,
                allowEscapeKey: true,
                customClass: {
                    container: 'booking-details-modal',
                    popup: 'max-h-[85vh] overflow-y-auto'
                },
                didOpen: () => {
                    // Add animation to elements
                    const elements = document.querySelectorAll('.booking-details-modal .p-3, .booking-details-modal .p-4');
                    elements.forEach((el, index) => {
                        el.style.opacity = '0';
                        el.style.transform = 'translateY(20px)';
                        setTimeout(() => {
                            el.style.transition = 'all 0.3s ease';
                            el.style.opacity = '1';
                            el.style.transform = 'translateY(0)';
                        }, index * 100);
                    });
                },
                willClose: () => {
                    // Add closing animation
                    const elements = document.querySelectorAll('.booking-details-modal .p-3, .booking-details-modal .p-4');
                    elements.forEach((el, index) => {
                        el.style.transition = 'all 0.2s ease';
                        el.style.opacity = '0';
                        el.style.transform = 'translateY(-20px)';
                    });
                }
            });

            // Handle close button click
            document.querySelector('.swal2-close').addEventListener('click', () => {
                // Remove modal and backdrop
                const modalElement = document.querySelector('.swal2-container');
                const backdropElement = document.querySelector('.swal2-backdrop-show');

                if (modalElement) {
                    modalElement.style.opacity = '0';
                    modalElement.style.transition = 'opacity 0.2s ease';
                }

                if (backdropElement) {
                    backdropElement.style.opacity = '0';
                    backdropElement.style.transition = 'opacity 0.2s ease';
                }

                // Remove elements after animation
                setTimeout(() => {
                    if (modalElement) modalElement.remove();
                    if (backdropElement) backdropElement.remove();
                    // Remove any remaining modal classes from body
                    document.body.classList.remove('swal2-shown', 'swal2-height-auto');
                }, 200);
            });

            // Handle outside click
            document.querySelector('.swal2-backdrop-show')?.addEventListener('click', (e) => {
                if (e.target.classList.contains('swal2-backdrop-show')) {
                    const modalElement = document.querySelector('.swal2-container');
                    const backdropElement = document.querySelector('.swal2-backdrop-show');

                    if (modalElement) {
                        modalElement.style.opacity = '0';
                        modalElement.style.transition = 'opacity 0.2s ease';
                    }

                    if (backdropElement) {
                        backdropElement.style.opacity = '0';
                        backdropElement.style.transition = 'opacity 0.2s ease';
                    }

                    setTimeout(() => {
                        if (modalElement) modalElement.remove();
                        if (backdropElement) backdropElement.remove();
                        document.body.classList.remove('swal2-shown', 'swal2-height-auto');
                    }, 200);
                }
            });
        } else {
            throw new Error('No booking details found');
        }
    } catch (error) {
        console.error('Error displaying booking details:', error);
        Swal.fire({
            title: 'Error',
            text: error.message || 'Failed to load booking details',
            icon: 'error',
            confirmButtonText: 'OK'
        });
    }
}

// Add smooth transition functions
function smoothGoto(url) {
    document.querySelector('.loadscreen').style.display = 'flex';
    document.querySelector('.loadscreen').style.opacity = '1';
    setTimeout(() => {
        window.location.href = url;
    }, 500);
}

function smoothLogout() {
    document.querySelector('.loadscreen').style.display = 'flex';
    document.querySelector('.loadscreen').style.opacity = '1';
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 500);
}

// Show loadscreen on page load
window.addEventListener('load', function() {
    const loadscreen = document.querySelector('.loadscreen');
    loadscreen.style.display = 'flex';
    setTimeout(() => {
        loadscreen.style.opacity = '0';
        setTimeout(() => {
            loadscreen.style.display = 'none';
        }, 500);
    }, 500);
});

// Update all navigation links to use smooth transition
document.addEventListener('DOMContentLoaded', function() {
    const links = document.querySelectorAll('a[href]');
    links.forEach(link => {
        if (link.getAttribute('href').startsWith('#')) return;
        if (link.getAttribute('href').startsWith('tel:')) return;
        if (link.getAttribute('href').startsWith('mailto:')) return;
        if (link.getAttribute('href').startsWith('javascript:')) return;

        link.addEventListener('click', function(e) {
            e.preventDefault();
            smoothGoto(this.getAttribute('href'));
        });
    });

    // Update logout button
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            smoothLogout();
        });
    }
});