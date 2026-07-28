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

// Show job details inside an alert box for simple design
const showJobDetails = (id) => {
    const job = allStaffJobs.find(j => j.id === id);
    if (!job) return;

    swal.fire({
        title: `<strong>Job Details: ${job.orderId}</strong>`,
        html: `
            <div class="text-start">
                <p><strong>Customer Name:</strong> ${job.customerName || 'N/A'}</p>
                <p><strong>Description:</strong> ${job.description || 'N/A'}</p>
                <p><strong>Deadline:</strong> ${job.deadline || 'N/A'}</p>
                <p><strong>Priority Level:</strong> ${job.priority || 'Normal'}</p>
                <p><strong>Current Status:</strong> ${job.status || 'New Orders'}</p>
                <p><strong>Design File:</strong> ${job.artworkOriginalName || 'None'}</p>
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1B263B',
        confirmButtonText: 'Close'
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
