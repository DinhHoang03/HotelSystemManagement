// Variables to track current state
let currentTab = 'employees';
let currentEmployeeStatus = 'PENDING';
let currentCustomerStatus = 'ALL';
let currentPage = 0;
let pageSize = 10;
let totalPages = 1;
let accountChart;

// Initialize AOS
AOS.init({
    duration: 1000
});

// Authentication check for admin access
function checkAdminAuth() {
    const adminToken = localStorage.getItem('token');
    
    if (!adminToken) {
        // Redirect to login page if no admin token
        Swal.fire({
            title: 'Access Denied',
            text: 'You need to login as an admin to access this page.',
            icon: 'error',
            confirmButtonText: 'Go to Login',
            confirmButtonColor: '#4F46E5'
        }).then(() => {
            window.location.href = '/login';
        });
        return false;
    }
    
    try {
        // Decode token to check if user is admin
        const tokenParts = adminToken.split('.');
        const payload = JSON.parse(atob(tokenParts[1]));
        
        // Check if token has admin role (using scope instead of roles array)
        if (!payload.scope || payload.scope !== 'ROLE_ADMIN') {
            // Redirect to home page if not admin
            Swal.fire({
                title: 'Permission Denied',
                text: 'You do not have administrator privileges.',
                icon: 'error',
                confirmButtonText: 'Go to Home',
                confirmButtonColor: '#4F46E5'
            }).then(() => {
                window.location.href = '/';
            });
            return false;
        }
        
        // Set admin username in the UI
        $('#adminName').text(payload.sub);
        $('#sidebarAdminName').text(payload.sub);
        
        return true;
    } catch (error) {
        console.error('Error decoding token:', error);
        
        // Redirect to login page if token is invalid
        Swal.fire({
            title: 'Authentication Error',
            text: 'Your session is invalid. Please login again.',
            icon: 'error',
            confirmButtonText: 'Go to Login',
            confirmButtonColor: '#4F46E5'
        }).then(() => {
            window.location.href = '/login';
        });
        return false;
    }
}

// Admin logout function
function adminLogout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
}

// Document ready function
$(document).ready(function() {
    // Check admin authentication
    if (!checkAdminAuth()) return;
    
    // Initialize page
    initializePage();
    
    // Toggle sidebar
    $('#toggleSidebar').click(function() {
        $('#sidebar').toggleClass('-translate-x-full');
    });
    
    // Toggle user menu
    $('#userMenuButton').click(function() {
        $('#userMenu').toggleClass('hidden');
    });
});

// Initialize page with data
function initializePage() {
    // Load account statistics
    loadAccountStats();
    
    // Initialize chart with empty data (will be updated once data is loaded)
    initializeChart();
    
    // Default: Load employees with 'PENDING' status
    loadEmployees('PENDING', 0, 10);
    
    // Set up event listeners
    setupEventListeners();
}

// Set up event listeners
function setupEventListeners() {
    // Tab switching between employees and customers
    $('#employeesTab').click(function() {
        currentTab = 'employees';
        $(this).addClass('bg-indigo-600 text-white').removeClass('bg-white text-gray-700');
        $('#customersTab').addClass('bg-white text-gray-700').removeClass('bg-indigo-600 text-white');
        $('#employeeStatusFilter').removeClass('hidden');
        $('#customerStatusFilter').addClass('hidden');
        
        // Reset to page 1 when switching tabs
        loadEmployees(currentEmployeeStatus, 0, pageSize);
    });
    
    $('#customersTab').click(function() {
        currentTab = 'customers';
        $(this).addClass('bg-indigo-600 text-white').removeClass('bg-white text-gray-700');
        $('#employeesTab').addClass('bg-white text-gray-700').removeClass('bg-indigo-600 text-white');
        $('#employeeStatusFilter').addClass('hidden');
        $('#customerStatusFilter').removeClass('hidden');
        
        // Reset to page 1 when switching tabs
        loadCustomers(0, pageSize);
    });
    
    // Employee status filter buttons
    $('.status-filter').click(function() {
        const status = $(this).data('status');
        currentEmployeeStatus = status;
        
        // Update active button state
        $('.status-filter').removeClass('status-active bg-indigo-100 text-indigo-700').addClass('bg-gray-100 text-gray-700');
        $(this).addClass('status-active bg-indigo-100 text-indigo-700').removeClass('bg-gray-100 text-gray-700');
        
        // Reset to page 1 when changing filters
        loadEmployees(status, 0, pageSize);
    });
    
    // Pagination controls
    $('#prevPage').click(function() {
        if (currentPage > 0) {
            if (currentTab === 'employees') {
                loadEmployees(currentEmployeeStatus, currentPage - 1, pageSize);
            } else {
                loadCustomers(currentPage - 1, pageSize);
            }
        }
    });
    
    $('#nextPage').click(function() {
        if (currentPage < totalPages - 1) {
            if (currentTab === 'employees') {
                loadEmployees(currentEmployeeStatus, currentPage + 1, pageSize);
            } else {
                loadCustomers(currentPage + 1, pageSize);
            }
        }
    });
}

// Load account stats
function loadAccountStats() {
    const adminToken = localStorage.getItem('token');
    
    // Set loading state for all counters
    $('#totalUsers').text('Loading...');
    $('#totalCustomers').text('Loading...');
    $('#totalEmployees').text('Loading...');
    
    // Get total users count
    fetch('/admin/total-users', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load total users count');
        }
        return response.json();
    })
    .then(data => {
        $('#totalUsers').text(data.result);
    })
    .catch(error => {
        console.error('Error loading total users:', error);
        $('#totalUsers').text('Error');
    });
    
    // Get customers count
    fetch('/admin/count-customers', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load customers count');
        }
        return response.json();
    })
    .then(data => {
        $('#totalCustomers').text(data.result);
        // Update chart after we have customer count
        updateChart(data.result);
    })
    .catch(error => {
        console.error('Error loading customers count:', error);
        $('#totalCustomers').text('Error');
    });
    
    // Get employees count
    fetch('/admin/count-employees', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load employees count');
        }
        return response.json();
    })
    .then(data => {
        $('#totalEmployees').text(data.result);
        // Update chart after we have employee count
        updateChart(null, data.result);
    })
    .catch(error => {
        console.error('Error loading employees count:', error);
        $('#totalEmployees').text('Error');
    });
}

// Initialize chart
function initializeChart() {
    const ctx = document.getElementById('accountDistribution').getContext('2d');
    
    // Create an empty pie chart that will be updated when data is loaded
    accountChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Customers', 'Employees'],
            datasets: [{
                data: [0, 0], // Initial empty data
                backgroundColor: [
                    '#10B981', // Green for customers
                    '#4F46E5'  // Indigo for employees
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                title: {
                    display: true,
                    text: 'Loading data...',
                    font: {
                        size: 16
                    }
                }
            }
        }
    });
}

// Store customer and employee counts for chart update
let customerCount = 0;
let employeeCount = 0;

// Update chart with real data
function updateChart(customers, employees) {
    // Update stored counts if values are provided
    if (customers !== null && customers !== undefined) {
        customerCount = customers;
    }
    
    if (employees !== null && employees !== undefined) {
        employeeCount = employees;
    }
    
    // Only update chart if we have both counts
    if (customerCount > 0 || employeeCount > 0) {
        // Update chart data
        accountChart.data.datasets[0].data = [customerCount, employeeCount];
        
        // Update chart title
        accountChart.options.plugins.title.text = 'Account Distribution';
        
        // Update chart
        accountChart.update();
    }
}

// Load employees by status
function loadEmployees(status, page, size) {
    currentPage = page;
    pageSize = size;
    
    // Show loading state
    $('#accountsList').html(`
        <div class="flex items-center justify-center py-8">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
    `);
    
    const adminToken = localStorage.getItem('token');
    
    fetch(`/admin/get-employees/list?page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load employees');
        }
        return response.json();
    })
    .then(data => {
        const allEmployees = data.result.content;
        totalPages = data.result.totalPages;
        
        // Filter employees by status and exclude admin account
        const employees = allEmployees.filter(emp => {
            return emp.userStatus === status && emp.username.toLowerCase() !== "admin";
        });
        
        // Update pagination info
        updatePaginationInfo(page, size, employees.length, totalPages);
        
        if (employees && employees.length > 0) {
            // Render the employee list
            renderEmployeeList(employees);
        } else {
            $('#accountsList').html(`
                <div class="text-center py-8 text-gray-500">
                    No employees found with status: ${status}
                </div>
            `);
        }
    })
    .catch(error => {
        console.error('Error loading employees:', error);
        $('#accountsList').html(`
            <div class="text-center py-8 text-red-500">
                Failed to load employees: ${error.message}
            </div>
        `);
    });
}

// Load customers with pagination
function loadCustomers(page, size) {
    currentPage = page;
    pageSize = size;
    
    // Show loading state
    $('#accountsList').html(`
        <div class="flex items-center justify-center py-8">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
    `);
    
    const adminToken = localStorage.getItem('token');
    
    fetch(`/admin/get-customers/list?page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to load customers');
        }
        return response.json();
    })
    .then(data => {
        const customers = data.result.content;
        totalPages = data.result.totalPages;
        
        // Update pagination info
        updatePaginationInfo(page, size, data.result.totalElements, totalPages);
        
        if (customers && customers.length > 0) {
            // Render the customer list
            renderCustomerList(customers);
        } else {
            $('#accountsList').html(`
                <div class="text-center py-8 text-gray-500">
                    No customers found
                </div>
            `);
        }
    })
    .catch(error => {
        console.error('Error loading customers:', error);
        $('#accountsList').html(`
            <div class="text-center py-8 text-red-500">
                Failed to load customers: ${error.message}
            </div>
        `);
    });
}

// Render employee list
function renderEmployeeList(employees) {
    let html = `
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                    <tr>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Contact Info</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
    `;

    employees.forEach(employee => {
        // Determine badge class based on status
        let statusBadgeClass = '';
        switch(employee.userStatus) {
            case 'PENDING':
                statusBadgeClass = 'status-badge status-pending';
                break;
            case 'APPROVED':
                statusBadgeClass = 'status-badge status-approved';
                break;
            case 'REJECTED':
                statusBadgeClass = 'status-badge status-rejected';
                break;
            default:
                statusBadgeClass = 'status-badge bg-gray-100 text-gray-800';
        }

        // Format the role properly
        let roleName = 'No role';
        if (employee.role) {
            const roleMatch = employee.role.match(/\[Role\(name=([A-Z_]+)\)\]/);
            if (roleMatch && roleMatch[1]) {
                roleName = roleMatch[1];
            } else {
                roleName = employee.role;
            }
        }

        // Determine available actions based on status
        let actionsHtml = '';
        if (employee.userStatus === 'PENDING' || employee.userStatus === 'REJECTED') {
            actionsHtml += `<button onclick="approveEmployee('${employee.id}')" class="text-green-600 hover:text-green-900 mr-3"><i class="fas fa-check"></i> Approve</button>`;
        }
        if (employee.userStatus === 'PENDING' || employee.userStatus === 'APPROVED') {
            actionsHtml += `<button onclick="rejectEmployee('${employee.id}')" class="text-red-600 hover:text-red-900"><i class="fas fa-times"></i> Reject</button>`;
        }

        html += `
            <tr>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div class="flex-shrink-0 h-10 w-10">
                            <div class="h-10 w-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-500">
                                <i class="fas fa-user"></i>
                            </div>
                        </div>
                        <div class="ml-4">
                            <div class="text-sm font-medium text-gray-900">${employee.name || 'N/A'}</div>
                            <div class="text-sm text-gray-500">${employee.username}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${employee.email || 'No email'}</div>
                    <div class="text-sm text-gray-500">${employee.phone || 'No phone'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${roleName}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="${statusBadgeClass}">${employee.userStatus}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                    ${actionsHtml}
                </td>
            </tr>
        `;
    });

    html += `
                </tbody>
            </table>
        </div>
    `;

    $('#accountsList').html(html);
}

// Render customer list
function renderCustomerList(customers) {
    let html = `
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                    <tr>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Customer</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Contact Info</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Identity ID</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Address</th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
    `;

    customers.forEach(customer => {
        html += `
            <tr>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div class="flex-shrink-0 h-10 w-10">
                            <div class="h-10 w-10 rounded-full bg-green-100 flex items-center justify-center text-green-500">
                                <i class="fas fa-user"></i>
                            </div>
                        </div>
                        <div class="ml-4">
                            <div class="text-sm font-medium text-gray-900">${customer.name || 'N/A'}</div>
                            <div class="text-sm text-gray-500">${customer.username}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${customer.email || 'No email'}</div>
                    <div class="text-sm text-gray-500">${customer.phone || 'No phone'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    ${customer.identityId || 'N/A'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    ${customer.address || 'No address'}
                </td>
            </tr>
        `;
    });

    html += `
                </tbody>
            </table>
        </div>
    `;

    $('#accountsList').html(html);
}

// Update pagination info
function updatePaginationInfo(page, size, totalItems, totalPages) {
    const start = page * size + 1;
    const end = Math.min((page + 1) * size, totalItems);
    
    $('#pageStart').text(start);
    $('#pageEnd').text(end);
    $('#totalItems').text(totalItems);
    
    // Update page numbers
    let pageNumbersHtml = '';
    for (let i = Math.max(0, page - 2); i < Math.min(totalPages, page + 3); i++) {
        if (i === page) {
            pageNumbersHtml += `<button class="relative inline-flex items-center px-4 py-2 border border-indigo-500 bg-indigo-50 text-sm font-medium text-indigo-600">${i + 1}</button>`;
        } else {
            pageNumbersHtml += `<button onclick="goToPage(${i})" class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50">${i + 1}</button>`;
        }
    }
    $('#pageNumbers').html(pageNumbersHtml);
    
    // Disable/enable prev/next buttons
    $('#prevPage').prop('disabled', page === 0);
    $('#nextPage').prop('disabled', page === totalPages - 1);
    
    if (page === 0) {
        $('#prevPage').addClass('opacity-50 cursor-not-allowed');
    } else {
        $('#prevPage').removeClass('opacity-50 cursor-not-allowed');
    }
    
    if (page === totalPages - 1) {
        $('#nextPage').addClass('opacity-50 cursor-not-allowed');
    } else {
        $('#nextPage').removeClass('opacity-50 cursor-not-allowed');
    }
}

// Go to specific page
function goToPage(page) {
    currentPage = page;
    if (currentTab === 'employees') {
        loadEmployees(currentEmployeeStatus, page, pageSize);
    } else {
        loadCustomers(page, pageSize);
    }
}

// Approve employee
function approveEmployee(employeeId) {
    const adminToken = localStorage.getItem('token');
    
    // Show loading message
    Swal.fire({
        title: 'Processing...',
        text: 'Approving employee account',
        icon: 'info',
        showConfirmButton: false,
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });
    
    fetch(`/admin/approve/${employeeId}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to approve employee');
        }
        return response.json();
    })
    .then(data => {
        // Show success message
        Swal.fire({
            title: 'Success!',
            text: `Employee ${data.result.name} has been approved.`,
            icon: 'success',
            confirmButtonText: 'OK',
            confirmButtonColor: '#4F46E5'
        });
        
        // Reload employee list
        loadEmployees(currentEmployeeStatus, currentPage, pageSize);
    })
    .catch(error => {
        console.error('Error approving employee:', error);
        Swal.fire({
            title: 'Error!',
            text: `Failed to approve employee: ${error.message}`,
            icon: 'error',
            confirmButtonText: 'OK',
            confirmButtonColor: '#EF4444'
        });
    });
}

// Reject employee
function rejectEmployee(employeeId) {
    const adminToken = localStorage.getItem('token');
    
    // Show confirmation dialog
    Swal.fire({
        title: 'Are you sure?',
        text: 'Are you sure you want to reject this employee account?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, reject it',
        cancelButtonText: 'Cancel',
        confirmButtonColor: '#EF4444',
        cancelButtonColor: '#6B7280'
    }).then((result) => {
        if (result.isConfirmed) {
            // Show loading message
            Swal.fire({
                title: 'Processing...',
                text: 'Rejecting employee account',
                icon: 'info',
                showConfirmButton: false,
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });
            
            fetch(`/admin/reject/${employeeId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${adminToken}`,
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to reject employee');
                }
                return response.json();
            })
            .then(data => {
                // Show success message
                Swal.fire({
                    title: 'Success!',
                    text: `Employee ${data.result.name} has been rejected.`,
                    icon: 'success',
                    confirmButtonText: 'OK',
                    confirmButtonColor: '#4F46E5'
                });
                
                // Reload employee list
                loadEmployees(currentEmployeeStatus, currentPage, pageSize);
            })
            .catch(error => {
                console.error('Error rejecting employee:', error);
                Swal.fire({
                    title: 'Error!',
                    text: `Failed to reject employee: ${error.message}`,
                    icon: 'error',
                    confirmButtonText: 'OK',
                    confirmButtonColor: '#EF4444'
                });
            });
        }
    });
} 