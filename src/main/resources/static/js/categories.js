/**
 * Student Budget Tracker - Categories Controller
 */

let allCategoriesList = [];
let activeFilterType = 'ALL';

document.addEventListener('DOMContentLoaded', async () => {
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    await loadCategories();

    // Modal submit listener
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
});

async function loadCategories() {
    try {
        const res = await API.get('/api/categories');
        if (res && res.success) {
            allCategoriesList = res.data;
            renderCategoriesTable();
        }
    } catch (err) {
        showToast('Error loading categories: ' + err.message, 'error');
    }
}

function filterCategories(type) {
    activeFilterType = type;

    // Update active tab styles
    document.querySelectorAll('.js-tab').forEach(btn => {
        if (btn.getAttribute('data-type') === type) {
            btn.className = 'btn btn-primary btn-sm js-tab active';
        } else {
            btn.className = 'btn btn-secondary btn-sm js-tab';
        }
    });

    renderCategoriesTable();
}

function renderCategoriesTable() {
    const tbody = document.getElementById('categories-tbody');
    let filtered = allCategoriesList;

    if (activeFilterType !== 'ALL') {
        filtered = allCategoriesList.filter(c => c.type === activeFilterType);
    }

    if (filtered.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; padding: 40px;">
                    <div class="empty-state" style="padding: 10px;">
                        <div class="empty-icon">🏷️</div>
                        <div class="empty-title">No categories found</div>
                        <div class="empty-desc">Click '+ New Custom Category' to add a custom category.</div>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = filtered.map(cat => {
        const isExpense = cat.type === 'EXPENSE';
        const typeBadge = isExpense ? 'badge-expense' : 'badge-income';
        const scopeBadge = cat.systemDefault
            ? '<span class="badge badge-neutral" title="Included with every student account">🔒 System Default</span>'
            : '<span class="badge badge-income" title="Created by you">⭐ Custom Category</span>';

        const actionButtons = cat.systemDefault
            ? '<span style="font-size: 12px; color: var(--text-muted);">Protected</span>'
            : `
                <button type="button" class="btn-icon btn-sm" title="Edit Category" onclick="openEditCategoryModal(${cat.id})">✏️</button>
                <button type="button" class="btn-icon btn-sm" title="Delete Category" onclick="deleteCategory(${cat.id})">🗑️</button>
              `;

        return `
            <tr>
                <td>
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span class="category-dot" style="width: 14px; height: 14px; background: ${escapeHtml(cat.color || '#6366F1')};"></span>
                        <code style="font-size: 12px; color: var(--text-muted);">${escapeHtml(cat.color || '#6366F1')}</code>
                    </div>
                </td>
                <td style="font-weight: 700;">
                    ${escapeHtml(cat.name)}
                </td>
                <td>
                    <span class="badge ${typeBadge}">${cat.type}</span>
                </td>
                <td>
                    ${scopeBadge}
                </td>
                <td style="text-align: center;">
                    ${actionButtons}
                </td>
            </tr>
        `;
    }).join('');
}

function openAddCategoryModal() {
    document.getElementById('cat-modal-title').textContent = 'New Custom Category';
    document.getElementById('edit-cat-id').value = '';
    document.getElementById('category-form').reset();
    document.getElementById('cat-color').value = '#6366F1';
    document.querySelector('input[name="catType"][value="EXPENSE"]').checked = true;
    openModal('category-modal');
}

function openEditCategoryModal(id) {
    const cat = allCategoriesList.find(c => c.id === id);
    if (!cat) return;

    document.getElementById('cat-modal-title').textContent = 'Edit Custom Category';
    document.getElementById('edit-cat-id').value = cat.id;
    document.getElementById('cat-name').value = cat.name;
    document.getElementById('cat-color').value = cat.color || '#6366F1';
    document.getElementById('cat-icon').value = cat.icon || 'tag';

    const radio = document.querySelector(`input[name="catType"][value="${cat.type}"]`);
    if (radio) radio.checked = true;

    openModal('category-modal');
}

async function handleCategorySubmit(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('save-cat-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    const editId = document.getElementById('edit-cat-id').value;
    const name = document.getElementById('cat-name').value.trim();
    const type = document.querySelector('input[name="catType"]:checked').value;
    const color = document.getElementById('cat-color').value;
    const icon = document.getElementById('cat-icon').value;

    const payload = { name, type, color, icon };

    try {
        let res;
        if (editId) {
            res = await API.put(`/api/categories/${editId}`, payload);
        } else {
            res = await API.post('/api/categories', payload);
        }

        if (res && res.success) {
            showToast(editId ? 'Category updated!' : 'Category created!', 'success');
            closeModal('category-modal');
            await loadCategories();
        } else {
            showToast(res.message || 'Error saving category', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Operation failed', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Category';
    }
}

async function deleteCategory(id) {
    if (!confirm('Are you sure you want to delete this custom category?')) return;

    try {
        const res = await API.delete(`/api/categories/${id}`);
        if (res && res.success) {
            showToast('Category deleted successfully', 'success');
            await loadCategories();
        } else {
            showToast(res.message || 'Error deleting category', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Delete failed', 'error');
    }
}
