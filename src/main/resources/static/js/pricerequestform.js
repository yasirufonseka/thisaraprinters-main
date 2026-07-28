// Auto-calculate total amount when unit price or delivery charge changes
const calculateTotal = () => {
    const unitPrice = parseFloat(document.getElementById("unitprice").value) || 0;
    const deliverCharge = parseFloat(document.getElementById("deliverCharge").value) || 0;
    const total = unitPrice + deliverCharge;
    document.getElementById("totalAmount").value = total > 0 ? total.toFixed(2) : "";
};

document.getElementById("unitprice").addEventListener("input", calculateTotal);
document.getElementById("deliverCharge").addEventListener("input", calculateTotal);

// Validation on blur
document.getElementById("unitprice").addEventListener("blur", () => {
    const unitPrice = document.getElementById("unitprice").value;
    const errorMassage = document.getElementById("errorMassage");
    if (unitPrice == null || unitPrice == undefined || unitPrice == "") {
        errorMassage.innerHTML = "Please enter a valid unit price";
    } else {
        errorMassage.innerHTML = "";
    }
});

document.getElementById("deliverCharge").addEventListener("blur", () => {
    const deliverCharge = document.getElementById("deliverCharge").value;
    if (deliverCharge == null || deliverCharge == undefined || deliverCharge == "") {
        Swal.fire({
            icon: "error",
            title: "Error",
            text: "Please enter a valid delivery charge",
            timer: 1500,
            showConfirmButton: false,
        });
    }
});

document.getElementById("deliverDate").addEventListener("blur", () => {
    const deliverDate = document.getElementById("deliverDate").value;
    if (deliverDate == null || deliverDate == undefined || deliverDate == "") {
        Swal.fire({
            icon: "error",
            title: "Error",
            text: "Please enter a valid date",
            timer: 1500,
            showConfirmButton: false,
        });
    }
});

const submitForm = () => {
    const unitPrice = document.getElementById("unitprice").value;
    const deliverCharge = document.getElementById("deliverCharge").value;
    const deliverDate = document.getElementById("deliverDate").value;
    const totalAmount = document.getElementById("totalAmount").value;
    const priceRequestId = document.getElementById("priceRequestId").value;
    const supplierId = document.getElementById("supplierId").value;

    // Validate all fields
    if (!unitPrice || !deliverCharge || !deliverDate || !totalAmount) {
        Swal.fire({
            icon: "error",
            title: "Error",
            text: "Please fill all the fields",
            timer: 1500,
            showConfirmButton: false,
        });
        return;
    }

    // Validate priceRequestId and supplierId
    if (!priceRequestId || !supplierId) {
        Swal.fire({
            icon: "error",
            title: "Invalid Link",
            text: "This form link is invalid. Please use the link from the email.",
            showConfirmButton: true,
        });
        return;
    }

    const formData = {
        unitPrice: parseFloat(unitPrice),
        deliveryCharge: parseFloat(deliverCharge),
        deliveryDate: deliverDate,
        totalAmount: parseFloat(totalAmount),
        priceRequestId: parseInt(priceRequestId),
        supplierId: parseInt(supplierId)
    };

    Swal.fire({
        icon: "question",
        title: "Submit Quotation",
        text: "Are you sure you want to submit this quotation?",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Submit"
    }).then((result) => {
        if (result.isConfirmed) {
            postHTTPService(`/supplier/pricerequest/reply`, "POST", "json", formData).then((response) => {
                Swal.fire({
                    icon: "success",
                    title: "Quotation Submitted",
                    text: response.message,
                    timer: 2000,
                    showConfirmButton: false,
                }).then(() => {
                    // Reset the form after successful submission
                    document.getElementById("detailsForm").reset();
                    document.getElementById("totalAmount").value = "";
                });
            }).catch((error) => {
                Swal.fire({
                    icon: "error",
                    title: "Error submitting quotation",
                    text: error.message || "Something went wrong",
                    timer: 1500,
                    showConfirmButton: false,
                });
                console.log(error);
            });
        }
    });
};