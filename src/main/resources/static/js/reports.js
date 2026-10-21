/**
 * Student Budget Tracker - Financial Reports & Analytics Controller
 */

let dailyTimelineChart = null;

document.addEventListener('DOMContentLoaded', async () => {
    const user = await loadCurrentUserProfile();
    if (!user) return;

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    }

    // Default to This Month
    setPreset('this-month');

    // Filter Form listener
    document.getElementById('report-filter-form').addEventListener('submit', (e) => {
        e.preventDefault();
        loadReport();
    });

    await loadReport();
});

function setPreset(preset) {
    const now = new Date();
    const startInput = document.getElementById('report-start');
    const endInput = document.getElementById('report-end');

    if (preset === 'this-month') {
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        startInput.value = firstDay.toISOString().slice(0, 10);
        endInput.value = lastDay.toISOString().slice(0, 10);
    } else if (preset === 'last-month') {
        const firstDay = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth(), 0);
        startInput.value = firstDay.toISOString().slice(0, 10);
        endInput.value = lastDay.toISOString().slice(0, 10);
    } else if (preset === 'last-90-days') {
        const past = new Date();
        past.setDate(now.getDate() - 90);
        startInput.value = past.toISOString().slice(0, 10);
        endInput.value = now.toISOString().slice(0, 10);
    }

    loadReport();
}

async function loadReport() {
    const start = document.getElementById('report-start').value;
    const end = document.getElementById('report-end').value;

    document.getElementById('rep-period-label').textContent = `${formatDate(start)} – ${formatDate(end)}`;

    const params = new URLSearchParams();
    if (start) params.append('startDate', start);
    if (end) params.append('endDate', end);

    try {
        const res = await API.get(`/api/reports/analytics?${params.toString()}`);
        if (!res || !res.success) return;

        const data = res.data;
        const currency = data.currencySymbol || '$';

        // Summary Metric Tiles
        document.getElementById('rep-income').textContent = formatCurrency(data.totalIncome, currency);
        document.getElementById('rep-expense').textContent = formatCurrency(data.totalExpenses, currency);
        
        const savEl = document.getElementById('rep-savings');
        savEl.textContent = formatCurrency(data.netSavings, currency);
        savEl.style.color = Number(data.netSavings) >= 0 ? 'var(--success)' : 'var(--danger)';

        document.getElementById('rep-rate').textContent = `Savings Rate: ${data.savingsRate}%`;
        document.getElementById('rep-daily').textContent = formatCurrency(data.averageDailyExpense, currency);
        document.getElementById('rep-tx-count').textContent = `${data.totalTransactionCount} recorded transactions`;

        // Render Daily Timeline Chart
        renderDailyTimelineChart(data.dailyBreakdown, currency);

        // Render Category Tables
        renderCategoryBreakdownTable('rep-expense-cat-tbody', data.expenseCategories, currency, 'No expenses in this period');
        renderCategoryBreakdownTable('rep-income-cat-tbody', data.incomeCategories, currency, 'No income in this period');

        // Render Budget Variance Table
        renderVarianceTable(data.budgetComparisons, currency);

    } catch (err) {
        showToast('Error loading reports: ' + err.message, 'error');
    }
}

function renderDailyTimelineChart(dailyData, currency) {
    const ctx = document.getElementById('chart-daily-timeline').getContext('2d');

    if (dailyTimelineChart) {
        dailyTimelineChart.destroy();
    }

    if (!dailyData || dailyData.length === 0) {
        dailyTimelineChart = new Chart(ctx, {
            type: 'bar',
            data: { labels: ['No activity recorded in this date range'], datasets: [] },
            options: { responsive: true, maintainAspectRatio: false }
        });
        return;
    }

    const labels = dailyData.map(d => formatDate(d.date));
    const incomes = dailyData.map(d => Number(d.income));
    const expenses = dailyData.map(d => Number(d.expense));

    dailyTimelineChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomes,
                    backgroundColor: '#10B981',
                    borderRadius: 4
                },
                {
                    label: 'Expenses',
                    data: expenses,
                    backgroundColor: '#EF4444',
                    borderRadius: 4
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
                x: {
                    grid: { display: false }
                }
            }
        }
    });
}

function renderCategoryBreakdownTable(tbodyId, categories, currency, emptyMsg) {
    const tbody = document.getElementById(tbodyId);

    if (!categories || categories.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; padding: 24px; color: var(--text-muted);">${emptyMsg}</td></tr>`;
        return;
    }

    tbody.innerHTML = categories.map(cat => `
        <tr>
            <td>
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="category-dot" style="background: ${escapeHtml(cat.color || '#6366F1')};"></span>
                    <span style="font-weight: 600;">${escapeHtml(cat.categoryName)}</span>
                </div>
            </td>
            <td style="text-align: right; font-weight: 700;">
                ${formatCurrency(cat.totalAmount, currency)}
            </td>
            <td style="text-align: right;">
                <span class="badge badge-neutral">${cat.percentage}%</span>
            </td>
        </tr>
    `).join('');
}

function renderVarianceTable(comparisons, currency) {
    const tbody = document.getElementById('rep-variance-tbody');

    if (!comparisons || comparisons.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 24px; color: var(--text-muted);">No category budgets set for this month</td></tr>';
        return;
    }

    tbody.innerHTML = comparisons.map(c => {
        const isOver = c.overBudget;
        const varianceClass = isOver ? 'amount-expense' : 'amount-income';
        const badge = isOver
            ? '<span class="badge badge-expense">Exceeded</span>'
            : '<span class="badge badge-income">Under Budget</span>';

        return `
            <tr>
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="category-dot" style="background: ${escapeHtml(c.color || '#6366F1')};"></span>
                        <span style="font-weight: 600;">${escapeHtml(c.categoryName)}</span>
                    </div>
                </td>
                <td style="text-align: right;">${formatCurrency(c.budgeted, currency)}</td>
                <td style="text-align: right; font-weight: 700;">${formatCurrency(c.actual, currency)}</td>
                <td style="text-align: right; font-weight: 700;" class="${varianceClass}">
                    ${isOver ? '-' : '+'}${formatCurrency(Math.abs(c.variance), currency)}
                </td>
                <td style="text-align: center;">${badge}</td>
            </tr>
        `;
    }).join('');
}
