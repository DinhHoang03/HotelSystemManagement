// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Initialize GSAP
gsap.registerPlugin(ScrollTrigger);

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

// Function to load customer profile
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

            // Update profile menu with user information
            updateProfileMenu(data);

            return data.id;
        }
        return null;
    } catch (error) {
        console.error('Error loading profile:', error);
        return null;
    }
}

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

window.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        document.getElementById('loadscreen').classList.add('hide');
        setTimeout(function() {
            document.getElementById('loadscreen').style.display = 'none';
        }, 400);
    }, 800);
});

// Chuyển trang mượt
function smoothGoto(url) {
    const loadscreen = document.getElementById('loadscreen');
    loadscreen.style.display = 'flex';
    loadscreen.classList.remove('hide');
    setTimeout(() => {
        loadscreen.classList.add('hide');
        setTimeout(() => {
            window.location.href = url;
        }, 400);
    }, 10);
}

// Logout mượt
function smoothLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    smoothGoto('/login.html');
}

// Bắt sự kiện click cho các link nội bộ
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('a[href]').forEach(function(link) {
        const url = link.getAttribute('href');
        if (url && url.startsWith('/') && !url.startsWith('//') && !link.hasAttribute('target')) {
            link.addEventListener('click', function(e) {
                if (!link.classList.contains('no-smooth')) {
                    e.preventDefault();
                    smoothGoto(url);
                }
            });
        }
    });
});