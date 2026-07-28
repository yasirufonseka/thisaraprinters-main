const inputs = document.querySelectorAll("#employeeFormData .form-control, #employeeFormData .form-select, #employeeFormData .form-check-input");
let employee = [];
let isUpdate = false;
isView = false;


inputs.forEach(input => {
    input.addEventListener("blur", function () {
        // Find the error message element that belongs to this specific input
        const errorMsg = input.parentElement.querySelector(".errorMessage") || input.closest(".mb-3")?.querySelector(".errorMessage");

        if (input.value.trim() === "") {
            if (errorMsg) errorMsg.textContent = "Input field can't be empty";
            input.classList.add("input-error");
        } else {
            if (errorMsg) errorMsg.textContent = "";
            input.classList.remove("input-error");
        }
    });
});

//validate nic
const employeeNic = document.getElementById("employeeNIC");
function validateNIC() {
    const nicRegex = /^\d{12}$/;
    const oldNicRegex = /^\d{9}[vV]$/;
    const nic = employeeNic.value.trim();
    const errorMessage = employeeNic.parentElement.querySelector(".errorMessage") || employeeNic.closest(".mb-3")?.querySelector(".errorMessage");

    if (nic === "") return;

    if (!nicRegex.test(nic) && !oldNicRegex.test(nic)) {
        if (errorMessage) errorMessage.textContent = "Invalid NIC format. Please enter 9 digits with V/X or 12 digits.";
        employeeNic.classList.add("input-error");
    } else {
        if (errorMessage) errorMessage.textContent = "";
        employeeNic.classList.remove("input-error");
    }
    setBirthDay();
}

//set date of birth
const setDob = document.getElementById("employeeDOB");

function setBirthDay() {
    let nic = employeeNic.value.trim();
    if (nic.length !== 10 && nic.length !== 12) return;

    let year, dayValue;

    if (nic.length === 12) {
        year = parseInt(nic.substring(0, 4));
        dayValue = parseInt(nic.substring(4, 7));
    } else {
        year = parseInt("19" + nic.substring(0, 2));
        dayValue = parseInt(nic.substring(2, 5));
    }

    // check for female brithday
    let isFemale = false;
    if (dayValue > 500) {
        dayValue = dayValue - 500;
        isFemale = true;
    }

    // Calculate the month and date from dayValue using a leap year (e.g. 2004)
    // to ensure Sri Lankan NIC day counting (which always assumes 366 days/year) is accurate.
    let date = new Date(2004, 0, dayValue);

    const yyyy = year;
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');

    const formattedDateForInput = `${yyyy}-${mm}-${dd}`;

    if (setDob) {
        setDob.value = formattedDateForInput;
    }

    // Automatically check the correct gender radio button
    const radioMale = document.getElementById("radioMale");
    const radioFemale = document.getElementById("radioFemale");
    if (isFemale) {
        if (radioFemale) radioFemale.checked = true;
    } else {
        if (radioMale) radioMale.checked = true;
    }
}

//validetion for email
const validateEmail = () => {
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    const emailField = document.getElementById("employeeEmail");
    if (!emailField) return;
    const email = emailField.value.trim();
    const errorMessage = emailField.parentElement.querySelector(".errorMessage") || emailField.closest(".mb-3")?.querySelector(".errorMessage");
    if (email === "") return;
    if (!emailRegex.test(email)) {
        if (errorMessage) errorMessage.textContent = "Invalid email format. Please enter a valid email address.";
        emailField.classList.add("input-error");
    } else {
        if (errorMessage) errorMessage.textContent = "";
        emailField.classList.remove("input-error");
    }
}

//validate mobile no
const validateMobileNo = () => {
    const mobileNoRegex = /^[0-9]{10}$/;
    const phoneField = document.getElementById("employeePhone");
    if (!phoneField) return;
    const mobileNo = phoneField.value.trim();
    const errorMsg = phoneField.parentElement.querySelector(".errorMessage") || phoneField.closest(".mb-3")?.querySelector(".errorMessage");
    if (mobileNo === "") return;
    if (!mobileNoRegex.test(mobileNo)) {
        if (errorMsg) errorMsg.textContent = "Invalid mobile number format. Please enter 10 digits.";
        phoneField.classList.add("input-error");
    } else {
        if (errorMsg) errorMsg.textContent = "";
        phoneField.classList.remove("input-error");
    }
}

//set calling name
const setCallingName = () => {
    const fullName = document.getElementById("employeeFullName").value.trim();
    const callingName = document.getElementById("employeeName");
    if (fullName === "") return;
    const names = fullName.split(" ");
    callingName.value = names[0];
}

const refreshEmployeeData = () => {

    getHTTPService("/employees/get/alldata", "GET", "json")
        .done(function (data, jqXHR) {

            employee = data;
            console.log("Employee data fetched successfully:", data);
            populateEmployeeTable();

        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching employee data:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            alert("Error: " + textStatus);
        })
        .always(function () {

            console.log("Request complete");
        });

}

// load employee data when the page is ready
$(document).ready(function () {
    loadDesignations();
    refreshEmployeeData();
});

// load designations into the dropdown
const loadDesignations = () => {
    getHTTPService("/employees/get/designations", "GET", "json")
        .done(function (data) {
            const select = document.getElementById("jobposition");
            if (!select) return;
            // clear all except the first placeholder option
            while (select.options.length > 1) select.remove(1);
            data.forEach(d => {
                const opt = new Option(d.designation, d.id);
                select.add(opt);
            });
        })
        .fail(function () {
            console.error("Failed to load designations");
        });
};

//add employee array data into the table after fetching from the server

const employeeTableBody = document.querySelector(".employeeTable tbody");

const populateEmployeeTable = (data = employee) => {
    employeeTableBody.innerHTML = "";
    data.forEach(emp => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td onclick="viewEmployee(${emp.id})">${emp.fullname}</td>
            <td onclick="viewEmployee(${emp.id})">${emp.callingname}</td>
            <td onclick="viewEmployee(${emp.id})">${emp.nic}</td>
            <td onclick="viewEmployee(${emp.id})">${emp.designationid ? emp.designationid.designation : 'N/A'}</td>
            <td onclick="viewEmployee(${emp.id})">${emp.email}</td>
            <td onclick="viewEmployee(${emp.id})">${emp.phonenumber}</td>
            <td class="d-flex flex-row">
                <button class="btn btn-teal px-3 py-2 ms-2 viewform"  onclick="updateEmployee(${emp.id})">Edit</button>
                <button class="btn btn-red px-3 py-2 ms-2 viewform" onclick="deleteEmployee(${emp.id})">Delete</button>
            </td>
        `;
        employeeTableBody.appendChild(row);
    });
}

//update employee
const updateEmployee = (id) => {
    console.log(id);
    isUpdate = true;
    isView = false;
    const submitBtn = document.getElementById("submitButton");
    const clearBtn = document.getElementById("clearButton");
    if (submitBtn) submitBtn.textContent = "Update Employee";
    const modalTitle = document.querySelector(".modal-title");
    if (modalTitle) modalTitle.textContent = "Update Employee";

    // Reset readOnly on all inputs (in case view mode was previously opened)
    document.querySelectorAll("#employeeFormData .form-control, #employeeFormData .form-select, #employeeFormData .form-check-input").forEach(input => {
        input.readOnly = false;
        input.disabled = false;
    });
    const jobSelect = document.getElementById("jobposition");
    if (jobSelect) jobSelect.disabled = false;

    //open employee form model
    if (submitBtn) submitBtn.classList.remove("d-none");
    if (clearBtn) clearBtn.classList.remove("d-none");
    const modal = new bootstrap.Modal(document.getElementById("employeeModal"));
    modal.show();

    //set employee data to the form
    const emp = employee.find(e => e.id === id);
    document.getElementById("employeeFullName").value = emp.fullname;
    document.getElementById("employeeName").value = emp.callingname;
    document.getElementById("employeeNIC").value = emp.nic;
    document.getElementById("employeeDOB").value = emp.dob;
    document.getElementById("employeeEmail").value = emp.email;
    document.getElementById("employeePhone").value = emp.phonenumber;
    document.getElementById("emgpersonname").value = emp.emgpersonname || "";
    document.getElementById("emgpersonphonenumber").value = emp.emgpersonphonenumber || "";
    document.querySelector(`input[name="gender"][value="${emp.gender}"]`).checked = true;
    document.querySelector(`textarea[name='address']`).value = emp.address;
    if (emp.designationid && emp.designationid.id) {
        const desigSelect = document.querySelector(`select[name="designationid"]`);
        if (desigSelect) desigSelect.value = emp.designationid.id;
    }
    // Store the employee data
    document.getElementById("employeeFormData").dataset.id = id;
    const imagePreview = document.getElementById('imagePreview');
    const uploadPrompt = document.getElementById('uploadPrompt');

    imagePreview.src = `data:image/jpeg;base64,${emp.image}`;
    imagePreview.classList.remove('d-none');
    uploadPrompt.classList.remove('d-none');
}

const validateEmptyFormData = () => {
    let isValid = true;
    const inputs = document.querySelectorAll("#employeeFormData .form-control, #employeeFormData .form-select");
    inputs.forEach(input => {
        if (input.value.trim() === "") {
            const errorMsg = input.parentElement.querySelector(".errorMessage") || input.closest(".mb-3")?.querySelector(".errorMessage");
            if (errorMsg) errorMsg.textContent = "Input field can't be empty";
            input.classList.add("input-error");
            isValid = false;
        }
    });
    return isValid;
};

document.getElementById("uploadArea").addEventListener("click",function () {
    document.getElementById("imageInput").click();
    
});

function previewImage(input) {
    const file = input.files[0];
    if (file) {
        const reader = new FileReader();

        reader.onload = (e) => {
            const imagePreview = document.getElementById('imagePreview');
            const uploadPrompt = document.getElementById('uploadPrompt');

            imagePreview.src = e.target.result;
            imagePreview.classList.remove('d-none');
            uploadPrompt.classList.add('d-none');
        }
        reader.readAsDataURL(file);
    }
    
}


const employeeFormDataListener = (event) => {
    event.preventDefault();

   const imageFile = document.getElementById("imageInput").files[0];
   const imageSize = imageFile.size;
   console.log("image siz"+" "+ imageSize/1024/1024+" MB");
   
    if (!validateEmptyFormData()) {
        return;
    }
   
    const formData = new FormData(employeeFormData);
    //const convertToJSON = Object.fromEntries(formData.entries());
   // console.log("Form data collected:", convertToJSON);
    const employeeId = document.getElementById("employeeFormData").dataset.id;

    if(imageFile){
        // Do not append manually, FormData already includes it if the input is in the form
        // formData.append("image",imageFile);
    }
    // Store isUpdate before resetting it
    const shouldUpdate = isUpdate;

    isUpdate = false;
    isView = false;
    document.getElementById("submitButton").textContent = "Add Employee";
    document.querySelector(".modal-title").textContent = "Add Employee";

    if (shouldUpdate) {
        console.log("Updating employee with ID:", employeeId);

    }

    const url = shouldUpdate ? `/employees/update/${employeeId}` : "/employees/add/employee";
    const method = "POST";

    $.ajax({
        url: url,
        type: "POST",
        data: formData,
        contentType: false,
        processData:false,
        async: false,
    })
        .done(function (data, jqXHR) {
            swal.fire({
                title: "Success",
                text: shouldUpdate ? "Employee updated successfully!" : "Employee added successfully!",
                icon: "success",
                showConfirmButton: true,
                confirmButtonText: "OK",
            }).then((result) => {
                console.log(shouldUpdate ? "Employee updated successfully:" : "Employee added successfully:", data);
                refreshEmployeeData();
                window.location.href = "/employees/getemployees";
                $('#employeeModal').modal('hide');
            });
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error(shouldUpdate ? "Error updating employee:" : "Error adding employee:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            swal.fire({
                title: "Error",
                text: shouldUpdate ? "Failed to update employee. Please try again." : "Failed to add employee. Please try again." + jqXHR.responseText + textStatus + errorThrown,
                icon: "error",
                confirmButtonText: "OK"
            });

        })
        .always(function () {
            console.log("Request complete");
        });

    document.getElementById("employeeFormData").reset();
};


//employeeFormData.addEventListener("submit", employeeFormDataListener);

// search employee

const searchEmployees = () => {
    const searchValue = document.getElementById("searchEmployee").value.trim().toLowerCase();
    console.log("Search value:", searchValue);
    const filterEmployee = employee.filter(emp => emp.fullname.toLowerCase().includes(searchValue) ||
        emp.callingname.toLowerCase().includes(searchValue) ||
        emp.nic.toLowerCase().includes(searchValue) ||
        emp.email.toLowerCase().includes(searchValue));
    populateEmployeeTable(filterEmployee);
}
const resetForm = () => {
    if (document.activeElement) {
        document.activeElement.blur();
    }

    const imagePreview = document.getElementById('imagePreview');
    const uploadPrompt = document.getElementById('uploadPrompt');

    imagePreview.src = "";
    imagePreview.classList.add('d-none');
    uploadPrompt.classList.remove('d-none');

    const form = document.getElementById("employeeFormData");
    if (form) form.reset();
    isUpdate = false;
    isView = false;
    
    const submitBtn = document.getElementById("submitButton");
    const clearBtn = document.getElementById("clearButton");
    if (submitBtn) {
        submitBtn.textContent = "Add Employee";
        submitBtn.style.display = "block";
        submitBtn.classList.remove("d-none");
    }
    if (clearBtn) clearBtn.classList.remove("d-none");
    const modalTitle = document.querySelector(".modal-title");
    if (modalTitle) modalTitle.textContent = "Add Employee";

    // Make all inputs editable again
    const inputs = document.querySelectorAll("#employeeFormData .form-control, #employeeFormData .form-select, #employeeFormData .form-check-input");
    inputs.forEach(input => {
        input.readOnly = false;
        input.disabled = false;
    });
    // Re-enable select
    const jobSelect = document.getElementById("jobposition");
    if (jobSelect) jobSelect.disabled = false;
}

const viewEmployee = (id) => {

    isView = true;
    isUpdate = false;
    const modalTitle = document.querySelector(".modal-title");
    if (modalTitle) modalTitle.textContent = "Employee Details";
    const submitBtn = document.getElementById("submitButton");
    const clearBtn = document.getElementById("clearButton");
    if (submitBtn) submitBtn.classList.add("d-none");
    if (clearBtn) clearBtn.classList.add("d-none");


    //make inputs readonly
    const inputs = document.querySelectorAll("#employeeFormData .form-control, #employeeFormData .form-select, #employeeFormData .form-check-input ");
    inputs.forEach(input => {
        if (input.type === 'radio' || input.tagName === 'SELECT') {
            input.disabled = true;
        } else {
            input.readOnly = true;
        }
    });

    //open employee form model
    const modal = new bootstrap.Modal(document.getElementById("employeeModal"));
    modal.show();

    //set employee data to the form
    const emp = employee.find(e => e.id === id);
    console.log(emp.image);
    document.getElementById("employeeFullName").value = emp.fullname;
    document.getElementById("employeeName").value = emp.callingname;
    document.getElementById("employeeNIC").value = emp.nic;
    document.getElementById("employeeDOB").value = emp.dob;
    document.getElementById("employeeEmail").value = emp.email;
    document.getElementById("employeePhone").value = emp.phonenumber;
    document.getElementById("emgpersonname").value = emp.emgpersonname || "";
    document.getElementById("emgpersonphonenumber").value = emp.emgpersonphonenumber || "";
    document.querySelector(`input[name="gender"][value="${emp.gender}"]`).checked = true;
    document.querySelector(`textarea[name='address']`).value = emp.address;
    document.querySelector(`select[name="designationid"] option[value="${emp.designationid.id}"]`).selected = true;
    const imagePreview = document.getElementById('imagePreview');
    const uploadPrompt = document.getElementById('uploadPrompt');

    imagePreview.src = `data:image/jpeg;base64,${emp.image}`;
    imagePreview.classList.remove('d-none');
    uploadPrompt.classList.add('d-none');
}

const deleteEmployee = (id) => {
    swal.fire({
        title: "Are you sure?",
        text: "You will not be able to recover this employee!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, delete it!"
    }).then((result) => {
        if (result.isConfirmed) {
            getHTTPService(`/employees/delete/${id}`, "DELETE", "json")
                .done(function (data, jqXHR) {
                    swal.fire({
                        title: "Deleted!",
                        text: "Employee has been deleted.",
                        icon: "success",
                        showConfirmButton: true,
                        confirmButtonText: "OK",
                    }).then((result) => {
                        console.log("Employee deleted successfully:", data);
                        refreshEmployeeData();
                        window.location.href = "/employees/getemployees";
                    });
                })
                .fail(function (jqXHR, textStatus, errorThrown) {
                    console.error("Error deleting employee:", textStatus, errorThrown);
                    console.error("Response text:", jqXHR.responseText);
                    console.error("Status code:", jqXHR.status);
                    swal.fire({
                        title: "Error",
                        text: "Failed to delete employee. Please try again.",
                        icon: "error",
                        confirmButtonText: "OK"
                    });
                })
                .always(function () {
                    console.log("Request complete");
                });
        }
    });
}
