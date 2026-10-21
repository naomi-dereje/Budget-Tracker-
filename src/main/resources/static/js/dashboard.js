/**
 * Student Budget Tracker - Dashboard Controller & Visualizations
 */

let incomeExpenseChart = null;
let categoryChart = null;
let trendChart = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication and load profile
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });
    }

    // Initialize month picker to current YYYY-MM
    const monthPicker = document.getElementById('selected-month');
    const now = new Date();
    const currentYearMonth = now.toISOString().slice(0, 7);
    monthPicker.value = currentYearMonth;

    // Month Navigation Listeners
    monthPicker.addEventListener('change', () => loadDashboardData(monthPicker.value));

    document.getElementById('prev-month-btn').addEventListener('click', () => {
        const [year, month] = monthPicker.value.split('-').map(Number);
        const d = new Date(year, month - 2, 1);
        monthPicker.value = d.toISOString().slice(0, 7);
        loadDashboardData(monthPicker.value);
    });

    document.getElementById('next-month-btn').addEventListener('click', () => {
        const [year, month] = monthPicker.value.split('-').map(Number);
        const d = new Date(year, month, 1);
        monthPicker.value = d.toISOString().slice(0, 7);
        loadDashboardData(monthPicker.value);
    });

    // Default modal date to today
    document.getElementById('tx-date').value = now.toISOString().slice(0, 10);

    // Initial load
    await loadDashboardData(monthPicker.value);

    // Modal form listener
    const modalForm = document.getElementById('modal-tx-form');
    modalForm.addEventListener('submit', handleAddTransactionSubmit);
});

// Load Dashboard Data from REST Backend
async function loadDashboardData(yearMonth) {
    try {
        const res = await API.get(`/api/reports/dashboard?month=${yearMonth}`);
        if (!res || !res.success) return;

        const data = res.data;
        const currency = data.currencySymbol || '$';

        // Update Stat Cards
        document.getElementById('stat-income').textContent = formatCurrency(data.totalIncome, currency);
        document.getElementById('stat-expense').textContent = formatCurrency(data.totalExpenses, currency);
        
        const balanceEl = document.getElementById('stat-balance');
        balanceEl.textContent = formatCurrency(data.netBalance, currency);
        balanceEl.style.color = Number(data.netBalance) >= 0 ? 'var(--success)' : 'var(--danger)';

        // Budget Utilization
        const utilEl = document.getElementById('stat-utilization');
        const progressBar = document.getElementById('stat-budget-progress');
        const budgetDetails = document.getElementById('stat-budget-details');
        const warningBanner = document.getElementById('budget-warning-banner');

        const rate = data.budgetUtilizationRate;
        utilEl.textContent = `${rate}%`;
        progressBar.style.width = `${Math.min(100, Math.max(0, rate))}%`;

        // Budget colors & alerts
        progressBar.className = 'progress-bar';
        if (data.overBudget) {
            progressBar.classList.add('progress-danger');
            utilEl.style.color = 'var(--danger)';
            warningBanner.style.display = 'block';
            warningBanner.style.background = 'var(--danger-light)';
            warningBanner.style.border = '1px solid var(--danger-border)';
            warningBanner.style.color = 'var(--danger)';
            warningBanner.innerHTML = `⚠️ <strong>Budget Exceeded!</strong> You have spent ${formatCurrency(data.totalExpenses, currency)}, which is <strong>${formatCurrency(data.totalExpenses - data.monthlyBudgetLimit, currency)}</strong> over your ${formatCurrency(data.monthlyBudgetLimit, currency)} monthly budget.`;
        } else if (rate >= 80) {
            progressBar.classList.add('progress-warning');
            utilEl.style.color = 'var(--warning)';
            warningBanner.style.display = 'block';
            warningBanner.style.background = 'var(--warning-light)';
            warningBanner.style.border = '1px solid var(--warning-border)';
            warningBanner.style.color = 'var(--warning)';
            warningBanner.innerHTML = `🔔 <strong>Budget Warning:</strong> You have utilized ${rate}% of your monthly limit (${formatCurrency(data.remainingBudget, currency)} remaining).`;
        } else {
            progressBar.classList.add('progress-success');
            utilEl.style.color = 'var(--text-primary)';
            warningBanner.style.display = 'none';
        }

        budgetDetails.textContent = data.monthlyBudgetLimit > 0
            ? `Limit: ${formatCurrency(data.monthlyBudgetLimit, currency)} (${formatCurrency(data.remainingBudget, currency)} remaining)`
            : 'No budget set for this month';

        // Render Charts
        renderIncomeExpenseChart(data.totalIncome, data.totalExpenses, currency, yearMonth);
        renderCategoryBreakdownChart(data.categoryExpenses, currency);
        renderSpendingTrendsChart(data.spendingTrends, currency);

        // Render Recent Transactions
        renderRecentTransactions(data.recentTransactions, currency);

    } catch (err) {
        showToast('Error loading dashboard: ' + err.message, 'error');
    }
}

// 1. Income vs Expense Bar Chart
function renderIncomeExpenseChart(income, expense, currency, yearMonth) {
    const ctx = document.getElementById('chart-income-expense').getContext('2d');
    document.getElementById('chart-month-label').textContent = yearMonth;

    if (incomeExpenseChart) {
        incomeExpenseChart.destroy();
    }

    incomeExpenseChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Total Income', 'Total Expenses'],
            datasets: [{
                data: [Number(income), Number(expense)],
                backgroundColor: ['#10B981', '#EF4444'],
                borderRadius: 8,
                barThickness: 45
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (ctx) => `${ctx.label}: ${formatCurrency(ctx.raw, currency)}`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: (v) => `${currency}${v}`
                    },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
}

// 2. Category Breakdown Doughnut Chart
function renderCategoryBreakdownChart(categories, currency) {
    const ctx = document.getElementById('chart-category-breakdown').getContext('2d');

    if (categoryChart) {
        categoryChart.destroy();
    }

    if (!categories || categories.length === 0) {
        categoryChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['No Expenses Recorded'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['#E2E8F0']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: { enabled: false }
                }
            }
        });
        return;
    }

    const labels = categories.map(c => c.categoryName);
    const amounts = categories.map(c => Number(c.amount));
    const colors = categories.map(c => c.color || '#6366F1');

    categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: amounts,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: 'var(--bg-surface)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: { boxWidth: 12, font: { size: 12 } }
                },
                tooltip: {
                    callbacks: {
                        label: (ctx) => ` ${ctx.label}: ${formatCurrency(ctx.raw, currency)} (${categories[ctx.dataIndex].percentage}%)`
                    }
                }
            },
            cutout: '68%'
        }
    });
}

// 3. 6-Month Spending Trends Area/Line Chart
function renderSpendingTrendsChart(trends, currency) {
    const ctx = document.getElementById('chart-spending-trend').getContext('2d');

    if (trendChart) {
        trendChart.destroy();
    }

    const labels = trends.map(t => t.month);
    const incomeData = trends.map(t => Number(t.income));
    const expenseData = trends.map(t => Number(t.expense));

    trendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    borderColor: '#10B981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2.5
                },
                {
                    label: 'Expense',
                    data: expenseData,
                    borderColor: '#EF4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2.5
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' },
                tooltip: {
                    callbacks: {
                        label: (ctx) => ` ${ctx.dataset.label}: ${formatCurrency(ctx.raw, currency)}`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: (v) => `${currency}${v}` },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: { grid: { display: false } }
            }
        }
    });
}

// Render Recent Transactions Table
function renderRecentTransactions(transactions, currency) {
    const tbody = document.getElementById('recent-transactions-tbody');
    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 36px;">
                    <div class="empty-state" style="padding: 10px;">
                        <div class="empty-icon">📝</div>
                        <div class="empty-title">No transactions recorded yet</div>
                        <div class="empty-desc">Click '+ Add Record' above to record your first income or expense for this month.</div>
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
                <td style="text-align: center;">
                    <button type="button" class="btn-icon btn-sm" title="Delete record" onclick="deleteTransactionFromDashboard(${tx.id})">
                        🗑️
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// Quick Add Transaction Modal Helpers
async function openAddTransactionModal() {
    await loadCategoriesForModal();
    openModal('add-transaction-modal');
}

async function loadCategoriesForModal() {
    const type = document.querySelector('input[name="txType"]:checked').value;
    const select = document.getElementById('tx-category');
    select.innerHTML = '<option value="">Loading categories...</option>';

    try {
        const res = await API.get(`/api/categories/type/${type}`);
        if (res && res.success) {
            select.innerHTML = '<option value="">Select a category...</option>' + 
                res.data.map(cat => `<option value="${cat.id}">${escapeHtml(cat.name)}</option>`).join('');
        }
    } catch (e) {
        select.innerHTML = '<option value="">Error loading categories</option>';
    }
}

async function handleAddTransactionSubmit(e) {
    e.preventDefault();
    const submitBtn = document.getElementById('save-tx-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    const type = document.querySelector('input[name="txType"]:checked').value;
    const amount = parseFloat(document.getElementById('tx-amount').value);
    const categoryId = parseInt(document.getElementById('tx-category').value, 10);
    const description = document.getElementById('tx-description').value.trim();
    const transactionDate = document.getElementById('tx-date').value;

    try {
        const res = await API.post('/api/transactions', {
            type,
            amount,
            categoryId,
            description,
            transactionDate
        });

        if (res && res.success) {
            showToast('Transaction recorded successfully!', 'success');
            closeModal('add-transaction-modal');
            document.getElementById('modal-tx-form').reset();
            // Refresh dashboard
            const monthPicker = document.getElementById('selected-month');
            await loadDashboardData(monthPicker.value);
        } else {
            showToast(res.message || 'Error creating transaction', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Error creating transaction', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Transaction';
    }
}

async function deleteTransactionFromDashboard(id) {
    if (!confirm('Are you sure you want to delete this transaction record?')) return;

    try {
        const res = await API.delete(`/api/transactions/${id}`);
        if (res && res.success) {
            showToast('Transaction deleted successfully', 'success');
            const monthPicker = document.getElementById('selected-month');
            await loadDashboardData(monthPicker.value);
        }
    } catch (err) {
        showToast('Error deleting transaction: ' + err.message, 'error');
    }
}
