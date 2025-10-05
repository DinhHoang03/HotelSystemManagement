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

// Initialize date pickers
flatpickr("#checkInDate", {
    minDate: "today",
    dateFormat: "Y-m-d",
    onChange: function(selectedDates, dateStr) {
        if (selectedDates.length > 0) {
            flatpickr("#checkOutDate", {
                minDate: selectedDates[0],
                dateFormat: "Y-m-d"
            });
            loadSelectedItems();
        }
    }
});

flatpickr("#checkOutDate", {
    minDate: "today",
    dateFormat: "Y-m-d",
    onChange: function() {
        loadSelectedItems();
    }
});

// Check authentication
$(document).ready(function() {
    checkAuth();
    loadSelectedItems();
});

// Toggle user menu with animation
$('#userMenuButton').click(function() {
    const menu = $('#userMenu');
    if (menu.hasClass('hidden')) {
        menu.removeClass('hidden');
        setTimeout(() => {
            menu.removeClass('opacity-0 scale-95').addClass('opacity-100 scale-100');
        }, 10);
    } else {
        menu.removeClass('opacity-100 scale-100').addClass('opacity-0 scale-95');
        setTimeout(() => {
            menu.addClass('hidden');
        }, 300);
    }
});

// Close menu when clicking outside
$(document).click(function(event) {
    if (!$(event.target).closest('#userMenuButton, #userMenu').length) {
        const menu = $('#userMenu');
        menu.removeClass('opacity-100 scale-100').addClass('opacity-0 scale-95');
        setTimeout(() => {
            menu.addClass('hidden');
        }, 300);
    }
});

// Load selected items from localStorage
function loadSelectedItems() {
    const selectedRooms = JSON.parse(localStorage.getItem('selectedRooms') || '[]');
    const selectedServices = JSON.parse(localStorage.getItem('selectedServices') || '[]');
    const checkInDate = $('#checkInDate').val();
    const checkOutDate = $('#checkOutDate').val();

    if (!checkInDate || !checkOutDate) {
        $('#selectedItems').html('<p class="text-gray-500">Please select check-in and check-out dates to view selected items</p>');
        return;
    }

    let roomTotal = 0;
    let serviceTotal = 0;
    let itemsHtml = '';

    // Display selected rooms
    if (selectedRooms.length > 0) {
        itemsHtml += '<div class="mb-6"><h4 class="text-lg font-semibold text-gray-800 mb-3">Selected Rooms</h4>';
        selectedRooms.forEach(room => {
            const total = room.totalRoomAmount || 0;
            roomTotal += total;
            itemsHtml += `
                <div class="bg-gray-50 p-4 rounded-lg mb-3">
                    <div class="flex justify-between items-center">
                        <div>
                            <h5 class="font-medium">${room.roomNumber}</h5>
                            <p class="text-sm text-gray-600">${room.roomType}</p>
                        </div>
                        <span class="text-indigo-600 font-medium">${total.toLocaleString()} VND</span>
                    </div>
                </div>
            `;
        });
        itemsHtml += '</div>';
    }

    // Display selected services
    if (selectedServices.length > 0) {
        itemsHtml += '<div class="mb-6"><h4 class="text-lg font-semibold text-gray-800 mb-3">Selected Services</h4>';
        selectedServices.forEach(service => {
            const total = service.totalItemsPrice || 0;
            serviceTotal += total;
            itemsHtml += `
                <div class="bg-gray-50 p-4 rounded-lg mb-3">
                    <div class="flex justify-between items-center">
                        <div>
                            <h5 class="font-medium">${service.name}</h5>
                            <p class="text-sm text-gray-600">Quantity: ${service.quantity}</p>
                        </div>
                        <span class="text-indigo-600 font-medium">${total.toLocaleString()} VND</span>
                    </div>
                </div>
            `;
        });
        itemsHtml += '</div>';
    }

    if (itemsHtml === '') {
        itemsHtml = '<p class="text-gray-500">No items selected. Please visit the Services and Rooms pages to make selections.</p>';
    }

    $('#selectedItems').html(itemsHtml);
    $('#roomTotal').text(`${roomTotal.toLocaleString()} VND`);
    $('#serviceTotal').text(`${serviceTotal.toLocaleString()} VND`);
    $('#grandTotal').text(`${(roomTotal + serviceTotal).toLocaleString()} VND`);
}

// Update booking summary
function updateBookingSummary() {
    const selectedRooms = JSON.parse(localStorage.getItem('selectedRooms') || '[]');
    const selectedServices = JSON.parse(localStorage.getItem('selectedServices') || '[]');

    console.log('Selected Rooms:', selectedRooms);
    console.log('Selected Services:', selectedServices);

    // Debug: Log room structure in detail
    if (selectedRooms.length > 0) {
        console.log('First room details:');
        for (const key in selectedRooms[0]) {
            console.log(`${key}: ${selectedRooms[0][key]} (${typeof selectedRooms[0][key]})`);
        }
    }

    let roomTotal = 0;
    let serviceTotal = 0;

    // Update selected rooms
    const roomsList = $('#selectedRoomsList');
    roomsList.empty();

    if (selectedRooms.length > 0) {
        selectedRooms.forEach(room => {
            // Kiểm tra dữ liệu phòng
            if (!room || typeof room !== 'object') {
                console.warn('Invalid room data:', room);
                return;
            }

            if (!room.roomNumber) {
                console.warn('Missing room number:', room);
                return;
            }

            let total = 0;

            // Sử dụng totalRoomAmount nếu có
            if (room.totalRoomAmount) {
                // Kiểm tra kiểu dữ liệu
                if (typeof room.totalRoomAmount === 'string') {
                    total = parseFloat(room.totalRoomAmount);
                } else {
                    total = room.totalRoomAmount;
                }

                if (isNaN(total)) {
                    console.warn('Invalid totalRoomAmount for room:', room);
                    total = 0;
                }
            }
            // Nếu không có totalRoomAmount, không hiển thị giá tạm tính
            else {
                console.warn('No totalRoomAmount for room:', room);
                total = 0;
            }

            roomTotal += total;

            roomsList.append(`
                <div class="bg-gray-50 p-4 rounded-lg animate-fade-in">
                    <div class="flex justify-between items-center">
                        <div>
                            <h5 class="font-medium">Room ${room.roomNumber}</h5>
                            <p class="text-sm text-gray-600">${room.roomType}</p>
                            <p class="text-sm text-gray-600">Check-in: ${room.checkInDate}</p>
                            <p class="text-sm text-gray-600">Check-out: ${room.checkOutDate}</p>
                        </div>
                        <span class="text-indigo-600 font-medium">${total > 0 ? total.toLocaleString() + ' VND' : 'Pending price...'}</span>
                    </div>
                </div>
            `);
        });
    } else {
        roomsList.html('<p class="text-gray-500 text-sm">No rooms selected</p>');
    }

    // Update selected services
    const servicesList = $('#selectedServicesList');
    servicesList.empty();

    if (selectedServices.length > 0) {
        selectedServices.forEach(service => {
            let total = 0;

            if (service.totalItemsPrice) {
                if (typeof service.totalItemsPrice === 'string') {
                    total = parseFloat(service.totalItemsPrice);
                } else {
                    total = service.totalItemsPrice;
                }

                if (isNaN(total)) {
                    console.warn('Invalid totalItemsPrice for service:', service);
                    total = 0;
                }
            }

            serviceTotal += total;

            servicesList.append(`
                <div class="bg-gray-50 p-4 rounded-lg animate-fade-in">
                    <div class="flex justify-between items-center">
                        <div>
                            <h5 class="font-medium">${service.name}</h5>
                            <p class="text-sm text-gray-600">Quantity: ${service.quantity}</p>
                        </div>
                        <span class="text-indigo-600 font-medium">${total > 0 ? total.toLocaleString() + ' VND' : 'Pending price...'}</span>
                    </div>
                </div>
            `);
        });
    } else {
        servicesList.html('<p class="text-gray-500 text-sm">No services selected</p>');
    }

    // Update totals
    $('#roomTotal').text(`${roomTotal.toLocaleString()} VND`);
    $('#serviceTotal').text(`${serviceTotal.toLocaleString()} VND`);
    $('#grandTotal').text(`${(roomTotal + serviceTotal).toLocaleString()} VND`);
}

// Complete booking
function completeBooking() {
    const selectedRooms = JSON.parse(localStorage.getItem('selectedRooms') || '[]');
    const selectedServices = JSON.parse(localStorage.getItem('selectedServices') || '[]');
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    console.log('Selected Rooms:', selectedRooms);
    console.log('Selected Services:', selectedServices);
    console.log('User ID:', userId);
    console.log('Token available:', !!token);

    if (!userId) {
        alert('Please login to complete booking');
        return;
    }

    if (!token) {
        alert('Please login again to complete your booking');
        return;
    }

    if (selectedRooms.length === 0 && selectedServices.length === 0) {
        alert('Please select at least one room or service');
        return;
    }

    // Lấy danh sách bookingRoomIds và bookingItemIds
    const bookingRoomIds = selectedRooms.map(room => {
        console.log('Room:', room);
        if (!room.bookingRoomId) {
            console.error('Missing bookingRoomId for room:', room);
            return null;
        }
        return room.bookingRoomId;
    }).filter(id => id !== null);

    const bookingItemIds = selectedServices.map(service => {
        console.log('Service:', service);
        if (!service.bookingItemId) {
            console.error('Missing bookingItemId for service:', service);
            return null;
        }
        return service.bookingItemId;
    }).filter(id => id !== null);

    console.log('Booking Room IDs:', bookingRoomIds);
    console.log('Booking Item IDs:', bookingItemIds);

    // Kiểm tra xem có bookingRoomIds không
    if (bookingRoomIds.length === 0) {
        alert('No valid room bookings found. Please select a room first.');
        return;
    }

    // Kiểm tra xem có bookingRoomId nào là null hoặc undefined không
    if (bookingRoomIds.some(id => !id)) {
        alert('Invalid room booking data. Please try selecting rooms again.');
        return;
    }

    // Tạo request mới
    const request = {
        customerId: userId,
        bookingRoomIds: bookingRoomIds,
        bookingItemIds: bookingItemIds
    };

    console.log('Booking request:', request);

    // Thêm tiền tố "Bearer " vào token nếu chưa có
    const authToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    console.log('Using auth token:', authToken);

    $.ajax({
        url: '/booking/create',
        type: 'POST',
        headers: {
            'Authorization': authToken,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(request),
        success: function(response) {
            console.log('Booking response:', response);
            if (response && response.result) {
                // Hiển thị thông báo thành công
                Swal.fire({
                    title: 'Success!',
                    text: 'Your booking has been completed successfully.',
                    icon: 'success',
                    confirmButtonText: 'View My Bookings',
                    showCancelButton: true,
                    cancelButtonText: 'Stay Here'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // Xóa dữ liệu booking khỏi localStorage
                        localStorage.removeItem('selectedRooms');
                        localStorage.removeItem('selectedServices');
                        // Chuyển hướng đến trang my-booking
                        window.location.href = '/my-bookings.html';
                    }
                });
            } else {
                Swal.fire({
                    title: 'Error!',
                    text: 'Failed to complete booking. Please try again.',
                    icon: 'error'
                });
            }
        },
        error: function(xhr) {
            console.error('Error creating booking:', xhr);

            let errorMessage = 'An error occurred while creating your booking. Please try again.';

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

// Update summary when page loads
$(document).ready(function() {
    updateBookingSummary();

    // Update summary when storage changes
    $(window).on('storage', function() {
        updateBookingSummary();
    });
});

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