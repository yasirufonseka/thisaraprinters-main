

//  On Page Load 
document.addEventListener('DOMContentLoaded', () => {
    loadSupplierOrders();
    loadCustomerPayments();
});

// handle supplier payment
let allSupplierOrders = [];

function loadSupplierOrders() {
    getHTTPService('/payment/supplier-orders', 'GET', 'json')
        .then(orders => {
            allSupplierOrders = orders;
            renderSupplierTable(orders);
            updateSupplierSummary(orders);
        })
        .catch(() => showError('Failed to load supplier orders.'));
}

function renderSupplierTable(orders) {
    const tbody = document.getElementById('supplierOrdersBody');
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No purchase orders found.</td></tr>';
        return;
    }
    tbody.innerHTML = orders.map(o => {
        const statusClass = getStatusBadgeClass(o.paymentStatus);
        const supplierName = o.supplier ? o.supplier.companyname : '-';
        const amount = o.paidAmount != null ? 'Rs. ' + Number(o.paidAmount).toLocaleString('en-LK', {minimumFractionDigits: 2}) : '-';
        return `
        <tr>
            <td><strong>PO-${o.id}</strong></td>
            <td>${supplierName}</td>
            <td>${o.orderDate || '-'}</td>
            <td>${o.items || '-'}</td>
            <td>${o.quantity || '-'}</td>
            <td><span class="badge ${statusClass}">${o.paymentStatus || 'Unpaid'}</span></td>
            <td>
                <button class="btn btn-teal btn-sm me-1" onclick="openSupplierPaymentModal(${JSON.stringify(o).replace(/"/g, '&quot;')})">
                    Edit Payment
                </button>
            </td>
        </tr>`;
    }).join('');
}


function updateSupplierSummary(orders) {
    const total  = orders.length;
    const paid   = orders.filter(o => o.paymentStatus === 'Paid').length;
    const pending = total - paid;
    document.getElementById('spTotal').textContent  = total;
    document.getElementById('spPaid').textContent   = paid;
    document.getElementById('spPending').textContent = pending;
}

// Search supplier orders
document.getElementById('searchSupplierOrder').addEventListener('input', function () {
    const q = this.value.toLowerCase();
    const filtered = allSupplierOrders.filter(o =>
        (o.supplier?.companyname || '').toLowerCase().includes(q) ||
        ('PO-' + o.id).toLowerCase().includes(q) ||
        (o.items || '').toLowerCase().includes(q)
    );
    renderSupplierTable(filtered);
});

//  Supplier payment modal
let currentOrderId = null;

function openSupplierPaymentModal(order) {
    currentOrderId = order.id;
    document.getElementById('spEditPoId').value = order.id;
    document.getElementById('spEditStatus').value = order.paymentStatus || 'Unpaid';
    document.getElementById('spEditNotes').value  = order.paymentNotes || '';

    handleSupplierStatusChange(order.paymentStatus);

    const modal = new bootstrap.Modal(document.getElementById('editSupplierPaymentModal'));
    modal.show();
}

function handleSupplierStatusChange(status) {
    const section = document.getElementById('spPaymentDetailsSection');
    const proofSection = document.getElementById('spProofSection');
    if (status === 'Paid') {
        section.style.display = 'block';
        proofSection.style.display = 'block';
    } else {
        section.style.display = 'none';
        proofSection.style.display = 'none';
    }
}

function submitSupplierPaymentUpdate(e) {
    e.preventDefault();
    const form = document.getElementById('editSupplierPaymentForm');
    const formData = new FormData(form);

    showLoading('Updating supplier payment...');

    $.ajax({
        url: `/payment/supplier/${currentOrderId}/payment`,
        type: 'POST',
        data: formData,
        contentType: false,
        processData: false
    })
    .done(res => {
        Swal.fire({ icon: 'success', title: 'Updated!', text: res.message || 'Payment updated', timer: 2000, showConfirmButton: false });
        bootstrap.Modal.getInstance(document.getElementById('editSupplierPaymentModal')).hide();
        loadSupplierOrders();
    })
    .fail(() => showError('Failed to update payment.'));
}


//  CUSTOMER PAYMENTS


let allCustomerPayments = [];

function loadCustomerPayments() {
    getHTTPService('/payment/customer-payments', 'GET', 'json')
        .then(payments => {
            allCustomerPayments = payments;
            renderCustomerTable(payments);
            updateCustomerSummary(payments);
        })
        .catch(() => showError('Failed to load customer payments.'));
}

// add customer payment details into table

function renderCustomerTable(payments) {
    const tbody = document.getElementById('customerPaymentsBody');
    if (!payments || payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No customer payments recorded yet.</td></tr>';
        return;
    }
    tbody.innerHTML = payments.map(p => {
        const quotation = p.quotation || p.production?.quotationid;
        const statusClass = getStatusBadgeClass(p.paymentStatus);
        const customerName = p.customer ? p.customer.name : (quotation?.customer?.name || '-');
        const quotationId  = quotation ? quotation.id : '-';
        const total = quotation ? 'Rs. ' + Number(quotation.quotationamount).toLocaleString('en-LK', {minimumFractionDigits: 2}) : '-';
        const paid  = p.paidAmount != null ? 'Rs. ' + Number(p.paidAmount).toLocaleString('en-LK', {minimumFractionDigits: 2}) : '-';
        return `
        <tr>
            <td><strong>${p.invoiceNo || 'PENDING'}</strong></td>
            <td>${customerName}</td>
            <td>Q-${quotationId}</td>
            <td>${total}</td>
            <td>${paid}</td>
            <td>${p.paymentDate || '-'}</td>
            <td><span class="badge ${statusClass}">${p.paymentStatus || '-'}</span></td>
            <td>
                <button class="btn btn-invoice btn-sm me-1" onclick="generateInvoice(${p.id})">
                    <svg xmlns="http://www.w3.org/2000/svg" height="14px" viewBox="0 -960 960 960" width="14px" fill="currentColor" class="me-1">
                        <path d="M360-240h240v-80H360v80Zm0-160h240v-80H360v80ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520ZM240-800v200-200 640-640Z"/>
                    </svg>
                    Invoice
                </button>
                <button class="btn btn-teal btn-sm" onclick="openEditCustomerPaymentModal(${JSON.stringify(p).replace(/"/g, '&quot;')})">Edit</button>
            </td>
        </tr>`;
    }).join('');
}

// update customer payment summary

function updateCustomerSummary(payments) {
    const total   = payments.length;
    const paid    = payments.filter(p => p.paymentStatus === 'Paid').length;
    const pending = total - paid;
    document.getElementById('cpTotal').textContent   = total;
    document.getElementById('cpPaid').textContent    = paid;
    document.getElementById('cpPending').textContent = pending;
}

// Search customer payments
document.getElementById('searchCustomerPayment').addEventListener('input', function () {
    const q = this.value.toLowerCase();
    const filtered = allCustomerPayments.filter(p =>
        (p.customer?.name || '').toLowerCase().includes(q) ||
        (p.invoiceNo || '').toLowerCase().includes(q) ||
        (p.paymentStatus || '').toLowerCase().includes(q)
    );
    renderCustomerTable(filtered);
});

//  Add Customer Payment Modal 
function submitAddCustomerPayment(e) {
    e.preventDefault();

    const quotationId  = document.getElementById('cpQuotationId').value;
    const customerId   = document.getElementById('cpCustomerId').value;
    const status       = document.getElementById('cpStatus').value;
    const method       = document.getElementById('cpMethod').value;
    const amount       = document.getElementById('cpAmount').value;
    const referenceNo  = document.getElementById('cpReferenceNo')?.value || '';
    const notes        = document.getElementById('cpNotes').value;

    if (!quotationId || !customerId || !status || !method || !amount) {
        showError('Please fill all required fields.');
        return;
    }

    const payload = {
        quotationId:   parseInt(quotationId),
        customerId:    parseInt(customerId),
        paymentStatus: status,
        paymentMethod: method,
        paidAmount:    parseFloat(amount),
        referenceNo:   referenceNo,
        paymentNotes:  notes
    };

    showLoading('Recording payment...');

    postHTTPService('/payment/customer/add', 'POST', 'json', payload)
        .then(res => {
            Swal.fire({ icon: 'success', title: 'Recorded!', text: res.message || 'Payment recorded successfully', timer: 2200, showConfirmButton: false });
            bootstrap.Modal.getInstance(document.getElementById('addCustomerPaymentModal')).hide();
            document.getElementById('addCustomerPaymentForm').reset();
            loadCustomerPayments();
        })
        .catch(() => showError('Failed to record payment.'));
}

//  Edit Customer Payment Modal 
let currentCustomerPaymentId = null;

function openEditCustomerPaymentModal(payment) {
    currentCustomerPaymentId = payment.id;
    document.getElementById('cpEditStatus').value      = payment.paymentStatus || '';
    document.getElementById('cpEditMethod').value      = payment.paymentMethod || '';
    document.getElementById('cpEditAmount').value      = payment.paidAmount   || '';
    if (document.getElementById('cpEditReferenceNo')) {
        document.getElementById('cpEditReferenceNo').value = payment.referenceNo || '';
    }
    document.getElementById('cpEditNotes').value       = payment.paymentNotes || '';

    const modal = new bootstrap.Modal(document.getElementById('editCustomerPaymentModal'));
    modal.show();
}

function submitEditCustomerPayment(e) {
    e.preventDefault();

    const payload = {
        paymentStatus: document.getElementById('cpEditStatus').value,
        paymentMethod: document.getElementById('cpEditMethod').value,
        paidAmount:    parseFloat(document.getElementById('cpEditAmount').value),
        referenceNo:   document.getElementById('cpEditReferenceNo')?.value || '',
        paymentNotes:  document.getElementById('cpEditNotes').value
    };

    showLoading('Updating payment...');

    postHTTPService(`/payment/customer/update/${currentCustomerPaymentId}`, 'POST', 'json', payload)
        .then(res => {
            Swal.fire({ icon: 'success', title: 'Updated!', text: res.message || 'Payment updated successfully', timer: 2000, showConfirmButton: false });
            bootstrap.Modal.getInstance(document.getElementById('editCustomerPaymentModal')).hide();
            loadCustomerPayments();
        })
        .catch(() => showError('Failed to update payment.'));
}

// Populate customer id when quotation is selected in the Add Payment form
function onQuotationSelected(select) {
    const opt  = select.options[select.selectedIndex];
    const cid  = opt.dataset.customerId || '';
    const cname = opt.dataset.customerName || '';
    document.getElementById('cpCustomerId').value = cid;
    document.getElementById('cpCustomerDisplay').value = cname;

    const amount = opt.dataset.quotationamount || '';
    const advance = opt.dataset.advance || 0;
    const balance = parseFloat(amount) - parseFloat(advance);
    document.getElementById('cpQuotationTotal').textContent = amount ? 'Rs. ' + parseFloat(amount).toLocaleString('en-LK', {minimumFractionDigits: 2}) : '-';
    document.getElementById('cpAdvanceAmount').textContent  = 'Rs. ' + parseFloat(advance).toLocaleString('en-LK', {minimumFractionDigits: 2});
    document.getElementById('cpBalanceDue').textContent     = 'Rs. ' + balance.toLocaleString('en-LK', {minimumFractionDigits: 2});
}


//   Generate customer invoice


function generateInvoice(paymentId) {
    getHTTPService(`/payment/invoice/${paymentId}`, 'GET', 'json')
        .then(data => {
            renderInvoice(data);
            const modal = new bootstrap.Modal(document.getElementById('invoiceModal'));
            modal.show();
        })
        .catch(() => showError('Could not load invoice data.'));
}

function renderInvoice(data) {
    const q  = data.quotation || data.production?.quotationid || {};
    const c  = data.customer  || q.customer || {};
    const now = new Date();
    const dateStr = now.toLocaleDateString('en-LK', { year: 'numeric', month: 'long', day: '2-digit' });

    const balance = (q.quotationamount || 0) - (data.paidAmount || 0);

    const html = `
    <div class="invoice-header">
        <div>
            <div class="invoice-company-name">Thisara Printers</div>
            <div class="invoice-company-sub">Professional Printing Solutions</div>
            <div class="invoice-company-sub" style="margin-top:4px;">Tel: 032 2297228 | thisaraprinters@yahoo.com</div>
        </div>
        <div class="text-end">
            <div class="invoice-badge">INVOICE</div>
            <div style="margin-top:10px; font-size:0.82rem; color:#555;">
                <strong>Invoice No:</strong> ${data.invoiceNo || '-'}<br>
                <strong>Date:</strong> ${dateStr}
            </div>
        </div>
    </div>

    <div class="invoice-meta">
        <div class="invoice-meta-block">
            <label>Bill To</label>
            <p style="font-size:1rem;">${c.name || '-'}</p>
            <p style="font-weight:400; font-size:0.85rem; color:#555;">${c.address || ''}</p>
            <p style="font-weight:400; font-size:0.85rem; color:#555;">${c.phone || ''}</p>
            <p style="font-weight:400; font-size:0.85rem; color:#555;">${c.email || ''}</p>
        </div>
        <div class="invoice-meta-block">
            <label>Payment Info</label>
            <p>Method: ${data.paymentMethod || '-'}</p>
            <p>Date: ${data.paymentDate || '-'}</p>
            <p>Status: <span style="color:${data.paymentStatus === 'Paid' ? '#198754' : '#FFC107'}">${data.paymentStatus || '-'}</span></p>
        </div>
    </div>

    <table class="invoice-table">
        <thead>
            <tr>
                <th>Description</th>
                <th>Details</th>
                <th class="text-end">Amount (Rs.)</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Paper Cost</td>
                <td>${q.papertype || '-'} | ${q.productsize || '-'}</td>
                <td class="text-end">${fmt(q.paperCost)}</td>
            </tr>
            <tr>
                <td>Impression Cost</td>
                <td>${q.color || '-'} | Qty: ${q.quantity || '-'}</td>
                <td class="text-end">${fmt(q.impressionCost)}</td>
            </tr>
            <tr>
                <td>Finishing Cost</td>
                <td>Lamination: ${q.lamination || 'None'} | Foiling: ${q.foiling || 'None'}</td>
                <td class="text-end">${fmt(q.finishingCost)}</td>
            </tr>
            <tr>
                <td>Service Charge</td>
                <td>${q.serviceChargePercentage || 0}%</td>
                <td class="text-end">${fmt(q.serviceChargeAmount)}</td>
            </tr>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="2">Subtotal</td>
                <td class="text-end">${fmt(q.quotationamount)}</td>
            </tr>
            <tr>
                <td colspan="2">Advance Paid</td>
                <td class="text-end" style="color:#198754;">- ${fmt(q.advanceamount)}</td>
            </tr>
            <tr>
                <td colspan="2">Amount Paid (This Payment)</td>
                <td class="text-end" style="color:#198754;">- ${fmt(data.paidAmount)}</td>
            </tr>
            <tr class="invoice-total-row">
                <td colspan="2">Balance Due</td>
                <td class="text-end">${fmt(balance < 0 ? 0 : balance)}</td>
            </tr>
        </tfoot>
    </table>

    ${data.paymentNotes ? `<div style="font-size:0.83rem;color:#555;margin-top:8px;"><strong>Notes:</strong> ${data.paymentNotes}</div>` : ''}

    <div class="invoice-footer">
        Thank you for your business! &nbsp;|&nbsp; Thisara Printers &nbsp;|&nbsp; Generated on ${dateStr}
    </div>`;

    document.getElementById('invoicePrintArea').innerHTML = html;
}

function fmt(val) {
    if (val == null || isNaN(val)) return 'Rs. 0.00';
    return 'Rs. ' + Number(val).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function printInvoice() {
    window.print();
}

// ══════════════════════════════════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════════════════════════════════

function getStatusBadgeClass(status) {
    switch ((status || '').toLowerCase()) {
        case 'paid':    return 'bg-success';
        case 'pending': return 'bg-warning text-dark';
        case 'unpaid':  return 'bg-danger';
        case 'partial': return 'bg-info text-dark';
        default:        return 'bg-secondary';
    }
}

function showLoading(msg) {
    Swal.fire({ title: msg, allowOutsideClick: false, didOpen: () => Swal.showLoading() });
}

function showError(msg) {
    Swal.fire({ icon: 'error', title: 'Error', text: msg });
}
