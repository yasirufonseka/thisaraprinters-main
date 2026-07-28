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
            // swal.fire({
            //     icon: "success",
            //     title: "Success",
            //     text: "User Saved successfully",
            //     timer: 1000,
            //     showConfirmButton: false,
            // }).then(() => {
            //     window.location.reload();
            // })

            return data;
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching data:", textStatus, errorThrown);
            console.error("Response text:", jqXHR.responseText);
            console.error("Status code:", jqXHR.status);
            // swal.fire({
            //     icon: "error",
            //     title: "Error",
            //     text: "Error fetching data" + textStatus,
            // })
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