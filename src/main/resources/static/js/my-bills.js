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
    loadCustomerProfile();
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
const pageSize = 10;
let totalPages = 1;

// Format date to YYYY-MM-DD for API requests
function formatDateToYYYYMMDD(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0
    }).format(amount);
}

// Function to load customer profile and get customerId
async function loadCustomerProfile() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return null;
    }

    try {
        const response = await fetch('/customer/profile', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('token');
                window.location.href = '/login.html';
                return null;
            }
            throw new Error('Failed to load profile');
        }

        const data = await response.json();
        if (data && data.id) {
            localStorage.setItem('customerId', data.id);

            // Save customer name to localStorage
            if (data.name) {
                localStorage.setItem('customerName', data.name);
            }

            // Update profile menu with user information
            updateProfileMenu(data);

            // Load bills after getting customer ID
            loadBills(currentPage);

            return data.id;
        }
        return null;
    } catch (error) {
        console.error('Error loading profile:', error);
        return null;
    }
}

// Function to load bills
async function loadBills(page) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login.html';
            return;
        }

        // Get customerId from localStorage or load profile
        let customerId = localStorage.getItem('customerId');
        if (!customerId) {
            customerId = await loadCustomerProfile();
            if (!customerId) {
                throw new Error('Failed to get customer ID');
            }
        }

        // Hiển thị thông báo đang tải
        const billsList = document.getElementById('billsContainer');
        billsList.innerHTML = '<div class="text-center py-8"><i class="fas fa-spinner fa-spin text-3xl text-indigo-500"></i><p class="mt-4 text-gray-500">Đang tải danh sách hóa đơn...</p></div>';

        console.log(`Loading bills for customer: ${customerId}, page: ${page}`);
        const response = await fetch(`/bill/list/${customerId}?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            credentials: 'include',
            cache: 'no-cache' // Thêm cái này để đảm bảo không lấy dữ liệu từ cache
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('customerId');
                window.location.href = '/login.html';
                return;
            }
            throw new Error('Failed to load bills');
        }

        const data = await response.json();
        console.log('Bills data received:', data);

        const bills = data.result.content;
        totalPages = data.result.totalPages;

        // Update pagination info
        document.getElementById('pageInfo').textContent = `Trang ${page + 1} / ${totalPages}`;
        document.getElementById('prevPage').disabled = page === 0;
        document.getElementById('nextPage').disabled = page === totalPages - 1;

        // Clear existing bills
        billsList.innerHTML = '';

        // Add each bill to the list
        if (bills && bills.length > 0) {
            bills.forEach(bill => {
                const billCard = document.createElement('div');
                billCard.className = 'bill-card';

                // Format dates
                const issueDate = new Date(bill.issueDate);
                const paymentDate = new Date(bill.paymentDate);
                const formattedIssueDate = issueDate.toLocaleDateString('vi-VN');
                const formattedPaymentDate = paymentDate.toLocaleDateString('vi-VN');

                // Format amount
                const formattedAmount = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(bill.grandTotal);

                // Get customer name from localStorage
                const customerName = localStorage.getItem('customerName') || 'Khách hàng';

                // Calculate days remaining for paymentBill
                const today = new Date();
                const daysRemaining = Math.ceil((paymentDate - today) / (1000 * 60 * 60 * 24));
                const paymentStatus = daysRemaining > 0 ? `Còn ${daysRemaining} ngày` : 'Đã quá hạn!';

                billCard.innerHTML = `
                    <div class="right-border-dots"></div>
                    <div class="bill-header">
                        <div>
                            <span class="bill-id">#${bill.bookingBillId}</span>
                            <div class="bill-date">
                                <i class="fas fa-calendar-alt"></i>
                                <span>Ngày tạo: ${formattedIssueDate}</span>
                            </div>
                        </div>
                        <div class="hotel-logo">
                            <img src="https://cdn-icons-png.flaticon.com/512/2111/2111320.png" alt="Hotel Logo">
                        </div>
                    </div>
                    <div class="bill-body">
                        ${daysRemaining <= 3 ? `<div class="bill-badge">Thanh toán ngay!</div>` : ''}
                        <div class="flex justify-between items-start">
                            <div>
                                <h3 class="text-base font-semibold text-gray-900 mb-2">Chi tiết hóa đơn</h3>
                                <div class="bill-info-row">
                                    <i class="fas fa-clock"></i>
                                    <span class="text-xs ${daysRemaining <= 0 ? 'text-red-600 font-bold' : 'text-gray-600'}">
                                        Hạn thanh toán: ${formattedPaymentDate} (${paymentStatus})
                                    </span>
                                </div>
                                <div class="bill-info-row">
                                    <i class="fas fa-user"></i>
                                    <span class="text-xs text-gray-600">Khách hàng: ${customerName}</span>
                                </div>
                                <div class="bill-info-row">
                                    <i class="fas fa-calendar-alt"></i>
                                    <span class="text-xs text-gray-600">Ngày phát hành: ${formattedIssueDate}</span>
                                </div>
                            </div>
                            <div class="text-right">
                                <div class="bill-amount-label">Tổng tiền</div>
                                <div class="bill-price">${formattedAmount}</div>
                            </div>
                        </div>

                        <div class="bill-separator my-3"></div>

                        <div class="mb-4">
                            <div class="font-medium text-gray-700 mb-2">Thông tin thanh toán</div>
                            <div class="grid grid-cols-2 gap-3 bg-gray-50 p-2 rounded-lg">
                                <div>
                                    <p class="text-xs text-gray-500">Phương thức thanh toán</p>
                                    <p class="text-sm font-medium">ZaloPay</p>
                                </div>
                                <div>
                                    <p class="text-xs text-gray-500">Trạng thái</p>
                                    <p class="text-sm font-medium text-orange-500">Đang chờ thanh toán</p>
                                </div>
                            </div>
                        </div>

                        <div class="mb-3">
                            <div class="font-medium text-gray-700 mb-1">Điều khoản & Điều kiện</div>
                            <ul class="text-xs text-gray-500 list-disc list-inside">
                                <li>Vui lòng thanh toán đúng hạn để đảm bảo dịch vụ.</li>
                                <li>Số tiền đã bao gồm thuế VAT và phí dịch vụ.</li>
                            </ul>
                        </div>

                        <div class="bill-separator"></div>

                        <div class="flex items-center justify-between mt-3">
                            <div>
                                <p class="text-xs text-gray-500">
                                    <i class="fas fa-info-circle mr-1"></i>
                                    Thanh toán trước hạn để tránh mất đặt phòng
                                </p>
                            </div>
                            <button onclick="payBill('${bill.bookingBillId}')" class="pay-button">
                                <i class="fas fa-credit-card"></i>
                                <span>Thanh toán</span>
                            </button>
                        </div>
                    </div>
                    <div class="bill-footer">
                        <div class="text-xs text-gray-600">
                            <i class="fas fa-qrcode mr-1"></i>Quét mã để thanh toán
                        </div>
                        <div class="text-xs text-gray-600">
                            <i class="fas fa-hotel mr-1"></i>DinhRise Hotel
                        </div>
                    </div>
                `;

                billsList.appendChild(billCard);
            });
        } else {
            billsList.innerHTML = '<p class="text-center text-gray-500 py-8">Bạn chưa có hóa đơn nào</p>';
        }
    } catch (error) {
        console.error('Error loading bills:', error);
        //alert('Có lỗi xảy ra khi tải hóa đơn: ' + error.message);

        const billsList = document.getElementById('billsContainer');
        billsList.innerHTML = `
            <div class="bg-red-50 text-red-700 p-4 rounded-lg text-center">
                <p class="font-bold">Có lỗi xảy ra khi tải hóa đơn</p>
                <p class="mt-2">${error.message}</p>
                <button onclick="loadBills(currentPage)" class="mt-4 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition duration-300">
                    Thử lại
                </button>
            </div>
        `;
    }
}

// Function to show payment modal
function showPaymentModal(billId) {
    const modal = document.getElementById('paymentModal');
    const modalContent = document.getElementById('modalContent');

    // Find the bill card element
    const billCard = document.querySelector(`[onclick*="payBill('${billId}')"]`).closest('.bill-card');

    // Get the amount from the bill card
    const amountElement = billCard.querySelector('.bill-price');
    const amount = amountElement.textContent.replace(/[^\d]/g, ''); // Remove all non-digit characters

    // Store billId and amount for later use
    modal.setAttribute('data-bill-id', billId);
    localStorage.setItem('bookingBillAmount', amount);

    // Show modal with animation
    modal.classList.remove('hidden');
    modal.classList.add('flex');

    // Animate modal content
    setTimeout(() => {
        modalContent.classList.remove('scale-95', 'opacity-0');
        modalContent.classList.add('scale-100', 'opacity-100');
    }, 50);
}

// Function to close payment modal
function closePaymentModal() {
    const modal = document.getElementById('paymentModal');
    const modalContent = document.getElementById('modalContent');

    // Animate modal content
    modalContent.classList.remove('scale-100', 'opacity-100');
    modalContent.classList.add('scale-95', 'opacity-0');

    // Hide modal after animation
    setTimeout(() => {
        modal.classList.remove('flex');
        modal.classList.add('hidden');
    }, 300);
}

// Function to process payment based on selected method
async function processPayment(method) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login.html';
            return;
        }

        const modal = document.getElementById('paymentModal');
        const billId = modal.getAttribute('data-bill-id');

        console.log(`Processing ${method} payment for bill:`, billId);

        // Lưu thông tin vào localStorage
        localStorage.setItem('currentBookingBillId', billId);
        localStorage.setItem('selectedPaymentMethod', method.toUpperCase());

        // Show loading state
        const selectedOption = document.querySelector(`.payment-option[onclick*="${method}"]`);
        const originalContent = selectedOption.innerHTML;
        selectedOption.innerHTML = `
            <div class="flex items-center justify-center w-full">
                <i class="fas fa-spinner fa-spin text-indigo-600 text-xl"></i>
                <span class="ml-3">Đang xử lý...</span>
            </div>
        `;

        // Determine API endpoint based on payment method
        const endpoint = method === 'zalopay' ? '/zalopay/order' : '/paypal/order';

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ bookingBillId: billId }),
            credentials: 'include'
        });

        console.log('Payment API response:', response);

        // Reset loading state
        selectedOption.innerHTML = originalContent;

        if (!response.ok) {
            let errorMessage = 'Payment processing failed';
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                console.error('Error parsing error response:', e);
            }

            // Show error with SweetAlert2
            Swal.fire({
                icon: 'error',
                title: 'Lỗi Thanh Toán',
                html: `
                    <div class="text-left">
                        <p class="mb-2">Không thể xử lý thanh toán qua ${method === 'zalopay' ? 'ZaloPay' : 'PayPal'}.</p>
                        <p class="text-sm text-gray-600">${errorMessage}</p>
                        <div class="mt-4 p-3 bg-red-50 rounded-lg">
                            <p class="text-sm text-red-600">
                                <i class="fas fa-info-circle mr-2"></i>
                                Nếu lỗi vẫn tiếp tục, vui lòng:
                            </p>
                            <ul class="list-disc list-inside text-sm text-red-600 mt-2">
                                <li>Kiểm tra kết nối mạng</li>
                                <li>Thử lại sau vài phút</li>
                                <li>Liên hệ hỗ trợ nếu vẫn gặp vấn đề</li>
                            </ul>
                        </div>
                    </div>
                `,
                confirmButtonText: 'Đóng',
                confirmButtonColor: '#4F46E5'
            });

            throw new Error(`Failed to create ${method} order: ${errorMessage}`);
        }

        const data = await response.json();
        console.log('Payment response data:', data);

        if (data && data.result) {
            try {
                // Lưu số tiền vào localStorage
                if (method === 'zalopay') {
                    // Parse the JSON string for ZaloPay
                    const zaloPayResponse = JSON.parse(data.result);
                    console.log('Parsed ZaloPay response:', zaloPayResponse);

                    // Lưu số tiền từ ZaloPay response
                    if (zaloPayResponse.amount) {
                        localStorage.setItem('bookingBillAmount', zaloPayResponse.amount);
                    }

                    if (zaloPayResponse.return_code === 1 && zaloPayResponse.order_url) {
                        window.open(zaloPayResponse.order_url, '_blank');
                        closePaymentModal();
                        Swal.fire({
                            icon: 'info',
                            title: 'Thanh Toán ZaloPay',
                            text: 'Vui lòng hoàn tất thanh toán trên ZaloPay. Sau khi thanh toán, trang sẽ tự động cập nhật.',
                            confirmButtonColor: '#4F46E5'
                        });
                    } else {
                        throw new Error(zaloPayResponse.return_message || 'ZaloPay payment failed');
                    }
                } else {
                    // For PayPal
                    const paypalResponse = data.result;

                    // Lưu số tiền từ PayPal response
                    if (data.amount) {
                        localStorage.setItem('bookingBillAmount', data.amount);
                    }

                    if (typeof paypalResponse === 'string' && paypalResponse.includes('paypal.com')) {
                        window.open(paypalResponse, '_blank');
                        closePaymentModal();
                        Swal.fire({
                            icon: 'info',
                            title: 'Thanh Toán PayPal',
                            text: 'Vui lòng hoàn tất thanh toán trên PayPal. Sau khi thanh toán, trang sẽ tự động cập nhật.',
                            confirmButtonColor: '#4F46E5'
                        });
                    } else {
                        throw new Error('Invalid PayPal payment URL');
                    }
                }

                // Reload bills after 5 seconds
                setTimeout(() => {
                    loadBills(currentPage);
                }, 5000);

            } catch (parseError) {
                console.error(`Error parsing ${method} response:`, parseError);
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi Xử Lý',
                    text: `Không thể xử lý phản hồi từ ${method === 'zalopay' ? 'ZaloPay' : 'PayPal'}. Vui lòng thử lại sau.`,
                    confirmButtonColor: '#4F46E5'
                });
            }
        } else {
            throw new Error('Invalid payment response');
        }
    } catch (error) {
        console.error('Payment processing error:', error);
        // Clear payment data from localStorage on error
        localStorage.removeItem('currentBookingBillId');
        localStorage.removeItem('selectedPaymentMethod');
        localStorage.removeItem('bookingBillAmount');
    }
}

// Update the payBill function to show modal instead of direct payment
function payBill(billId) {
    showPaymentModal(billId);
}

// Event listeners for pagination
document.getElementById('prevPage').addEventListener('click', () => {
    if (currentPage > 0) {
        currentPage--;
        loadBills(currentPage);
    }
});

document.getElementById('nextPage').addEventListener('click', () => {
    if (currentPage < totalPages - 1) {
        currentPage++;
        loadBills(currentPage);
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

// Load initial bills when page loads
document.addEventListener('DOMContentLoaded', async () => {
    // Khi quay lại trang này sau khi thanh toán
    // kiểm tra nếu có bookingBillId trong localStorage thì xóa
    const bookingBillId = localStorage.getItem('currentBookingBillId');
    if (bookingBillId) {
        console.log('Found bookingBillId in localStorage:', bookingBillId);
        localStorage.removeItem('currentBookingBillId');

        // Đợi một chút để backend xử lý xong
        setTimeout(() => {
            loadBills(currentPage);
        }, 1000);
    }
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