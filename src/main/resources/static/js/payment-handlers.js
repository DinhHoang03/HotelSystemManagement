/**
 * Payment Handlers - Common JavaScript for payment pages
 */

// Utility functions
function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
    const results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function formatCurrency(amount) {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
    }).format(amount);
}

function formatDateTime(dateTime) {
    if (!dateTime) return new Date().toLocaleString('vi-VN');
    return new Date(dateTime).toLocaleString('vi-VN');
}

// Animation handlers
function initAnimations() {
    document.addEventListener('DOMContentLoaded', () => {
        const elements = document.querySelectorAll('.animation-card');
        elements.forEach((element, index) => {
            setTimeout(() => {
                element.style.opacity = '1';
            }, index * 200);
        });
    });
}

// Common payment handling
class PaymentHandler {
    constructor(pageType) {
        this.pageType = pageType; // 'success', 'cancel', or 'error'
        this.bookingBillId = localStorage.getItem('currentBookingBillId');
        this.urlParams = new URLSearchParams(window.location.search);
        
        // Initialize page based on type
        this.initPage();
        this.setupEventListeners();
        initAnimations();
    }

    initPage() {
        // Common initialization
        if (this.pageType === 'success') {
            this.initSuccessPage();
        } else if (this.pageType === 'cancel') {
            this.initCancelPage();
        } else if (this.pageType === 'error') {
            this.initErrorPage();
        }
    }

    initSuccessPage() {
        const orderId = this.urlParams.get('orderId');
        const amount = this.urlParams.get('amount');
        const paymentTime = this.urlParams.get('paymentTime');
        
        document.getElementById('orderId').textContent = this.bookingBillId || orderId || 'N/A';
        document.getElementById('amount').textContent = formatCurrency(amount);
        document.getElementById('paymentTime').textContent = formatDateTime(paymentTime);
    }

    initCancelPage() {
        document.getElementById('cancelTime').textContent = formatDateTime();
    }

    initErrorPage() {
        const errorCode = this.urlParams.get('errorCode');
        const errorMessage = this.urlParams.get('errorMessage');
        
        document.getElementById('orderId').textContent = this.bookingBillId || 'N/A';
        document.getElementById('errorTime').textContent = formatDateTime();
        
        if (errorCode) {
            document.getElementById('errorTitle').textContent = `Lỗi mã: ${errorCode}`;
        }
        
        if (errorMessage) {
            document.getElementById('errorDetail').textContent = errorMessage;
        }
    }

    setupEventListeners() {
        // Handle retry button for cancel and error pages
        const retryBtn = document.getElementById('retryBtn');
        if (retryBtn) {
            retryBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (!this.bookingBillId) {
                    window.location.href = '/payment.html';
                    return;
                }
                window.location.href = `/payment.html?billId=${this.bookingBillId}`;
            });
        }
        
        // Handle home button for all pages
        const homeBtn = document.getElementById('homeBtn');
        if (homeBtn) {
            homeBtn.addEventListener('click', (e) => {
                e.preventDefault();
                window.location.href = '/customer-dashboard.html';
            });
        }
        
        // Handle view bookings button for success page
        const viewBookingsBtn = document.getElementById('viewBookingsBtn');
        if (viewBookingsBtn) {
            viewBookingsBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.deleteBillAndRedirect('/my-bookings.html');
            });
        }
    }

    deleteBillAndRedirect(url) {
        const billId = this.bookingBillId || this.urlParams.get('orderId');
        console.log('Attempting to delete bill ID:', billId);
        
        if (!billId) {
            console.error('No bill ID found in localStorage or URL parameters');
            alert('Không tìm thấy ID hóa đơn để xóa');
            window.location.href = url;
            return;
        }
        
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No authentication token found');
            alert('Bạn cần đăng nhập lại để hoàn tất quá trình này');
            window.location.href = '/login.html';
            return;
        }
        
        // Hiển thị thông báo đang xử lý
        const successElement = document.querySelector('.animation-card');
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
        
        console.log(`Sending DELETE request to: https://localhost:8443/bill/del/${billId}`);
        
        // Gọi API xóa bill
        fetch(`https://localhost:8443/bill/del/${billId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            console.log('Delete bill response status:', response.status);
            
            if (!response.ok) {
                throw new Error('Server returned status: ' + response.status);
            }
            
            return response.text().then(text => {
                return text ? JSON.parse(text) : {};
            }).catch(e => {
                console.log('Response is not JSON or is empty:', e);
                return {};
            });
        })
        .then(data => {
            console.log('Delete bill success data:', data);
            // Xóa bookingBillId khỏi localStorage
            localStorage.removeItem('currentBookingBillId');
            // Chuyển hướng đến trang đích không hiển thị thông báo
            window.location.href = url;
        })
        .catch(error => {
            console.error('Error deleting bill:', error);
            
            // Hiển thị dialog lỗi chi tiết hơn
            const errorDialog = document.createElement('div');
            errorDialog.innerHTML = this.createErrorDialogHTML(error, billId);
            document.body.appendChild(errorDialog);
            
            // Xử lý sự kiện đóng dialog
            document.getElementById('errorCloseBtn').addEventListener('click', function() {
                document.body.removeChild(errorDialog);
            });
            
            // Xử lý sự kiện tiếp tục
            document.getElementById('errorContinueBtn').addEventListener('click', function() {
                document.body.removeChild(errorDialog);
                window.location.href = url;
            });
        })
        .finally(() => {
            // Xóa thông báo đang xử lý
            if (processingMessage && processingMessage.parentNode) {
                document.body.removeChild(processingMessage);
            }
        });
    }

    createErrorDialogHTML(error, billId) {
        return `
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
    }
} 