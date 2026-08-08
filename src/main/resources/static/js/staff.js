let allStaffJobs = [];

// Fetch and load all production records from the backend
const loadStaffJobs = () => {
    getHTTPService('/production/all', 'GET', 'json').then((data) => {
        if (!Array.isArray(data)) {
            document.getElementById('staffTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load production records</td></tr>';
            return;
        }
        allStaffJobs = data;
        renderStaffTable(allStaffJobs);
    }).catch((error) => {
        console.error("Error loading production jobs:", error);
        document.getElementById('staffTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load production records</td></tr>';
    });
};

// Render table records dynamically
const renderStaffTable = (jobs) => {
    const tableBody = document.getElementById('staffTableBody');
    if (!tableBody) return;

    if (!jobs || jobs.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-5">No production jobs found.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    jobs.forEach((job) => {
        const row = document.createElement('tr');

        // Row priority highlight class
        if (job.priority === 'Urgent') {
            row.className = 'priority-urgent';
        } else if (job.priority === 'High') {
            row.className = 'priority-high';
        } else {
            row.className = 'priority-normal';
        }

        if (job.status === 'Ready to Deliver') {
            row.style.opacity = '0.9';
        } else if (job.status === 'Dispatched') {
            row.style.opacity = '0.6';
        }

        // Deadline styling
        let deadlineCell = `<td>${job.deadline || 'N/A'}</td>`;
        if (job.status === 'Ready to Deliver') {
            deadlineCell = `<td class="text-success fw-bold">Completed</td>`;
        } else if (job.priority === 'Urgent' || (job.deadline && new Date(job.deadline) < new Date())) {
            deadlineCell = `<td class="text-danger fw-bold">${job.deadline || 'N/A'}</td>`;
        }

        // Artwork status or view/upload actions
        let artworkCell = '';
        if (job.artworkPath) {
            // Machine operator or Designer can view artwork
            artworkCell = `
                <div class="d-flex align-items-center justify-content-center gap-2">
                    <button class="btn btn-sm btn-teal text-white py-1 px-2" onclick="viewArtwork('${job.artworkPath}', '${job.artworkOriginalName}')">
                        View Design
                    </button>
                    <button class="btn btn-sm btn-outline-danger py-1 px-2" onclick="deleteArtwork('${job.orderId}')">
                        ✕
                    </button>
                </div>
            `;
        } else {
            // Upload button
            artworkCell = `
                <button class="btn btn-sm btn-outline-dark py-1 px-2" onclick="openUploadModal('${job.orderId}')">
                    Upload Design
                </button>
            `;
        }

        // Dropdown select status menu
        const phases = ['New Orders', 'Design Phase', 'Printing', 'Finishing', 'Ready to Deliver', 'Dispatched'];
        let selectOptions = phases.map(phase => 
            `<option value="${phase}" ${job.status === phase ? 'selected' : ''}>${phase}</option>`
        ).join('');

        const selectHtml = `
            <select class="form-select status-select mx-auto" onchange="updateStaffStatus('${job.orderId}', this.value)">
                ${selectOptions}
            </select>
        `;

        row.style.cursor = 'pointer';
        row.title = 'Click to view quotation details';
        row.addEventListener('click', (e) => {
            // Don't trigger row click if user clicked a button/select inside the row
            if (e.target.closest('button, select, a')) return;
            showJobDetails(job.id);
        });

        row.innerHTML = `
            <td class="fw-bold">${job.orderId || ''}</td>
            <td>${job.customerName || 'N/A'}</td>
            <td>${job.description || ''}</td>
            ${deadlineCell}
            <td>${artworkCell}</td>
            <td>${selectHtml}</td>
            <td>
                <div class="d-flex justify-content-center gap-2">
                    <button class="btn btn-sm btn-outline-dark px-3 py-1" onclick="showJobDetails(${job.id})">Details</button>
                </div>
            </td>
        `;
        tableBody.appendChild(row);
    });
};

// Handle status updates from the dropdown selection
const updateStaffStatus = (orderId, newStatus) => {
    const payload = { orderId: orderId, status: newStatus };

    postHTTPService('/production/update-status', 'POST', 'json', payload).then((response) => {
        swal.fire({
            icon: 'success',
            title: 'Phase Updated',
            text: response?.message || `Job status moved to ${newStatus}.`,
            timer: 1500,
            showConfirmButton: false
        }).then(() => {
            loadStaffJobs();
        });
    }).catch((error) => {
        console.error("Failed to update status:", error);
        swal.fire({
            icon: 'error',
            title: 'Update Failed',
            text: 'Unable to update production job status.'
        });
        loadStaffJobs();
    });
};

// Open the upload modal
const openUploadModal = (orderId) => {
    document.getElementById('uploadOrderId').value = orderId;
    document.getElementById('artworkFile').value = '';
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('uploadModal'));
    modal.show();
};

// Submit artwork design via AJAX upload endpoint
const submitArtwork = (event) => {
    event.preventDefault();
    const orderId = document.getElementById('uploadOrderId').value;
    const fileInput = document.getElementById('artworkFile');
    
    if (fileInput.files.length === 0) {
        swal.fire({ icon: 'warning', title: 'File Required', text: 'Please select a design file to upload.' });
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    // Show loading spinner
    swal.fire({
        title: 'Uploading...',
        text: 'Uploading artwork to production job tracking.',
        allowOutsideClick: false,
        didOpen: () => {
            swal.showLoading();
        }
    });

    $.ajax({
        url: `/production/upload-artwork/${orderId}`,
        type: 'POST',
        data: formData,
        contentType: false,
        processData: false,
        success: function (response) {
            swal.fire({
                icon: 'success',
                title: 'Success!',
                text: 'Artwork uploaded successfully.',
                timer: 1500,
                showConfirmButton: false
            }).then(() => {
                const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('uploadModal'));
                modal.hide();
                loadStaffJobs();
            });
        },
        error: function (xhr) {
            console.error(xhr);
            swal.fire({
                icon: 'error',
                title: 'Upload Failed',
                text: 'Failed to upload artwork: ' + (xhr.responseJSON?.message || 'Error occurred')
            });
        }
    });
};

// Delete artwork design file
const deleteArtwork = (orderId) => {
    swal.fire({
        icon: 'warning',
        title: 'Delete Artwork',
        text: 'Are you sure you want to delete this artwork design?',
        showCancelButton: true,
        confirmButtonColor: '#ff5252',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Yes, delete it'
    }).then((result) => {
        if (!result.isConfirmed) return;

        getHTTPService(`/production/delete-artwork/${orderId}`, 'DELETE', 'json')
            .done(function (response) {
                swal.fire({
                    icon: 'success',
                    title: 'Deleted!',
                    text: 'Artwork design has been deleted.',
                    timer: 1500,
                    showConfirmButton: false
                }).then(() => {
                    loadStaffJobs();
                });
            })
            .fail(function (xhr) {
                console.error(xhr);
                swal.fire({
                    icon: 'error',
                    title: 'Deletion Failed',
                    text: 'Failed to delete artwork'
                });
            });
    });
};

// View artwork design details
const viewArtwork = (path, name) => {
    const container = document.getElementById('artworkDisplayContainer');
    document.getElementById('artworkFileName').textContent = name || '';

    const downloadLink = document.getElementById('downloadArtworkLink');
    // Extract just the filename from the stored path (e.g. "uploads/artwork/uuid.png" -> "uuid.png")
    const filename = path.split('/').pop();
    const absolutePath = '/artwork-uploads/' + filename;
    downloadLink.href = absolutePath;
    downloadLink.download = name || filename;

    // Check if image or pdf
    if (path.toLowerCase().endsWith('.pdf')) {
        container.innerHTML = `<iframe src="${absolutePath}" width="100%" height="450px" style="border:none; border-radius: 8px;"></iframe>`;
    } else {
        container.innerHTML = `<img src="${absolutePath}" class="artwork-preview img-fluid" alt="Design artwork preview" onerror="this.outerHTML='<div class=\'text-danger mt-2\'>⚠️ Image could not be loaded. The file may be missing.</div>'">`;
    }

    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('viewArtworkModal'));
    modal.show();
};

// Show full quotation details when a row is clicked
const showJobDetails = (id) => {
    const job = allStaffJobs.find(j => j.id === id);
    if (!job) return;

    const q = job.quotationid || {};

    const fmt = (val, prefix = '', suffix = '') =>
        (val != null && val !== '') ? `${prefix}${val}${suffix}` : 'N/A';

    const fmtCurrency = (val) =>
        (val != null && val > 0) ? `Rs. ${Number(val).toLocaleString('en-LK', {minimumFractionDigits: 2, maximumFractionDigits: 2})}` : 'N/A';

    const statusColor = {
        'APPROVED': '#198754', 'PENDING': '#ffc107', 'REJECTED': '#dc3545'
    };
    const statusBg = statusColor[q.quotationstatus] || '#6c757d';

    swal.fire({
        title: `<strong style="color:#1B263B;">📋 Quotation — ${job.orderId}</strong>`,
        width: 600,
        html: `
            <div class="text-start" style="font-size:0.92rem; line-height:1.7;">

                <!-- Job Info -->
                <div style="background:#f8f9fa; border-radius:10px; padding:12px 16px; margin-bottom:12px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:6px;">
                        <div>
                            <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; letter-spacing:.5px; font-weight:600;">Customer</span><br>
                            <strong>${job.customerName || 'Walk-in Customer'}</strong>
                        </div>
                        <span style="background:${statusBg}; color:#fff; padding:3px 12px; border-radius:20px; font-size:0.78rem; font-weight:600;">
                            ${q.quotationstatus || 'N/A'}
                        </span>
                    </div>
                    <div style="margin-top:8px; color:#444;">${q.quotationdescription || job.description || 'No description'}</div>
                </div>

                <!-- Product Specs -->
                <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:8px; margin-bottom:12px;">
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Product Size</span><br>
                        <strong>${fmt(q.productsize)}</strong>
                    </div>
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Quantity</span><br>
                        <strong>${fmt(q.quantity, '', ' pcs')}</strong>
                    </div>
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Color</span><br>
                        <strong>${fmt(q.color)}</strong>
                    </div>
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Cutting Type</span><br>
                        <strong>${fmt(q.cuttingtype)}</strong>
                    </div>
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Lamination</span><br>
                        <strong>${fmt(q.lamination)}</strong>
                    </div>
                    <div style="background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:10px;">
                        <span style="font-size:0.7rem; color:#6c757d; text-transform:uppercase; font-weight:600;">Binding</span><br>
                        <strong>${fmt(q.bindingtype)}</strong>
                    </div>
                </div>

                <!-- Sheets Banner -->
                <div style="background:#f0f7ff; border:1px solid #cce0ff; border-radius:10px; padding:12px 16px; margin-bottom:12px; display:flex; align-items:center; gap:14px;">
                    <svg xmlns="http://www.w3.org/2000/svg" height="32px" viewBox="0 -960 960 960" width="32px" fill="#1B263B"><path d="M320-240h320v-80H320v80Zm0-160h320v-80H320v80ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520ZM240-800v200-200 640-640Z"/></svg>
                    <div>
                        <span style="font-size:0.72rem; color:#6c757d; text-transform:uppercase; letter-spacing:.5px; font-weight:600;">Total Sheets Needed</span><br>
                        <span style="font-size:1.8rem; font-weight:800; color:#1B263B; line-height:1.1;">
                            ${(job.totalSheetsNeeded != null && job.totalSheetsNeeded > 0) ? job.totalSheetsNeeded : '—'}
                        </span>
                        <span style="color:#6c757d; margin-left:4px; font-size:0.9rem;">sheets</span>
                        &nbsp;&nbsp;
                        <span style="font-size:0.8rem; color:#555;">(Wastage: ${fmt(q.wastageSheets, '', ' sheets')})</span>
                    </div>
                </div>

                <!-- Cost Breakdown -->
                <div style="background:#fff; border:1px solid #e0e0e0; border-radius:10px; overflow:hidden; margin-bottom:8px;">
                    <div style="background:#1B263B; color:#fff; padding:8px 14px; font-size:0.78rem; font-weight:600; text-transform:uppercase; letter-spacing:.5px;">Cost Breakdown</div>
                    <table style="width:100%; border-collapse:collapse; font-size:0.88rem;">
                        <tr style="border-bottom:1px solid #f0f0f0;">
                            <td style="padding:7px 14px; color:#555;">Paper Cost</td>
                            <td style="padding:7px 14px; text-align:right; font-weight:600;">${fmtCurrency(q.paperCost)}</td>
                        </tr>
                        <tr style="border-bottom:1px solid #f0f0f0;">
                            <td style="padding:7px 14px; color:#555;">Finishing Cost</td>
                            <td style="padding:7px 14px; text-align:right; font-weight:600;">${fmtCurrency(q.finishingCost)}</td>
                        </tr>
                        <tr style="border-bottom:1px solid #f0f0f0;">
                            <td style="padding:7px 14px; color:#555;">Impression Cost</td>
                            <td style="padding:7px 14px; text-align:right; font-weight:600;">${fmtCurrency(q.impressionCost)}</td>
                        </tr>
                        <tr style="border-bottom:1px solid #f0f0f0;">
                            <td style="padding:7px 14px; color:#555;">Service Charge (${q.serviceChargePercentage || 0}%)</td>
                            <td style="padding:7px 14px; text-align:right; font-weight:600;">${fmtCurrency(q.serviceChargeAmount)}</td>
                        </tr>
                        <tr style="background:#f8f9fa;">
                            <td style="padding:9px 14px; font-weight:700; color:#1B263B;">Total Amount</td>
                            <td style="padding:9px 14px; text-align:right; font-weight:800; font-size:1rem; color:#1B263B;">${fmtCurrency(q.quotationamount)}</td>
                        </tr>
                    </table>
                </div>

                <div style="font-size:0.78rem; color:#888; text-align:right;">Quotation Date: ${fmt(q.quotationdate)} &nbsp;|&nbsp; Deadline: ${job.deadline || 'N/A'}</div>
            </div>
        `,
        showConfirmButton: true,
        confirmButtonColor: '#1B263B',
        confirmButtonText: 'Close',
        showClass: { popup: 'animate__animated animate__fadeInDown animate__faster' }
    });
};

// Client-side search and status phase filter
const filterStaffJobs = () => {
    const searchVal = document.getElementById('searchStaffJobs').value.toLowerCase().trim();
    const phaseVal = document.getElementById('filterStaffStatus').value;

    const filtered = allStaffJobs.filter((job) => {
        const matchesSearch = (job.orderId || '').toLowerCase().includes(searchVal) ||
                            (job.customerName || '').toLowerCase().includes(searchVal);
        const matchesPhase = !phaseVal || job.status === phaseVal;
        
        return matchesSearch && matchesPhase;
    });

    renderStaffTable(filtered);
};

// DOM listener to trigger loading staff jobs
window.addEventListener('DOMContentLoaded', () => {
    loadStaffJobs();
});
