let allProductionJobs = [];

// Fetch and load all production records from the backend
const loadProductionJobs = () => {
    getHTTPService('/production/all', 'GET', 'json').then((data) => {
        if (!Array.isArray(data)) {
            document.getElementById('productionTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load production records</td></tr>';
            return;
        }
        allProductionJobs = data;
        renderTable(allProductionJobs);
    }).catch((error) => {
        console.error("Error loading production jobs:", error);
        document.getElementById('productionTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load production records</td></tr>';
    });
};

// Render table records dynamically
const renderTable = (jobs) => {
    const tableBody = document.getElementById('productionTableBody');
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

        // Priority badge
        let priorityBadge = '';
        if (job.priority === 'Urgent') {
            priorityBadge = '<span class="badge bg-danger">Urgent</span>';
        } else if (job.priority === 'High') {
            priorityBadge = '<span class="badge bg-warning text-dark">High</span>';
        } else {
            priorityBadge = '<span class="badge bg-success">Normal</span>';
        }

        // Dropdown select status menu
        const phases = ['New Orders', 'Design Phase', 'Printing', 'Finishing', 'Ready to Deliver', 'Dispatched'];
        let selectOptions = phases.map(phase => 
            `<option value="${phase}" ${job.status === phase ? 'selected' : ''}>${phase}</option>`
        ).join('');

        const selectHtml = `
            <select class="form-select status-select mx-auto" onchange="updateStatus('${job.orderId}', this.value)">
                ${selectOptions}
            </select>
        `;

        // Action buttons
        let actionsHtml = '';
        if (job.status === 'Ready to Deliver') {
            actionsHtml = `<button class="btn btn-teal px-3 py-1 btn-sm" onclick="dispatchJob('${job.orderId}')">Dispatch</button>`;
        } else {
            actionsHtml = `<button class="btn btn-sm btn-outline-dark px-3 py-1" onclick="showDetails(${job.id})">Details</button>`;
        }

        row.innerHTML = `
            <td class="fw-bold">${job.orderId || ''}</td>
            <td>${job.customerName || 'N/A'}</td>
            <td>${job.description || ''}</td>
            ${deadlineCell}
            <td>${priorityBadge}</td>
            <td>${selectHtml}</td>
            <td>
                <div class="d-flex justify-content-center gap-2">
                    ${actionsHtml}
                </div>
            </td>
        `;
        tableBody.appendChild(row);
    });
};

// Handle status updates from the dropdown selection
const updateStatus = (orderId, newStatus) => {
    const payload = { orderId: orderId, status: newStatus };

    postHTTPService('/production/update-status', 'POST', 'json', payload).then((response) => {
        swal.fire({
            icon: 'success',
            title: 'Phase Updated',
            text: response?.message || `Job status moved to ${newStatus}.`,
            timer: 1500,
            showConfirmButton: false
        }).then(() => {
            loadProductionJobs();
        });
    }).catch((error) => {
        console.error("Failed to update status:", error);
        swal.fire({
            icon: 'error',
            title: 'Update Failed',
            text: 'Unable to update production job status.'
        });
        loadProductionJobs();
    });
};

// Show job details inside the specs modal
const showDetails = (id) => {
    const job = allProductionJobs.find(j => j.id === id);
    if (!job) return;

    document.getElementById('modalOrderId').textContent = job.orderId || '';
    document.getElementById('modalCustomerName').textContent = job.customerName || 'N/A';
    document.getElementById('modalDescription').textContent = job.description || 'No additional specifications provided.';
    document.getElementById('modalDeadline').textContent = job.deadline || 'N/A';
    
    // Priority markup inside modal
    let priorityMarkup = '';
    if (job.priority === 'Urgent') {
        priorityMarkup = '<span class="badge bg-danger">Urgent</span>';
    } else if (job.priority === 'High') {
        priorityMarkup = '<span class="badge bg-warning text-dark">High</span>';
    } else {
        priorityMarkup = '<span class="badge bg-success">Normal</span>';
    }
    document.getElementById('modalPriority').innerHTML = priorityMarkup;
    document.getElementById('modalStatus').textContent = job.status || '';

    // Trigger show modal
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('detailsModal'));
    modal.show();
};

// Client-side search and status phase filter
const filterProduction = () => {
    const searchVal = document.getElementById('searchProduction').value.toLowerCase().trim();
    const phaseVal = document.getElementById('filterStatus').value;

    const filtered = allProductionJobs.filter((job) => {
        const matchesSearch = (job.orderId || '').toLowerCase().includes(searchVal) ||
                            (job.customerName || '').toLowerCase().includes(searchVal);
        const matchesPhase = !phaseVal || job.status === phaseVal;
        
        return matchesSearch && matchesPhase;
    });

    renderTable(filtered);
};

// Dispatch job action (removes it from tracking queue or updates status to Dispatched)
const dispatchJob = (orderId) => {
    swal.fire({
        icon: 'question',
        title: 'Dispatch Order',
        text: `Are you sure you want to dispatch order ${orderId}? This will remove it from the active production queue.`,
        showCancelButton: true,
        confirmButtonColor: '#17A2B8',
        cancelButtonColor: '#ff5252',
        confirmButtonText: 'Confirm Dispatch'
    }).then((result) => {
        if (!result.isConfirmed) return;

        getHTTPService(`/production/delete-by-order/${orderId}`, 'DELETE', 'json').then((response) => {
            swal.fire({
                icon: 'success',
                title: 'Dispatched Successfully',
                text: response?.message || `Order ${orderId} has been successfully dispatched.`,
                timer: 1500,
                showConfirmButton: false
            }).then(() => {
                loadProductionJobs();
            });
        }).catch((error) => {
            console.error("Failed to dispatch order:", error);
            swal.fire({
                icon: 'error',
                title: 'Dispatch Failed',
                text: 'Unable to process the dispatch requests.'
            });
        });
    });
};

// DOM listener to trigger loading jobs
window.addEventListener('DOMContentLoaded', () => {
    loadProductionJobs();
});
