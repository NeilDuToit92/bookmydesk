document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
});

function setActiveTab(activeTabId) {
    const tabs = document.querySelectorAll('.tab-buttons button');
    tabs.forEach(tab => {
        if (tab.id === activeTabId) {
            tab.classList.add('active');
        } else {
            tab.classList.remove('active');
        }
    });
}

function loadUsers() {
    setActiveTab('tabUsers');
    const contentDiv = document.getElementById('contentDiv');
    contentDiv.innerHTML = '';

    fetch('/api/user/all')
        .then(response => response.json())
        .then(users => {
            const table = document.createElement('table');
            table.style.width = '100%';

            // Create the thead element
            const thead = document.createElement('thead');
            thead.innerHTML = `
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Enabled</th>
                            <th>Admin</th>
                            <th>Upcoming Bookings</th>
                            <th>Total Bookings</th>
                            <th></th>
                        </tr>
                    `;
            table.appendChild(thead);

            const tbody = document.createElement('tbody');
            tbody.id = 'user-table-body';
            table.appendChild(tbody);

            contentDiv.appendChild(table);

            users.forEach((user, index) => {
                const userRow = document.createElement('tr');

                userRow.innerHTML = `
                            <td>${user.displayName}</td>
                            <td>${user.email}</td>
                            <td>
                                <label>
                                     <input type="checkbox" ${user.enabled ? 'checked' : ''} onchange="handleEnabledToggle(${user.id}, this.checked)"/>
                                </label>
                            </td>
                            <td>
                                <label>
                                     <input type="checkbox" ${user.admin ? 'checked' : ''} onchange="handleAdminToggle(${user.id}, this.checked)"/>
                                </label>
                            </td>
                            <td>${user.upcomingBookingCount}</td>
                            <td>${user.bookingCount}</td>
                            <td>
                                <button type="button" class="btn btn-link" onclick="toggleAllBookings(${index})">
                                    All Bookings
                                </button>
                                <button type="button" class="btn btn-link" onclick="toggleUpcomingBookings(${index})">
                                    Upcoming Bookings
                                </button>
                            </td>
                        `;

                tbody.appendChild(userRow);

                const upcomingBookingRow = document.createElement('tr');
                upcomingBookingRow.id = `upcoming-bookings-${index}`;
                upcomingBookingRow.classList.add('hidden-row-hide');

                let upcomingBookingsHtml = '<td colspan="7"><h2>Upcoming Bookings</h2><table style="width: 25%;"><thead><tr><th>Date</th><th>Desk ID</th></tr></thead><tbody>';
                user.upcomingBookings.forEach(booking => {
                    upcomingBookingsHtml += `<tr>
                                <td>${booking.permanent ? 'Permanent' : booking.date}</td>
                                <td>${booking.displayId}</td>
                            </tr>`;
                });
                upcomingBookingsHtml += '</tbody></table></td>';

                upcomingBookingRow.innerHTML = upcomingBookingsHtml;
                tbody.appendChild(upcomingBookingRow);

                const allBookingRow = document.createElement('tr');
                allBookingRow.id = `all-bookings-${index}`;
                allBookingRow.classList.add('hidden-row-hide');

                let allBookingsHtml = '<td colspan="7"><h2>All Bookings</h2><table style="width: 25%;"><thead><tr><th>Date</th><th>Desk ID</th></tr></thead><tbody>';
                user.allBookings.forEach(booking => {
                    allBookingsHtml += `<tr>
                                <td>${booking.permanent ? 'Permanent' : booking.date}</td>
                                <td>${booking.displayId}</td>
                            </tr>`;
                });
                allBookingsHtml += '</tbody></table></td>';

                allBookingRow.innerHTML = allBookingsHtml;
                tbody.appendChild(allBookingRow);
            });
        })
        .catch(error => console.error('Error fetching user data:', error));
}

function loadDeskReservations() {
    setActiveTab('tabReservations');
    const contentDiv = document.getElementById('contentDiv');
    contentDiv.innerHTML = '';

    fetch('/api/booking/permanent/all')
        .then(response => response.json())
        .then(bookings => {
            const table = document.createElement('table');
            table.style.width = '100%';

            const thead = document.createElement('thead');
            thead.innerHTML = `
                        <tr>
                            <th>Reserved for</th>
                            <th>Desk ID</th>
                            <th>Date reserved</th>
                            <th></th>
                        </tr>
                    `;
            table.appendChild(thead);

            const tbody = document.createElement('tbody');
            table.appendChild(tbody);

            bookings.forEach(booking => {
                const bookingRow = document.createElement('tr');
                bookingRow.innerHTML = `
                        <td>${booking.bookedBy}</td>
                        <td>${booking.displayId}</td>
                        <td>${booking.date}</td>
                    `;
                tbody.appendChild(bookingRow);
            });

            // Append the table to the contentDiv
            contentDiv.appendChild(table);
        })
        .catch(error => console.error('Error fetching permanent bookings data:', error));
}

function loadDesks() {
    setActiveTab('desks');
    const contentDiv = document.getElementById('contentDiv');
    contentDiv.innerHTML = '';

    fetch('/api/desk/all')
        .then(response => response.json())
        .then(desks => {

            const table = document.createElement('table');
            table.style.width = '100%';

            // Create the thead element
            const thead = document.createElement('thead');
            thead.innerHTML = `
                        <tr>
                            <th>Desk ID</th>
                            <th>X coordinate</th>
                            <th>Y coordinate</th>
                        </tr>
                    `;
            table.appendChild(thead);

            const tbody = document.createElement('tbody');
            table.appendChild(tbody);

            desks.forEach(desk => {
                const deskRow = document.createElement('tr');
                deskRow.innerHTML = `
                        <td>${desk.displayId}</td>
                        <td>${desk.x}</td>
                        <td>${desk.y}</td>
<!--                        <td>${JSON.stringify(desk)}</td>-->
                    `;
                tbody.appendChild(deskRow);
            });

            contentDiv.appendChild(table);
        })
        .catch(error => console.error('Error fetching desk data:', error));
}

function toggleUpcomingBookings(rowId) {
    let bookingsRow = document.getElementById("upcoming-bookings-" + rowId);
    if (bookingsRow.classList.contains('hidden-row-hide')) {
        bookingsRow.classList.remove('hidden-row-hide');
        bookingsRow.classList.add('hidden-row-show');
    } else {
        bookingsRow.classList.remove('hidden-row-show');
        bookingsRow.classList.add('hidden-row-hide');
    }
}

function toggleAllBookings(rowId) {
    let bookingsRow = document.getElementById("all-bookings-" + rowId);
    if (bookingsRow.classList.contains('hidden-row-hide')) {
        bookingsRow.classList.remove('hidden-row-hide');
        bookingsRow.classList.add('hidden-row-show');
    } else {
        bookingsRow.classList.remove('hidden-row-show');
        bookingsRow.classList.add('hidden-row-hide');
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
    const url = `/api/user/${userId}`;

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