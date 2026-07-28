let materialList = [];

// Show / hide the custom size height & width fields when change on dropdown selection
function toggleCustomSize() {
  const select = document.getElementById('productSize');
  const wrap   = document.getElementById('customSizeWrap');
  if (!select || !wrap) return;

  if (select.value === 'custom') {
    wrap.style.display = 'block';
  } else {
    wrap.style.display = 'none';
    const heightInput = document.getElementById('customSizeHeight');
    const widthInput  = document.getElementById('customSizeWidth');
    if (heightInput) heightInput.value = '';
    if (widthInput)  widthInput.value  = '';
  }
}



function toggleDeliveryFields() {
  const orderType = document.getElementById('orderType').value;
  const deliveryFields = document.querySelectorAll('.delivery-field');

  deliveryFields.forEach(field => {
    if (orderType === 'delivery') {
      field.style.display = 'block';
    } else {
      field.style.display = 'none';
    }
  });
}


//open customer search model
const openClientSearch = (event, modalId) => {
  event.preventDefault();
  const modal = document.getElementById(modalId);
  modal.style.display = 'block';
  modal.classList.add('show');
  modal.setAttribute('aria-hidden', 'false');
  modal.setAttribute('aria-modal', 'true');
  modal.setAttribute('role', 'dialog');
  modal.setAttribute('tabindex', '-1');

}
//close customer search  model
const closeSearchModel = (event, modelId) => {
  event.preventDefault();
  const modal = document.getElementById(modelId);
  modal.style.display = 'none';
  modal.classList.remove('show');
  //modal.setAttribute('aria-hidden', 'true');
  modal.setAttribute('aria-modal', 'false');
  modal.setAttribute('role', 'dialog');
  modal.setAttribute('tabindex', '0');
}

//search client from the client database
function searchClient() {

  const globalCustomerList = window.globalCustomer; //
  const searchClientsName = document.getElementById('clientName').value.trim();
  // console.log(searchClientsName);

  const showSearchedClientDiv = document.getElementById('showSearchedClient');

  // filter sample data
  const findClient = globalCustomerList.filter(cus =>cus.name.toLowerCase().includes(searchClientsName.toLowerCase()) )
  console.log(findClient);

  if (findClient.length >0) {
    showSearchedClientDiv.dataset.client = JSON.stringify(findClient[0]);

    // Store the full client object in the dataset for retrieval
    showSearchedClientDiv.innerHTML = findClient.map(client=>`

    <div class="col-md-12">
      <div class="card w-75 h-50 text-dark bg-body-tertiary" style="cursor: pointer;">
        <div class="card-body">
          <h3 class="card-title">${client.name}</h3>
          <p class="card-text">${client.email}</p>
        </div>
      </div>
    </div>`).join("");

  } else {
    // Handle case where no client is found
    delete showSearchedClientDiv.dataset.client;
    showSearchedClientDiv.innerHTML = '<p class="text-muted">No client found.</p>';
  }

}

function selectedClient() {
  const model = document.getElementById('searchCustomerModal')
  //get the selected client from the showSearchedClient div
  const showSearchedClient = document.getElementById('showSearchedClient');

  // Check if we have a client stored (divs don't have .value)
  if (showSearchedClient.dataset.client) {
    const selectedClient = JSON.parse(showSearchedClient.dataset.client);
    console.log(selectedClient);

    // Populate the customer name field
    const customerNameInput = document.getElementById('customerName');
    if (customerNameInput) {
      customerNameInput.value = selectedClient.name;
      customerNameInput.dataset.customer = JSON.stringify(selectedClient);
      
      // Clear the validation error if it was previously set
      customerNameInput.classList.remove("input-error");
      const errDiv = document.getElementById("customerNameError");
      if (errDiv) errDiv.textContent = "";
    }

    // Optional: Close the modal after selection
    const modal = document.getElementById('searchCustomerModal');
    modal.style.display = 'none';
    modal.classList.remove('show');
  //  modal.setAttribute('aria-hidden', 'true');
    modal.setAttribute('aria-modal', 'false');
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('tabindex', '0');

    //Using the existing close logic if accessible or bootstrap methods
      bootstrap.Modal.getOrCreateInstance(modal).hide();

  }
  else {
    alert('Please select a client');
  }
}

function makeMaterialList(){
  const select = document.getElementById('materials');
  const selectedValue = select.value;
  const selectedText = select.options[select.selectedIndex].text;
  if (!selectedValue) return;

  // Avoid duplicates
  if(materialList.some(m => m.id === selectedValue)) return;

  // Add to array with both id and name
  materialList.push({ id: selectedValue, name: selectedText });
  renderMaterialList();
}

// function renderMaterialList() {
//   const div = document.getElementById('selectedMaterial');
//   div.innerHTML = materialList.map((m, index) => `
//         <div data-id="${m.id}">
//             ${m.name}
//             <span onclick="removeMaterial(${index})" style="cursor:pointer">✕</span>
//         </div>
//     `).join('');
// }
//
// function removeMaterial(index) {
//   materialList.splice(index, 1);
//   renderMaterialList();
// }

// Show cost-per-sheet input when a radio group has a selection (e.g. Binding)
function toggleCostInput(wrapId) {
  const wrap = document.getElementById(wrapId);
  if (wrap) {
    wrap.style.display = 'block';
  }
}

// Show cost-per-sheet input when at least one checkbox in a group is ticked (e.g. Cutting, Foiling)
function toggleCostInputByCheckboxGroup(wrapId, checkboxIds) {
  const anyChecked = checkboxIds.some(id => {
    const el = document.getElementById(id);
    return el && el.checked;
  });
  const wrap = document.getElementById(wrapId);
  if (wrap) {
    wrap.style.display = anyChecked ? 'block' : 'none';
    if (!anyChecked) {
      const input = wrap.querySelector('input[type="number"]');
      if (input) input.value = '';
    }
  }
}

// Show the correct lamination cost input based on which radio group  is selected
function toggleLaminationCost() {
  const thermalSelected = document.getElementById('laminationThermalGloss').checked ||
                          document.getElementById('laminationThermalMat').checked;
  const normalSelected  = document.getElementById('laminationNormalGloss').checked ||
                          document.getElementById('laminationNormalMat').checked;

  const thermalWrap = document.getElementById('laminationThermalCostWrap');
  const normalWrap  = document.getElementById('laminationNormalCostWrap');

  if (thermalWrap) {
    thermalWrap.style.display = thermalSelected ? 'block' : 'none';
    if (!thermalSelected) {
      const input = thermalWrap.querySelector('input[type="number"]');
      if (input) input.value = '';
    }
  }
  if (normalWrap) {
    normalWrap.style.display = normalSelected ? 'block' : 'none';
    if (!normalSelected) {
      const input = normalWrap.querySelector('input[type="number"]');
      if (input) input.value = '';
    }
  }
}

// Fetch unit rate dynamically from backend  selection and update UI calculations
function onMaterialChange() {
  const select = document.getElementById('materials');
  const badge = document.getElementById('paperRateBadge');
  if (!select) return;
console.log(select);
  const variantId = select.value;
  const paperRateInput = document.getElementById('paperRatePerSheet');

  if (!variantId) {
    if (paperRateInput) paperRateInput.value = '';
    if (badge) badge.style.display = 'none';
    calculateQuotation();
    return;
  }

  // Get preloaded option as backup
  const selectedOption = select.options[select.selectedIndex];
  const dataPrice = parseFloat(selectedOption.getAttribute('data-price')) || 0.0;

  const updateBadge = (status, colorClass) => {
    if (!badge) return;
    badge.textContent = status;
    badge.className = `badge ${colorClass}`;
    badge.style.display = 'inline-block';
  };




  // Query AJAX price endpoint
  $.get(`/order/material-price/${variantId}`)
    .done(function(data) {
      if (data && data.sheetRate !== undefined) {
        if (paperRateInput) paperRateInput.value = data.sheetRate.toFixed(2);
        updateBadge('Dynamic', 'bg-success');
      } else {
        if (paperRateInput) paperRateInput.value = dataPrice.toFixed(2);
        updateBadge('Dynamic', 'bg-success');
      }
      calculateQuotation();
    })
    .fail(function() {
      if (paperRateInput) paperRateInput.value = dataPrice.toFixed(2);
      updateBadge('Dynamic', 'bg-success');
      calculateQuotation();
    });
}

function onPaperRateManualInput() {
  const badge = document.getElementById('paperRateBadge');
  if (badge) {
    badge.textContent = 'Manual Override';
    badge.className = 'badge bg-warning text-dark';
    badge.style.display = 'inline-block';
  }
  calculateQuotation();
}

const QUOTATION_SERVICE_CHARGE_RATE = 0.25;
const MILLIMETERS_PER_INCH = 25.4;

//get values from form and safely validate them
function getNumberValue(elementId, fallbackValue = 0) {
  const element = document.getElementById(elementId);
  const value = element ? parseFloat(element.value) : NaN;
  return Number.isFinite(value) ? value : fallbackValue;
}

function isPositiveNumber(value) {
  return Number.isFinite(value) && value > 0;
}

function isNonNegativeNumber(value) {
  return Number.isFinite(value) && value >= 0;
}

// Resolve finished product dimensions in millimeters.
function getProductDimensions() {
  const productSizeElement = document.getElementById('productSize');
  const productSize = productSizeElement ? productSizeElement.value : '';

  switch (productSize) {
    case 'A3':
      return { width: 297, height: 420 };
    case 'A4':
      return { width: 210, height: 297 };
    case 'A5':
      return { width: 148, height: 210 };
    case 'Letter':
      return { width: 215.9, height: 279.4 };
    case 'Legal':
      return { width: 215.9, height: 355.6 };
    case 'custom':
      return {
        width: getNumberValue('customSizeWidth') * MILLIMETERS_PER_INCH,
        height: getNumberValue('customSizeHeight') * MILLIMETERS_PER_INCH
      };
    default:
      return { width: 0, height: 0 };
  }
}

// Resolve selected raw material sheet dimensions in millimeters.
function getMaterialDimensions() {
  const materialsSelect = document.getElementById('materials');
  const selectedOption = materialsSelect ? materialsSelect.options[materialsSelect.selectedIndex] : null;

  if (!selectedOption || !selectedOption.value) {
    return { width: 0, height: 0, selected: false };
  }
//get hight and width from html dom becuase material sent through thymeleaf
  return {
    width: parseFloat(selectedOption.getAttribute('data-width')) || 0,
    height: parseFloat(selectedOption.getAttribute('data-height')) || 0,
    selected: true
  };
}

// Calculate maximum products that fit on one sheet after margin and gutter.
function calculateProductsPerSheet(sheetWidth, sheetHeight, productWidth, productHeight, edgeMargin) {
  if (
    !isPositiveNumber(sheetWidth) ||
    !isPositiveNumber(sheetHeight) ||
    !isPositiveNumber(productWidth) ||
    !isPositiveNumber(productHeight) ||
    !isNonNegativeNumber(edgeMargin)
  ) {
    return 0;
  }

  const usableSheetWidth = sheetWidth - (2 * edgeMargin);
  const usableSheetHeight = sheetHeight - (2 * edgeMargin);

  if (!isPositiveNumber(usableSheetWidth) || !isPositiveNumber(usableSheetHeight)) {
    return 0;
  }

  const normalOrientationFit =
    Math.floor((sheetWidth ) / (productWidth + edgeMargin)) *
    Math.floor((sheetHeight ) / (productHeight + edgeMargin));

  const rotatedOrientationFit =
    Math.floor((sheetWidth ) / (productHeight + edgeMargin)) *
    Math.floor((sheetHeight ) / (productWidth + edgeMargin));

  return Math.max(normalOrientationFit, rotatedOrientationFit, 0);
}

// Calculate selected finishing cost rates and total finishing cost.
function calculateFinishingCost(totalSheets) {
  const bindingPerfect = document.getElementById('bindingPerfect');
  const bindingSpiral = document.getElementById('bindingSpiral');
  const cuttingDie = document.getElementById('cuttingDie');
  const cuttingGill = document.getElementById('cuttingGill');
  const foilingUv = document.getElementById('foilingUv');
  const foilingNormal = document.getElementById('foilingNormal');
  const laminationThermalGloss = document.getElementById('laminationThermalGloss');
  const laminationThermalMat = document.getElementById('laminationThermalMat');
  const laminationNormalGloss = document.getElementById('laminationNormalGloss');
  const laminationNormalMat = document.getElementById('laminationNormalMat');

  const bindingSelected = (bindingPerfect && bindingPerfect.checked) || (bindingSpiral && bindingSpiral.checked);
  const cuttingSelected = (cuttingDie && cuttingDie.checked) || (cuttingGill && cuttingGill.checked);
  const foilingSelected = (foilingUv && foilingUv.checked) || (foilingNormal && foilingNormal.checked);
  const thermalLaminationSelected =
    (laminationThermalGloss && laminationThermalGloss.checked) ||
    (laminationThermalMat && laminationThermalMat.checked);
  const normalLaminationSelected =
    (laminationNormalGloss && laminationNormalGloss.checked) ||
    (laminationNormalMat && laminationNormalMat.checked);

  const bindingRate = bindingSelected ? getNumberValue('bindingCostPerSheet') : 0;
  const cuttingRate = cuttingSelected ? getNumberValue('cuttingCostPerSheet') : 0;
  const foilingRate = foilingSelected ? getNumberValue('foilingCostPerSheet') : 0;

  let laminationRate = 0;
  if (thermalLaminationSelected) {
    laminationRate = getNumberValue('laminationThermalCostPerSheet');
  } else if (normalLaminationSelected) {
    laminationRate = getNumberValue('laminationNormalCostPerSheet');
  }

  const finishingRatePerSheet = bindingRate + cuttingRate + foilingRate + laminationRate;

  return {
    bindingRate,
    cuttingRate,
    foilingRate,
    laminationRate,
    finishingRatePerSheet,
    totalFinishingCost: totalSheets * finishingRatePerSheet
  };
}

// Calculate raw material paper cost.
function calculatePaperCost(totalSheets, paperRatePerSheet) {
  return totalSheets * paperRatePerSheet;
}

// Calculate all quotation totals using the same formulas as the Spring Boot backend.
function calculateQuotationTotals(quantity, wastageSheets, productsPerSheet, paperRatePerSheet, impressionCost) {
  const printedSheets = productsPerSheet > 0 && quantity > 0 && wastageSheets > 0
    ? Math.ceil((quantity + wastageSheets)/ productsPerSheet)
    : 0;
  const totalSheets = printedSheets ;
  const totalPaperCost = calculatePaperCost(totalSheets, paperRatePerSheet);
  const finishing = calculateFinishingCost(quantity);
  const baseProductionCost = totalPaperCost + finishing.totalFinishingCost + (impressionCost || 0);
  const serviceChargeAmount = baseProductionCost * QUOTATION_SERVICE_CHARGE_RATE;
  const totalQuotationAmount = baseProductionCost + serviceChargeAmount;
  const unitPrice = quantity > 0 ? totalQuotationAmount / quantity : 0;

  return {
    printedSheets,
    totalSheets,
    totalPaperCost,
    totalFinishingCost: finishing.totalFinishingCost,
    serviceChargeAmount,
    totalQuotationAmount,
    unitPrice
  };
}

function showQuotationError(message) {
  const errorDiv = document.getElementById('impositionError');
  const submitButton = document.querySelector('#quotationForm button[type="submit"]');

  if (errorDiv) {
    errorDiv.innerText = message;
    errorDiv.style.display = 'block';
  }

  if (submitButton) {
    submitButton.disabled = true;
  }
}

function clearQuotationError() {
  const errorDiv = document.getElementById('impositionError');
  const submitButton = document.querySelector('#quotationForm button[type="submit"]');

  if (errorDiv) {
    errorDiv.innerText = '';
    errorDiv.style.display = 'none';
  }

  if (submitButton) {
    submitButton.disabled = false;
  }
}

// Validate inputs before writing quotation totals.
function validateQuotation(quantity, wastageSheets, paperRatePerSheet, productDimensions, materialDimensions, edgeMargin, gutter, productsPerSheet) {
  if (!isPositiveNumber(quantity)) {
    return {
      valid: false,
      message: 'Error: Quantity must be greater than 0. Quotation generation is disabled.'
    };
  }

  if (!isNonNegativeNumber(wastageSheets)) {
    return {
      valid: false,
      message: 'Error: Wastage sheets must be zero or greater. Quotation generation is disabled.'
    };
  }

  if (!isNonNegativeNumber(paperRatePerSheet)) {
    return {
      valid: false,
      message: 'Error: Paper rate per sheet must be zero or greater. Quotation generation is disabled.'
    };
  }

  if (!materialDimensions.selected) {
    return {
      valid: false,
      message: 'Error: Please select a material. Quotation generation is disabled.'
    };
  }

  if (!isPositiveNumber(materialDimensions.width) || !isPositiveNumber(materialDimensions.height)) {
    return {
      valid: false,
      message: 'Error: Selected material has invalid sheet dimensions. Quotation generation is disabled.'
    };
  }

  if (!isPositiveNumber(productDimensions.width) || !isPositiveNumber(productDimensions.height)) {
    return {
      valid: false,
      message: 'Error: Product size is invalid. Quotation generation is disabled.'
    };
  }

  if (!isNonNegativeNumber(edgeMargin)) {
    return {
      valid: false,
      message: 'Error: Edge margin must be zero or greater. Quotation generation is disabled.'
    };
  }

  if (!isNonNegativeNumber(gutter)) {
    return {
      valid: false,
      message: 'Error: Gutter must be zero or greater. Quotation generation is disabled.'
    };
  }

  if (productsPerSheet <= 0) {
    return {
      valid: false,
      message: 'Error: Raw stock sheet dimensions (' +
        materialDimensions.width + 'x' + materialDimensions.height +
        ' mm) are smaller than finished product dimensions (' +
        Math.round(productDimensions.width) + 'x' + Math.round(productDimensions.height) +
        ' mm) after margin/gutter. Quotation generation is disabled.'
    };
  }

  return { valid: true, message: '' };
}

// Write calculated values to the existing quotation UI.
function updateQuotationUI(totals) {
  document.getElementById('displayTotalSheets').innerText = totals.totalSheets;
  document.getElementById('displayPaperCost').innerText = totals.totalPaperCost.toFixed(2);
  document.getElementById('displayFinishingCost').innerText = totals.totalFinishingCost.toFixed(2);
  document.getElementById('displayServiceCharge').innerText = totals.serviceChargeAmount.toFixed(2);

  document.getElementById('unitPrice').value = totals.unitPrice.toFixed(2);
  document.getElementById('totalPrice').value = totals.totalQuotationAmount.toFixed(2);
}

function resetQuotationUI() {
  updateQuotationUI({
    totalSheets: 0,
    totalPaperCost: 0,
    totalFinishingCost: 0,
    serviceChargeAmount: 0,
    totalQuotationAmount: 0,
    unitPrice: 0
  });
}

// Calculate quotation details dynamically on frontend.
function calculateQuotation() {
  // Read base quotation inputs.
  const quantity = getNumberValue('quantity');
  const wastageSheets = getNumberValue('wastageSheets');
  const paperRatePerSheet = getNumberValue('paperRatePerSheet');
  const impressionCost = getNumberValue('impressionCost')
  const edgeMarginMm = getNumberValue('edgeMarginMm');
  const gutterMm = getNumberValue('gutterMm');
  let totalImpressionCost = 0;

  if(quantity<1000){
    totalImpressionCost = impressionCost * 1000
  }else {
    totalImpressionCost = impressionCost * quantity
  }

  // Resolve product and material dimensions.
  const productDimensions = getProductDimensions();
  const materialDimensions = getMaterialDimensions();

  // Calculate imposition.
  const productsPerSheet = calculateProductsPerSheet(
    materialDimensions.width,
    materialDimensions.height,
    productDimensions.width,
    productDimensions.height,
    edgeMarginMm

  );

  // Validate before calculating totals.
  const validation = validateQuotation(
    quantity,
    wastageSheets,
    paperRatePerSheet,
    productDimensions,
    materialDimensions,
    edgeMarginMm,
    productsPerSheet
  );

  if (!validation.valid) {
    resetQuotationUI();
    showQuotationError(validation.message);
    return;
  }

  // Calculate final quotation totals.
  const totals = calculateQuotationTotals(
    quantity,
    wastageSheets,
    productsPerSheet,
    paperRatePerSheet,
    totalImpressionCost
  );

  const totalsAreValid =
    Number.isFinite(totals.totalSheets) &&
    Number.isFinite(totals.totalPaperCost) &&
    Number.isFinite(totals.totalFinishingCost) &&
    Number.isFinite(totals.serviceChargeAmount) &&
    Number.isFinite(totals.totalQuotationAmount) &&
    Number.isFinite(totals.unitPrice);

  if (!totalsAreValid) {
    resetQuotationUI();
    showQuotationError('Error: Quotation calculation produced invalid values. Quotation generation is disabled.');
    return;
  }

  clearQuotationError();
  updateQuotationUI(totals);
}

// Handle form submission and payload generation
function submitQuotation(event) {
  event.preventDefault();

  const quantity = parseFloat(document.getElementById('quantity').value) || 0;
  const materialId = document.getElementById('materials').value;
  const customerNameInput = document.getElementById('customerName');

  // Validations
  if (quantity <= 0) {
    swal.fire({
      icon: 'warning',
      title: 'Validation Warning',
      text: 'Quantity must be greater than 0.'
    });
    return;
  }

  if (!materialId) {
    swal.fire({
      icon: 'warning',
      title: 'Validation Warning',
      text: 'Please select a material.'
    });
    return;
  }

  let customerObj = null;
  if (customerNameInput && customerNameInput.dataset.customer) {
    customerObj = JSON.parse(customerNameInput.dataset.customer);
  }

  if (!customerObj) {
    if (customerNameInput) {
      customerNameInput.classList.add("input-error");
    }
    const errDiv = document.getElementById("customerNameError");
    if (errDiv) {
      errDiv.textContent = "Please select a customer";
    }
    swal.fire({
      icon: 'warning',
      title: 'Validation Warning',
      text: 'Please select a customer.'
    });
    return;
  } else {
    if (customerNameInput) {
      customerNameInput.classList.remove("input-error");
    }
    const errDiv = document.getElementById("customerNameError");
    if (errDiv) {
      errDiv.textContent = "";
    }
  }

  // Imposition validation
  const productDimensions = getProductDimensions();
  const materialDimensions = getMaterialDimensions();
  const edgeMarginMm = getNumberValue('edgeMarginMm');
  //const gutterMm = getNumberValue('gutterMm');
  const productsPerSheet = calculateProductsPerSheet(
    materialDimensions.width,
    materialDimensions.height,
    productDimensions.width,
    productDimensions.height,
    edgeMarginMm
  );

  if (productsPerSheet === 0) {
    swal.fire({
      icon: 'error',
      title: 'Imposition Fit Error',
      text: 'Finished product dimensions cannot fit on the raw stock dimensions after margin/gutter.'
    });
    return;
  }

  // Gather form values
  const productsize = document.getElementById('productSize').value;
  let finalSize = productsize;
  if (productsize === 'custom') {
    const height = parseFloat(document.getElementById('customSizeHeight').value) || 0;
    const width = parseFloat(document.getElementById('customSizeWidth').value) || 0;
    finalSize = `Custom (${height}x${width} inches)`;
  }

  const wastageSheets = parseInt(document.getElementById('wastageSheets').value) || 0;
  const colorSelect2 = document.getElementById('colorType');
  const color = colorSelect2.options[colorSelect2.selectedIndex].text;
  const description = document.getElementById('description').value || "";

  // Finishing type strings
  let bindingtype = "None";
  if (document.getElementById('bindingPerfect').checked) bindingtype = "Perfect";
  else if (document.getElementById('bindingSpiral').checked) bindingtype = "Spiral";
  const cuttingOptions = [];
  if (document.getElementById('cuttingDie').checked) cuttingOptions.push("Die Cutting");
  if (document.getElementById('cuttingGill').checked) cuttingOptions.push("Gillnetting");
  const cuttingtype = cuttingOptions.join(", ") || "None";

  const foilingOptions = [];
  if (document.getElementById('foilingUv').checked) foilingOptions.push("UV Spot");
  if (document.getElementById('foilingNormal').checked) foilingOptions.push("Normal Foiling");
  const foiling = foilingOptions.join(", ") || "None";

  let lamination = "None";
  if (document.getElementById('laminationThermalGloss').checked) lamination = "Thermal Gloss";
  else if (document.getElementById('laminationThermalMat').checked) lamination = "Thermal Mat";
  else if (document.getElementById('laminationNormalGloss').checked) lamination = "Normal Gloss";
  else if (document.getElementById('laminationNormalMat').checked) lamination = "Normal Mat";

  const paperRatePerSheet = parseFloat(document.getElementById('paperRatePerSheet').value) || 0;
  const impressionCost = parseFloat(document.getElementById('impressionCost').value) || 0;
  const paperCost = parseFloat(document.getElementById('displayPaperCost').innerText) || 0;
  const finishingCost = parseFloat(document.getElementById('displayFinishingCost').innerText) || 0;
  const serviceChargePercentage = 25;
  const serviceChargeAmount = parseFloat(document.getElementById('displayServiceCharge').innerText) || 0;
  const unitPrice = parseFloat(document.getElementById('unitPrice').value) || 0;
  const quotationamount = parseFloat(document.getElementById('totalPrice').value) || 0;
  const expiryDate = document.getElementById('expiryDate').value || null;



  const requestData = {
    productsize: finalSize,
    quantity: quantity,
    color: color,
    quotationdescription: description,
    cuttingtype: cuttingtype,
    foiling: foiling,
    lamination: lamination,
    bindingtype: bindingtype,
    quotationamount: quotationamount,
    advanceamount: 0,
    quotationstatus: "Pending",
    expiryDate: expiryDate || null,
    customer: { id: customerObj.id },
    materialsList: [{ id: parseInt(materialId) }],
    paperRatePerSheet: paperRatePerSheet,
    paperCost: paperCost,
    finishingCost: finishingCost,
    impressionCost: impressionCost,
    wastageSheets: wastageSheets,
    edgeMarginMm: edgeMarginMm,
    serviceChargePercentage: serviceChargePercentage,
    serviceChargeAmount: serviceChargeAmount,
    unitPrice: unitPrice
  };

  postHTTPService('/order/save/quotation', "POST", "json", requestData)
  .fail(function(jqXHR) {
    const serverMsg = jqXHR.responseJSON && jqXHR.responseJSON.message
      ? jqXHR.responseJSON.message
      : 'Failed to save quotation. Please try again.';
    swal.fire({ icon: 'error', title: 'Error!', text: serverMsg });
  })
  .then((response) => {
    if (!response) return;
    swal.fire({
      icon: 'success',
      title: 'Success!',
      text: 'Quotation generated and saved successfully.',
      timer: 2000,
      showConfirmButton: false
    });

    const modalEl = document.getElementById('generateQuatationModal');
    const modalInstance = bootstrap.Modal.getOrCreateInstance(modalEl);
    modalInstance.hide();

    const form = document.getElementById('quotationForm');
    if (form) form.reset();

    if (customerNameInput) {
      delete customerNameInput.dataset.customer;
    }

    const costWraps = ['bindingCostWrap', 'cuttingCostWrap', 'foilingCostWrap', 'laminationThermalCostWrap', 'laminationNormalCostWrap'];
    costWraps.forEach(id => {
      const el = document.getElementById(id);
      if (el) el.style.display = 'none';
    });

    calculateQuotation();
    loadQuotations();
  });
}

function searchQuotations() {
  const query = document.getElementById("searchQuotation").value.toLowerCase();
  const rows = document.querySelectorAll("#quotationTableBody tr");
  rows.forEach(row => {
    // If the row is a "No quotations found" message, ignore it
    if (row.cells.length === 1) return;
    
    const customer = row.cells[1].textContent.toLowerCase();
    const description = row.cells[6].textContent.toLowerCase();
    
    if (customer.includes(query) || description.includes(query)) {
      row.style.display = "";
    } else {
      row.style.display = "none";
    }
  });
}

function loadQuotations() {
  getHTTPService('/order/quotations', 'GET', 'json')
    .then((data) => {
      const tbody = document.getElementById('quotationTableBody');
      if (!tbody) return;
      if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No quotations found.</td></tr>';
        return;
      }

      tbody.innerHTML = data.map((q) => {
        const customerName = q.customer ? q.customer.name : 'Walk-in Customer';
        const qDate = q.quotationdate || 'N/A';
        const expDate = q.expiryDate || 'N/A';
        const amount = typeof q.quotationamount === 'number' ? q.quotationamount.toFixed(2) : '0.00';
        const desc = q.quotationdescription || '';
        
        let statusBadge = '';
        let actionButtons = '';

        const qJson = JSON.stringify(q).replace(/"/g, '&quot;');

        if (q.quotationstatus === 'APPROVED' || q.quotationstatus === 'IN PRODUCTION') {
          statusBadge = '<span class="badge bg-success">Approved / In Production</span>';
          actionButtons = `
            <button class="btn btn-info px-3 py-2" onclick="printQuotation(JSON.parse(this.dataset.q))" data-q="${qJson}" title="Print Quotation"><i class="bi bi-printer"></i> Print</button>
            <button class="btn btn-secondary px-3 py-2 ms-2 fw-bold" disabled title="Already in Production">Sent to Production</button>
            <button class="btn btn-danger px-3 py-2 ms-2" onclick="deleteQuotation(${q.id})">Delete</button>
          `;
        } else {
          statusBadge = `<span class="badge bg-warning text-dark">${q.quotationstatus || 'PENDING'}</span>`;
          actionButtons = `
            <button class="btn btn-info px-3 py-2" onclick="printQuotation(JSON.parse(this.dataset.q))" data-q="${qJson}" title="Print Quotation"><i class="bi bi-printer"></i> Print</button>
            <button class="btn btn-warning px-3 py-2 ms-2 text-dark fw-bold" onclick="sendToProduction(${q.id})" title="Send to Production">Send to Production</button>
            <button class="btn btn-danger px-3 py-2 ms-2" onclick="deleteQuotation(${q.id})">Delete</button>
          `;
        }

        return `
          <tr>
            <td>Q-${q.id}</td>
            <td>${customerName}</td>
            <td>${qDate}</td>
            <td>${statusBadge}</td>
            <td>Rs. ${amount}</td>
            <td>${expDate}</td>
            <td>${desc}</td>
            <td>
              <div class="d-flex flex-row">
                ${actionButtons}
              </div>
            </td>
          </tr>
        `;
      }).join('');
    })
    .catch((error) => {
      console.error("Error loading quotations:", error);
      const tbody = document.getElementById('quotationTableBody');
      if (tbody) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-danger">Failed to load quotations.</td></tr>';
      }
    });
}

// Open a professional print window for a single quotation
function printQuotation(q) {
  const customerName = q.customer ? q.customer.name : 'Walk-in Customer';
  const customerAddress = (q.customer && q.customer.address) ? q.customer.address : 'N/A';
  const customerPhone = (q.customer && q.customer.phone) ? q.customer.phone : 'N/A';
  const customerEmail = (q.customer && q.customer.email) ? q.customer.email : 'N/A';

  const fmt = (v) => (typeof v === 'number' ? v.toFixed(2) : (v || 'N/A'));
  const fmtInt = (v) => (typeof v === 'number' ? Math.round(v) : (v || 'N/A'));

  const finishing = [
    q.bindingtype && q.bindingtype !== 'NONE' ? 'Binding: ' + q.bindingtype : '',
    q.cuttingtype && q.cuttingtype !== 'NONE' ? 'Cutting: ' + q.cuttingtype : '',
    q.foiling && q.foiling !== 'None' ? 'Foiling: ' + q.foiling : '',
    q.lamination && q.lamination !== 'None' ? 'Lamination: ' + q.lamination : ''
  ].filter(Boolean).join(' | ') || 'None';

  const html = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Quotation Q-${q.id} – Thisara Printers</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', Arial, sans-serif; color: #1a1a2e; background: #fff; padding: 32px; font-size: 13px; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #1a1a2e; padding-bottom: 16px; margin-bottom: 20px; }
    .company-name { font-size: 26px; font-weight: 800; color: #1a1a2e; letter-spacing: 1px; }
    .company-sub { font-size: 11px; color: #555; margin-top: 2px; }
    .quotation-badge { background: #1a1a2e; color: #fff; padding: 6px 14px; border-radius: 6px; font-size: 15px; font-weight: 700; text-align: right; }
    .quotation-badge small { display: block; font-size: 10px; font-weight: 400; color: #aaa; margin-bottom: 2px; }
    .section { display: flex; gap: 24px; margin-bottom: 20px; }
    .box { flex: 1; border: 1px solid #dde1ea; border-radius: 8px; padding: 14px 16px; }
    .box h4 { font-size: 11px; text-transform: uppercase; letter-spacing: 0.8px; color: #888; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
    .row-item { display: flex; justify-content: space-between; margin-bottom: 5px; }
    .row-item .label { color: #555; }
    .row-item .value { font-weight: 600; }
    .cost-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    .cost-table th { background: #1a1a2e; color: #fff; padding: 8px 12px; text-align: left; font-size: 11px; text-transform: uppercase; }
    .cost-table td { padding: 8px 12px; border-bottom: 1px solid #eee; }
    .cost-table tr:last-child td { border-bottom: none; }
    .total-row td { font-weight: 700; font-size: 14px; background: #f3f4f8; }
    .footer { display: flex; gap: 40px; margin-top: 24px; padding-top: 14px; border-top: 2px dashed #ccc; font-size: 11px; color: #555; }
    .sig-line { flex: 1; }
    .sig-line .line { border-top: 1px solid #999; margin-top: 28px; margin-bottom: 4px; }
    .status-badge { display: inline-block; padding: 3px 10px; border-radius: 4px; font-size: 11px; font-weight: 700; background: #fffbea; color: #7a5d00; border: 1px solid #f0c84a; }
    @media print {
      body { padding: 16px; }
      button { display: none; }
    }
  </style>
</head>
<body>
  <div class="header">
    <div>
      <div class="company-name">Thisara Printers</div>
    </div>
    <div class="quotation-badge">
      <small>Quotation For</small>
      ${q.customerName}
    </div>
  </div>

  <div class="section">
    <div class="box">
      <h4>Customer Details</h4>
      <div class="row-item"><span class="label">Name</span><span class="value">${customerName}</span></div>
      <div class="row-item"><span class="label">Email</span><span class="value">${customerEmail}</span></div>
      <div class="row-item"><span class="label">Phone</span><span class="value">${customerPhone}</span></div>
      <div class="row-item"><span class="label">Address</span><span class="value">${customerAddress}</span></div>
    </div>
    <div class="box">
      <h4>Quotation Details</h4>
      <div class="row-item"><span class="label">Date</span><span class="value">${q.quotationdate || 'N/A'}</span></div>
      <div class="row-item"><span class="label">Expiry Date</span><span class="value">${q.expiryDate || 'N/A'}</span></div>
      <div class="row-item"><span class="label">Status</span><span class="value"><span class="status-badge">${q.quotationstatus || 'PENDING'}</span></span></div>
      <div class="row-item"><span class="label">Description</span><span class="value">${q.quotationdescription || '—'}</span></div>
    </div>
  </div>

  <div class="section">
    <div class="box">
      <h4>Product Specifications</h4>
      <div class="row-item"><span class="label">Product Size</span><span class="value">${q.productsize || 'N/A'}</span></div>
      <div class="row-item"><span class="label">Colour</span><span class="value">${q.color || 'N/A'}</span></div>
      <div class="row-item"><span class="label">Quantity</span><span class="value">${fmtInt(q.quantity)} pcs</span></div>
      <div class="row-item"><span class="label">Finishing</span><span class="value">${finishing}</span></div>
    </div>
  </div>

  <table class="cost-table">
    <thead>
      <tr><th>Cost Item</th><th>Amount (Rs.)</th></tr>
    </thead>
    <tbody>
  
      <tr><td>Service Charge (${fmtInt(q.serviceChargePercentage)}%)</td><td>Rs. ${fmt(q.serviceChargeAmount)}</td></tr>
      <tr class="total-row"><td>TOTAL QUOTATION AMOUNT</td><td>Rs. ${fmt(q.quotationamount)}</td></tr>
      <tr><td>Unit Price</td><td>Rs. ${fmt(q.unitPrice)} / pc</td></tr>
      <tr><td>Advance Amount Paid</td><td>Rs. ${fmt(q.advanceamount)}</td></tr>
    </tbody>
  </table>

  <div class="footer">
    <div class="sig-line"><div class="line"></div>Customer Signature</div>
    <div class="sig-line"><div class="line"></div>Authorised Signature</div>
    <div style="flex:2; text-align:right; font-size:10px; color:#aaa; padding-top: 32px;">
      Printed on ${new Date().toLocaleDateString()}<br>Thisara Printers – All rights reserved
    </div>
  </div>

  <script>window.onload = function() { window.print(); }<\/script>
</body>
</html>`;

  const win = window.open('', '_blank', 'width=900,height=700');
  if (win) {
    win.document.write(html);
    win.document.close();
  } else {
    alert('Pop-up blocked. Please allow pop-ups for this site to print quotations.');
  }
}

function deleteQuotation(id) {
  swal.fire({
    icon: 'warning',
    title: 'Delete Quotation',
    text: 'Are you sure you want to delete quotation Q-' + id + '?',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: 'Yes, delete it!'
  }).then((result) => {
    if (result.isConfirmed) {
      getHTTPService('/order/quotation/delete/' + id, 'DELETE', 'json')
        .then((response) => {
          swal.fire({
            icon: 'success',
            title: 'Deleted!',
            text: 'Quotation has been deleted.',
            timer: 1500,
            showConfirmButton: false
          }).then(() => {
            loadQuotations();
          });
        })
        .catch((err) => {
          console.error("Error deleting quotation:", err);
          swal.fire({
            icon: 'error',
            title: 'Failed',
            text: 'Could not delete quotation.'
          });
        });
    }
  });
}

function sendToProduction(id) {
  const defaultDeadline = new Date();
  defaultDeadline.setDate(defaultDeadline.getDate() + 7);
  const defaultDeadlineStr = defaultDeadline.toISOString().split('T')[0];

  swal.fire({
    title: 'Send to Production',
    html: `
      <div class="text-start">
        <label for="swalPriority" class="form-label fw-bold">Priority</label>
        <select id="swalPriority" class="form-select mb-3">
          <option value="Normal">Normal</option>
          <option value="High">High</option>
          <option value="Urgent">Urgent</option>
        </select>
        <label for="swalDeadline" class="form-label fw-bold">Deadline Date</label>
        <input type="date" id="swalDeadline" class="form-control mb-3" value="${defaultDeadlineStr}">
        <label for="swalAdvance" class="form-label fw-bold">Advance Amount (Rs.) <small class="text-muted fw-normal">— optional, if customer has paid</small></label>
        <input type="number" id="swalAdvance" class="form-control" placeholder="0.00" min="0" step="0.01">
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: 'Confirm Send',
    confirmButtonColor: '#ffc107',
    cancelButtonColor: '#6c757d',
    preConfirm: () => {
      return {
        priority: document.getElementById('swalPriority').value,
        deadline: document.getElementById('swalDeadline').value,
        advanceAmount: parseFloat(document.getElementById('swalAdvance').value) || 0
      };
    }
  }).then((result) => {
    if (result.isConfirmed && result.value) {
      const payload = result.value;
      postHTTPService('/order/send-to-production/' + id, 'POST', 'json', payload)
        .then((response) => {
          swal.fire({
            icon: 'success',
            title: 'Success!',
            text: 'Quotation successfully sent to production tracking.',
            timer: 2000,
            showConfirmButton: false
          }).then(() => {
            loadQuotations();
          });
        })
        .catch((err) => {
          console.error("Error sending to production:", err);
          const serverMsg = err.responseJSON && err.responseJSON.message
            ? err.responseJSON.message
            : 'Failed to send quotation to production.';
          swal.fire({
            icon: 'error',
            title: 'Error!',
            text: serverMsg
          });
        });
    }
  });
}

window.addEventListener('DOMContentLoaded', () => {
  loadQuotations();
  loadOrdersTab();
});

// Load live production jobs into the Order tab
function loadOrdersTab() {
  const tbody = document.getElementById('orderTableBody');
  if (!tbody) return;

  getHTTPService('/production/all', 'GET', 'json')
    .then((data) => {
      if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No production jobs found.</td></tr>';
        return;
      }

      tbody.innerHTML = data.map((job) => {
        // Priority badge
        let priorityBadge = '';
        if (job.priority === 'Urgent') {
          priorityBadge = '<span class="badge bg-danger">Urgent</span>';
        } else if (job.priority === 'High') {
          priorityBadge = '<span class="badge bg-warning text-dark">High</span>';
        } else {
          priorityBadge = '<span class="badge bg-success">Normal</span>';
        }

        // Status dropdown
        const phases = ['New Orders', 'Design Phase', 'Printing', 'Finishing', 'Ready to Deliver', 'Dispatched'];
        const selectOptions = phases.map(p =>
          `<option value="${p}" ${job.status === p ? 'selected' : ''}>${p}</option>`
        ).join('');
        const statusSelect = `<select class="form-select status-select" style="width:160px;" onchange="updateOrderStatus('${job.orderId}', this.value)">${selectOptions}</select>`;

        // Deadline styling
        let deadlineCell = job.deadline || 'N/A';
        if (job.status === 'Ready to Deliver' || job.status === 'Dispatched') {
          deadlineCell = `<span class="text-success fw-bold">${job.deadline || 'N/A'}</span>`;
        } else if (job.deadline && new Date(job.deadline) < new Date()) {
          deadlineCell = `<span class="text-danger fw-bold">${job.deadline}</span>`;
        }

        // Action button
        let actionBtn = '';
        if (job.status === 'Ready to Deliver') {
          actionBtn = `<button class="btn btn-teal px-3 py-2 btn-sm" onclick="dispatchOrderJob('${job.orderId}')">Dispatch</button>`;
        } else {
          actionBtn = `<button class="btn btn-sm btn-outline-dark px-3 py-2" onclick="viewOrderDetails(${JSON.stringify(job).replace(/"/g, '&quot;')})">Details</button>`;
        }

        return `
          <tr class="${job.priority === 'Urgent' ? 'table-danger' : job.priority === 'High' ? 'table-warning' : ''}">
            <td class="fw-bold">${job.orderId || ''}</td>
            <td>${job.customerName || 'N/A'}</td>
            <td>${job.description || ''}</td>
            <td>${deadlineCell}</td>
            <td>${priorityBadge}</td>
            <td>${statusSelect}</td>
            <td>
              <div class="d-flex justify-content-center gap-2">
                ${actionBtn}
              </div>
            </td>
          </tr>
        `;
      }).join('');
    })
    .catch((err) => {
      console.error('Error loading order jobs:', err);
      tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-danger">Failed to load production jobs.</td></tr>';
    });
}

// Update status of a production job from the Order tab
function updateOrderStatus(orderId, newStatus) {
  postHTTPService('/production/update-status', 'POST', 'json', { orderId, status: newStatus })
    .then(() => {
      swal.fire({
        icon: 'success',
        title: 'Status Updated',
        text: `Order ${orderId} moved to ${newStatus}.`,
        timer: 1500,
        showConfirmButton: false
      }).then(() => loadOrdersTab());
    })
    .catch(() => {
      swal.fire({ icon: 'error', title: 'Update Failed', text: 'Unable to update job status.' });
      loadOrdersTab();
    });
}

// Dispatch a production job from the Order tab
function dispatchOrderJob(orderId) {
  swal.fire({
    icon: 'question',
    title: 'Dispatch Order',
    text: `Dispatch ${orderId}? This removes it from the active queue.`,
    showCancelButton: true,
    confirmButtonColor: '#17A2B8',
    cancelButtonColor: '#ff5252',
    confirmButtonText: 'Confirm Dispatch'
  }).then((result) => {
    if (!result.isConfirmed) return;
    getHTTPService(`/production/delete-by-order/${orderId}`, 'DELETE', 'json')
      .then(() => {
        swal.fire({ icon: 'success', title: 'Dispatched', timer: 1500, showConfirmButton: false })
          .then(() => loadOrdersTab());
      })
      .catch(() => {
        swal.fire({ icon: 'error', title: 'Dispatch Failed', text: 'Unable to dispatch order.' });
      });
  });
}

// Show a quick details alert for an Order tab job
function viewOrderDetails(job) {
  swal.fire({
    title: `<strong>${job.orderId}</strong>`,
    html: `
      <div class="text-start">
        <p><strong>Customer:</strong> ${job.customerName || 'N/A'}</p>
        <p><strong>Description:</strong> ${job.description || 'N/A'}</p>
        <p><strong>Deadline:</strong> ${job.deadline || 'N/A'}</p>
        <p><strong>Priority:</strong> ${job.priority || 'Normal'}</p>
        <p><strong>Status:</strong> ${job.status || 'N/A'}</p>
      </div>
    `,
    icon: 'info',
    confirmButtonText: 'Close'
  });
}