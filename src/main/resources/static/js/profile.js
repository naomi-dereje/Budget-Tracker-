/**
 * Student Budget Tracker - User Profile & Security Settings Controller
 */

document.addEventListener('DOMContentLoaded', async () => {
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    await loadProfileDetails();

    document.getElementById('profile-form').addEventListener('submit', handleProfileUpdate);
    document.getElementById('password-form').addEventListener('submit', handlePasswordChange);
});

async function loadProfileDetails() {
    try {
        const res = await API.get('/api/user/profile');
        if (res && res.success) {
            const u = res.data;
            document.getElementById('prof-email').value = u.email || '';
            document.getElementById('prof-name').value = u.fullName || '';
            document.getElementById('prof-univ').value = u.university || '';
            document.getElementById('prof-currency').value = u.currencySymbol || '$';
            document.getElementById('prof-baseline').value = u.monthlyBudgetBaseline || '0.00';
        }
    } catch (err) {
        showToast('Error loading profile: ' + err.message, 'error');
    }
}

async function handleProfileUpdate(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('save-profile-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    const fullName = document.getElementById('prof-name').value.trim();
    const university = document.getElementById('prof-univ').value.trim();
    const currencySymbol = document.getElementById('prof-currency').value;
    const monthlyBudgetBaseline = parseFloat(document.getElementById('prof-baseline').value) || 0;

    const payload = {
        fullName,
        university,
        currencySymbol,
        monthlyBudgetBaseline
    };

    try {
        const res = await API.put('/api/user/profile', payload);
        if (res && res.success) {
            showToast('Profile updated successfully!', 'success');
            await loadCurrentUserProfile();
        } else {
            showToast(res.message || 'Error updating profile', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Update failed', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Profile Changes';
    }
}

async function handlePasswordChange(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('change-pw-btn');

    const currentPassword = document.getElementById('curr-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmNewPassword = document.getElementById('confirm-new-password').value;

    if (newPassword !== confirmNewPassword) {
        showToast('New passwords do not match. Please verify.', 'error');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Updating...';

    try {
        const res = await API.put('/api/user/password', { currentPassword, newPassword });
        if (res && res.success) {
            showToast('Password changed successfully!', 'success');
            document.getElementById('password-form').reset();
        } else {
            showToast(res.message || 'Password update failed', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Password change failed', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Update Password';
    }
}
