// Initialize AOS
AOS.init({
    duration: 1000,
    once: true
});

// Smooth scrolling with offset
$(document).ready(function() {
    $('a[href^="#"]').on('click', function(e) {
        e.preventDefault();

        const target = $(this.hash);
        if (target.length) {
            const navHeight = $('nav').outerHeight();
            const targetPosition = target.offset().top - navHeight;

            $('html, body').stop().animate({
                scrollTop: targetPosition
            }, {
                duration: 300,
                easing: 'linear'
            });
        }
    });
});

// Scroll to top button
$(window).scroll(function() {
    if ($(window).scrollTop() > 300) {
        $('#scrollToTop').addClass('visible');
    } else {
        $('#scrollToTop').removeClass('visible');
    }

    // Update navigation bar and text colors
    if ($(window).scrollTop() > 50) {
        $('nav').css({
            'background': 'rgba(255, 255, 255, 0.95)',
            'backdrop-filter': 'blur(10px)',
            '-webkit-backdrop-filter': 'blur(10px)',
            'border-bottom': '1px solid rgba(0, 0, 0, 0.1)'
        });
        $('.nav-link').css({
            'color': '#000000',
            'text-shadow': 'none'
        });
        $('.nav-text').css({
            'color': '#000000',
            'text-shadow': 'none'
        });
        $('.text-gray-800').css({
            'color': '#000000'
        });
        $('#userMenuButton').css('background', 'rgba(79, 70, 229, 0.1)');
        $('.hotel-name').addClass('rainbow-text');
        $('.hotel-name').css('color', 'transparent');
        // Update Book Now button text color
        $('.nav-link.bg-indigo-600').css({
            'color': 'white',
            'text-shadow': 'none'
        });
        // Update chat bot text color
        $('#chatButton, #chatHeader').css({
            'color': '#ffffff'
        });
        // Update username and view profile text color
        $('#userMenuButton span').css({
            'color': '#000000'
        });
    } else {
        $('nav').css({
            'background': 'rgba(255, 255, 255, 0.1)',
            'backdrop-filter': 'blur(10px)',
            '-webkit-backdrop-filter': 'blur(10px)',
            'border-bottom': '1px solid rgba(255, 255, 255, 0.1)'
        });
        $('.nav-link').css({
            'color': 'white',
            'text-shadow': 'none'
        });
        $('.nav-text').css({
            'color': 'white',
            'text-shadow': 'none'
        });
        $('.text-gray-800').css({
            'color': 'white'
        });
        $('#userMenuButton').css('background', 'rgba(255, 255, 255, 0.2)');
        $('.hotel-name').removeClass('rainbow-text');
        $('.hotel-name').css('color', 'white');
        // Update Book Now button text color
        $('.nav-link.bg-indigo-600').css({
            'color': 'white',
            'text-shadow': 'none'
        });
        // Update chat bot text color
        $('#chatButton, #chatHeader').css({
            'color': '#ffffff'
        });
        // Update username and view profile text color
        $('#userMenuButton span').css({
            'color': '#ffffff'
        });
    }
});

function scrollToTop() {
    $('html, body').animate({
        scrollTop: 0
    }, {
        duration: 500,
        easing: 'linear'
    });
}

// Check authentication
$(document).ready(function() {
    checkAuth();
    // Load profile data immediately after page load
    loadProfileData();
});

// Toggle user menu
$('#userMenuButton').click(function(e) {
    e.stopPropagation();
    openProfilePanel();
});

// Add these functions
function openProfilePanel() {
    $('#profilePanel').addClass('active');
    $('#profileOverlay').addClass('active');
    loadProfileData();
}

function closeProfilePanel() {
    $('#profilePanel').removeClass('active');
    $('#profileOverlay').removeClass('active');
}

function loadProfileData() {
    const token = localStorage.getItem('token');
    if (!token) return;

    $.ajax({
        url: '/customer/profile',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            console.log('Profile response:', response);
            if (response) {
                // Update profile panel information
                $('#profilePanelName').text(response.name || 'User');
                $('#profilePanelAvatar').attr('src', `https://ui-avatars.com/api/?name=${encodeURIComponent(response.name || 'User')}&background=4F46E5&color=fff&size=128`);
                $('#profileEmail').text(response.email || 'Not provided');
                $('#profilePhone').text(response.phone || 'Not provided');
                $('#profileAddress').text(response.address || 'Not provided');

                // Also update the navigation bar display
                $('#usernameDisplay').text(response.name || 'User');
                $('#userAvatar').attr('src', `https://ui-avatars.com/api/?name=${encodeURIComponent(response.name || 'User')}&background=4F46E5&color=fff&size=128`);
            } else {
                console.error('Invalid response format:', response);
            }
        },
        error: function(xhr) {
            console.error('Error loading profile data:', xhr);
            // Show error message to user
            $('#profileEmail').text('Error loading profile');
            $('#profilePhone').text('Please try again later');
            $('#profileAddress').text('Error loading address');
        }
    });
}

// Close panel when clicking overlay
$('#profileOverlay').click(function() {
    closeProfilePanel();
});

// Image Slider
let currentSlide = 0;
const slides = $('.swiper-slide');
const totalSlides = slides.length;

function showSlide(index) {
    if (index < 0) {
        currentSlide = totalSlides - 1;
    } else if (index >= totalSlides) {
        currentSlide = 0;
    } else {
        currentSlide = index;
    }

    // Hide all slides
    slides.removeClass('active');

    // Show current slide
    slides.eq(currentSlide).addClass('active');

    // Update indicators
    $('.indicator').removeClass('active');
    $('.indicator').eq(currentSlide).addClass('active');
}

function nextSlide() {
    showSlide(currentSlide + 1);
}

function prevSlide() {
    showSlide(currentSlide - 1);
}

// Auto slide every 5 seconds
let slideInterval = setInterval(nextSlide, 5000);

// Reset interval when manually changing slides
function resetInterval() {
    clearInterval(slideInterval);
    slideInterval = setInterval(nextSlide, 5000);
}

// Event listeners for manual navigation
$('#nextSlide').click(function() {
    nextSlide();
    resetInterval();
});

$('#prevSlide').click(function() {
    prevSlide();
    resetInterval();
});

// Event listeners for indicators
$('.indicator').click(function() {
    const index = $(this).index();
    showSlide(index);
    resetInterval();
});

// Initialize first slide
showSlide(0);

// Add touch support
let touchStartX = 0;
let touchEndX = 0;

$('.swiper').on('touchstart', function(e) {
    touchStartX = e.originalEvent.touches[0].clientX;
});

$('.swiper').on('touchend', function(e) {
    touchEndX = e.originalEvent.changedTouches[0].clientX;
    handleSwipe();
});

function handleSwipe() {
    const swipeThreshold = 50;
    const diff = touchStartX - touchEndX;

    if (Math.abs(diff) > swipeThreshold) {
        if (diff > 0) {
            // Swipe left
            nextSlide();
        } else {
            // Swipe right
            prevSlide();
        }
        resetInterval();
    }
}

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

// Initialize counters animation
function animateCounters() {
    $('.counter').each(function() {
        const $this = $(this);
        const countTo = parseInt($this.attr('data-count'));

        $({ countNum: 0 }).animate({
            countNum: countTo
        }, {
            duration: 3000,
            easing: 'swing',
            step: function() {
                const current = Math.floor(this.countNum);
                $this.text(current + (current === countTo ? '+' : ''));
            },
            complete: function() {
                $this.text(countTo + '+');
            }
        });
    });
}

// Trigger counter animation when section is in view
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            animateCounters();
            observer.unobserve(entry.target);
        }
    });
}, { threshold: 0.3 });

document.querySelectorAll('.counter').forEach(counter => {
    observer.observe(counter);
});

// Chat functionality
$(document).ready(function() {
    const chatButton = $('#chatButton');
    const chatWindow = $('#chatWindow');
    const closeChat = $('#closeChat');
    const chatForm = $('#chatForm');
    const messageInput = $('#messageInput');
    const chatMessages = $('#chatMessages');
    const chatHeader = $('#chatHeader');

    // Make chat widget draggable
    let isDragging = false;
    let currentX;
    let currentY;
    let initialX;
    let initialY;
    let xOffset = 0;
    let yOffset = 0;

    function dragStart(e) {
        if (e.type === "touchstart") {
            initialX = e.touches[0].clientX - xOffset;
            initialY = e.touches[0].clientY - yOffset;
        } else {
            initialX = e.clientX - xOffset;
            initialY = e.clientY - yOffset;
        }

        if (e.target === chatButton[0] || e.target === chatHeader[0]) {
            isDragging = true;
        }
    }

    function dragEnd(e) {
        initialX = currentX;
        initialY = currentY;
        isDragging = false;
    }

    function drag(e) {
        if (isDragging) {
            e.preventDefault();

            if (e.type === "touchmove") {
                currentX = e.touches[0].clientX - initialX;
                currentY = e.touches[0].clientY - initialY;
            } else {
                currentX = e.clientX - initialX;
                currentY = e.clientY - initialY;
            }

            xOffset = currentX;
            yOffset = currentY;

            setTranslate(currentX, currentY, chatButton);
            if (!chatWindow.hasClass('hidden')) {
                setTranslate(currentX, currentY, chatWindow);
            }
        }
    }

    function setTranslate(xPos, yPos, el) {
        el.css({
            transform: `translate3d(${xPos}px, ${yPos}px, 0)`
        });
    }

    // Add event listeners for drag and drop
    document.addEventListener("touchstart", dragStart, false);
    document.addEventListener("touchend", dragEnd, false);
    document.addEventListener("touchmove", drag, false);

    document.addEventListener("mousedown", dragStart, false);
    document.addEventListener("mouseup", dragEnd, false);
    document.addEventListener("mousemove", drag, false);

    // Toggle chat window with smooth effect
    chatButton.click(function(e) {
        if (!isDragging) {
            if (chatWindow.hasClass('visible')) {
                chatWindow.removeClass('visible');
                setTimeout(() => chatWindow.addClass('hidden'), 300);
            } else {
                chatWindow.removeClass('hidden');
                setTimeout(() => chatWindow.addClass('visible'), 10);
            }
        }
    });

    closeChat.click(function() {
        chatWindow.removeClass('visible');
        setTimeout(() => chatWindow.addClass('hidden'), 300);
    });

    // Add a welcome message with emoji
    addMessage('bot', '👋 Chào bạn! Tôi có thể giúp được gì cho bạn hôm nay? 😊');

    // Handle form submission
    chatForm.submit(function(e) {
        e.preventDefault();
        const message = messageInput.val().trim();
        if (message) {
            addMessage('user', message);
            messageInput.val('');
            addTypingIndicator();
            $.ajax({
                url: 'https://localhost:8443/gemini/ask',
                type: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('token'),
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify(message),
                success: function(response) {
                    removeTypingIndicator();
                    if (response.result) {
                        // Remove ** characters and replace with spaces
                        const cleanedResponse = response.result.replace(/\*\*/g, ' ');
                        addMessageLetterByLetter('bot', cleanedResponse);
                    }
                },
                error: function(xhr) {
                    removeTypingIndicator();
                    addMessage('bot', '😔 Xin lỗi, tôi gặp một chút vấn đề. Bạn có thể thử lại sau được không?');
                    console.error('Chat error:', xhr);
                }
            });
        }
    });

    function addMessage(type, message) {
        // Convert \n to <br>
        message = message.replace(/\n/g, '<br>');
        const messageHtml = `
            <div class="flex ${type === 'user' ? 'justify-end' : 'justify-start'}">
                <div class="max-w-[80%] ${type === 'user' ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white' : 'bg-gray-100 text-gray-800'} rounded-lg px-4 py-2 shadow-sm">
                    ${message}
                </div>
            </div>
        `;
        chatMessages.append(messageHtml);
        scrollToBottom();
    }

    // Render bot answer letter by letter
    function addMessageLetterByLetter(type, message) {
        message = message.replace(/\n/g, '<br>');
        const container = $(`<div class="flex ${type === 'user' ? 'justify-end' : 'justify-start'}"><div class="max-w-[80%] ${type === 'user' ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white' : 'bg-gray-100 text-gray-800'} rounded-lg px-4 py-2 shadow-sm"></div></div>`);
        chatMessages.append(container);
        const msgDiv = container.find('div').last();
        let i = 0;
        function typeChar() {
            if (i < message.length) {
                // Handle <br> as a unit
                if (message.substring(i, i+4) === '<br>') {
                    msgDiv.append('<br>');
                    i += 4;
                } else {
                    msgDiv.append(message[i]);
                    i++;
                }
                scrollToBottom();
                setTimeout(typeChar, 15);
            }
        }
        typeChar();
    }

    function addTypingIndicator() {
        const typingHtml = `
            <div class="flex justify-start" id="typingIndicator">
                <div class="bg-gray-100 text-gray-800 rounded-lg px-4 py-2">
                    <div class="flex space-x-1">
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
                    </div>
                </div>
            </div>
        `;
        chatMessages.append(typingHtml);
        scrollToBottom();
    }

    function removeTypingIndicator() {
        $('#typingIndicator').remove();
    }

    function scrollToBottom() {
        chatMessages.scrollTop(chatMessages[0].scrollHeight);
    }
});