document.addEventListener('DOMContentLoaded', function () {

    document.getElementById('bookingDate').value = new Date().toISOString().split('T')[0];
    fetchAndDisplayDesks();

    document.getElementById('bookingDate').addEventListener('change', function () {
        fetchAndDisplayDesks();
        displayWeekdays();
    });

    addSingleEventListeners();
    displayWeekdays();

});

function fetchAndDisplayDesks() {
    const bookingDate = document.getElementById('bookingDate').value;

    const imageContainer = document.querySelector('.image-container');

    // Remove existing desk-div elements
    const existingDesks = document.querySelectorAll('.desk-div');
    existingDesks.forEach(desk => desk.remove());

    fetch('/api/desk?date=' + bookingDate)
        .then(response => response.json())
        .then(deskData => {
            deskData.forEach(desk => {
                const deskDiv = document.createElement('div');
                deskDiv.classList.add('desk-div');
                deskDiv.style.position = 'absolute';
                deskDiv.style.left = desk.x + 'px';
                deskDiv.style.top = desk.y + 'px';

                const deskCircle = document.createElement('div');
                deskCircle.classList.add('desk');
                deskCircle.textContent = desk.displayId;
                deskCircle.setAttribute('data-id', desk.databaseId);
                deskCircle.setAttribute('data-status', desk.status);
                switch (desk.status) {
                    case "AVAILABLE":
                        deskCircle.classList.add("desk-available");
                        break;
                    case "BOOKED":
                        deskCircle.classList.add("desk-booked");
                        break;
                    case "RESERVED":
                        deskCircle.classList.add("desk-reserved");
                        break;
                }

                const popupInfo = document.createElement('div');
                popupInfo.classList.add('popup-info');
                // const statusParagraph = document.createElement('p');
                // statusParagraph.textContent = desk.status;
                // popupInfo.appendChild(statusParagraph);
                const bookedByParagraph = document.createElement('p');
                if (desk.bookedBy) {
                    bookedByParagraph.textContent = desk.displayId + ' - ' + desk.bookedBy;
                } else {
                    bookedByParagraph.textContent = desk.displayId + ' - ' + "Click to book";
                }

                popupInfo.appendChild(bookedByParagraph);

                deskDiv.appendChild(deskCircle);
                deskDiv.appendChild(popupInfo);
                imageContainer.appendChild(deskDiv);
            });

            addRecurringEventListeners();
        })
        .catch(error => console.error('Error fetching desk data:', error));
}

function showPopup(element, overlay, bookPopup, cancelPopup) {
    let deskId = element.textContent;
    let deskDbId = element.getAttribute('data-id');
    const deskIdSpan = document.getElementById('deskId');
    const deskDbIdInput = document.getElementById('deskDbId');
    document.getElementById('dateToBook').textContent = document.getElementById('bookingDate').value;

    overlay.style.display = 'flex';
    deskIdSpan.textContent = deskId;
    deskDbIdInput.value = deskDbId;
    let deskStatus = element.getAttribute('data-status');
    console.log('deskStatus "' + deskStatus + '"');
    switch (deskStatus) {
        case "AVAILABLE":
            bookPopup.style.display = 'block';
            break;
        case "BOOKED":
            cancelPopup.style.display = 'block';
            break;
        case "RESERVED":
            //Cancel popup??
            break;
    }
    return deskStatus;
}


function addRecurringEventListeners() {
    const desks = document.querySelectorAll('.desk');
    const overlay = document.getElementById('overlay');
    const bookPopup = document.getElementById('bookPopup');
    const cancelPopup = document.getElementById('cancelPopup');

    desks.forEach(desk => {
        desk.addEventListener('click', function () {
            showPopup(this, overlay, bookPopup, cancelPopup);
        });
        desk.closest('.desk-div').querySelector('.popup-info').addEventListener('click', function () {
            let deskDiv = this.parentElement;
            let desk = deskDiv.querySelector('.desk');
            showPopup(desk, overlay, bookPopup, cancelPopup);
        });
    });

    document.querySelectorAll('.desk-div').forEach(function (desk) {
        const popup = desk.querySelector('.popup-info');
        desk.addEventListener('mouseenter', function (event) {
            let viewportRight = window.scrollX + window.innerWidth;
            let viewportBottom = window.scrollY + window.innerHeight;

            popup.style.display = 'inline-block';
            let popupRect = popup.getBoundingClientRect();
            let popupLeft = popupRect.left + window.scrollX;
            let popupWidth = popupRect.width;
            let popupRight = popupLeft + popupWidth;

            let popupTop = popupRect.top + window.scrollY;
            let popupHeight = popupRect.height;
            let popupBottom = popupTop + popupHeight;

            //TODO: Does not work when zoomed in
            if (popupRight > viewportRight) {
                //If falling off the right of the viewport, move it to just inside the viewport
                let overhang = popupRight - viewportRight;
                popup.style.left = (-1 * overhang) + 'px';
            }

            if (popupBottom > viewportBottom) {
                //If falling off the bottom of the viewport, move it to just inside the viewport
                let overhang = popupBottom - viewportBottom;
                popup.style.top = (-1 * overhang) + 'px';
            }

        });
        desk.addEventListener('mouseleave', function (event) {
            popup.style.display = 'none';
            popup.style.left = '';
            popup.style.top = '';
        });
    });
}

function addSingleEventListeners() {
    const desks = document.querySelectorAll('.desk');
    const overlay = document.getElementById('overlay');
    const bookPopup = document.getElementById('bookPopup');
    const cancelPopup = document.getElementById('cancelPopup');
    const closePopupBooking = document.getElementById('closePopupBooking');
    const closePopupCancel = document.getElementById('closePopupCancel');
    const confirmBooking = document.getElementById('confirmBooking');
    const cancelBooking = document.getElementById('cancelBooking');

    closePopupBooking.addEventListener('click', function () {
        overlay.style.display = 'none';
        bookPopup.style.display = 'none';
    });

    closePopupCancel.addEventListener('click', function () {
        overlay.style.display = 'none';
        cancelPopup.style.display = 'none';
    });

    confirmBooking.addEventListener('click', function () {
        const date = document.getElementById('bookingDate').value;
        const deskId = document.getElementById('deskDbId').value;

        const url = '/api/booking/' + deskId + '?date=' + encodeURIComponent(date);

        // Send a POST request using fetch API
        fetch(url, {
            method: 'POST'
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorBody => {
                        throw new Error(errorBody.message || 'Failed to book seat');
                    });
                }
                overlay.style.display = 'none';
                bookPopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast("Booking success", "success")
            })
            .catch(error => {
                overlay.style.display = 'none';
                bookPopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast(error, "error")
            });
    });

    cancelBooking.addEventListener('click', function () {
        const date = document.getElementById('bookingDate').value;
        const deskId = document.getElementById('deskDbId').value;

        const url = '/api/booking/' + deskId + '?date=' + encodeURIComponent(date);

        // Send a POST request using fetch API
        fetch(url, {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorBody => {
                        throw new Error(errorBody.message || 'Failed to book seat');
                    });
                }
                overlay.style.display = 'none';
                cancelPopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast("Cancellation success", "success")
            })
            .catch(error => {
                overlay.style.display = 'none';
                bookPopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast(error, "error")
            });
    });

    // Optional: You can add this script if you want to close the popup when clicking outside
    document.addEventListener('click', function (event) {
        if (!event.target.closest('.desk-div')) {
            document.querySelectorAll('.popup-info').forEach(function (popup) {
                popup.style.display = 'none';
            });
        }
    });

    // Keyboard event listeners
    document.addEventListener("keydown", function (event) {
        switch (event.key) {
            case "+":
            case "=":
                changeDate(1);
                break;
            case "-":
            case "_":
                changeDate(-1);
                break;
        }
    });
}

function limitToWeekdays() {
    var input = document.getElementById('bookingDate');
    var selectedDate = new Date(input.value);

    // Get the day of the week (0 for Sunday, 1 for Monday, ..., 6 for Saturday)
    var dayOfWeek = selectedDate.getDay();

    // If selected date is Saturday or Sunday, adjust it to next Monday
    if (dayOfWeek === 0) { // Sunday
        selectedDate.setDate(selectedDate.getDate() + 1);
    } else if (dayOfWeek === 6) { // Saturday
        selectedDate.setDate(selectedDate.getDate() + 2);
    }

    // Set the minimum and maximum date to the nearest weekdays
    input.min = formatDate(selectedDate);
    input.max = input.min; // We want only one selectable date

    // Set the input value to the nearest weekday
    input.value = formatDate(selectedDate);
}

function formatDate(date) {
    let year = date.getFullYear();
    let month = (date.getMonth() + 1).toString().padStart(2, '0');
    let day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function changeDate(days) {
    const inputDate = document.getElementById("bookingDate");
    let currentDate = new Date(inputDate.value);

    // Skip weekends
    const weekendDays = [0, 6]; // Sunday and Saturday
    let skipDays = 0;
    while (skipDays < Math.abs(days)) {
        currentDate.setDate(currentDate.getDate() + Math.sign(days));
        if (!weekendDays.includes(currentDate.getDay())) {
            skipDays++;
        }
    }

    inputDate.value = currentDate.toISOString().slice(0, 10);
    inputDate.dispatchEvent(new Event('change'));

    displayWeekdays();
}

function getMonday(date) {
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Sunday
    return new Date(date.setDate(diff));
}

function displayWeekdays() {
    const weekdaysDiv = document.getElementById("weekdays");
    weekdaysDiv.innerHTML = ""; // Clear previous content

    const inputDate = new Date(document.getElementById("bookingDate").value);
    const startingMonday = getMonday(new Date());

    const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const weekendDays = [0, 6]; // Sunday and Saturday

    // Iterate over 14 days (2 weeks)
    for (let i = 0; i < 14; i++) {
        const currentDate = new Date(startingMonday);
        currentDate.setDate(currentDate.getDate() + i);

        const dayOfWeek = daysOfWeek[currentDate.getDay()];
        const dayOfMonth = currentDate.getDate();

        const dayBlock = document.createElement("div");

        const dayName = document.createElement("div");
        dayName.classList.add("day-name");
        dayName.textContent = dayOfWeek;

        const dayDate = document.createElement("div");
        dayDate.classList.add("day-date");
        dayDate.textContent = dayOfMonth;

        dayBlock.appendChild(dayName);
        dayBlock.appendChild(dayDate);

        dayBlock.setAttribute("data-date", currentDate.toISOString().slice(0, 10));

        if (currentDate.toDateString() === inputDate.toDateString()) {
            dayBlock.classList.add("current-day");
        }

        if (weekendDays.includes(currentDate.getDay())) {
            dayBlock.classList.add("weekend-block");
        } else {
            dayBlock.classList.add("day-block");
            dayBlock.addEventListener("click", function () {
                document.getElementById("bookingDate").value = this.getAttribute("data-date");
                document.getElementById("bookingDate").dispatchEvent(new Event('change')); // Trigger change event
                displayWeekdays(); // Update the weekdays display
            });
        }

        weekdaysDiv.appendChild(dayBlock);
    }
}

let toastTimeoutId;

function showToast(message, type) {
    const toast = document.getElementById('toast');
    toast.className = 'toast'; // Reset to base class

    // Set the message and type class
    if (type === 'success') {
        toast.classList.add('toast-success');
    } else if (type === 'error') {
        toast.classList.add('toast-error');
    }

    toast.textContent = message;

    // Show the toast with animation
    toast.classList.add('show');

    // Clear any existing timeout to avoid multiple hides
    if (toastTimeoutId) {
        clearTimeout(toastTimeoutId);
    }

    // Hide the toast after 4 seconds
    toastTimeoutId = setTimeout(function() {
        toast.classList.add('hide');

        // Remove the 'hide' class after the animation ends to reset the toast
        toast.addEventListener('transitionend', function() {
            if (toast.classList.contains('hide')) {
                toast.className = 'toast'; // Reset to base class
            }
        }, { once: true });
    }, 4000);
}