// Global variables
let currentPage = 0;
const pageSize = 10;
const token = localStorage.getItem('token');
let totalPages = 0;
let totalElements = 0;

// Check authentication
if (!token) {
    window.location.href = '/login.html';
}

// Modal functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('opacity-0', 'pointer-events-none');
    if (modalId === 'roomModal') {
        populateRoomTypeSelect();
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('opacity-0', 'pointer-events-none');
}

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
async function fetchRoomTypes() {
    try {
        const response = await fetch(`https://localhost:8443/type/get-all/list`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        if (data && data.result && data.result.content) {
            return data.result.content;
        } else {
            console.error('Unexpected API response format:', data);
            return [];
        }
    } catch (error) {
        console.error('Error fetching room types:', error);
        return [];
    }
}

async function fetchRooms() {
    try {
        const response = await fetch(`https://localhost:8443/room/list/?page=${currentPage}&size=${pageSize}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        if (data && data.result) {
            // Update pagination info
            totalPages = data.result.totalPages;
            totalElements = data.result.totalElements;
            currentPage = data.result.number;
            
            // Update pagination controls
            updatePaginationControls();
            
            return data.result.content;
        } else {
            console.error('Unexpected API response format:', data);
            return [];
        }
    } catch (error) {
        console.error('Error fetching rooms:', error);
        return [];
    }
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

async function deleteRoomType(id) {
    showConfirmationModal(
        'Bạn có chắc chắn muốn xóa loại phòng này không? Hành động này không thể hoàn tác.',
        async () => {
            try {
                await fetch(`https://localhost:8443/type/del/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                await loadRoomTypes();
                await loadRoomStats();
            } catch (error) {
                console.error('Error deleting room type:', error);
                alert('Failed to delete room type');
            }
        }
    );
}

async function deleteRoom(id) {
    // Kiểm tra id hợp lệ
    if (!id || id === 'null' || id === 'undefined') {
        alert('Không thể xóa phòng này vì thiếu thông tin ID. Vui lòng làm mới trang và thử lại.');
        return;
    }
    
    console.log(`Attempting to delete room with ID: ${id}`);
    
    showConfirmationModal(
        'Bạn có chắc chắn muốn xóa phòng này không? Hành động này không thể hoàn tác.',
        async () => {
            try {
                const response = await fetch(`https://localhost:8443/room/del/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                console.log('Delete response status:', response.status);
                
                if (response.ok) {
                    console.log('Room successfully deleted');
                    await loadRooms();
                    await loadRoomStats();
                } else {
                    const responseText = await response.text();
                    console.error('Error deleting room:', responseText);
                    alert(`Failed to delete room: ${responseText}`);
                }
            } catch (error) {
                console.error('Error deleting room:', error);
                alert('Failed to delete room: Network error');
            }
        }
    );
}

// UI update functions
async function loadRoomTypes() {
    const roomTypes = await fetchRoomTypes();
    const tableBody = document.getElementById('roomTypeTableBody');
    tableBody.innerHTML = roomTypes.map(type => `
        <tr class="table-row">
            <td class="px-6 py-4 whitespace-nowrap">${type.roomTypes}</td>
            <td class="px-6 py-4 whitespace-nowrap">${type.halfDayPrice.toLocaleString('vi-VN')} VND</td>
            <td class="px-6 py-4 whitespace-nowrap">${type.fullDayPrice.toLocaleString('vi-VN')} VND</td>
            <td class="px-6 py-4 whitespace-nowrap">${type.fullWeekPrice.toLocaleString('vi-VN')} VND</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <button onclick="deleteRoomType(${type.roomTypeId})" class="text-red-600 hover:text-red-900">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

async function loadRooms() {
    const rooms = await fetchRooms();
    
    // Debug: kiểm tra dữ liệu phòng
    console.log('Loaded rooms:', rooms);
    if (rooms.length > 0) {
        console.log('Sample room data:', rooms[0]);
    }
    
    const tableBody = document.getElementById('roomTableBody');
    tableBody.innerHTML = rooms.map(room => {
        // Debug: log từng phòng để tìm vấn đề
        console.log(`Room: ${room.roomNumber}, ID=${room.roomId}`);
        
        return `
        <tr class="table-row">
            <td class="px-6 py-4 whitespace-nowrap">${room.roomNumber}</td>
            <td class="px-6 py-4 whitespace-nowrap">${room.roomType}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                    ${getStatusColor(room.roomStatus)}">
                    ${room.roomStatus}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <button onclick="deleteRoom(${room.roomId || 'null'})" class="text-red-600 hover:text-red-900">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
        `;
    }).join('');
}

async function loadRoomStats() {
    try {
        const response = await fetch('https://localhost:8443/admin/count-rooms', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        document.getElementById('totalRooms').textContent = data.result;

        // Get available and occupied rooms count
        const rooms = await fetchRooms();
        const availableCount = rooms.filter(room => room.roomStatus === 'AVAILABLE').length;
        const occupiedCount = rooms.filter(room => room.roomStatus === 'OCCUPIED').length;

        document.getElementById('availableRooms').textContent = availableCount;
        document.getElementById('occupiedRooms').textContent = occupiedCount;
    } catch (error) {
        console.error('Error loading room stats:', error);
    }
}

function getStatusColor(status) {
    switch (status) {
        case 'AVAILABLE':
            return 'bg-green-100 text-green-800';
        case 'OCCUPIED':
            return 'bg-red-100 text-red-800';
        case 'CLEANING':
            return 'bg-yellow-100 text-yellow-800';
        case 'NEED_CLEANING':
            return 'bg-orange-100 text-orange-800';
        case 'UNDER_MAINTENANCE':
            return 'bg-gray-100 text-gray-800';
        default:
            return 'bg-gray-100 text-gray-800';
    }
}

async function populateRoomTypeSelect() {
    const roomTypes = await fetchRoomTypes();
    const select = document.getElementById('roomType');
    
    if (roomTypes && roomTypes.length > 0) {
        console.log('Available room types:', roomTypes);
        // Hiển thị chi tiết từng loại phòng để debug
        roomTypes.forEach(type => {
            console.log(`Room type: id=${type.roomTypeId}, name=${type.roomTypes}`);
        });
        
        // Store room types for later use
        window.availableRoomTypes = roomTypes;
        
        // Use roomTypeId as the value to guarantee correct match with backend
        select.innerHTML = roomTypes.map(type => 
            `<option value="${type.roomTypeId}" data-name="${type.roomTypes}">${type.roomTypes}</option>`
        ).join('');
    } else {
        console.error('No room types available for dropdown');
        select.innerHTML = '<option value="">No room types available</option>';
    }
}

// Form handlers
async function handleRoomTypeSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const roomTypeData = {
        roomTypes: formData.get('roomTypes'),
        halfDayPrice: parseInt(formData.get('halfDayPrice')),
        fullDayPrice: parseInt(formData.get('fullDayPrice')),
        fullWeekPrice: parseInt(formData.get('fullWeekPrice'))
    };

    try {
        const response = await fetch('https://localhost:8443/type/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(roomTypeData)
        });

        if (response.ok) {
            closeModal('roomTypeModal');
            event.target.reset();
            await loadRoomTypes();
            await loadRoomStats();
        } else {
            const error = await response.json();
            alert(error.message || 'Failed to create room type');
        }
    } catch (error) {
        console.error('Error creating room type:', error);
        alert('Failed to create room type');
    }
}

async function handleRoomSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    
    // Get the room type ID from the form's select
    const roomTypeId = formData.get('roomType');
    
    // Find the room type from our global list by ID
    const roomTypeObj = window.availableRoomTypes.find(type => type.roomTypeId == roomTypeId);
    
    if (!roomTypeObj) {
        alert('Room type not found! Please select a valid room type.');
        return;
    }
    
    // Use the exact roomType value from database
    const roomTypeName = roomTypeObj.roomTypes;
    
    console.log(`Selected room type: ID=${roomTypeId}, ExactDatabaseName=${roomTypeName}`);
    
    const roomData = {
        roomNumber: formData.get('roomNumber'),
        roomType: roomTypeName // Use EXACT format from database
    };
    
    console.log('Sending room data:', roomData);

    try {
        console.log('Token being sent:', token);
        
        const response = await fetch('https://localhost:8443/room/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(roomData)
        });

        console.log('Response status:', response.status);
        const responseText = await response.text();
        console.log('Response body:', responseText);
        
        if (response.ok) {
            closeModal('roomModal');
            event.target.reset();
            await loadRooms();
            await loadRoomStats();
        } else {
            try {
                const error = JSON.parse(responseText);
                alert(error.message || 'Failed to create room');
            } catch(e) {
                alert(`Failed to create room: ${responseText}`);
            }
        }
    } catch (error) {
        console.error('Error creating room:', error);
        alert('Failed to create room');
    }
}

function updatePaginationControls() {
    const paginationContainer = document.getElementById('paginationControls');
    if (!paginationContainer) return;

    let paginationHTML = `
        <div class="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6">
            <div class="flex justify-between flex-1 sm:hidden">
                <button onclick="changePage(${currentPage - 1})" 
                    class="relative inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                    ${currentPage === 0 ? 'disabled' : ''}>
                    Previous
                </button>
                <button onclick="changePage(${currentPage + 1})"
                    class="relative inline-flex items-center px-4 py-2 ml-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                    ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                    Next
                </button>
            </div>
            <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                    <p class="text-sm text-gray-700">
                        Showing <span class="font-medium">${(currentPage * pageSize) + 1}</span> to 
                        <span class="font-medium">${Math.min((currentPage + 1) * pageSize, totalElements)}</span> of 
                        <span class="font-medium">${totalElements}</span> results
                    </p>
                </div>
                <div>
                    <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                        <button onclick="changePage(${currentPage - 1})"
                            class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                            ${currentPage === 0 ? 'disabled' : ''}>
                            <span class="sr-only">Previous</span>
                            <i class="fas fa-chevron-left"></i>
                        </button>
                        ${generatePageNumbers()}
                        <button onclick="changePage(${currentPage + 1})"
                            class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                            ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                            <span class="sr-only">Next</span>
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </nav>
                </div>
            </div>
        </div>
    `;
    paginationContainer.innerHTML = paginationHTML;
}

function generatePageNumbers() {
    let pageNumbers = '';
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        pageNumbers += `
            <button onclick="changePage(${i})"
                class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium 
                ${i === currentPage ? 'text-indigo-600 bg-indigo-50' : 'text-gray-700 hover:bg-gray-50'}">
                ${i + 1}
            </button>
        `;
    }
    return pageNumbers;
}

async function changePage(newPage) {
    if (newPage >= 0 && newPage < totalPages) {
        currentPage = newPage;
        await loadRooms();
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
        loadRoomTypes(),
        loadRooms(),
        loadRoomStats()
    ]);
}; 