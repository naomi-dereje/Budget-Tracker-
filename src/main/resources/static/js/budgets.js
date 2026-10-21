/**
 * Student Budget Tracker - Budgets Management Controller
 */

let currentBudgetData = null;
let expenseCategories = [];

document.addEventListener('DOMContentLoaded', async () => {
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    const monthInput = document.getElementById('budget-month');
    const now = new Date();
    monthInput.value = now.toISOString().slice(0, 7);

    // Month Navigation Listeners
    monthInput.addEventListener('change', () => loadBudget(monthInput.value));

    document.getElementById('prev-month-btn').addEventListener('click', () => {
        const [year, month] = monthInput.value.split('-').map(Number);
        const d = new Date(year, month - 2, 1);
        monthInput.value = d.toISOString().slice(0, 7);
        loadBudget(monthInput.value);
    });

    document.getElementById('next-month-btn').addEventListener('click', () => {
        const [year, month] = monthInput.value.split('-').map(Number);
        const d = new Date(year, month, 1);
        monthInput.value = d.toISOString().slice(0, 7);
        loadBudget(monthInput.value);
    });

    await loadExpenseCategories();
    await loadBudget(monthInput.value);

    // Form submission listener
    document.getElementById('budget-form').addEventListener('submit', handleSaveBudgetSubmit);
});

async function loadExpenseCategories() {
    try {
        const res = await API.get('/api/categories/type/EXPENSE');
        if (res && res.success) {
            expenseCategories = res.data;
        }
    } catch (e) {
        console.error('Error loading expense categories:', e);
    }
}

async function loadBudget(yearMonth) {
    try {
        const res = await API.get(`/api/budgets/current?month=${yearMonth}`);
        if (!res || !res.success) return;

        currentBudgetData = res.data;
        const currency = (window.currentUser && window.currentUser.currencySymbol) || '$';

        document.getElementById('overview-month-title').textContent = `Budget for ${yearMonth}`;
        document.getElementById('budget-notes').textContent = currentBudgetData.notes || 'No notes added for this month';
        document.getElementById('overview-limit').textContent = formatCurrency(currentBudgetData.monthlyLimit, currency);
        document.getElementById('overview-spent').textContent = formatCurrency(currentBudgetData.totalSpent, currency);

        const remEl = document.getElementById('overview-remaining');
        remEl.textContent = formatCurrency(currentBudgetData.remainingBudget, currency);
        remEl.style.color = currentBudgetData.overBudget ? 'var(--danger)' : 'var(--success)';

        const utilEl = document.getElementById('overview-utilization');
        const rate = currentBudgetData.percentageUsed;
        utilEl.textContent = `${rate}%`;

        const progressBar = document.getElementById('overview-progress');
        progressBar.style.width = `${Math.min(100, Math.max(0, rate))}%`;
        progressBar.className = 'progress-bar';

        const statusBadge = document.getElementById('budget-status-badge');
        if (currentBudgetData.overBudget) {
            progressBar.classList.add('progress-danger');
            statusBadge.className = 'badge badge-expense';
            statusBadge.textContent = '⚠️ Budget Exceeded';
        } else if (rate >= 80) {
            progressBar.classList.add('progress-warning');
            statusBadge.className = 'badge badge-warning';
            statusBadge.textContent = '🔔 Near Limit';
        } else {
            progressBar.classList.add('progress-success');
            statusBadge.className = 'badge badge-income';
            statusBadge.textContent = '✓ On Track';
        }

        renderCategoryBudgets(currentBudgetData.categoryBudgets, currency);

    } catch (err) {
        showToast('Error loading budget: ' + err.message, 'error');
    }
}

function renderCategoryBudgets(catBudgets, currency) {
    const container = document.getElementById('category-budgets-container');

    if (!catBudgets || catBudgets.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🎯</div>
                <div class="empty-title">No category limits set for this month</div>
                <div class="empty-desc">Setting category limits helps you monitor exactly where your money goes. Click 'Configure Budget' to set limits.</div>
                <button type="button" class="btn btn-primary btn-sm" onclick="openBudgetEditorModal()">Configure Category Budgets</button>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 20px;">
            ${catBudgets.map(cb => {
                const isOver = cb.overBudget;
                const progressClass = isOver ? 'progress-danger' : (cb.percentageUsed >= 80 ? 'progress-warning' : 'progress-success');
                const badgeClass = isOver ? 'badge-expense' : (cb.percentageUsed >= 80 ? 'badge-warning' : 'badge-income');
                const badgeText = isOver ? 'Exceeded' : (cb.percentageUsed >= 80 ? 'Near Limit' : 'On Track');

                return `
                    <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 18px 20px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <span class="category-dot" style="width: 12px; height: 12px; background: ${escapeHtml(cb.categoryColor || '#6366F1')};"></span>
                                <span style="font-weight: 700; font-size: 15px;">${escapeHtml(cb.categoryName)}</span>
                                <span class="badge ${badgeClass}" style="font-size: 11px;">${badgeText}</span>
                            </div>
                            <div style="font-size: 13px; font-weight: 600;">
                                <span style="color: ${isOver ? 'var(--danger)' : 'var(--text-primary)'};">${formatCurrency(cb.actualSpent, currency)}</span>
                                <span style="color: var(--text-muted);">/ ${formatCurrency(cb.allocatedAmount, currency)}</span>
                            </div>
                        </div>

                        <div class="progress-container" style="height: 10px; margin: 10px 0;">
                            <div class="progress-bar ${progressClass}" style="width: ${Math.min(100, Math.max(0, cb.percentageUsed))}%;"></div>
                        </div>

                        <div style="display: flex; justify-content: space-between; font-size: 12px; color: var(--text-muted);">
                            <span>${cb.percentageUsed}% used</span>
                            <span style="font-weight: 600; color: ${isOver ? 'var(--danger)' : 'var(--success)'};">
                                ${isOver ? 'Over by ' + formatCurrency(Math.abs(cb.remainingAmount), currency) : formatCurrency(cb.remainingAmount, currency) + ' remaining'}
                            </span>
                        </div>
                    </div>
                `;
            }).join('')}
        </div>
    `;
}

function openBudgetEditorModal() {
    const month = document.getElementById('budget-month').value;
    document.getElementById('modal-month').value = month;

    if (currentBudgetData) {
        document.getElementById('modal-total-limit').value = currentBudgetData.monthlyLimit || '';
        document.getElementById('modal-notes').value = currentBudgetData.notes || '';
    }

    const container = document.getElementById('category-allocations-inputs');
    const existingAllocationsMap = {};

    if (currentBudgetData && currentBudgetData.categoryBudgets) {
        currentBudgetData.categoryBudgets.forEach(cb => {
            existingAllocationsMap[cb.categoryId] = cb.allocatedAmount;
        });
    }

    container.innerHTML = expenseCategories.map(cat => {
        const currentVal = existingAllocationsMap[cat.id] || '';
        return `
            <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; background: var(--bg-subtle); padding: 8px 14px; border-radius: var(--radius-sm);">
                <div style="display: flex; align-items: center; gap: 8px; flex: 1;">
                    <span class="category-dot" style="background: ${escapeHtml(cat.color || '#6366F1')};"></span>
                    <span style="font-size: 13px; font-weight: 600;">${escapeHtml(cat.name)}</span>
                </div>
                <div style="width: 140px;">
                    <input type="number" 
                           class="form-input js-cat-allocation" 
                           data-category-id="${cat.id}" 
                           placeholder="Limit ($)" 
                           step="0.01" 
                           min="0" 
                           value="${currentVal}"
                           style="padding: 6px 10px; font-size: 13px;">
                </div>
            </div>
        `;
    }).join('');

    openModal('budget-modal');
}

async function handleSaveBudgetSubmit(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('save-budget-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    const yearMonth = document.getElementById('modal-month').value;
    const monthlyLimit = parseFloat(document.getElementById('modal-total-limit').value);
    const notes = document.getElementById('modal-notes').value.trim();

    const categoryAllocations = [];
    document.querySelectorAll('.js-cat-allocation').forEach(input => {
        const val = parseFloat(input.value);
        const categoryId = parseInt(input.getAttribute('data-category-id'), 10);
        if (!isNaN(val) && val > 0) {
            categoryAllocations.push({
                categoryId,
                allocatedAmount: val
            });
        }
    });

    const payload = {
        yearMonth,
        monthlyLimit,
        notes,
        categoryAllocations
    };

    try {
        const res = await API.post('/api/budgets', payload);
        if (res && res.success) {
            showToast('Budget saved successfully!', 'success');
            closeModal('budget-modal');
            document.getElementById('budget-month').value = yearMonth;
            await loadBudget(yearMonth);
        } else {
            showToast(res.message || 'Error saving budget', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Failed to save budget', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Budget';
    }
}
