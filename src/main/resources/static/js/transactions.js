/**
 * Student Budget Tracker - Transactions CRUD Controller
 */

let currentPage = 0;
const pageSize = 15;
let allCategories = [];
let currentTransactionsList = [];

document.addEventListener('DOMContentLoaded', async () => {
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    // Load Categories for filter dropdown
    await loadCategories();

    // Default form date to today
    document.getElementById('form-date').value = new Date().toISOString().slice(0, 10);

    // Initial Load
    await fetchTransactions(0);

    // Filter Form listener
    document.getElementById('filter-form').addEventListener('submit', (e) => {
        e.preventDefault();
        currentPage = 0;
        fetchTransactions(0);
    });

    document.getElementById('clear-filters-btn').addEventListener('click', () => {
        document.getElementById('filter-search').value = '';
        document.getElementById('filter-type').value = '';
        document.getElementById('filter-category').value = '';
        document.getElementById('filter-start-date').value = '';
        document.getElementById('filter-end-date').value = '';
        currentPage = 0;
        fetchTransactions(0);
    });

    // Pagination Listeners
    document.getElementById('prev-page-btn').addEventListener('click', () => {
        if (currentPage > 0) fetchTransactions(currentPage - 1);
    });

    document.getElementById('next-page-btn').addEventListener('click', () => {
        fetchTransactions(currentPage + 1);
    });

    // Modal submit listener
    document.getElementById('tx-form').addEventListener('submit', handleFormSubmit);
});

async function loadCategories() {
    try {
        const res = await API.get('/api/categories');
        if (res && res.success) {
            allCategories = res.data;
            const filterSelect = document.getElementById('filter-category');
            filterSelect.innerHTML = '<option value="">All Categories</option>' +
                allCategories.map(c => `<option value="${c.id}">${escapeHtml(c.name)} (${c.type})</option>`).join('');
        }
    } catch (e) {
        console.error('Error loading categories:', e);
    }
}

async function fetchTransactions(page = 0) {
    const tbody = document.getElementById('transactions-tbody');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 32px; color: var(--text-muted);">Loading transactions...</td></tr>';

    const search = document.getElementById('filter-search').value.trim();
    const type = document.getElementById('filter-type').value;
    const categoryId = document.getElementById('filter-category').value;
    const startDate = document.getElementById('filter-start-date').value;
    const endDate = document.getElementById('filter-end-date').value;

    const params = new URLSearchParams({
        page: page,
        size: pageSize
    });

    if (search) params.append('search', search);
    if (type) params.append('type', type);
    if (categoryId) params.append('categoryId', categoryId);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    try {
        const res = await API.get(`/api/transactions?${params.toString()}`);
        if (!res || !res.success) return;

        const pageData = res.data;
        currentPage = pageData.number;
        currentTransactionsList = pageData.content;

        renderTransactionsTable(pageData.content);
        updatePaginationControls(pageData);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger); padding: 24px;">Failed to load transactions: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderTransactionsTable(transactions) {
    const tbody = document.getElementById('transactions-tbody');
    const currency = (window.currentUser && window.currentUser.currencySymbol) || '$';

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 48px;">
                    <div class="empty-state" style="padding: 10px;">
                        <div class="empty-icon">💳</div>
                        <div class="empty-title">No transactions match your search</div>
                        <div class="empty-desc">Try clearing your filters or click '+ New Transaction' to record a new entry.</div>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = transactions.map(tx => {
        const isIncome = tx.type === 'INCOME';
        const sign = isIncome ? '+' : '-';
        const amountClass = isIncome ? 'amount-income' : 'amount-expense';
        const badgeClass = isIncome ? 'badge-income' : 'badge-expense';

        return `
            <tr>
                <td style="white-space: nowrap; color: var(--text-secondary); font-size: 13px;">
                    ${formatDate(tx.transactionDate)}
                </td>
                <td>
                    <span class="category-pill">
                        <span class="category-dot" style="background: ${escapeHtml(tx.categoryColor || '#6366F1')};"></span>
                        ${escapeHtml(tx.categoryName || 'General')}
                    </span>
                </td>
                <td style="font-weight: 500;">
                    ${escapeHtml(tx.description)}
                </td>
                <td>
                    <span class="badge ${badgeClass}">${tx.type}</span>
                </td>
                <td style="text-align: right; font-size: 15px;" class="${amountClass}">
                    ${sign}${formatCurrency(tx.amount, currency)}
                </td>
                <td style="text-align: center; white-space: nowrap;">
                    <button type="button" class="btn-icon btn-sm" title="Edit" onclick="openEditModal(${tx.id})">
                        ✏️
                    </button>
                    <button type="button" class="btn-icon btn-sm" title="Delete" onclick="deleteTransaction(${tx.id})">
                        🗑️
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function updatePaginationControls(pageData) {
    const totalElements = pageData.totalElements;
    const pageNum = pageData.number + 1;
    const totalPages = Math.max(1, pageData.totalPages);

    document.getElementById('pagination-info').textContent =
        `Showing ${pageData.numberOfElements} of ${totalElements} records`;

    document.getElementById('current-page-badge').textContent = `Page ${pageNum} of ${totalPages}`;
    document.getElementById('prev-page-btn').disabled = pageData.first;
    document.getElementById('next-page-btn').disabled = pageData.last;
}

// Modal Handlers
function openAddModal() {
    document.getElementById('modal-title').textContent = 'Record Transaction';
    document.getElementById('edit-tx-id').value = '';
    document.getElementById('tx-form').reset();
    document.querySelector('input[name="modalTxType"][value="EXPENSE"]').checked = true;
    document.getElementById('form-date').value = new Date().toISOString().slice(0, 10);

    populateModalCategories('EXPENSE');
    openModal('tx-modal');
}

function handleModalTypeChange() {
    const type = document.querySelector('input[name="modalTxType"]:checked').value;
    populateModalCategories(type);
}

function populateModalCategories(type, selectedId = null) {
    const select = document.getElementById('form-category');
    const filtered = allCategories.filter(c => c.type === type);

    select.innerHTML = '<option value="">Select Category...</option>' +
        filtered.map(c => `
            <option value="${c.id}" ${selectedId && Number(selectedId) === c.id ? 'selected' : ''}>
                ${escapeHtml(c.name)}
            </option>
        `).join('');
}

async function openEditModal(id) {
    try {
        const res = await API.get(`/api/transactions/${id}`);
        if (!res || !res.success) return;

        const tx = res.data;
        document.getElementById('modal-title').textContent = 'Edit Transaction';
        document.getElementById('edit-tx-id').value = tx.id;
        document.getElementById('form-amount').value = tx.amount;
        document.getElementById('form-desc').value = tx.description;
        document.getElementById('form-date').value = tx.transactionDate;

        const radio = document.querySelector(`input[name="modalTxType"][value="${tx.type}"]`);
        if (radio) radio.checked = true;

        populateModalCategories(tx.type, tx.categoryId);
        openModal('tx-modal');
    } catch (err) {
        showToast('Error opening transaction: ' + err.message, 'error');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('form-submit-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    const editId = document.getElementById('edit-tx-id').value;
    const type = document.querySelector('input[name="modalTxType"]:checked').value;
    const amount = parseFloat(document.getElementById('form-amount').value);
    const categoryId = parseInt(document.getElementById('form-category').value, 10);
    const description = document.getElementById('form-desc').value.trim();
    const transactionDate = document.getElementById('form-date').value;

    const payload = { type, amount, categoryId, description, transactionDate };

    try {
        let res;
        if (editId) {
            res = await API.put(`/api/transactions/${editId}`, payload);
        } else {
            res = await API.post('/api/transactions', payload);
        }

        if (res && res.success) {
            showToast(editId ? 'Transaction updated successfully!' : 'Transaction recorded!', 'success');
            closeModal('tx-modal');
            fetchTransactions(currentPage);
        } else {
            showToast(res.message || 'Operation failed', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Error saving transaction', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Record';
    }
}

async function deleteTransaction(id) {
    if (!confirm('Are you sure you want to permanently delete this transaction?')) return;

    try {
        const res = await API.delete(`/api/transactions/${id}`);
        if (res && res.success) {
            showToast('Transaction deleted successfully', 'success');
            fetchTransactions(currentPage);
        } else {
            showToast(res.message || 'Delete failed', 'error');
        }
    } catch (err) {
        showToast('Error deleting transaction: ' + err.message, 'error');
    }
}

// Export to CSV Function
function exportTransactionsCSV() {
    if (!currentTransactionsList || currentTransactionsList.length === 0) {
        showToast('No transaction records available to export.', 'warning');
        return;
    }

    const headers = ['ID', 'Date', 'Type', 'Category', 'Description', 'Amount'];
    const rows = currentTransactionsList.map(tx => [
        tx.id,
        tx.transactionDate,
        tx.type,
        `"${(tx.categoryName || '').replace(/"/g, '""')}"`,
        `"${(tx.description || '').replace(/"/g, '""')}"`,
        tx.amount
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + 
        [headers.join(','), ...rows.map(e => e.join(','))].join('\n');

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `student_budget_transactions_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();

    showToast('Exported transactions CSV successfully!', 'success');
}
