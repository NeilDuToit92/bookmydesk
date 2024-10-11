document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
});

function loadUsers() {
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
                            <th>Last Seen</th>
                            <th>Upcoming Bookings</th>
                            <th>Total Bookings</th>
                        </tr>
                    `;
            table.appendChild(thead);

            const tbody = document.createElement('tbody');
            tbody.id = 'user-table-body';
            table.appendChild(tbody);

            contentDiv.appendChild(table);

            users.forEach((user, index) => {
                const userRow = document.createElement('tr');
                //TODO: Row click to view/edit user
                //userRow.onclick = () => editUser(user.id);

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
                            <td>${formatDate(user.lastSeen)}</td>
                            <td>${user.upcomingBookingCount} <button type="button" class="btn btn-link btn-small" onclick="toggleUpcomingBookings(${index}, this)">Show</button></td>
                            <td>${user.bookingCount} <button type="button" class="btn btn-link btn-small" onclick="toggleAllBookings(${index}, this)">Show</button></td>
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

function editUser(userId) {
    console.log("Edit user " + userId);
}