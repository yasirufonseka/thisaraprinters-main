//load function on page load
let editingStockLotId = null; // null = Add mode, number = Edit mode
$(document).ready(function () {
    populateCategory();
    populateMaterial();
})

function getUnitForCategory(categoryName) {
    if (!categoryName) return '';
    const name = categoryName.toLowerCase();
    if (name.includes('paper')) {
        return 'Sheets';
    } else if (name.includes('ink')) {
        return 'Kg';
    } else if (name.includes('plate') || name.includes('blanket') || name.includes('pieces')) {
        return 'Pieces';
    } else if (name.includes('chemical') || name.includes('solution') || name.includes('liter')) {
        return 'Liters';
    }
    return '';
}

// Add Material Form Submission
function submitMaterial(event) {
    event.preventDefault();

    const formData = {
        material: document.getElementById('materialName').value,
       // availablequantity: parseInt(document.getElementById('materialQuantity').value),
        units: document.getElementById('materialUnit').value,
        reorderlevel: parseInt(document.getElementById('reorderlevel').value),
        status: document.getElementById('materialStatus').value
    };

    const response = postHTTPService('/inventory/api/materials/add', 'POST', 'json', formData);

    if (response.responseText && response.responseText.includes('Error')) {
        Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: 'Failed to add material. Please try again.'
        });
    } else {
        Swal.fire({
            icon: 'success',
            title: 'Material Added!',
            text: 'Material has been added successfully.',
            timer: 2000,
            timerProgressBar: true,
            willClose: () => {
                location.reload();
            }
        });
    }

    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('addMaterialModal'));
    if (modal) modal.hide();
}

// Update Material
function updateMaterial(materialId) {
    const response = getHTTPService('/inventory/api/materials/' + materialId, 'GET', 'json');

    if (response.responseText && response.responseText.includes('Error')) {
        Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: 'Failed to load material data.'
        });
        return;
    }

    const material = response.responseJSON;

    // Populate form with existing data
    document.getElementById('materialName').value = material.material;
    document.getElementById('materialQuantity').value = material.availablequantity;
    document.getElementById('materialUnit').value = material.units;
    document.getElementById('materialReorderLevel').value = material.reorderlevel;
    document.getElementById('materialStatus').value = material.status;

    // Change modal title and button text
    document.getElementById('addMaterialModalHeader').textContent = 'Edit Material';
    document.getElementById('materialFormData').onsubmit = function (event) {
        event.preventDefault();

        const updatedData = {
            id: materialId,
            material: document.getElementById('materialName').value,
            availablequantity: parseInt(document.getElementById('materialQuantity').value),
            units: document.getElementById('materialUnit').value,
            reorderlevel: parseInt(document.getElementById('materialReorderLevel').value),
            status: document.getElementById('materialStatus').value
        };

        const updateResponse = postHTTPService('/inventory/api/materials/' + materialId, 'PUT', 'json', updatedData);

        if (updateResponse.responseText && updateResponse.responseText.includes('Error')) {
            Swal.fire({
                icon: 'error',
                title: 'Error!',
                text: 'Failed to update material. Please try again.'
            });
        } else {
            Swal.fire({
                icon: 'success',
                title: 'Material Updated!',
                text: 'Material has been updated successfully.',
                timer: 2000,
                timerProgressBar: true,
                willClose: () => {
                    location.reload();
                }
            });
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('addMaterialModal'));
        if (modal) modal.hide();
    };

    // Show modal
    const addMaterialModal = new bootstrap.Modal(document.getElementById('addMaterialModal'));
    addMaterialModal.show();
}

// Delete Material
function deleteMaterial(materialId) {
    Swal.fire({
        title: 'Are you sure?',
        text: 'This action cannot be undone.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            const response = getHTTPService('/inventory/api/materials/' + materialId, 'DELETE', 'json');

            if (response.responseText && response.responseText.includes('Error')) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error!',
                    text: 'Failed to delete material. Please try again.'
                });
            } else {
                Swal.fire({
                    icon: 'success',
                    title: 'Deleted!',
                    text: 'Material has been deleted successfully.',
                    timer: 2000,
                    timerProgressBar: true,
                    willClose: () => {
                        location.reload();
                    }
                });
            }
        }
    });
}

// ─── Helpers to pop an error message under any input field ────────────────────
function showFieldError(inputId, message) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.classList.add("input-error");
    const errorDiv = input.parentElement.querySelector(".errorMessage");
    if (errorDiv) errorDiv.textContent = message;
}

function clearFieldError(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.classList.remove("input-error");
    const errorDiv = input.parentElement.querySelector(".errorMessage");
    if (errorDiv) errorDiv.textContent = "";
}

// ─── Wire up blur listeners for the GRN modal fields ─────────────────────────
document.addEventListener("DOMContentLoaded", function () {
    const grnTextFields = [
        { id: "grnSupplierInvoice", label: "Invoice number" },
        { id: "grnBatchNo", label: "Batch number" },
        { id: "grnQty", label: "Received quantity" },
    ];
    grnTextFields.forEach(({ id, label }) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.addEventListener("blur", () => {
            if (!el.value.trim()) showFieldError(id, `${label} is required`);
            else clearFieldError(id);
        });
        el.addEventListener("input", () => clearFieldError(id));
    });

    // GRN placed order is a select
    const grnOrderEl = document.getElementById("grnPlacedOrder");
    if (grnOrderEl) {
        grnOrderEl.addEventListener("change", () => {
            if (!grnOrderEl.value) showFieldError("grnPlacedOrder", "Please select a placed order");
            else clearFieldError("grnPlacedOrder");
        });
    }

    // Usage form fields
    const usageQtyEl = document.getElementById("usageQty");
    if (usageQtyEl) {
        usageQtyEl.addEventListener("blur", () => {
            if (!usageQtyEl.value.trim()) showFieldError("usageQty", "Quantity is required");
            else clearFieldError("usageQty");
        });
        usageQtyEl.addEventListener("input", () => clearFieldError("usageQty"));
    }

    const usageJobEl = document.getElementById("usageJob");
    if (usageJobEl) {
        usageJobEl.addEventListener("blur", () => {
            if (!usageJobEl.value.trim()) showFieldError("usageJob", "Job ID / purpose is required");
            else clearFieldError("usageJob");
        });
        usageJobEl.addEventListener("input", () => clearFieldError("usageJob"));
    }
});

// ─── GRN validation — check required fields before we hit the API ─────────────
function validateGRNForm() {
    let isValid = true;

    const requiredTextFields = [
        { id: "grnSupplierInvoice", label: "Invoice number" },
        { id: "grnBatchNo", label: "Batch number" },
        { id: "grnQty", label: "Received quantity" },
    ];

    requiredTextFields.forEach(({ id, label }) => {
        const el = document.getElementById(id);
        if (!el || !el.value.trim()) {
            showFieldError(id, `${label} is required`);
            isValid = false;
        } else {
            clearFieldError(id);
        }
    });

    const grnOrderEl = document.getElementById("grnPlacedOrder");
    if (!grnOrderEl || !grnOrderEl.value) {
        showFieldError("grnPlacedOrder", "Please select a placed order");
        isValid = false;
    } else {
        clearFieldError("grnPlacedOrder");
    }

    return isValid;
}

// ─── Usage form validation ────────────────────────────────────────────────────
function validateUsageForm() {
    let isValid = true;

    const usageQtyEl = document.getElementById("usageQty");
    if (!usageQtyEl || !usageQtyEl.value.trim()) {
        showFieldError("usageQty", "Quantity used is required");
        isValid = false;
    } else {
        clearFieldError("usageQty");
    }

    const usageJobEl = document.getElementById("usageJob");
    if (!usageJobEl || !usageJobEl.value.trim()) {
        showFieldError("usageJob", "Job ID / purpose is required");
        isValid = false;
    } else {
        clearFieldError("usageJob");
    }

    return isValid;
}

// Submit GRN Form — handles both Add and Edit
function submitGRN(event) {
    event.preventDefault();

    // Make sure the required fields are filled in before we do anything
    if (!validateGRNForm()) return;

    const selectedItem = $('#grnItem').find(':selected');
    const category = selectedItem.data('category') || '';
    let qty = parseInt(document.getElementById('grnQty').value) || 0;
    if (category === 'Paper') {
        const spr = parseInt(document.getElementById('grnSheetsPerReam').value) || 500;
        qty = qty * spr;
    }

    const grnData = {
        supplierInvoiceNo: document.getElementById('grnSupplierInvoice').value,
        batchNo: document.getElementById('grnBatchNo').value,
        recivedquantity: qty,
        units: document.getElementById('grnUnits').value,
        receivedDate: document.getElementById('grnDate').value || null,
        expiryDate: document.getElementById('grnExpiryDate').value || null,
        notes: document.getElementById('grnNotes').value || null,
        variant: { id: parseInt(document.getElementById('grnItem').value) },
        purchaseOrderId: parseInt(document.getElementById('grnPlacedOrder').value),
        receivedByUser: { id: parseInt(document.getElementById('grnReceivedBy').value) }
    };

    // Edit mode → PUT,  Add mode → POST
    const isEdit = editingStockLotId !== null;
    const url  = isEdit
        ? '/inventory/api/stocklot/' + editingStockLotId + '/update'
        : '/inventory/api/grn/save-full';
    const method = isEdit ? 'PUT' : 'POST';

    const response = postHTTPService(url, method, 'json', grnData);
    const serverMessage = response.responseJSON && response.responseJSON.message
        ? response.responseJSON.message
        : (isEdit ? 'GRN updated successfully.' : 'Goods receipt note has been saved successfully.');

    if (response.status >= 400 || (response.responseText && response.responseText.includes('Error'))) {
        Swal.fire({ icon: 'error', title: 'Error!', text: serverMessage });
    } else {
        Swal.fire({
            icon: 'success',
            title: isEdit ? 'GRN Updated!' : 'GRN Saved!',
            text: serverMessage,
            timer: 2000,
            timerProgressBar: true,
            willClose: () => { location.reload(); }
        });
        const modal = bootstrap.Modal.getInstance(document.getElementById('grnModal'));
        if (modal) modal.hide();
    }
}

// Submit Usage Form
function submitUsage(event) {
    event.preventDefault();

    // Don't go any further if required fields are empty
    if (!validateUsageForm()) return;

    const usageData = {
        materialId: parseInt(document.getElementById('usageItem').value),
        quantityUsed: parseInt(document.getElementById('usageQty').value),
        purpose: document.getElementById('usageJob').value,
        dateUsed: document.getElementById('usageDate').value
    };

    const response = postHTTPService('/inventory/api/materials/usage', 'POST', 'json', usageData);

    if (response.responseText && response.responseText.includes('Error')) {
        Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: 'Failed to record usage. Please try again.'
        });
    } else {
        Swal.fire({
            icon: 'success',
            title: 'Usage Recorded!',
            text: 'Material usage has been recorded successfully.',
            timer: 2000,
            timerProgressBar: true,
            willClose: () => {
                location.reload();
            }
        });
    }

    const modal = bootstrap.Modal.getInstance(document.getElementById('usageModal'));
    if (modal) modal.hide();
}

// Search functionality
$(document).ready(function () {
    $('#searchInventory').on('keyup', function () {
        const searchTerm = $(this).val().toLowerCase();

        $('#materialTable tbody tr').filter(function () {
            $(this).toggle($(this).text().toLowerCase().indexOf(searchTerm) > -1);
        });
    });

    $('#searchGRN').on('keyup', function () {
        const searchTerm = $(this).val().toLowerCase();

        $('#grnTable tbody tr').filter(function () {
            $(this).toggle($(this).text().toLowerCase().indexOf(searchTerm) > -1);
        });
    });

    // Reset form when modal is closed
    $('#addMaterialModal').on('hidden.bs.modal', function () {
        document.getElementById('materialFormData').reset();
        document.getElementById('addMaterialModalHeader').textContent = 'Add Material';
        document.getElementById('materialFormData').onsubmit = submitMaterial;
    });

    // Reset GRN form when modal is closed — also reset edit state
    $('#grnModal').on('hidden.bs.modal', function () {
        document.getElementById('grnFormData').reset();
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('grnDate').value = today;
        document.getElementById('grnPlacedOrder').innerHTML = '<option value="">-- Select Placed Order --</option>';
        document.getElementById('grnSupplierDisplay').value = '';
        $('#grnSheetsPerReamContainer').hide();
        // Reset to Add mode
        editingStockLotId = null;
        document.querySelector('#grnModal .modal-title').textContent = 'Receive Goods (GRN)';
        document.querySelector('#grnModal button[type="submit"]').textContent = 'Save GRN';
    });

    // Load purchase orders when GRN modal opens
    $('#grnModal').on('show.bs.modal', function () {
        if (editingStockLotId === null) {
            loadPurchaseOrders();
        }
    });

    // Auto-fill supplier display when a Placed Order is selected
    $('#grnPlacedOrder').on('change', function () {
        const selected = $(this).find(':selected');
        const supplierName = selected.data('supplier-name') || '';
        $('#grnSupplierDisplay').val(supplierName);
    });

    $('#grnItem').on('change', function () {
        const option = $(this).find(':selected');
        const selectedUnit = option.data('unit');
        const category = option.data('category') || '';
        const sheetsPerReam = option.data('sheets-per-ream');

        if (category === 'Paper') {
            $('#grnSheetsPerReamContainer').show();
            $('#grnSheetsPerReam').val(sheetsPerReam || 500);
            $('#grnUnits').val('Sheets');
        } else {
            $('#grnSheetsPerReamContainer').hide();
            if (selectedUnit) {
                $('#grnUnits').val(selectedUnit);
            } else {
                const autoUnit = getUnitForCategory(category);
                if (autoUnit) {
                    $('#grnUnits').val(autoUnit);
                }
            }
        }
    });

    // Reset Usage form when modal is closed
    $('#usageModal').on('hidden.bs.modal', function () {
        document.getElementById('usageFormData').reset();
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('usageDate').value = today;
    });

    // Reset Return Stock form when modal is closed
    $('#returnStockModal').on('hidden.bs.modal', function () {
        document.getElementById('returnStockFormData').reset();
    });

    // Auto-fill fields when variant is selected in Return Stock modal
    $('#returnItem').on('change', function () {
        const option = $(this).find(':selected');
        const unit = option.data('unit');
        const width = option.data('width');
        const height = option.data('height');
        const weight = option.data('weight');

        if (unit) $('#returnUnit').val(unit);
        if (width !== undefined) $('#returnWidth').val(width);
        if (height !== undefined) $('#returnHeight').val(height);
        if (weight !== undefined) $('#returnWeight').val(weight);
    });
});

// Load purchase orders into the GRN modal dropdown
function loadPurchaseOrders(callback) {
    getHTTPService('/inventory/api/purchase-orders', 'GET', 'json')
        .done(function (data) {
            const select = document.getElementById('grnPlacedOrder');
            select.innerHTML = '<option value="">-- Select Placed Order --</option>';
            if (Array.isArray(data)) {
                data.forEach(function (po) {
                    const opt = document.createElement('option');
                    opt.value = po.id;
                    opt.textContent = (po.poNumber || ('PO-' + po.id)) + ' | ' + (po.items || '') + ' | ' + (po.supplierName || '');
                    opt.setAttribute('data-supplier-name', po.supplierName || '');
                    opt.setAttribute('data-supplier-id', po.supplierId || '');
                    select.appendChild(opt);
                });
            }
            if (typeof callback === 'function') callback();
        })
        .fail(function () {
            console.error('Failed to load purchase orders.');
        });
}

// Default open tab
document.addEventListener("DOMContentLoaded", function () {
    const defaultTab = document.querySelector(".tab-btn");
    if (defaultTab) defaultTab.click();
});

function populateCategory() {
    // Listen on the Bootstrap modal 'show.bs.modal' event so that
    // categories are fetched and populated BEFORE the modal is visible.
    const addMaterialModal = document.getElementById("addMaterialModal");
    if (!addMaterialModal) return;

    addMaterialModal.addEventListener("show.bs.modal", function () {
        getHTTPService("/inventory/get/category", "GET", "json")
            .done(function (categories) {
                const materialCategory = document.getElementById("materialCategory");
                if (!materialCategory) return;
                const list = categories || [];

                materialCategory.innerHTML = '<option value="" disabled selected>Select Category</option>';
                list.forEach(function (item) {
                    materialCategory.innerHTML += `<option value="${item.id}">${item.name}</option>`;
                });
            })
            .fail(function () {
                console.error("Error loading categories");
            });
    });
}

const materialCategoryElement = document.getElementById("materialCategory");
if (materialCategoryElement) {
    materialCategoryElement.addEventListener("change", function (event) {
        const materialCategory = document.getElementById("materialCategory");
        const hight = document.getElementById("hightContainer");
        const width = document.getElementById("widthContainer");
        const weidth = document.getElementById("weidthContainer");
    //hide the div
    hight.classList.add("d-none");
    width.classList.add("d-none");
    weidth.classList.add("d-none");

    const selectedOption = materialCategory.options[materialCategory.selectedIndex].text;
    const autoUnit = getUnitForCategory(selectedOption);
    if (autoUnit) {
        $('#materialUnit').val(autoUnit);
    }

    if(selectedOption === "Paper") {
        hight.classList.add("d-inline");
        width.classList.add("d-inline");
        weidth.classList.remove("d-inline");
        
        hight.classList.remove("d-none");
        width.classList.remove("d-none");
        weidth.classList.add("d-none");
    } else {
        hight.classList.remove("d-inline");
        width.classList.remove("d-inline");
        weidth.classList.add("d-inline");
        
        hight.classList.add("d-none");
        width.classList.add("d-none");
        weidth.classList.remove("d-none");
    }

});
}

function populateMaterial() {
    // populate material dropdown when category changes
    const categorySelect = document.getElementById("materialCategory");
    if (categorySelect) {
        categorySelect.addEventListener("change", function () {
            // Capture selectedCategory HERE before entering any callback
            const selectedCategory = parseInt(this.value);
            getHTTPService("/inventory/get/materials", "GET", "json")
                .done(function (response) {
                    const materialSelect = document.getElementById("materialName");
                    const materials = response || [];

                    // clear existing options
                    materialSelect.options.length = 0;
                    materialSelect.add(new Option('Select Material', ''));

                    if (isNaN(selectedCategory)) return;

                    // Filter materials by selected category id
                    materials
                        .filter(function (item) {
                            return item.category && item.category.id === selectedCategory;
                        })
                        .forEach(function (item) {
                            materialSelect.add(new Option(item.material, item.id));
                        });
                    materialSelect.add(new Option('Add new Material', 'custom'));
                })
                .fail(function () {
                    console.error("Error loading materials");
                });
        });


    }}

function addMaterial(event) {
    if (event) event.preventDefault();
    const materialIdValue = document.getElementById("materialName").value;
    const formData = {
        materialName : { id: parseInt(materialIdValue) },
        materialgsm : parseInt(document.getElementById("materialgsm").value) || 0,
        sheetperream : parseInt(document.getElementById("sheetperream").value) || 0,
        reorderlevel : parseInt(document.getElementById("reorderlevel").value) || 0,
        hightofpaper : parseFloat(document.getElementById("hightofpaper").value) || 0,
        widthtofpaper : parseFloat(document.getElementById("widthtofpaper").value) || 0,
        weight : parseFloat(document.getElementById("weight").value) || 0,
        unit : document.getElementById("materialUnit").value
    }

    postHTTPService("/inventory/save/material","POST","json",formData).then((responce) => {
        swal.fire({
            title: 'Success!',
            icon: 'success',
            text: responce.message || 'Material added successfully!',
            confirmButtonText: 'OK',

        })
    }).catch((error) => {
        swal.fire({
            title: 'Error!',
            icon: 'error',
            text: error.message || 'Error occurred while saving.',
            confirmButtonText: 'OK',
        })
        console.log(error);
    })

}

function populateTable(){
   const table = document.getElementById("materialTable");
   getHTTPService("")

}

// ── Edit Stock Lot: fetch GRN data and open modal in edit mode ──
function updateStockLot(stockLotId) {
    const response = getHTTPService('/inventory/api/stocklot/' + stockLotId, 'GET', 'json');

    if (!response.responseJSON || response.status >= 400) {
        Swal.fire({ icon: 'error', title: 'Error!', text: 'Could not load GRN data.' });
        return;
    }

    const data = response.responseJSON;

    // Switch to edit mode
    editingStockLotId = stockLotId;
    document.querySelector('#grnModal .modal-title').textContent = 'Edit GRN (' + (data.grnNumber || '') + ')';
    document.querySelector('#grnModal button[type="submit"]').textContent = 'Update GRN';

    // Fill form fields
    document.getElementById('grnNumber').value          = data.grnNumber        || '';
    document.getElementById('grnSupplierInvoice').value = data.supplierInvoiceNo || '';
    document.getElementById('grnBatchNo').value         = data.batchNo           || '';
    document.getElementById('grnQty').value             = data.receivedQuantity  || '';
    document.getElementById('grnDate').value            = data.receivedDate      || '';
    document.getElementById('grnExpiryDate').value      = data.expiryDate        || '';
    document.getElementById('grnNotes').value           = data.notes             || '';
    document.getElementById('grnSupplierDisplay').value = data.supplierName      || '';

    // Load POs then select the right one
    loadPurchaseOrders(function () {
        if (data.purchaseOrderId) $('#grnPlacedOrder').val(data.purchaseOrderId);
        if (data.supplierName)    $('#grnSupplierDisplay').val(data.supplierName);
    });

    // Select other dropdowns by value
    if (data.variantId) {
        $('#grnItem').val(data.variantId);
        const selected = $('#grnItem').find(':selected');
        const category = selected.data('category') || '';
        const sheetsPerReam = selected.data('sheets-per-ream') || 500;

        if (category === 'Paper') {
            $('#grnSheetsPerReamContainer').show();
            $('#grnSheetsPerReam').val(sheetsPerReam);
            $('#grnUnits').val('Sheets');
            const totalSheets = parseInt(data.receivedQuantity) || 0;
            $('#grnQty').val(totalSheets / sheetsPerReam);
        } else {
            $('#grnSheetsPerReamContainer').hide();
            $('#grnQty').val(data.receivedQuantity || '');
            if (data.units) $('#grnUnits').val(data.units);
        }
    }
    if (data.receivedByUserId) $('#grnReceivedBy').val(data.receivedByUserId);

    // Open the modal
    const modal = new bootstrap.Modal(document.getElementById('grnModal'));
    modal.show();
}

// Edit GRN directly by GRN (Inventory) ID
function editGRN(grnId) {
    const response = getHTTPService('/inventory/api/grn/' + grnId, 'GET', 'json');

    if (!response.responseJSON || response.status >= 400) {
        Swal.fire({ icon: 'error', title: 'Error!', text: 'Could not load GRN data.' });
        return;
    }

    const data = response.responseJSON;

    // Switch to edit mode using the linked stockLotId
    editingStockLotId = data.stockLotId;
    document.querySelector('#grnModal .modal-title').textContent = 'Edit GRN (' + (data.grnNumber || '') + ')';
    document.querySelector('#grnModal button[type="submit"]').textContent = 'Update GRN';

    // Fill form fields
    document.getElementById('grnNumber').value          = data.grnNumber        || '';
    document.getElementById('grnSupplierInvoice').value = data.supplierInvoiceNo || '';
    document.getElementById('grnBatchNo').value         = data.batchNo           || '';
    document.getElementById('grnQty').value             = data.receivedQuantity  || '';
    document.getElementById('grnDate').value            = data.receivedDate      || '';
    document.getElementById('grnExpiryDate').value      = data.expiryDate        || '';
    document.getElementById('grnNotes').value           = data.notes             || '';
    document.getElementById('grnSupplierDisplay').value = data.supplierName      || '';

    // Load POs then select the right one
    loadPurchaseOrders(function () {
        if (data.purchaseOrderId) $('#grnPlacedOrder').val(data.purchaseOrderId);
        if (data.supplierName)    $('#grnSupplierDisplay').val(data.supplierName);
    });

    // Select other dropdowns by value
    if (data.variantId) {
        $('#grnItem').val(data.variantId);
        const selected = $('#grnItem').find(':selected');
        const category = selected.data('category') || '';
        const sheetsPerReam = selected.data('sheets-per-ream') || 500;

        if (category === 'Paper') {
            $('#grnSheetsPerReamContainer').show();
            $('#grnSheetsPerReam').val(sheetsPerReam);
            $('#grnUnits').val('Sheets');
            const totalSheets = parseInt(data.receivedQuantity) || 0;
            $('#grnQty').val(totalSheets / sheetsPerReam);
        } else {
            $('#grnSheetsPerReamContainer').hide();
            $('#grnQty').val(data.receivedQuantity || '');
            if (data.units) $('#grnUnits').val(data.units);
        }
    }
    if (data.receivedByUserId) $('#grnReceivedBy').val(data.receivedByUserId);

    // Open the modal
    const modal = new bootstrap.Modal(document.getElementById('grnModal'));
    modal.show();
}

// ── Delete Stock Lot + its linked GRN ──
function deleteStockLot(stockLotId) {
    Swal.fire({
        title: 'Delete this record?',
        text: 'This will remove the stock lot and its GRN. This cannot be undone.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            const response = getHTTPService(
                '/inventory/api/stocklot/' + stockLotId + '/delete', 'DELETE', 'json'
            );
            const msg = response.responseJSON && response.responseJSON.message
                ? response.responseJSON.message : 'Record deleted.';

            if (response.status >= 400) {
                Swal.fire({ icon: 'error', title: 'Error!', text: msg });
            } else {
                Swal.fire({
                    icon: 'success', title: 'Deleted!', text: msg,
                    timer: 1500, timerProgressBar: true,
                    willClose: () => { location.reload(); }
                });
            }
        }
    });
}

// Submit Return Stock Form
function submitReturnStock(event) {
    event.preventDefault();

    const returnData = {
        variantId: parseInt(document.getElementById('returnItem').value),
        returnedQty: parseInt(document.getElementById('returnQty').value),
        jobNo: document.getElementById('returnJob').value,
        unit: document.getElementById('returnUnit').value,
        height: parseFloat(document.getElementById('returnHeight').value),
        width: parseFloat(document.getElementById('returnWidth').value),
        weight: parseFloat(document.getElementById('returnWeight').value)
    };

    const response = postHTTPService('/inventory/api/stocklot/return', 'POST', 'json', returnData);
    const serverMessage = response.responseJSON && response.responseJSON.message
        ? response.responseJSON.message : 'Return stock recorded successfully.';

    if (response.status >= 400 || (response.responseText && response.responseText.includes('Error'))) {
        Swal.fire({ icon: 'error', title: 'Error!', text: serverMessage });
    } else {
        Swal.fire({
            icon: 'success',
            title: 'Success!',
            text: serverMessage,
            timer: 2000,
            timerProgressBar: true,
            willClose: () => { location.reload(); }
        });
        const modal = bootstrap.Modal.getInstance(document.getElementById('returnStockModal'));
        if (modal) modal.hide();
    }
}


