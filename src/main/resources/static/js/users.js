document.addEventListener('DOMContentLoaded', () => {
    dataTable();
    registerEvents();
});

let usersTable;

function dataTable() {
    let assessmentsTable = $('#usersTable');
    let url = '/api/user/all';
    usersTable = assessmentsTable.DataTable({
        ajax:
            {
                url: url,
                dataSrc: ''
            },
        columns: [
            {"data": "displayName"},
            {"data": "email"},
            {"data": "enabled"},
            {"data": "admin"},
            {"data": "lastSeen"},
            {"data": "upcomingBookingCount"},
            {"data": "bookingCount"}
        ],
        columnDefs: [
            {
                targets: 2,
                orderable: false,
                render: function (data, type, row) {
                    if (data) {
                        return "<input type='checkbox' checked onChange='handleEnabledToggle(\"" + row.id + "\", false)'/>";
                    } else {
                        return "<input type='checkbox' onChange='handleEnabledToggle(\"" + row.id + "\", true)'/>";
                    }
                }
            },
            {
                targets: 3,
                orderable: false,
                orderDataType: 'dom-checkbox',
                render: function (data, type, row) {
                    if (data) {
                        return "<input type='checkbox' checked onChange='handleAdminToggle(\"" + row.id + "\", false)'/>";
                    } else {
                        return "<input type='checkbox' onChange='handleAdminToggle(\"" + row.id + "\", true)'/>";
                    }
                }
            },
            {
                targets: 4,
                render: function (data, type, row) {
                    return formatDate(data);
                }
            },
        ]
    });
}

function registerEvents() {
    usersTable.on('click', 'tbody tr', function () {
        let data = usersTable.row(this).data();
        console.log(data.id);
    });
}

function loadDeskReservations() {
    window.location.href = '/reservations';
}

function loadDesks() {
    window.location.href = '/deskedit';
}

function toggleUpcomingBookings(rowId, btn) {
    toggleBookings("upcoming-bookings-" + rowId, btn);
}

function toggleAllBookings(rowId, btn) {
    toggleBookings("all-bookings-" + rowId, btn);
}

function toggleBookings(elemenbookingsRowId, btn) {
    let bookingsRow = document.getElementById(elemenbookingsRowId);
    if (bookingsRow.classList.contains('hidden-row-hide')) {
        bookingsRow.classList.remove('hidden-row-hide');
        bookingsRow.classList.add('hidden-row-show');
        btn.innerText = 'Hide';
    } else {
        bookingsRow.classList.remove('hidden-row-show');
        bookingsRow.classList.add('hidden-row-hide');
        btn.innerText = 'Show';
    }
}

function handleAdminToggle(userId, isAdmin) {
    const data = {admin: isAdmin};
    handleUserUpdate(userId, JSON.stringify(data));
}

function handleEnabledToggle(userId, enabled) {
    const data = {enabled: enabled};
    handleUserUpdate(userId, JSON.stringify(data));
}

function handleUserUpdate(userId, payload) {
    const url = '/api/user/' + userId;

    fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: payload,
    })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch(error => {
            //TODO: reset UI if error occurs
            console.error('Error:', error);
        });
}

function editUser(userId) {
    console.log("Edit user " + userId);
}