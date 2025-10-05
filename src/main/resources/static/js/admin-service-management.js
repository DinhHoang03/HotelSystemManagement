// Global variables
let currentPage = 0;
const pageSize = 10;
const token = localStorage.getItem('token');

// Check authentication
if (!token) {
    window.location.href = '/login.html';
}

// Modal functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('opacity-0', 'pointer-events-none');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('opacity-0', 'pointer-events-none');
}

let deleteCallback = null;

function showConfirmationModal(message, callback) {
    const modal = document.getElementById('confirmationModal');
    document.getElementById('confirmationMessage').textContent = message;
    modal.classList.remove('opacity-0', 'pointer-events-none');
    deleteCallback = callback;
}

function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    modal.classList.add('opacity-0', 'pointer-events-none');
    deleteCallback = null;
}

document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
    if (deleteCallback) {
        await deleteCallback();
        closeConfirmationModal();
    }
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

// Close user menu when clicking outside
$(document).click(function(event) {
    if (!$(event.target).closest('#userMenuButton, #userMenu').length) {
        $('#userMenu').addClass('hidden');
    }
});

// API calls
async function fetchServices() {
    try {
        const response = await fetch(`https://localhost:8443/offer/list/?page=${currentPage}&size=${pageSize}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        return data.result.content;
    } catch (error) {
        console.error('Error fetching services:', error);
        return [];
    }
}

async function deleteService(serviceType) {
    showConfirmationModal(
        'Bạn có chắc chắn muốn xóa dịch vụ này không? Hành động này không thể hoàn tác.',
        async () => {
            try {
                await fetch(`https://localhost:8443/offer/del/${serviceType}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                await loadServices();
                await loadServiceStats();
            } catch (error) {
                console.error('Error deleting service:', error);
                alert('Failed to delete service');
            }
        }
    );
}

// UI update functions
async function loadServices() {
    const services = await fetchServices();
    const tableBody = document.getElementById('serviceTableBody');
    tableBody.innerHTML = services.map(service => `
        <tr class="table-row">
            <td class="px-6 py-4 whitespace-nowrap">${service.serviceType}</td>
            <td class="px-6 py-4 whitespace-nowrap">${service.price.toLocaleString('vi-VN')} VND</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <button onclick="deleteService('${service.serviceType}')" class="text-red-600 hover:text-red-900">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

async function loadServiceStats() {
    try {
        const services = await fetchServices();
        document.getElementById('totalServices').textContent = services.length;

        // For demo purposes - you might want to implement actual stats endpoints
        document.getElementById('mostUsedService').textContent = "Laundry";
        document.getElementById('totalRevenue').textContent = "5,000,000 VND";
    } catch (error) {
        console.error('Error loading service stats:', error);
    }
}

// Form handlers
async function handleServiceSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const serviceData = {
        serviceType: formData.get('serviceType'),
        price: parseInt(formData.get('price'))
    };

    try {
        const response = await fetch('https://localhost:8443/offer/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(serviceData)
        });

        if (response.ok) {
            closeModal('serviceModal');
            event.target.reset();
            await loadServices();
            await loadServiceStats();
        } else {
            const error = await response.json();
            alert(error.message || 'Failed to create service');
        }
    } catch (error) {
        console.error('Error creating service:', error);
        alert('Failed to create service');
    }
}

// Initialize page
window.onload = async () => {
    // Check admin authentication
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            throw new Error('Invalid token format');
        }

        const payload = JSON.parse(atob(tokenParts[1]));

        if (!payload.scope || payload.scope !== 'ROLE_ADMIN') {
            window.location.href = '/login.html';
            return;
        }

        $('#usernameDisplay').text(payload.sub || 'Admin');
    } catch (error) {
        console.error('Error validating admin token:', error);
        window.location.href = '/login.html';
        return;
    }

    await Promise.all([
        loadServices(),
        loadServiceStats()
    ]);
};