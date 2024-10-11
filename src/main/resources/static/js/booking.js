document.addEventListener('DOMContentLoaded', function () {

    document.getElementById('bookingDate').value = new Date().toISOString().split('T')[0];
    fetchAndDisplayDesks();

    document.getElementById('bookingDate').addEventListener('change', function () {
        if (!blockHistoricAndFutureDates()) {
            fetchAndDisplayDesks();
            fetchBookedDates();
        }
    });

    addSingleEventListeners();
    fetchBookedDates();
});

const maxDays = 29;
let currentUserHasBooking = false;
const headerHeight = 89; //Header height

function blockHistoricAndFutureDates() {
    const input = document.getElementById('bookingDate');
    const bookingDate = new Date(input.value);

    let today = new Date();
    today.setHours(0, 0, 0, 0);
    bookingDate.setHours(0, 0, 0, 0);

    let maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + maxDays - 1);
    maxDate.setHours(0, 0, 0, 0);

    if (bookingDate < today) {
        input.value = formatDate(today);
        document.getElementById("bookingDate").dispatchEvent(new Event('change'));
        return true;
    } else if (bookingDate > maxDate) {
        input.value = formatDate(maxDate);
        document.getElementById("bookingDate").dispatchEvent(new Event('change'));
        return true;
    }
    return false;
}

let currentFetchController = null;

function fetchAndDisplayDesks() {
    const bookingDate = document.getElementById('bookingDate').value;

    const desksContainer = document.getElementById('desksContainer');

    // Remove existing desk-div elements
    const existingDesks = document.querySelectorAll('.desk-div');
    existingDesks.forEach(desk => desk.remove());

    // Abort the previous fetch if it exists
    if (currentFetchController) {
        currentFetchController.abort();
    }

    // Create a new AbortController
    currentFetchController = new AbortController();
    const {signal} = currentFetchController;

    fetch('/api/desk?date=' + bookingDate, {signal})
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(deskData => {
            currentUserHasBooking = false;
            deskData.forEach(desk => {
                if (desk.bookedByCurrentUser || desk.bookedByCurrentUser === "true") {
                    currentUserHasBooking = true;
                }
            });

            deskData.forEach(desk => {
                const deskDiv = document.createElement('div');
                deskDiv.classList.add('desk-div');
                deskDiv.style.position = 'absolute';
                deskDiv.style.left = desk.x + 'px';
                deskDiv.style.top = (desk.y + headerHeight) + 'px';

                const deskCircle = document.createElement('div');
                deskCircle.classList.add('desk');
                deskCircle.textContent = desk.displayId;
                deskCircle.setAttribute('data-id', desk.databaseId);
                deskCircle.setAttribute('data-status', desk.status);
                deskCircle.setAttribute('data-current-user', desk.bookedByCurrentUser);
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
                const bookedByParagraph = document.createElement('p');

                let clickable = false;
                let hasPopup = false;

                if (desk.bookedByCurrentUser) {
                    if (desk.status === 'RESERVED') {
                        deskCircle.classList.add("desk-currentuser-reserved");
                    } else {
                        deskCircle.classList.add("desk-currentuser");
                        clickable = true;
                    }
                }

                if (desk.bookedBy) {
                    if (desk.status === 'RESERVED') {
                        bookedByParagraph.textContent = desk.displayId + ' - ' + desk.bookedBy + ' (Reserved)';
                    } else {
                        bookedByParagraph.textContent = desk.displayId + ' - ' + desk.bookedBy;
                    }
                    hasPopup = true;
                } else if (!currentUserHasBooking) {
                    bookedByParagraph.textContent = desk.displayId + ' - ' + "Click to book";
                    clickable = true;
                    hasPopup = true;
                }

                if (clickable) {
                    deskDiv.classList.add('pointer-hover');
                    deskCircle.classList.add('pointer-hover');
                } else {
                    deskDiv.classList.add('pointer-not-allowed');
                    deskCircle.classList.add('pointer-not-allowed');
                }

                if (hasPopup) {
                    popupInfo.appendChild(bookedByParagraph);
                    deskDiv.appendChild(popupInfo);
                }

                deskDiv.appendChild(deskCircle);
                desksContainer.appendChild(deskDiv);
            });

            addRecurringEventListeners();
            scaleLayout();
        })
        .catch(error => {
            if (error.name === 'AbortError') {
                console.log('Fetch aborted');
            } else {
                console.error('Error fetching desk data:', error);
            }
        });
}

function fetchBookedDates() {
    fetch('/api/booking/dates?days=' + maxDays)
        .then(response => response.json())
        .then(bookedDays => {
            displayWeekdays(bookedDays);
        })
        .catch(error => console.error('Error fetching user booked dates:', error));
}

function showPopup(element, overlay, bookPopup, cancelPopup) {
    let deskStatus = element.getAttribute('data-status');
    if (deskStatus === "AVAILABLE" || deskStatus === "BOOKED") {
        if (deskStatus === "AVAILABLE" && currentUserHasBooking) {
            return;
        }
        if (deskStatus === "BOOKED") {
            let bookedByCurrentUser = element.getAttribute('data-current-user');
            if (!bookedByCurrentUser || bookedByCurrentUser === "false" || bookedByCurrentUser.trim() === "") {
                return;
            }
        }

        let deskId = element.textContent;
        let deskDbId = element.getAttribute('data-id');
        const deskIdSpan = document.getElementById('deskId');
        const deskId2Span = document.getElementById('deskId2');
        const deskDbIdInput = document.getElementById('deskDbId');
        const bookingDate = document.getElementById('bookingDate').value;
        document.getElementById('dateToBook').textContent = bookingDate;
        document.getElementById('dateToBook2').textContent = bookingDate;

        overlay.style.display = 'flex';
        deskIdSpan.textContent = deskId;
        deskId2Span.textContent = deskId;
        deskDbIdInput.value = deskDbId;

        switch (deskStatus) {
            case "AVAILABLE":
                bookPopup.style.display = 'block';
                break;
            case "BOOKED":
                cancelPopup.style.display = 'block';
                break;
            case "RESERVED":
                //Do nothing
                break;
        }
        return deskStatus;
    }
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
        let deskSelector = desk.closest('.desk-div').querySelector('.popup-info');
        if (deskSelector) {
            deskSelector.addEventListener('click', function () {
                let deskDiv = this.parentElement;
                let desk = deskDiv.querySelector('.desk');
                showPopup(desk, overlay, bookPopup, cancelPopup);
            });
        }
    });

    document.querySelectorAll('.desk-div').forEach(function (desk) {
        const popup = desk.querySelector('.popup-info');
        if (popup) {
            desk.addEventListener('mouseenter', function () {
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
            desk.addEventListener('mouseleave', function () {
                popup.style.display = 'none';
                popup.style.left = '';
                popup.style.top = '';
            });
        }
    });
}

function addSingleEventListeners() {
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
                fetchBookedDates();
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
                        throw new Error(errorBody.message || 'Failed to cancel booking');
                    });
                }
                overlay.style.display = 'none';
                cancelPopup.style.display = 'none';
                fetchAndDisplayDesks();
                fetchBookedDates();
                showToast("Cancellation success", "success")
            })
            .catch(error => {
                overlay.style.display = 'none';
                cancelPopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast(error, "error")
            });
    });

    document.getElementById('overlay').addEventListener('click', function (event) {
        if (event.target === this) {
            bookPopup.style.display = 'none';
            cancelPopup.style.display = 'none';
            overlay.style.display = 'none';
        }
    });

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

function changeDate(days) {
    const inputDate = document.getElementById("bookingDate");
    let currentDate = new Date(inputDate.value);

    if (days === 1) {
        let maxDate = new Date();
        maxDate.setDate(maxDate.getDate() + maxDays - 1);
        maxDate.setHours(0, 0, 0, 0);
        if (currentDate > maxDate) {
            return;
        }
    }

    if (days === -1) {
        let minDate = new Date();
        minDate.setDate(minDate.getDate());
        if (currentDate < minDate) {
            return;
        }
    }

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
}

function displayWeekdays(bookedDays) {
    const weekdaysDiv = document.getElementById("weekdays");
    weekdaysDiv.innerHTML = ""; // Clear previous content

    const inputDate = new Date(document.getElementById("bookingDate").value);
    // const startingDay = getMonday(new Date());
    const startingDay = new Date();

    const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const weekendDays = [0, 6]; // Sunday and Saturday
    const skipWeekends = true;

    // Iterate over 14 days (2 weeks)
    for (let i = 0; i < maxDays; i++) {
        const currentDate = new Date(startingDay);
        currentDate.setDate(currentDate.getDate() + i);

        const dayBlock = document.createElement("div");

        if (skipWeekends && weekendDays.includes(currentDate.getDay())) {
            dayBlock.classList.add('day-skipped');
        } else {

            const dayOfWeek = daysOfWeek[currentDate.getDay()];
            const dayOfMonth = currentDate.getDate();

            const formattedDate = formatDate(currentDate);

            // Check if the current date is in the bookedDays list
            if (bookedDays.includes(formattedDate)) {
                dayBlock.classList.add('day-booked');
            }

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
                dayBlock.classList.add("selected-day");
            }

            let today = new Date();
            today.setHours(0, 0, 0, 0);
            currentDate.setHours(0, 0, 0, 0);

            if (currentDate.toDateString() === today.toDateString()) {
                dayBlock.classList.add("current-day");
            }

            if (currentDate < today) {
                dayBlock.classList.add("historic-block");
            } else if (weekendDays.includes(currentDate.getDay())) {
                dayBlock.classList.add("weekend-block");
            } else {
                dayBlock.classList.add("day-block");
                dayBlock.addEventListener("click", function () {
                    document.getElementById("bookingDate").value = this.getAttribute("data-date");
                    document.getElementById("bookingDate").dispatchEvent(new Event('change')); // Trigger change event
                });
            }
        }

        weekdaysDiv.appendChild(dayBlock);
    }
}