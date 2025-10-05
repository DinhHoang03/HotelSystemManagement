/**
 * Payment Success Page - Specific handling for successful payments
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize page handler
    const paymentSuccessHandler = new PaymentHandler('success');
});

// Lấy thông tin từ localStorage
const bookingBillId = localStorage.getItem('currentBookingBillId');
const selectedPaymentMethod = localStorage.getItem('selectedPaymentMethod');
const bookingBillAmount = localStorage.getItem('bookingBillAmount');

// Cập nhật thông tin lên giao diện
document.getElementById('orderId').textContent = bookingBillId || 'N/A';
document.getElementById('amount').textContent = bookingBillAmount ?
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(bookingBillAmount) : 'N/A';

// Cập nhật phương thức thanh toán
const paymentMethodElement = document.querySelector('.flex.justify-between.items-center.py-2.border-b.border-gray-200:nth-child(3) .font-medium');
paymentMethodElement.textContent = selectedPaymentMethod === 'PAYPAL' ? 'PayPal' : 'ZaloPay';

// Cập nhật thời gian thanh toán
document.getElementById('paymentTime').textContent = new Date().toLocaleString('vi-VN');

// Xử lý chức năng xóa bill khi nhấp vào nút điều hướng
document.getElementById('viewBookingsBtn').addEventListener('click', function(e) {
    e.preventDefault();
    deleteBillAndRedirect('/my-bookings.html');
});

document.getElementById('homeBtn').addEventListener('click', function(e) {
    e.preventDefault();
    deleteBillAndRedirect('/customer-dashboard.html');
});

// Hàm xóa bill và chuyển hướng
function deleteBillAndRedirect(url) {
    const billId = bookingBillId;
    console.log('Attempting to delete bill ID:', billId);

    if (!billId) {
        console.error('No bill ID found in localStorage');
        alert('Không tìm thấy ID hóa đơn để xóa');
        window.location.href = url;
        return;
    }

    // Xóa thông tin thanh toán khỏi localStorage sau khi hoàn tất
    function clearPaymentData() {
        localStorage.removeItem('currentBookingBillId');
        localStorage.removeItem('selectedPaymentMethod');
        localStorage.removeItem('bookingBillAmount');
    }

    const token = localStorage.getItem('token');
    if (!token) {
        console.error('No authentication token found');
        alert('Bạn cần đăng nhập lại để hoàn tất quá trình này');
        clearPaymentData();
        window.location.href = '/login.html';
        return;
    }

    // Hiển thị thông báo đang xử lý
    const successElement = document.querySelector('.success-animation');
    if (successElement) {
        successElement.style.opacity = '0.5';
    }

    // Thông báo đang xử lý
    const processingMessage = document.createElement('div');
    processingMessage.innerHTML = `
        <div style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
                    background: rgba(0,0,0,0.8); color: white; padding: 20px; border-radius: 10px;
                    z-index: 9999; text-align: center;">
            <p>Đang xóa hóa đơn...</p>
        </div>
    `;
    document.body.appendChild(processingMessage);

    fetch(`https://localhost:8443/bill/del/${billId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Server returned status: ' + response.status);
        }
        return response.text().then(text => {
            return text ? JSON.parse(text) : {};
        });
    })
    .then(data => {
        console.log('Delete bill success:', data);
        clearPaymentData();
        window.location.href = url;
    })
    .catch(error => {
        console.error('Error deleting bill:', error);
        const errorDialog = document.createElement('div');
        errorDialog.innerHTML = `
            <div style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
                        background: white; padding: 20px; border-radius: 10px; max-width: 80%;
                        z-index: 9999; text-align: left; box-shadow: 0 0 20px rgba(0,0,0,0.5);">
                <h3 style="color: red; margin-top: 0;">Lỗi khi xóa hóa đơn</h3>
                <p>${error.message}</p>
                <p>Bill ID: ${billId}</p>
                <pre style="background: #f0f0f0; padding: 10px; overflow: auto; max-height: 200px;">${error.stack || 'No stack trace available'}</pre>
                <div style="text-align: right; margin-top: 15px;">
                    <button id="errorCloseBtn" style="padding: 8px 15px; background: #4F46E5; color: white; border: none; border-radius: 5px; cursor: pointer;">Đóng</button>
                    <button id="errorContinueBtn" style="padding: 8px 15px; background: #22c55e; color: white; border: none; border-radius: 5px; margin-left: 10px; cursor: pointer;">Tiếp tục</button>
                </div>
            </div>
        `;
        document.body.appendChild(errorDialog);

        document.getElementById('errorCloseBtn').addEventListener('click', function() {
            document.body.removeChild(errorDialog);
        });

        document.getElementById('errorContinueBtn').addEventListener('click', function() {
            document.body.removeChild(errorDialog);
            clearPaymentData();
            window.location.href = url;
        });
    })
    .finally(() => {
        if (processingMessage && processingMessage.parentNode) {
            document.body.removeChild(processingMessage);
        }
    });
}

// Thêm hiệu ứng khi trang được tải
document.addEventListener('DOMContentLoaded', () => {
    const elements = document.querySelectorAll('.success-animation');
    elements.forEach((element, index) => {
        setTimeout(() => {
            element.style.opacity = '1';
        }, index * 200);
    });
});