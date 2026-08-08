  //  State 
let currentReport = 'sales';
const reportLabels = {
    'sales': 'Sales / Quotation Summary Report',
    'inventory': 'Inventory & Stock-Lot Report',
    'grn': 'GRN Report',
    'purchase-orders': 'Purchase Orders Report',
    'production': 'Production Status Report',
    'supplier-price': 'Supplier Price Comparison Report'
};

const reportFilters = {
    sales: [{ key: 'customer', label: 'Customer' }, { key: 'status', label: 'Quotation Status' }],
    inventory: [{ key: 'variant', label: 'Material / Variant' }, { key: 'lotType', label: 'Lot Type' }, { key: 'status', label: 'Stock Status' }],
    grn: [{ key: 'supplier', label: 'Supplier' }, { key: 'material', label: 'Material' }, { key: 'variant', label: 'Material Variant' }, { key: 'batchNo', label: 'Batch No.' }, { key: 'receivedBy', label: 'Received By' }],
    'purchase-orders': [{ key: 'supplier', label: 'Supplier' }, { key: 'paymentStatus', label: 'Payment Status' }],
    production: [{ key: 'customer', label: 'Customer' }, { key: 'status', label: 'Job Status' }, { key: 'priority', label: 'Priority' }],
    'supplier-price': [{ key: 'supplier', label: 'Supplier' }, { key: 'material', label: 'Material' }]
};

//  Init: set default dates (last 30 days) 
window.addEventListener('DOMContentLoaded', () => {
    const today = new Date();
    const prior = new Date(); prior.setDate(prior.getDate() - 30);
    document.getElementById('endDate').value = today.toISOString().slice(0, 10);
    document.getElementById('startDate').value = prior.toISOString().slice(0, 10);
    renderFilters();
    document.getElementById('startDate').addEventListener('change', loadFilterOptions);
    document.getElementById('endDate').addEventListener('change', loadFilterOptions);
});

//  Select report type 
function selectReport(type, el) {
    currentReport = type;
    document.getElementById('reportTypeLabel').textContent = reportLabels[type];
    document.querySelectorAll('.report-card').forEach(c => c.classList.remove('active-card'));
    el.classList.add('active-card');
    renderFilters();
}

function renderFilters() {
    const filters = reportFilters[currentReport] || [];
    document.getElementById('reportSpecificFilters').innerHTML = filters.length ? `
        <label class="d-block">Additional filters <span class="text-muted fw-normal">(optional)</span></label>
        <div class="d-flex flex-wrap gap-2">${filters.map(f =>
            `<select class="form-select form-select-sm report-filter" data-key="${f.key}" data-label="${f.label}" style="width:auto;min-width:150px"><option value="">${f.label}: All</option></select>`
        ).join('')}</div>` : '';
    loadFilterOptions();
}

function loadFilterOptions() {
    const start = document.getElementById('startDate').value;
    const end = document.getElementById('endDate').value;
    if (!start || !end || start > end) return;
    fetch(`/reports/filter-options?${new URLSearchParams({ type: currentReport, start, end })}`)
        .then(response => response.ok ? response.json() : Promise.reject())
        .then(options => document.querySelectorAll('.report-filter').forEach(select => {
            const selected = select.value;
            const label = select.dataset.label;
            const values = options[select.dataset.key] || [];
            select.innerHTML = `<option value="">${escapeHtml(label)}: All</option>` + values
                .map(value => `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`).join('');
            if (values.includes(selected)) select.value = selected;
        }))
        .catch(() => { /* The report itself can still be generated; its error message remains visible to the user. */ });
}

//   Generate Report
function generateReport() {
    const start = document.getElementById('startDate').value;
    const end = document.getElementById('endDate').value;
    if (!start || !end) { alert('Please select both start and end dates.'); return; }
    if (start > end) { alert('Start date must be before end date.'); return; }

    showLoading(true);
    hideResults();

    const params = new URLSearchParams({ start, end });
    document.querySelectorAll('.report-filter').forEach(input => { if (input.value.trim()) params.set(input.dataset.key, input.value.trim()); });
    getHTTPService(`/reports/${currentReport}?${params.toString()}`, 'GET', 'json')
        .then(data => {
            showLoading(false);
            if (data && data.error) {
                alert('Report error: ' + data.error);
                return;
            }
            renderReport(currentReport, data, start, end);
        })
        .catch(xhr => {
            showLoading(false);
            const msg = xhr.responseJSON?.error || xhr.responseJSON?.message || 'Failed to generate report.';
            alert('Error generating report: ' + msg);
        });
}

function showLoading(on) {
    document.getElementById('loadingOverlay').style.display = on ? 'flex' : 'none';
}
function hideResults() {
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('statsRow').style.display = 'none';
    document.getElementById('tableWrapper').style.display = 'none';
}

// Render  report
function renderReport(type, data, start, end) {
    const statsRow = document.getElementById('statsRow');
    const tableWrapper = document.getElementById('tableWrapper');

    let statsHtml = '';
    let tableHtml = '';

    switch (type) {
        // ── 1. Sales / Quotation Summary ─────────────────────────────
        case 'sales':
            statsHtml = `
            ${statCard('Total Quoted Value', 'Rs. ' + fmt(data.totalRevenue), '#ede9fe', '#7c3aed')}
            ${statCard('Total Advance', 'Rs. ' + fmt(data.totalAdvance), '#d1fae5', '#059669')}
            ${statCard('Total Quotations', data.totalOrders, '#e0f2fe', '#0284c7')}
            ${statCard('Pending', data.pendingCount, '#fef3c7', '#d97706')}
            ${statusBadgeStats(data.byStatus)}`;
            tableHtml = buildTable(
                ['Quote ID', 'Date', 'Customer', 'Product / Size', 'Qty', 'Quote Amt (Rs.)', 'Advance (Rs.)', 'Status', 'Expiry Date'],
                data.rows,
                ['id', 'date', 'customer', 'productSize', 'quantity', 'amount', 'advanceAmount', 'status', 'expiryDate'],
                { status: statusBadge, amount: v => fmt(v), advanceAmount: v => fmt(v) }
            );
            break;

        // ── 2. Inventory / Stock-Lot ──────────────────────────────────
        case 'inventory':
            statsHtml = `
            ${statCard('Total Lots', data.totalLots, '#d1fae5', '#059669')}
            ${statCard('Total Qty On-Hand', data.totalQty, '#e0f2fe', '#0284c7')}
            ${statCard('Below Reorder Level', data.belowReorderCount, '#fee2e2', '#dc2626')}`;
            tableHtml = buildTableInventory(data.rows);
            break;

        // ── 3a. GRN ──────────────────────────────────────────────────
        case 'grn':
            statsHtml = `
            ${statCard('Total GRNs', data.totalGRNs, '#fef3c7', '#d97706')}
            ${statCard('Total Qty Received', data.totalQtyReceived, '#d1fae5', '#059669')}`;
            tableHtml = buildTable(
                ['GRN Number', 'Received Date', 'Supplier', 'Supplier Invoice', 'Batch No', 'Material', 'Qty', 'Units', 'Notes'],
                data.rows,
                ['grnNumber', 'receivedDate', 'supplier', 'supplierInvoice', 'batchNo', 'material', 'receivedQty', 'units', 'notes']
            );
            break;

        // ── 3b. Purchase Orders ───────────────────────────────────────
        case 'purchase-orders':
            statsHtml = `
            ${statCard('Total Orders', data.totalOrders, '#fee2e2', '#dc2626')}
            ${statCard('Total Paid (Rs.)', 'Rs. ' + fmt(data.totalPaid), '#fef3c7', '#d97706')}
            ${statCard('Unpaid Orders', data.unpaidCount, '#ede9fe', '#7c3aed')}
            ${statusBadgeStats(data.byPaymentStatus)}`;
            tableHtml = buildTable(
                ['PO ID', 'Supplier', 'Order Date', 'Items', 'Qty', 'Payment Status', 'Method', 'Paid (Rs.)', 'Notes'],
                data.rows,
                ['id', 'supplier', 'orderDate', 'items', 'quantity', 'paymentStatus', 'paymentMethod', 'paidAmount', 'notes'],
                { paymentStatus: statusBadge, paidAmount: v => v != null ? fmt(v) : '-' }
            );
            break;

        // ── 4. Production Status ──────────────────────────────────────
        case 'production':
            statsHtml = `
            ${statCard('Total Jobs', data.totalJobs, '#e0f2fe', '#0284c7')}
            ${statCard('Overdue Jobs', data.overdueCount, '#fee2e2', '#dc2626')}
            ${statusBadgeStats(data.byStatus)}
            ${statusBadgeStats(data.byPriority, 'Priority')}`;
            tableHtml = buildTableProduction(data.rows);
            break;

        // ── 5. Supplier Price Comparison ──────────────────────────────
        case 'supplier-price':
            statsHtml = `
            ${statCard('Total Replies', data.totalReplies, '#fce7f3', '#db2777')}`;
            tableHtml = buildTable(
                ['Request ID', 'Material', 'Specification', 'Supplier', 'Unit Price (Rs.)', 'Qty', 'Total (Rs.)', 'Delivery Charge', 'Delivery Date', 'Reply Date'],
                data.rows,
                ['requestId', 'material', 'specification', 'supplier', 'unitPrice', 'quantity', 'totalAmount', 'deliveryCharge', 'deliveryDate', 'replyDate'],
                { unitPrice: v => v != null ? fmt(v) : '-', totalAmount: v => v != null ? fmt(v) : '-', deliveryCharge: v => v != null ? fmt(v) : '-' }
            );
            break;
    }

    statsRow.innerHTML = statsHtml;
    statsRow.style.display = 'flex';
    tableWrapper.innerHTML = `
    <div style="padding:16px 20px; border-bottom:1px solid var(--border-color,#e2e8f0); display:flex; justify-content:space-between; align-items:center;">
        <div>
            <strong>${reportLabels[type]}</strong>
            <span style="font-size:0.8rem; color:var(--text-muted,#64748b); margin-left:10px;">${start} to ${end}${activeFilterSummary()}</span>
        </div>
        <span style="font-size:0.8rem; color:var(--text-muted,#64748b);">${data.rows ? data.rows.length : 0} records</span>
    </div>
    <div style="overflow-x:auto;">${tableHtml}</div>`;
    tableWrapper.style.display = 'block';
}

function activeFilterSummary() {
    const selected = [...document.querySelectorAll('.report-filter')].filter(input => input.value.trim())
        .map(input => `${input.dataset.label}: ${input.value.trim()}`);
    return selected.length ? ` · Filters: ${selected.join(', ')}` : '';
}

function escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char]);
}

// ── Inventory table (custom: highlight below-reorder rows) ────────
function buildTableInventory(rows) {
    if (!rows || rows.length === 0) return emptyTableMsg();
    const headers = ['Lot ID', 'Variant (Material / GSM / Size)', 'Qty', 'Unit', 'Lot Type', 'Status', 'Source GRN', 'Received Date', 'Reorder Lvl'];
    const head = headers.map(h => `<th>${h}</th>`).join('');
    const body = rows.map(row => {
        const below = row.belowReorder === true;
        const rowStyle = below ? 'background:#fff5f5;' : '';
        const qtyStyle = below ? 'color:#dc2626;font-weight:700;' : '';
        const reorderCell = row.reorderLevel !== '-'
            ? `<td>${row.reorderLevel}${below ? ' <span class="badge-status" style="background:#fee2e2;color:#991b1b;margin-left:4px;">Low</span>' : ''}</td>`
            : `<td>-</td>`;
        return `<tr style="${rowStyle}">
            <td>${row.lotId}</td>
            <td>${row.variant}</td>
            <td style="${qtyStyle}">${row.quantity}</td>
            <td>${row.unit}</td>
            <td>${row.lotType}</td>
            <td>${statusBadge(row.status)}</td>
            <td>${row.sourceGrn}</td>
            <td>${row.receivedDate}</td>
            ${reorderCell}
        </tr>`;
    }).join('');
    return `<table class="tablestyle"><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

// ── Production table (custom: color-code daysRemaining) ────────────
function buildTableProduction(rows) {
    if (!rows || rows.length === 0) return emptyTableMsg();
    const headers = ['Order ID', 'Customer', 'Description', 'Deadline', 'Priority', 'Status', 'Days Remaining'];
    const head = headers.map(h => `<th>${h}</th>`).join('');
    const body = rows.map(row => {
        let daysCell;
        const d = row.daysRemaining;
        if (d == null) {
            daysCell = `<td><span style="color:var(--text-muted,#64748b)">No deadline</span></td>`;
        } else if (d < 0) {
            daysCell = `<td><span class="badge-status" style="background:#fee2e2;color:#991b1b;">${Math.abs(d)}d overdue</span></td>`;
        } else if (d <= 3) {
            daysCell = `<td><span class="badge-status" style="background:#fef3c7;color:#92400e;">${d}d left</span></td>`;
        } else {
            daysCell = `<td><span class="badge-status" style="background:#d1fae5;color:#065f46;">${d}d left</span></td>`;
        }
        return `<tr>
            <td>${row.orderId}</td>
            <td>${row.customer}</td>
            <td>${row.description}</td>
            <td>${row.deadline}</td>
            <td>${priorityBadge(row.priority)}</td>
            <td>${statusBadge(row.status)}</td>
            ${daysCell}
        </tr>`;
    }).join('');
    return `<table class="tablestyle"><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

// ── Helpers ───────────────────────────────────────────
function emptyTableMsg() {
    return `<div class="empty-state"><svg xmlns="http://www.w3.org/2000/svg" height="48" viewBox="0 -960 960 960" width="48" fill="currentColor"><path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Z"/></svg><h6 class="mt-2">No data found for selected date range</h6></div>`;
}

function statCard(label, value, bg, color) {
    return `<div class="col-6 col-md-3"><div class="stat-card"><div class="stat-value" style="color:${color}">${value}</div><div class="stat-label">${label}</div></div></div>`;
}

function statusBadgeStats(obj, label) {
    if (!obj || Object.keys(obj).length === 0) return '';
    const items = Object.entries(obj).map(([k, v]) => `<span class="badge-status me-1" style="background:#f1f5f9;color:#334155">${k}: ${v}</span>`).join('');
    return `<div class="col-12 col-md-6"><div class="stat-card"><div class="stat-label mb-2">${label || 'By Status'}</div><div>${items}</div></div></div>`;
}

function buildTable(headers, rows, fields, formatters) {
    if (!rows || rows.length === 0) return emptyTableMsg();
    const head = headers.map(h => `<th>${h}</th>`).join('');
    const body = rows.map(row => {
        const cells = fields.map(f => {
            let v = row[f];
            if (v == null) v = '-';
            if (formatters && formatters[f]) v = formatters[f](v);
            return `<td>${v}</td>`;
        }).join('');
        return `<tr>${cells}</tr>`;
    }).join('');
    return `<table class="tablestyle"><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

function statusBadge(v) {
    if (!v || v === '-') return '-';
    const colors = {
        'Approved': '#d1fae5:#065f46', 'Approved / In Production': '#d1fae5:#065f46',
        'Pending': '#fef3c7:#92400e', 'PENDING': '#fef3c7:#92400e',
        'Rejected': '#fee2e2:#991b1b',
        'Paid': '#d1fae5:#065f46', 'Unpaid': '#fee2e2:#991b1b', 'Partial': '#fef3c7:#92400e',
        'Dispatched': '#ede9fe:#5b21b6', 'Ready to Deliver': '#d1fae5:#065f46',
        'Printing': '#e0f2fe:#075985', 'Design Phase': '#fef3c7:#92400e',
        'Finishing': '#e0f2fe:#075985', 'New Orders': '#f1f5f9:#334155',
        'Available': '#d1fae5:#065f46', 'Used': '#fef3c7:#92400e', 'Damaged': '#fee2e2:#991b1b'
    };
    const pair = colors[v] ? colors[v].split(':') : ['#f1f5f9', '#334155'];
    return `<span class="badge-status" style="background:${pair[0]};color:${pair[1]}">${v}</span>`;
}

function priorityBadge(v) {
    if (!v || v === '-') return '-';
    const colors = { 'Urgent': '#fee2e2:#991b1b', 'High': '#fef3c7:#92400e', 'Normal': '#d1fae5:#065f46' };
    const pair = colors[v] ? colors[v].split(':') : ['#f1f5f9', '#334155'];
    return `<span class="badge-status" style="background:${pair[0]};color:${pair[1]}">${v}</span>`;
}

function fmt(v) {
    if (v == null) return '-';
    return Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
