function toggleBookings(rowId) {
    let bookingsRow = document.getElementById("bookings-" + rowId);
    if (bookingsRow.classList.contains('hidden-row-hide')) {
        bookingsRow.classList.remove('hidden-row-hide');
        bookingsRow.classList.add('hidden-row-show');
    } else {
        bookingsRow.classList.remove('hidden-row-show');
        bookingsRow.classList.add('hidden-row-hide');
    }
}

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
                            <th>Is Admin</th>
                            <th>Bookings</th>
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
                userRow.classList.add('clickable-row');

                userRow.innerHTML = `
                            <td>${user.displayName}</td>
                            <td>${user.email}</td>
                            <td>
                                <label>
                                    <input type="checkbox" ${user.isAdmin ? 'checked' : ''}/>
                                </label>
                            </td>
                            <td>${user.bookingCount}</td>
                            <td>
                                <button type="button" class="btn btn-link" onclick="toggleBookings(${index})">
                                    Toggle Bookings
                                </button>
                            </td>
                        `;

                tbody.appendChild(userRow);

                const bookingRow = document.createElement('tr');
                bookingRow.id = `bookings-${index}`;
                bookingRow.classList.add('hidden-row-hide');

                let bookingsHtml = '<td colspan="5"><table style="width: 25%;"><thead><tr><th>Date</th><th>Desk ID</th></tr></thead><tbody>';
                user.bookings.forEach(booking => {
                    bookingsHtml += `<tr>
                                <td>${booking.permanent ? 'Permanent' : booking.date}</td>
                                <td>${booking.displayId}</td>
                            </tr>`;
                });
                bookingsHtml += '</tbody></table></td>';

                bookingRow.innerHTML = bookingsHtml;
                tbody.appendChild(bookingRow);
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
<!--                            <th></th>-->
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

function toggleBookings(index) {
    const bookingRow = document.getElementById(`bookings-${index}`);
    bookingRow.classList.toggle('hidden-row-hide');
}