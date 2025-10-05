// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Check authentication
$(document).ready(function() {
    checkAuth();
    loadDashboardData();
});

// Toggle sidebar
$('#toggleSidebar').click(function() {
    $('#sidebar').toggleClass('collapsed');
    $('#mainContent').toggleClass('expanded');
});

// Toggle user menu
$('#userMenuButton').click(function() {
    $('#userMenu').toggleClass('hidden');
});

// Load dashboard data
function loadDashboardData() {
    // Load available rooms
    $.ajax({
        url: '/api/rooms/available',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            $('#availableRooms').text(response.length);
        }
    });

    // Load today's check-ins
    $.ajax({
        url: '/api/bookings/today/checkins',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            $('#todayCheckins').text(response.length);
        }
    });

    // Load today's check-outs
    $.ajax({
        url: '/api/bookings/today/checkouts',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            $('#todayCheckouts').text(response.length);
        }
    });

    // Load pending tasks
    $.ajax({
        url: '/api/tasks/pending',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            $('#pendingTasks').text(response.length);
        }
    });

    // Load recent bookings
    $.ajax({
        url: '/api/bookings/recent',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            const bookingsHtml = response.map(booking => `
                <tr>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${booking.id}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${booking.guestName}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${booking.roomNumber}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${booking.checkInDate}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${booking.checkOutDate}</td>
                    <td class="px-6 py-4 whitespace-nowrap">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusClass(booking.status)}">
                            ${booking.status}
                        </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <button class="text-indigo-600 hover:text-indigo-900" onclick="handleBookingAction('${booking.id}')">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
            $('#recentBookings').html(bookingsHtml);
        }
    });

    // Load today's tasks
    $.ajax({
        url: '/api/tasks/today',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            const tasksHtml = response.map(task => `
                <div class="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div>
                        <h3 class="text-sm font-medium text-gray-900">${task.title}</h3>
                        <p class="text-sm text-gray-500">${task.description}</p>
                    </div>
                    <button class="text-green-600 hover:text-green-800" onclick="completeTask('${task.id}')">
                        <i class="fas fa-check"></i>
                    </button>
                </div>
            `).join('');
            $('#todayTasks').html(tasksHtml);
        }
    });

    // Load recent notifications
    $.ajax({
        url: '/api/notifications/recent',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            const notificationsHtml = response.map(notification => `
                <div class="flex items-start p-4 bg-gray-50 rounded-lg">
                    <div class="flex-shrink-0">
                        <i class="fas ${getNotificationIcon(notification.type)} text-${getNotificationColor(notification.type)}-600"></i>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm font-medium text-gray-900">${notification.title}</p>
                        <p class="text-sm text-gray-500">${notification.message}</p>
                        <p class="text-xs text-gray-400 mt-1">${notification.timestamp}</p>
                    </div>
                </div>
            `).join('');
            $('#recentNotifications').html(notificationsHtml);
        }
    });
}

// Helper functions
function getStatusClass(status) {
    switch(status) {
        case 'CHECKED_IN':
            return 'bg-green-100 text-green-800';
        case 'CHECKED_OUT':
            return 'bg-gray-100 text-gray-800';
        case 'PENDING':
            return 'bg-yellow-100 text-yellow-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
}

function getNotificationIcon(type) {
    switch(type) {
        case 'BOOKING':
            return 'fa-calendar-check';
        case 'TASK':
            return 'fa-tasks';
        case 'ALERT':
            return 'fa-exclamation-circle';
        default:
            return 'fa-bell';
    }
}

function getNotificationColor(type) {
    switch(type) {
        case 'BOOKING':
            return 'blue';
        case 'TASK':
            return 'green';
        case 'ALERT':
            return 'red';
        default:
            return 'gray';
    }
}

function handleBookingAction(bookingId) {
    // Implement booking action logic
    console.log('Handling booking action for:', bookingId);
}

function completeTask(taskId) {
    // Implement task completion logic
    console.log('Completing task:', taskId);
}