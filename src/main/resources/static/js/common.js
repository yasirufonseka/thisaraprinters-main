const getHTTPService = (url, type, dataType) => {
    return $.ajax({
        url: url,
        type: type,
        dataType: dataType,
        async: false,
    })
        .done(function (data, jqXHR) {
            console.log("Data fetched successfully:", data);

            return data;
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching data:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            swal.fire({
                icon: "error",
                title: "Error",
                text: "Error fetching data" + textStatus,
            })
            return textStatus;
        })
        .always(function () {
            console.log("Request complete");
        });
}


const postHTTPService = (url, type, dataType, data) =>{
    return $.ajax({
        url: url,
        type: type,
        dataType: dataType,
        contentType: "application/json",
        async: false,
        data: JSON.stringify(data),
    })
        .done(function (data, jqXHR) {
            console.log("Data send successfully:", data);
            return data;
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching data:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            return textStatus;
        })
        .always(function () {
            console.log("Request complete");
        });
}

// For multipart/form-data requests (file uploads + form fields via FormData)
const postFormHTTPService = (url, formData) => {
    return $.ajax({
        url: url,
        type: 'POST',
        data: formData,
        contentType: false,
        processData: false,
    })
        .done(function (data) {
            console.log("Form data sent successfully:", data);
            return data;
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error("Error sending form data:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            return textStatus;
        })
        .always(function () {
            console.log("Request complete");
        });
}

// Global Tab switcher utility
const openTab = (evt, tabId) => {
    document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    
    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.style.display = 'block';
    }
    if (evt && evt.currentTarget) {
        evt.currentTarget.classList.add('active');
    }
};