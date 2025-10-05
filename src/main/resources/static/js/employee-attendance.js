let currentPage = 0;
const pageSize = 10;
let isCheckedIn = false;
let currentCheckInTime = null;
let currentCheckOutTime = null;

// Initialize
$(document).ready(function() {
    checkAuth();
    initializePage();
    updateClock();
    loadAttendanceHistory();
    setInterval(updateClock, 1000);
});

function initializePage() {
    // Toggle sidebar
    $('#toggleSidebar').click(function() {
        $('#sidebar').toggleClass('collapsed');
        $('#mainContent').toggleClass('expanded');
    });

    // Toggle user menu
    $('#userMenuButton').click(function() {
        $('#userMenu').toggleClass('hidden');
    });

    // Initialize buttons
    $('#checkInBtn').click(handleCheckIn);
    $('#checkOutBtn').click(handleCheckOut);
    $('#prevPage').click(() => changePage(-1));
    $('#nextPage').click(() => changePage(1));

    // Initially disable check-out button
    $('#checkOutBtn').prop('disabled', true).addClass('opacity-50 cursor-not-allowed');
}

function updateClock() {
    const now = new Date();
    const timeString = now.toLocaleTimeString();
    const dateString = now.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    $('#clockDisplay').text(timeString);
    $('#dateDisplay').text(dateString);
}

function updateAttendanceStatus(status, time) {
    const statusBadge = document.getElementById('statusBadge');
    const lastCheckTime = document.getElementById('lastCheckTime');

    if (status === 'CHECKED_IN') {
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-medium bg-green-100 text-green-800';
        statusBadge.textContent = 'Checked In';
        isCheckedIn = true;
    } else if (status === 'CHECKED_OUT') {
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-medium bg-red-100 text-red-800';
        statusBadge.textContent = 'Checked Out';
        isCheckedIn = false;
    } else {
        statusBadge.className = 'px-4 py-2 rounded-full text-sm font-medium bg-gray-100 text-gray-800';
        statusBadge.textContent = 'Not Available';
    }

    if (time) {
        lastCheckTime.textContent = `Last action: ${time}`;
    }
}

function handleCheckIn() {
    if (isCheckedIn) {
        showError('You are already checked in!');
        return;
    }

    $.ajax({
        url: 'https://localhost:8443/employee/check-in',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            if (response.result) {
                showSuccess('Successfully checked in!');
                currentCheckInTime = response.result.checkInDate;
                updateAttendanceStatus('CHECKED_IN', response.result.checkInDate);

                // Disable check-in button and enable check-out button
                $('#checkInBtn').prop('disabled', true).addClass('opacity-50 cursor-not-allowed');
                $('#checkOutBtn').prop('disabled', false).removeClass('opacity-50 cursor-not-allowed');
            }
        },
        error: function(xhr) {
            showError('Failed to check in. Please try again.');
            console.error('Check-in error:', xhr);
        }
    });
}

function handleCheckOut() {
    if (!isCheckedIn) {
        showError('You need to check in first!');
        return;
    }

    $.ajax({
        url: 'https://localhost:8443/employee/check-out',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            if (response.result) {
                showSuccess('Successfully checked out!');
                currentCheckOutTime = response.result.checkOutDate;
                updateAttendanceStatus('CHECKED_OUT', response.result.checkOutDate);

                // Create attendance record with check-in and check-out times
                createAttendanceRecord(currentCheckInTime, currentCheckOutTime);

                // Enable check-in button and disable check-out button
                $('#checkInBtn').prop('disabled', false).removeClass('opacity-50 cursor-not-allowed');
                $('#checkOutBtn').prop('disabled', true).addClass('opacity-50 cursor-not-allowed');

                // Reset times
                currentCheckInTime = null;
                currentCheckOutTime = null;

                // Reload attendance history
                loadAttendanceHistory();
            }
        },
        error: function(xhr) {
            showError('Failed to check out. Please try again.');
            console.error('Check-out error:', xhr);
        }
    });
}

function createAttendanceRecord(checkInTime, checkOutTime) {
    if (!checkInTime || !checkOutTime) {
        showError('Missing check-in or check-out time');
        return;
    }

    const attendanceData = {
        checkIn: checkInTime,
        checkOut: checkOutTime
    };

    $.ajax({
        url: 'https://localhost:8443/employee/attendance/create',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(attendanceData),
        success: function(response) {
            if (response.result) {
                showSuccess('Attendance record created successfully!');
            }
        },
        error: function(xhr) {
            showError('Failed to create attendance record.');
            console.error('Create attendance error:', xhr);
        }
    });
}

function loadAttendanceHistory() {
    $.ajax({
        url: `https://localhost:8443/employee/list?page=${currentPage}&size=${pageSize}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(response) {
            if (response.result && response.result.content) {
                const attendanceHtml = response.result.content.map(record => `
                    <tr>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            ${new Date(record.checkIn).toLocaleDateString()}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            ${record.checkIn}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            ${record.checkOut}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            ${record.workHour} hours
                        </td>
                    </tr>
                `).join('');

                $('#attendanceHistory').html(attendanceHtml);

                // Update pagination buttons
                $('#prevPage').prop('disabled', currentPage === 0);
                $('#nextPage').prop('disabled', !response.result.hasNext);

                // Update summary
                updateAttendanceSummary(response.result.content);
            }
        },
        error: function(xhr) {
            showError('Failed to load attendance history.');
        }
    });
}

function updateAttendanceSummary(records) {
    if (!records || records.length === 0) {
        $('#totalWorkHours').text('0 hours');
        $('#daysPresent').text('0 days');
        $('#averageHours').text('0 hours/day');
        return;
    }

    const totalHours = records.reduce((sum, record) => sum + record.workHour, 0);
    const daysPresent = records.length;
    const averageHours = (totalHours / daysPresent).toFixed(1);

    $('#totalWorkHours').text(`${totalHours} hours`);
    $('#daysPresent').text(`${daysPresent} days`);
    $('#averageHours').text(`${averageHours} hours/day`);
}

function changePage(delta) {
    currentPage += delta;
    if (currentPage < 0) currentPage = 0;
    loadAttendanceHistory();
}

function showSuccess(message) {
    Swal.fire({
        icon: 'success',
        title: 'Success',
        text: message,
        timer: 2000,
        showConfirmButton: false
    });
}

function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Error',
        text: message
    });
}