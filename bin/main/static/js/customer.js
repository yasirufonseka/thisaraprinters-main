let isUpdateMode = false;
let selectedCustomerId = null;
let allCustomers = [];

// Validation rules and helper functions
const rules = {
    companyName: (val) => val.length >= 2 && val.length <= 100,
    companyContact: (val) => /^0\d{9}$/.test(val),
    customerEmail: (val) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val),
    customerAddress: (val) => val.length > 0,
    contactPersonContact: (val) => val.length === 0 || /^0\d{9}$/.test(val),
};

const validateField = (elementId, validationFn) => {
    const element = document.getElementById(elementId);
    if (!element) return true;
    const value = element.value.trim();
    const isValid = validationFn(value);
    
    if (isValid) {
        element.classList.remove('is-invalid');
        element.classList.add('is-valid');
    } else {
        element.classList.remove('is-valid');
        element.classList.add('is-invalid');
    }
    return isValid;
};

// Setup validation listeners
const setupLiveValidation = () => {
    const fields = [
        { id: 'companyName', rule: rules.companyName },
        { id: 'companyContact', rule: rules.companyContact },
        { id: 'customerEmail', rule: rules.customerEmail },
        { id: 'customerAddress', rule: rules.customerAddress },
        { id: 'contactPersonContact', rule: rules.contactPersonContact }
    ];

    fields.forEach((field) => {
        const el = document.getElementById(field.id);
        if (el) {
            el.addEventListener('input', () => validateField(field.id, field.rule));
            el.addEventListener('blur', () => validateField(field.id, field.rule));
        }
    });
};

const clearValidationStates = () => {
    const form = document.getElementById('customerForm');
    if (form) {
        form.classList.remove('was-validated');
    }
    const inputs = ['companyName', 'companyContact', 'customerEmail', 'customerAddress', 'contactPersonContact'];
    inputs.forEach((id) => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.remove('is-valid', 'is-invalid');
        }
    });
};

const resetCustomerForm = () => {
    isUpdateMode = false;
    selectedCustomerId = null;
    document.getElementById('customerForm').reset();
    document.getElementById('customerId').value = 'Auto-generated';
    document.getElementById('customerHiddenId').value = '';
    document.getElementById('customerSubmitButton').textContent = 'Add Customer';
    clearValidationStates();
    const title = document.getElementById('customerModalLabel');
    if (title) {
        title.textContent = 'Add New Customer';
    }
};

const openCustomerModal = () => {
    resetCustomerForm();
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('customerModal'));
    modal.show();
};

const validateAllFields = () => {
    const isNameValid = validateField('companyName', rules.companyName);
    const isContactValid = validateField('companyContact', rules.companyContact);
    const isEmailValid = validateField('customerEmail', rules.customerEmail);
    const isAddressValid = validateField('customerAddress', rules.customerAddress);
    const isPersonContactValid = validateField('contactPersonContact', rules.contactPersonContact);

    return isNameValid && isContactValid && isEmailValid && isAddressValid && isPersonContactValid;
};

const submitCustomer = (event) => {
    event.preventDefault();

    const formIsValid = validateAllFields();
    if (!formIsValid) {
        document.getElementById('customerForm').classList.add('was-validated');
        swal.fire({
            icon: 'warning',
            title: 'Validation failed',
            text: 'Please correct all marked errors before submitting.',
        });
        return;
    }

    const payload = {
        name: document.getElementById('companyName').value.trim(),
        address: document.getElementById('customerAddress').value.trim(),
        email: document.getElementById('customerEmail').value.trim(),
        phone: document.getElementById('companyContact').value.trim(),
        contactperson: document.getElementById('contactPersonName').value.trim(),
        contactpersonphone: document.getElementById('contactPersonContact').value.trim(),
    };

    const confirmTitle = isUpdateMode ? 'Update Customer' : 'Add Customer';
    const confirmText = isUpdateMode ? 'Are you sure you want to update this customer?' : 'Are you sure you want to add this customer?';
    const url = isUpdateMode ? `/customer/update/${selectedCustomerId}` : '/customer/add/customer';

    swal.fire({
        icon: 'question',
        title: confirmTitle,
        text: confirmText,
        showCancelButton: true,
        confirmButtonColor: '#00AEEF',
        cancelButtonColor: '#ff5252',
        confirmButtonText: isUpdateMode ? 'Update' : 'Save'
    }).then((result) => {
        if (!result.isConfirmed) {
            return;
        }

        postHTTPService(url, 'POST', 'json', payload).then((response) => {
            swal.fire({
                icon: 'success',
                title: confirmTitle,
                text: response && response.message ? response.message : 'Saved successfully.',
                timer: 1500,
                showConfirmButton: false,
            }).then(() => {
                const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('customerModal'));
                modal.hide();
                refreshCustomerTable();
                resetCustomerForm();
            });
        }).catch((jqXHR) => {
            console.error(jqXHR);
            swal.fire({
                icon: 'error',
                title: 'Save failed',
                text: jqXHR?.responseJSON?.message || 'Unable to save customer details.',
            });
        });
    });
};

const refreshCustomerTable = () => {
    getHTTPService('/customer/all', 'GET', 'json').then((data) => {
        if (!Array.isArray(data)) {
            document.getElementById('customerTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load customers</td></tr>';
            return;
        }
        allCustomers = data;
        updateStats();
        populateCustomerTable(allCustomers);
    }).catch((error) => {
        console.error(error);
        document.getElementById('customerTableBody').innerHTML = '<tr><td colspan="7" class="text-center py-5">Unable to load customers</td></tr>';
    });
};

const updateStats = () => {
    const totalElement = document.getElementById('statTotalCustomers');
    const activeElement = document.getElementById('statActiveThisMonth');

    if (totalElement) {
        totalElement.textContent = allCustomers.length;
    }

    // Active this month: calculate based on createddate or updateddate being within current month/year
    if (activeElement) {
        const now = new Date();
        const currentMonth = now.getMonth(); // 0-indexed
        const currentYear = now.getFullYear();

        const activeCount = allCustomers.filter((customer) => {
            if (!customer.updateddate) return false;
            const updateDate = new Date(customer.updateddate);
            return updateDate.getMonth() === currentMonth && updateDate.getFullYear() === currentYear;
        }).length;

        activeElement.textContent = activeCount;
    }
};

const populateCustomerTable = (customers) => {
    const tableBody = document.getElementById('customerTableBody');
    if (!tableBody) {
        return;
    }

    if (!customers || customers.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-5">No customers found.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    customers.forEach((customer) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${customer.name || ''}</td>
            <td>${customer.contactperson || 'N/A'}</td>
            <td>${customer.email || ''}</td>
            <td>${customer.phone || ''}</td>
            <td>${customer.address || ''}</td>
            <td>${customer.contactpersonphone || 'N/A'}</td>
            <td class="d-flex flex-row justify-content-center gap-2">
                <button class="btn btn-teal px-3 py-2" onclick="editCustomer(${customer.id})">Edit</button>
                <button class="btn btn-red px-3 py-2" onclick="deleteCustomer(${customer.id})">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
};

const filterCustomers = () => {
    const searchVal = document.getElementById('customerSearchInput').value.toLowerCase().trim();
    const filterCountElement = document.getElementById('filterCount');
    
    if (!searchVal) {
        populateCustomerTable(allCustomers);
        if (filterCountElement) {
            filterCountElement.textContent = `Showing all ${allCustomers.length} customers`;
        }
        return;
    }

    const filtered = allCustomers.filter((customer) => {
        const name = (customer.name || '').toLowerCase();
        const email = (customer.email || '').toLowerCase();
        const phone = (customer.phone || '').toLowerCase();
        const address = (customer.address || '').toLowerCase();
        const person = (customer.contactperson || '').toLowerCase();

        return name.includes(searchVal) || 
               email.includes(searchVal) || 
               phone.includes(searchVal) || 
               address.includes(searchVal) || 
               person.includes(searchVal);
    });

    populateCustomerTable(filtered);
    if (filterCountElement) {
        filterCountElement.textContent = `Showing ${filtered.length} of ${allCustomers.length} matching customers`;
    }
};

const editCustomer = (id) => {
    getHTTPService(`/customer/getcustomer/${id}`, 'GET', 'json').done((customer) => {
        if (!customer || !customer.id) {
            swal.fire({
                icon: 'error',
                title: 'Customer not found',
                text: 'The selected customer could not be loaded.',
            });
            return;
        }

        isUpdateMode = true;
        selectedCustomerId = customer.id;

        document.getElementById('customerHiddenId').value = customer.id;
        document.getElementById('customerId').value = `Customer #${customer.id}`;
        document.getElementById('companyName').value = customer.name || '';
        document.getElementById('companyContact').value = customer.phone || '';
        document.getElementById('customerEmail').value = customer.email || '';
        document.getElementById('customerAddress').value = customer.address || '';
        document.getElementById('contactPersonName').value = customer.contactperson || '';
        document.getElementById('contactPersonContact').value = customer.contactpersonphone || '';

        document.getElementById('customerSubmitButton').textContent = 'Update Customer';
        const title = document.getElementById('customerModalLabel');
        if (title) {
            title.textContent = 'Update Customer';
        }

        // Validate fields pre-emptively on edit load
        validateAllFields();

        const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('customerModal'));
        modal.show();
    }).fail((jqXHR, textStatus, errorThrown) => {
        console.error(textStatus, errorThrown);
        swal.fire({
            icon: 'error',
            title: 'Failed to load customer',
            text: jqXHR.responseJSON?.message || 'Unable to fetch customer details.',
        });
    });
};

const deleteCustomer = (id) => {
    swal.fire({
        icon: 'warning',
        title: 'Delete Customer',
        text: 'Are you sure you want to delete this customer? This action cannot be undone.',
        showCancelButton: true,
        confirmButtonColor: '#ff5252',
        cancelButtonColor: '#00AEEF',
        confirmButtonText: 'Delete'
    }).then((result) => {
        if (!result.isConfirmed) {
            return;
        }

        getHTTPService(`/customer/delete/${id}`, 'DELETE', 'json').then((response) => {
            swal.fire({
                icon: 'success',
                title: 'Deleted',
                text: response && response.message ? response.message : 'Customer deleted successfully.',
                timer: 1500,
                showConfirmButton: false,
            }).then(() => refreshCustomerTable());
        }).catch((jqXHR) => {
            console.error(jqXHR);
            swal.fire({
                icon: 'error',
                title: 'Delete Failed',
                text: jqXHR?.responseJSON?.message || 'Unable to delete customer.',
            });
        });
    });
};

window.addEventListener('DOMContentLoaded', () => {
    refreshCustomerTable();
    setupLiveValidation();

    const customerForm = document.getElementById('customerForm');
    if (customerForm) {
        customerForm.addEventListener('reset', resetCustomerForm);
    }

    const modalElement = document.getElementById('customerModal');
    if (modalElement) {
        modalElement.addEventListener('hidden.bs.modal', resetCustomerForm);
    }
});

