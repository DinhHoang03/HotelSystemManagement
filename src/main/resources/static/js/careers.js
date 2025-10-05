// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Navigation scroll effect
$(window).scroll(function() {
    if ($(window).scrollTop() > 50) {
        $('.nav-container').addClass('scrolled');
    } else {
        $('.nav-container').removeClass('scrolled');
    }
});

// Enhanced smooth scrolling with offset and active link highlighting
$(document).ready(function() {
    // Cache selectors
    const navLinks = $('.nav-link');
    const sections = $('section[id], div[id]');
    const navHeight = $('nav').outerHeight();

    // Function to update active link
    function updateActiveLink() {
        const scrollPosition = $(window).scrollTop() + navHeight + 20;

        sections.each(function() {
            const section = $(this);
            const sectionTop = section.offset().top;
            const sectionHeight = section.outerHeight();
            const sectionId = section.attr('id');

            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                navLinks.removeClass('active');
                $(`.nav-link[data-section="${sectionId}"]`).addClass('active');
            }
        });
    }

    // Handle navigation
    navLinks.on('click', function(e) {
        const href = $(this).attr('href');

        // If it's a section link
        if (href.startsWith('#')) {
            e.preventDefault();
            const targetId = $(this).attr('data-section');
            const targetSection = $(`#${targetId}`);

            if (targetSection.length) {
                const targetPosition = targetSection.offset().top - navHeight;

                $('html, body').stop().animate({
                    scrollTop: targetPosition
                }, {
                    duration: 1000,
                    easing: 'easeInOutCubic',
                    complete: function() {
                        navLinks.removeClass('active');
                        $(`.nav-link[data-section="${targetId}"]`).addClass('active');
                    }
                });
            }
        }
    });

    // Update active link on scroll
    $(window).on('scroll', function() {
        updateActiveLink();
    });

    // Initial active link update
    updateActiveLink();
});

// Function to show HR contact information
function showHRContact(position) {
    Swal.fire({
        title: `Apply for ${position}`,
        html: `
            <div class="text-left">
                <p class="mb-4">Please contact our HR team for application details:</p>
                <p class="mb-2"><i class="fas fa-phone mr-2"></i> Phone: 0913513395</p>
                <p class="mb-2"><i class="fas fa-envelope mr-2"></i> Email: 2121050110@gmail.com</p>
                <p class="mt-4 text-sm text-gray-600">We look forward to reviewing your application!</p>
            </div>
        `,
        icon: 'info',
        confirmButtonText: 'Close',
        confirmButtonColor: '#4F46E5'
    });
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