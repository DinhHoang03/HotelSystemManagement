// Utility function to format date to YYYY-MM-DD
function formatDateToYYYYMMDD(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Utility function to format currency (VND)
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(amount);
}

// Document ready function - entry point
$(document).ready(function() {
    // Check if admin is logged in
    if (!checkAdminAuthentication()) {
        window.location.href = 'login.html';
        return;
    }

    // Initialize animations
    AOS.init();

    // Load initial dashboard data
    loadDashboardData();

    // Setup automatic refresh
    setupDailyRefresh();
    setupChartRefresh();

    // Setup click outside to close profile menu
    $(document).click(function(event) {
        if (!$(event.target).closest('#userMenuButton, #userMenu').length) {
            $('#userMenu').addClass('hidden');
        }
    });
});

// Function to load all dashboard data
function loadDashboardData() {
    loadEmployeeCount();
    loadRoomCount();
    loadTodayBookings();
    loadTodayRevenue();
    initializeCharts();
}

// Load employee count
function loadEmployeeCount() {
    const token = localStorage.getItem('token');
    if (!token) {
        $('#totalEmployeesCount').text('Auth Error');
        return;
    }

    fetch('https://localhost:8443/admin/count-employees', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch employee count: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result !== undefined) {
            $('#totalEmployeesCount').text(data.result);
        }
    })
    .catch(error => {
        console.error('Error fetching employee count:', error);
        $('#totalEmployeesCount').text('Error');
    });
}

// Load room count
function loadRoomCount() {
    const token = localStorage.getItem('token');
    if (!token) {
        $('#totalRoomsCount').text('Auth Error');
        return;
    }

    fetch('https://localhost:8443/admin/count-rooms', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch room count: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result !== undefined) {
            $('#totalRoomsCount').text(data.result);
        }
    })
    .catch(error => {
        console.error('Error fetching room count:', error);
        $('#totalRoomsCount').text('Error');
    });
}

// Load today's bookings with current date
function loadTodayBookings() {
    const token = localStorage.getItem('token');
    if (!token) {
        $('#todayBookingsCount').text('Auth Error');
        return;
    }

    const today = formatDateToYYYYMMDD(new Date());

    fetch(`https://localhost:8443/admin/today-bookings?date=${today}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch today bookings: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result !== undefined) {
            $('#todayBookingsCount').text(data.result);
        }
    })
    .catch(error => {
        console.error('Error fetching today bookings:', error);
        $('#todayBookingsCount').text('Error');
    });
}

// Load today's revenue with current date
function loadTodayRevenue() {
    const token = localStorage.getItem('token');
    if (!token) {
        $('#todayRevenueCount').text('Auth Error');
        return;
    }

    const today = formatDateToYYYYMMDD(new Date());

    // Try POST method with date in body
    fetch('https://localhost:8443/admin/today-revenue', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ now: today })
    })
    .then(response => {
        if (!response.ok) {
            // Fallback to GET if POST fails
            return tryGetTodayRevenue();
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result !== undefined) {
            $('#todayRevenueCount').text(formatCurrency(data.result));
        }
    })
    .catch(error => {
        console.error('Error fetching today revenue:', error);
        $('#todayRevenueCount').text('Error');
    });
}

// Fallback GET method for today's revenue
function tryGetTodayRevenue() {
    const token = localStorage.getItem('token');
    if (!token) return Promise.reject('No token');

    const today = formatDateToYYYYMMDD(new Date());

    return fetch(`https://localhost:8443/admin/today-revenue?now=${today}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('GET method failed: ' + response.status);
        }
        return response.json();
    });
}

// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
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

// Initialize charts
function initializeCharts() {
    // Get the current date to determine the default year and month range
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();

    // Create revenue chart (will be updated with API data)
    const revenueCtx = document.getElementById('revenueChart').getContext('2d');
    const revenueChart = new Chart(revenueCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Revenue',
                data: [],
                borderColor: '#4F46E5',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });

    // Load revenue data from API
    loadRevenueData(revenueChart, currentYear, 1, 6);

    // Create Occupancy Chart (will be updated with API data)
    const occupancyCtx = document.getElementById('occupancyChart').getContext('2d');
    const occupancyChart = new Chart(occupancyCtx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Occupancy Rate (%)',
                data: [],
                backgroundColor: '#10B981'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        callback: function(value) {
                            return value + '%';
                        }
                    }
                }
            }
        }
    });

    // Load occupancy data from API
    loadOccupancyData(occupancyChart);
}

// Load revenue data from API
function loadRevenueData(chart, year, startMonth, endMonth) {
    const token = localStorage.getItem('token');
    if (!token) {
        chart.data.labels = ['Auth Error'];
        chart.data.datasets[0].data = [0];
        chart.update();
        return;
    }

    // Show loading state in chart
    chart.data.labels = ['Loading...'];
    chart.data.datasets[0].data = [0];
    chart.update();

    // Call the revenue API
    fetch(`https://localhost:8443/admin/revenue?year=${year}&startMonth=${startMonth}&endMonth=${endMonth}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load revenue data');
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result) {
            const revenueData = data.result;
            const labels = Object.keys(revenueData);
            const values = Object.values(revenueData);

            // Update chart with real data
            chart.data.labels = labels;
            chart.data.datasets[0].data = values;
            chart.update();
        }
    })
    .catch(error => {
        console.error('Error loading revenue data:', error);
        chart.data.labels = ['Error'];
        chart.data.datasets[0].data = [0];
        chart.update();
    });
}

// Load occupancy data from API
function loadOccupancyData(chart) {
    const token = localStorage.getItem('token');
    if (!token) {
        chart.data.labels = ['Auth Error'];
        chart.data.datasets[0].data = [0];
        chart.update();
        return;
    }

    // Show loading state in chart
    chart.data.labels = ['Loading...'];
    chart.data.datasets[0].data = [0];
    chart.update();

    // Call the occupancy API
    fetch('https://localhost:8443/admin/room-occupancy', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load occupancy data');
        }
        return response.json();
    })
    .then(data => {
        if (data && data.result) {
            const occupancyData = data.result;
            const labels = Object.keys(occupancyData);
            const values = Object.values(occupancyData);

            // Update chart with real data
            chart.data.labels = labels;
            chart.data.datasets[0].data = values;
            chart.update();
        }
    })
    .catch(error => {
        console.error('Error loading occupancy data:', error);
        chart.data.labels = ['Error'];
        chart.data.datasets[0].data = [0];
        chart.update();
    });
}

// Check admin authentication
function checkAdminAuthentication() {
    const token = localStorage.getItem('token');

    if (!token) {
        // Redirect to login if token doesn't exist
        window.location.href = 'login.html';
        return false;
    }

    try {
        // Decode JWT token to check admin role
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            throw new Error('Invalid token format');
        }

        const payload = JSON.parse(atob(tokenParts[1]));

        // Check if token has admin role (scope field)
        if (!payload.scope || payload.scope !== 'ROLE_ADMIN') {
            // Redirect if not admin
            window.location.href = 'login.html';
            return false;
        }

        // Set admin username in header from token
        $('#usernameDisplay').text(payload.sub || 'Admin');

        return true;
    } catch (error) {
        console.error('Error validating admin token:', error);
        // Redirect to login if token is invalid
        window.location.href = 'login.html';
        return false;
    }
}

// Setup daily refresh to update statistics
function setupDailyRefresh() {
    // Calculate time until next day (midnight)
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);

    const timeUntilMidnight = tomorrow - now;

    // Schedule refresh at midnight
    setTimeout(() => {
        loadDashboardData();
        // After first execution, set up a daily interval
        setInterval(loadDashboardData, 24 * 60 * 60 * 1000);
    }, timeUntilMidnight);

    console.log(`Dashboard will refresh in ${Math.floor(timeUntilMidnight / 60000)} minutes at midnight`);
}

// Setup chart refresh (every hour)
function setupChartRefresh() {
    // Update charts every hour
    setInterval(() => {
        const currentDate = new Date();
        const currentYear = currentDate.getFullYear();

        // Reinitialize charts with fresh data
        const revenueChart = Chart.getChart('revenueChart');
        const occupancyChart = Chart.getChart('occupancyChart');

        if (revenueChart) {
            loadRevenueData(revenueChart, currentYear, 1, 12);
        }

        if (occupancyChart) {
            loadOccupancyData(occupancyChart);
        }
    }, 60 * 60 * 1000); // Every hour
}

// Helper functions
function getActivityIcon(type) {
    switch(type) {
        case 'EMPLOYEE':
            return 'fa-user';
        case 'BOOKING':
            return 'fa-calendar-check';
        case 'ROOM':
            return 'fa-bed';
        case 'SERVICE':
            return 'fa-concierge-bell';
        default:
            return 'fa-bell';
    }
}

function getActivityColor(type) {
    switch(type) {
        case 'EMPLOYEE':
            return 'blue';
        case 'BOOKING':
            return 'green';
        case 'ROOM':
            return 'yellow';
        case 'SERVICE':
            return 'purple';
        default:
            return 'gray';
    }
}

function getNotificationIcon(type) {
    switch(type) {
        case 'SYSTEM':
            return 'fa-server';
        case 'ALERT':
            return 'fa-exclamation-circle';
        case 'UPDATE':
            return 'fa-sync';
        default:
            return 'fa-bell';
    }
}

function getNotificationColor(type) {
    switch(type) {
        case 'SYSTEM':
            return 'blue';
        case 'ALERT':
            return 'red';
        case 'UPDATE':
            return 'yellow';
        default:
            return 'gray';
    }
}

// Function to set loading state for stats cards
function setLoadingState(elementId, isLoading) {
    const element = document.getElementById(elementId);
    if (!element) return;

    if (isLoading) {
        element.innerHTML = `
            <div class="flex justify-center items-center h-full">
                <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
            </div>
        `;
    } else {
        // Reset the loading state if needed
    }
}