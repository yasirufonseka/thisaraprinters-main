let employeeList = [];
let isUpdate = false;
let user;
let roleList = [];
let allModules = [];

$(document).ready(function () {
    isUpdate = false;
    getUserRole();
    
    // Load modules list and render the grid
    getHTTPService("/privilege/modules", "GET", "json").then((response) => {
        allModules = response;
        renderModulePrivilegeGrid();
    }).catch((error) => {
        console.error("Error loading modules:", error);
    });
    
    // Load privilege table
    loadPrivilegeTable();
});

const filterTableByText = (tableSelector, query) => {
    document.querySelectorAll(`${tableSelector} tbody tr`).forEach((row) => {
        if (row.cells.length === 1) return;
        row.style.display = !query || row.textContent.toLowerCase().includes(query) ? "" : "none";
    });
};

document.addEventListener("DOMContentLoaded", () => {
    const userSearch = document.getElementById("searchUser");
    const privilegeSearch = document.getElementById("searchPrivilege");

    userSearch?.addEventListener("input", () => {
        filterTableByText(".userTable", userSearch.value.trim().toLowerCase());
    });
    privilegeSearch?.addEventListener("input", () => {
        filterTableByText(".privilegeTable", privilegeSearch.value.trim().toLowerCase());
    });
});
// Default open tab
document.addEventListener("DOMContentLoaded", function () {
    var defaultBtn = document.querySelector(".tab-btn");
    if (defaultBtn) {
        defaultBtn.click();
    }
});

//get all employees
const getEmployeeList = () => {
    getHTTPService("/user/getemployeelist", "GET", "json").then((response) => {
        employeeList = response;
        console.log(employeeList);

        //create dynamic option for employee dropdown
        let employeeOptions = document.getElementById("userEmployee");
        employeeOptions.innerHTML = '<option value="">Select Employee</option>';
        employeeList.forEach(employee => {
            employeeOptions.innerHTML += `<option value="${employee.id}">${employee.fullname}</option>`;
        });
        
        // Reset form and header for new user
        document.getElementById("userFormData").reset();
        document.getElementById("addUserModalHeader").innerText = "Add New User";
        isUpdate = false;

    })
}

// ─── Helpers to show/hide inline error messages under form fields ─────────────
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

// ─── Validate the user form — returns false and highlights bad fields ──────────
function validateUserForm() {
    let isValid = true;

    const requiredFields = [
        { id: "username", label: "Username" },
        { id: "userPassword", label: "Password" },
    ];

    requiredFields.forEach(({ id, label }) => {
        const el = document.getElementById(id);
        if (!el || !el.value.trim()) {
            showFieldError(id, `${label} is required`);
            isValid = false;
        } else {
            clearFieldError(id);
        }
    });

    // Employee and role are dropdowns, so check their value differently
    const employeeEl = document.getElementById("userEmployee");
    if (!employeeEl || !employeeEl.value) {
        showFieldError("userEmployee", "Please select an employee");
        isValid = false;
    } else {
        clearFieldError("userEmployee");
    }

    const roleEl = document.getElementById("userRole");
    if (!roleEl || !roleEl.value) {
        showFieldError("userRole", "Please select a role");
        isValid = false;
    } else {
        clearFieldError("userRole");
    }

    return isValid;
}

// ─── Blur listeners so errors clear as soon as the user fixes them ────────────
document.addEventListener("DOMContentLoaded", function () {
    ["username", "userPassword"].forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        el.addEventListener("blur", () => {
            if (!el.value.trim()) showFieldError(id, `${id === "username" ? "Username" : "Password"} is required`);
            else clearFieldError(id);
        });
        el.addEventListener("input", () => clearFieldError(id));
    });

    ["userEmployee", "userRole"].forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        el.addEventListener("change", () => {
            if (!el.value) showFieldError(id, id === "userEmployee" ? "Please select an employee" : "Please select a role");
            else clearFieldError(id);
        });
    });
});

const submitUser = (evt) => {
    // Prevent the default form submission (page reload)
    evt.preventDefault();
    const userformdata = new FormData(userFormData);
    const convertUserFormData = Object.fromEntries(userformdata.entries());

    // Stop here if anything required is missing
    if (!validateUserForm()) return;

    // Convert employeeid to integer
    convertUserFormData.employeeid = parseInt(convertUserFormData.employeeid);
    
    // Convert roleId dropdown into integer
    convertUserFormData.roleId = parseInt(convertUserFormData.roleId);

    if (!isUpdate) {
        Swal.fire({
            icon: "question",
            title: "Please Confirm the Add User",
            text: "Are you sure you want to add this user?",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, add it!"
        }).then((result) => {
            if (result.isConfirmed) {
                postHTTPService("/user/add/user", "POST", "json", convertUserFormData).then((response) => {
                    Swal.fire({
                        icon: "success",
                        title: "User added successfully",
                        text: response.message,
                        timer: 1500,
                        showConfirmButton: false,
                    }).then(() => {

                        window.location.reload();
                    });
                    console.log(response);
                }).catch((error) => {
                    Swal.fire({
                        icon: "error",
                        title: "Error adding user",
                        text: error.message,
                        timer: 1500,
                        showConfirmButton: false,
                    })
                    console.log(error);
                });
                 
            }

        });
    } else {
        Swal.fire({
            icon: "question",
            title: "Please Confirm the Update User",
            text: "Are you sure you want to update this user?",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, update it!"
        }).then((result) => {
            if (result.isConfirmed) {
                postHTTPService(`/user/update/user/${user?.id}`, "PUT", "json", convertUserFormData)
                    .then((response) => {
                        Swal.mixin({
                            toast: true,
                            position: "center",
                            showConfirmButton: false,
                            timer: 2000,
                            timerProgressBar: false,
                            didOpen: (toast) => {
                                toast.onmouseenter = Swal.stopTimer;
                                toast.onmouseleave = Swal.resumeTimer;
                            }
                        }).fire({
                            icon: "success",
                            title: "Update is successful"
                        });

                        const modalEl = document.getElementById("addUserModal");
                        const modal = bootstrap.Modal.getInstance(modalEl);
                        if (modal) {
                            modal.hide();
                            window.location.reload();
                        }
                        isUpdate = false; // Reset flag
                    })
                    .catch((error) => {
                        Swal.fire({
                            icon: "error",
                            title: "Error updating user",
                            text: error.message,
                            timer: 1500,
                            showConfirmButton: false,
                        });
                    });
            }
        });
    }
    
}

const updateUser = (userid) => {
    console.log("button clicked");
    const id = userid;
    console.log(id);

    //change header text
    document.getElementById("addUserModalHeader").innerText = "Update User";

    //open modal
    isUpdate = true;
    const modalEl = document.getElementById("addUserModal");
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();
    
    // Reset form first to clear old data
    document.getElementById("userFormData").reset();
    user = null;

    //get user data 
    getHTTPService(`/user/getuserbyid/${id}`, "GET", "json").then((response) => {
        user = response;
        console.log("selected user: " + (user?.employeeid?.callingname || 'No Employee'));
        const empId = user?.employeeid?.id;
        const empLabel = user?.employeeid?.callingname;

        const select = document.querySelector(`select[name="employeeid"]`);
        if (empId && empLabel) {
            if (!select.querySelector(`option[value="${empId}"]`)) {
                select.add(new Option(empLabel, empId, true, true));
            }
            select.value = empId;
        } else {
            select.value = "";
        }
        
        // Set status with proper value handling
        const statusSelect = document.getElementById("userStatus");
        const statusValue = user?.status?.trim() || "Active";
        statusSelect.value = statusValue;
        console.log("Status set to: " + statusValue);

        const roleId = user?.role?.id;
        document.getElementById("userRole").value = roleId ? Number(roleId) : "";

        document.getElementById("username").value = user?.username ?? "";
        // Only set password if backend provided one; otherwise clear to avoid showing browser autofill
        if (user?.password) {
            document.getElementById("userPassword").value = user.password;
        } else {
            document.getElementById("userPassword").value = "";
            document.getElementById("userPassword").setAttribute("autocomplete", "new-password");
        }
        document.getElementById("userNote").value = user?.note ?? "";
    });
}

const deleteUser = (userid) => {
    Swal.fire({
        icon: "warning",
        title: "Confirm Delete",
        text: "Are you sure you want to delete this user?",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#3085d6",
        confirmButtonText: "Yes, delete!"
    }).then((result) => {
        if (result.isConfirmed) {
            postHTTPService(`/user/delete/user/${userid}`, "DELETE", "json").then((response) => {
                Swal.fire({
                    icon: "success",
                    title: "User deleted",
                    text: response.message,
                    timer: 1500,
                    showConfirmButton: false,
                }).then(() => {
                    window.location.reload();
                });
            }).catch((error) => {
                Swal.fire({
                    icon: "error",
                    title: "Error deleting user",
                    text: error.message
                });
            });
        }
    });
}


const resetForm = () => {
    window.location.reload();
    if (document.activeElement) {
        document.activeElement.blur();
    }
    document.getElementById("userFormData").reset();
    isUpdate = false;

    document.getElementById("submitButton").textContent = "Add User";
    document.getElementById("submitButton").style.display = "none";
    document.getElementById("submitButton").classList.remove("d-none");
    document.querySelector(".modal-title").textContent = "Add User";

    // Make all inputs editable again
    const inputs = document.querySelectorAll("#userFormData .form-control, #userFormData .form-select, #userFormData .form-check-input");
    inputs.forEach(input => {
        input.readOnly = false;
        input.disabled = false;
    });
}


const getUserRole = () => {
    console.log("get user role is calling");
    const userRole = document.getElementById("userRole");
    const userRoles=document.getElementById("userRoles");
    getHTTPService(`/user/getuser/roles`, "GET", "json").then((response) => {
        roleList = response;
        userRole.innerHTML = `<option value="">Select an option</option>`;
        //create roles dynamically
        response.forEach((role) => {
            const option = document.createElement("option");
            const option2 = document.createElement("option");
            option.value = role.id;
            option.textContent = role.name;
            option2.value = role.id;
            option2.textContent = role.name;

            //add option to the dropdown
            userRole.appendChild(option);
            userRoles.appendChild(option2);
        });

    }).catch((error) => {
        console.error("Error loading roles:", error);
    });
}

const renderModulePrivilegeGrid = () => {
    const tbody = document.getElementById("modulePrivilegeBody");
    if (!tbody) return;
    tbody.innerHTML = "";
    allModules.forEach((moduleName) => {
        const isReport = moduleName === 'Report';
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>
                <div class="form-check">
                    <input class="form-check-input module-chk" type="checkbox" value="${moduleName}" id="chk_${moduleName}" onchange="toggleModuleRow('${moduleName}')">
                    <label class="form-check-label fw-semibold text-dark" for="chk_${moduleName}">${moduleName}</label>
                </div>
            </td>
            <td>
                <div class="d-flex flex-row gap-4">
                    <div class="form-check">
                        <input class="form-check-input perm-chk" type="checkbox" value="View" id="view_${moduleName}" disabled>
                        <label class="form-check-label" for="view_${moduleName}">View</label>
                    </div>
                    ${isReport ? '' : `
                    <div class="form-check">
                        <input class="form-check-input perm-chk" type="checkbox" value="Insert" id="insert_${moduleName}" disabled>
                        <label class="form-check-label" for="insert_${moduleName}">Insert</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input perm-chk" type="checkbox" value="Update" id="update_${moduleName}" disabled>
                        <label class="form-check-label" for="update_${moduleName}">Update</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input perm-chk" type="checkbox" value="Delete" id="delete_${moduleName}" disabled>
                        <label class="form-check-label" for="delete_${moduleName}">Delete</label>
                    </div>
                    `}
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

const toggleModuleRow = (moduleName) => {
    const isChecked = document.getElementById(`chk_${moduleName}`).checked;
    const permissions = ['view', 'insert', 'update', 'delete'];
    permissions.forEach((perm) => {
        const permInput = document.getElementById(`${perm}_${moduleName}`);
        if (permInput) {
            permInput.disabled = !isChecked;
            if (!isChecked) {
                permInput.checked = false;
            }
        }
    });
}

const loadPrivilegeTable = () => {
    getHTTPService("/user/getuser/roles", "GET", "json").then((response) => {
        const tableBody = document.querySelector(".privilegeTable tbody");
        if (!tableBody) return;
        tableBody.innerHTML = "";
        
        response.forEach((role) => {
            let permissionsText = "";
            if (role.privileges && role.privileges.length > 0) {
                permissionsText = role.privileges.map(priv => {
                    let perms = [];
                    if (priv.canView) perms.push("V");
                    if (priv.canInsert) perms.push("I");
                    if (priv.canUpdate) perms.push("U");
                    if (priv.canDelete) perms.push("D");
                    return `${priv.module.name} (${perms.join(",")})`;
                }).join(", ");
            } else {
                permissionsText = "No privileges assigned";
            }
            
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>R${String(role.id).padStart(3, '0')}</td>
                <td>${role.name}</td>
                <td style="text-align: left; max-width: 400px; white-space: normal; word-wrap: break-word;">${permissionsText}</td>
                <td class="d-flex flex-row justify-content-center gap-2">
                    <button class="btn btn-teal px-3 py-2 text-dark ms-2" onclick="editPrivilege(${role.id})">Edit</button>
                    <button class="btn btn-red px-3 text-dark py-2 ms-2" onclick="deletePrivilege(${role.id})">Delete</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
        document.getElementById("searchPrivilege")?.dispatchEvent(new Event("input"));
    }).catch((error) => {
        console.error("Error loading privilege table:", error);
    });
}

const editPrivilege = (roleId) => {
    document.getElementById("userRoles").value = roleId;
    document.getElementById("userRoles").disabled = true;
    
    renderModulePrivilegeGrid();
    
    getHTTPService(`/privilege/getbyrole/${roleId}`, "GET", "json").then((privileges) => {
        privileges.forEach((priv) => {
            const moduleName = priv.module.name;
            const chkModule = document.getElementById(`chk_${moduleName}`);
            if (chkModule) {
                chkModule.checked = true;
                toggleModuleRow(moduleName);
                
                const viewEl = document.getElementById(`view_${moduleName}`);
                const insertEl = document.getElementById(`insert_${moduleName}`);
                const updateEl = document.getElementById(`update_${moduleName}`);
                const deleteEl = document.getElementById(`delete_${moduleName}`);
                
                if (viewEl) viewEl.checked = priv.canView;
                if (insertEl) insertEl.checked = priv.canInsert;
                if (updateEl) updateEl.checked = priv.canUpdate;
                if (deleteEl) deleteEl.checked = priv.canDelete;
            }
        });
        
        const modalEl = document.getElementById("addPrivilegeModal");
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    }).catch((error) => {
        console.error("Error loading privilege details:", error);
    });
}

const deletePrivilege = (roleId) => {
    Swal.fire({
        icon: "warning",
        title: "Confirm Delete",
        text: "Are you sure you want to revoke all privileges for this role?",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#3085d6",
        confirmButtonText: "Yes, revoke all!"
    }).then((result) => {
        if (result.isConfirmed) {
            postHTTPService(`/privilege/save/${roleId}`, "POST", "json", []).then((response) => {
                Swal.fire({
                    icon: "success",
                    title: "Privileges deleted",
                    text: response.message,
                    timer: 1500,
                    showConfirmButton: false,
                }).then(() => {
                    window.location.reload();
                });
            }).catch((error) => {
                Swal.fire({
                    icon: "error",
                    title: "Error",
                    text: error.message
                });
            });
        }
    });
}

const submitPrivilege = (evt) => {
    evt.preventDefault();
    const roleId = document.getElementById("userRoles").value;
    if (!roleId) {
        Swal.fire({
            icon: "warning",
            title: "Required Field",
            text: "Please select a Role first"
        });
        return;
    }
    
    const privilegeDtos = [];
    allModules.forEach((moduleName) => {
        const isChecked = document.getElementById(`chk_${moduleName}`);
        if (isChecked && isChecked.checked) {
            const viewInput = document.getElementById(`view_${moduleName}`);
            const insertInput = document.getElementById(`insert_${moduleName}`);
            const updateInput = document.getElementById(`update_${moduleName}`);
            const deleteInput = document.getElementById(`delete_${moduleName}`);
            privilegeDtos.push({
                module: moduleName,
                canView: viewInput ? viewInput.checked : false,
                canInsert: insertInput ? insertInput.checked : false,
                canUpdate: updateInput ? updateInput.checked : false,
                canDelete: deleteInput ? deleteInput.checked : false
            });
        }
    });
    
    Swal.fire({
        icon: "question",
        title: "Please Confirm",
        text: "Are you sure you want to save these privileges?",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, save!"
    }).then((result) => {
        if (result.isConfirmed) {
            postHTTPService(`/privilege/save/${roleId}`, "POST", "json", privilegeDtos).then((response) => {
                Swal.fire({
                    icon: "success",
                    title: "Privileges saved successfully",
                    text: response.message,
                    timer: 1500,
                    showConfirmButton: false,
                }).then(() => {
                    window.location.reload();
                });
            }).catch((error) => {
                Swal.fire({
                    icon: "error",
                    title: "Error saving privileges",
                    text: error.message
                });
            });
        }
    });
}

const resetPrivilegeForm = () => {
    document.getElementById("userRoles").disabled = false;
    document.getElementById("userRoles").value = "";
    renderModulePrivilegeGrid();
}
