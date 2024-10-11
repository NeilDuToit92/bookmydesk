document.addEventListener('DOMContentLoaded', function () {
    fetchAndDisplayDesks();
    fetchUsersWithoutReservedDesks();
    addSingleEventListeners();
});

let currentFetchController = null;

function fetchAndDisplayDesks() {
    const imageContainer = document.querySelector('.image-container');

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

    fetch('/api/desk/all', {signal})
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
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
                    deskCircle.setAttribute('data-user', desk.bookedBy);
                    let reserved = false;

                    if (desk.status === "RESERVED") {
                        deskCircle.classList.add("desk-reserved");
                        reserved = true;
                    } else {
                        deskCircle.classList.add("desk-available");
                    }

                    const popupInfo = document.createElement('div');
                    popupInfo.classList.add('popup-info');
                    const bookedByParagraph = document.createElement('p');

                    if (reserved) {
                        bookedByParagraph.textContent = desk.displayId + ' - ' + desk.bookedBy;
                    } else {
                        bookedByParagraph.textContent = desk.displayId + ' - ' + "Click to reserve";
                    }

                    deskDiv.classList.add('pointer-hover');
                    deskCircle.classList.add('pointer-hover');

                    popupInfo.appendChild(bookedByParagraph);
                    deskDiv.appendChild(popupInfo);

                    deskDiv.appendChild(deskCircle);
                    imageContainer.appendChild(deskDiv);
                }
            );

            addRecurringEventListeners();
        })
        .catch(error => {
            if (error.name === 'AbortError') {
                console.log('Fetch aborted');
            } else {
                console.error('Error fetching desk data:', error);
            }
        });
}

function fetchUsersWithoutReservedDesks() {
    fetch('/api/booking/permanent/users/unassigned')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(users => {
            const userDropdown = document.getElementById('reserveUserDropdown');
            userDropdown.innerHTML = ''; // Clear existing options

            users.forEach(user => {
                const option = document.createElement('option');
                option.value = user.id; // Set user ID as value
                option.textContent = user.displayName; // Set display name as text
                userDropdown.appendChild(option); // Add option to dropdown
            });
        })
        .catch(error => {
            if (error.name === 'AbortError') {
                console.log('Fetch aborted');
            } else {
                console.error('Error fetching desk data:', error);
            }
        });
}

function addSingleEventListeners() {
    const overlay = document.getElementById('overlay');
    const reservePopup = document.getElementById('reservePopup');
    const cancelPopup = document.getElementById('cancelPopup');
    const closePopupBooking = document.getElementById('closePopupBooking');
    const closePopupCancel = document.getElementById('closePopupCancel');
    const confirmBooking = document.getElementById('confirmBooking');
    const cancelBooking = document.getElementById('cancelBooking');

    closePopupBooking.addEventListener('click', function () {
        overlay.style.display = 'none';
        reservePopup.style.display = 'none';
    });

    closePopupCancel.addEventListener('click', function () {
        overlay.style.display = 'none';
        cancelPopup.style.display = 'none';
    });

    confirmBooking.addEventListener('click', function () {

        const deskId = document.getElementById('deskDbId').value;
        const userId = document.getElementById('reserveUserDropdown').value;

        const url = '/api/booking/' + deskId + '/reserve?userId=' + userId;

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
                reservePopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast("Reservation success", "success")
            })
            .catch(error => {
                overlay.style.display = 'none';
                reservePopup.style.display = 'none';
                fetchAndDisplayDesks();
                showToast(error, "error")
            });
    });

    cancelBooking.addEventListener('click', function () {
        const deskId = document.getElementById('deskDbId').value;

        const url = '/api/booking/' + deskId + '/reserve';

        // Send a POST request using fetch API
        fetch(url, {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorBody => {
                        throw new Error(errorBody.message || 'Failed to cancel reservation');
                    });
                }
                overlay.style.display = 'none';
                cancelPopup.style.display = 'none';
                fetchAndDisplayDesks();
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
            reservePopup.style.display = 'none';
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
}

function showPopup(element, overlay, reservePopup, cancelPopup) {
    let deskStatus = element.getAttribute('data-status');

    let deskId = element.textContent;
    let deskDbId = element.getAttribute('data-id');
    const deskIdSpan = document.getElementById('deskId');
    const deskId2Span = document.getElementById('deskId2');
    const deskDbIdInput = document.getElementById('deskDbId');
    const deskUser = document.getElementById('deskUser');

    overlay.style.display = 'flex';
    deskIdSpan.textContent = deskId;
    deskId2Span.textContent = deskId;
    deskDbIdInput.value = deskDbId;

    switch (deskStatus) {
        case "AVAILABLE":
            reservePopup.style.display = 'block';
            cancelPopup.style.display = 'none';
            break;
        case "RESERVED":
            deskUser.textContent = element.getAttribute('data-user');
            cancelPopup.style.display = 'block';
            reservePopup.style.display = 'none';
            break;
    }
}

function addRecurringEventListeners() {
    const desks = document.querySelectorAll('.desk');
    const overlay = document.getElementById('overlay');
    const reservePopup = document.getElementById('reservePopup');
    const cancelPopup = document.getElementById('cancelPopup');

    desks.forEach(desk => {
        desk.addEventListener('click', function () {
            showPopup(this, overlay, reservePopup, cancelPopup);
        });
        let deskSelector = desk.closest('.desk-div').querySelector('.popup-info');
        if (deskSelector) {
            deskSelector.addEventListener('click', function () {
                let deskDiv = this.parentElement;
                let desk = deskDiv.querySelector('.desk');
                showPopup(desk, overlay, reservePopup, cancelPopup);
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