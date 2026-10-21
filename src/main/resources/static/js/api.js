/**
 * Student Budget Tracker - Core API Client & Utilities
 */

const API = {
    // Read cookie by name (used for Spring Security XSRF-TOKEN)
    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    },

    // Central HTTP Request Dispatcher
    async request(url, options = {}) {
        const headers = options.headers || {};
        headers['Content-Type'] = headers['Content-Type'] || 'application/json';

        // Attach CSRF token if mutating request
        const method = (options.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            const xsrfToken = this.getCookie('XSRF-TOKEN');
            if (xsrfToken) {
                headers['X-XSRF-TOKEN'] = xsrfToken;
            }
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            // Handle unauthorized session expiration
            if (response.status === 401) {
                const isAuthPage = window.location.pathname.includes('login.html') || 
                                   window.location.pathname.includes('register.html');
                if (!isAuthPage) {
                    window.location.href = '/login.html?session=expired';
                    return null;
                }
            }

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                const errorMsg = data.message || `Request failed with status ${response.status}`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            console.error(`API Error on ${url}:`, error);
            throw error;
        }
    },

    get(url) {
        return this.request(url, { method: 'GET' });
    },

    post(url, body) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(url, body) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};

// Toast Notifications System
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✓';
    if (type === 'error') icon = '⚠️';
    if (type === 'warning') icon = '🔔';

    toast.innerHTML = `
        <span style="font-weight: bold; font-size: 16px;">${icon}</span>
        <div style="flex: 1;">${escapeHtml(message)}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Format currency
function formatCurrency(amount, symbol = '$') {
    if (amount === null || amount === undefined) amount = 0;
    const num = Number(amount);
    return `${symbol}${num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString + 'T00:00:00');
    return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}

// Escape HTML for XSS prevention
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Modal Helpers
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

// Dark Mode Theme Init & Toggle
function initTheme() {
    const savedTheme = localStorage.getItem('tracker_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.innerHTML = savedTheme === 'dark' ? '☀️' : '🌙';
    }
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('tracker_theme', next);
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.innerHTML = next === 'dark' ? '☀️' : '🌙';
    }
}

// Shared Navigation & User Profile Loader
async function loadCurrentUserProfile() {
    try {
        const res = await API.get('/api/auth/me');
        if (res && res.success && res.data) {
            const user = res.data;
            window.currentUser = user;
            
            // Populate common user UI elements
            document.querySelectorAll('.js-user-name').forEach(el => el.textContent = user.fullName);
            document.querySelectorAll('.js-user-email').forEach(el => el.textContent = user.email);
            document.querySelectorAll('.js-user-univ').forEach(el => el.textContent = user.university || 'University Student');
            document.querySelectorAll('.js-user-avatar').forEach(el => {
                const initials = user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
                el.textContent = initials || 'ST';
            });
            document.querySelectorAll('.js-user-currency').forEach(el => el.textContent = user.currencySymbol);

            return user;
        }
    } catch (e) {
        console.warn('Could not load user profile', e);
    }
    return null;
}

// Global Logout Handler
async function handleLogout() {
    try {
        await API.post('/api/auth/logout', {});
    } catch (e) {
        console.warn('Logout notification error', e);
    } finally {
        window.location.href = '/login.html?logged_out=true';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.addEventListener('click', toggleTheme);
    }
});
