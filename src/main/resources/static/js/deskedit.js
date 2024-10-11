document.addEventListener('DOMContentLoaded', function () {
    fetchAndDisplayDesks();
});

let currentFetchController = null;

function fetchAndDisplayDesks() {
    const bookingDate = new Date().toISOString().split('T')[0];

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

    fetch('/api/desk?date=' + bookingDate, {signal})
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
                deskCircle.classList.add("desk-unmoved");

                const popupInfo = document.createElement('div');
                popupInfo.classList.add('popup-info');
                const xInput = document.createElement('input');
                xInput.type = 'number';
                xInput.value = parseInt(deskDiv.style.left);
                xInput.classList.add('xy-number-input');
                const yInput = document.createElement('input');
                yInput.type = 'number';
                yInput.value = parseInt(deskDiv.style.top);
                yInput.classList.add('xy-number-input');

                // Update deskDiv position when input values change
                xInput.addEventListener('input', () => {
                    deskDiv.style.left = xInput.value + 'px';
                    deskMoved(deskCircle);
                });

                yInput.addEventListener('input', () => {
                    deskDiv.style.top = yInput.value + 'px';
                    deskMoved(deskCircle);
                });

                popupInfo.appendChild(document.createTextNode('X: '));
                popupInfo.appendChild(xInput);
                popupInfo.appendChild(document.createTextNode('Y: '));
                popupInfo.appendChild(yInput);
                deskDiv.appendChild(popupInfo);

                // Variables to hold the initial position
                let offsetX, offsetY;

                // Mouse down event to start dragging
                deskDiv.addEventListener('mousedown', (e) => {
                    offsetX = e.clientX - parseInt(deskDiv.style.left);
                    offsetY = e.clientY - parseInt(deskDiv.style.top);
                    document.addEventListener('mousemove', onMouseMove);
                    document.addEventListener('mouseup', onMouseUp);
                });

                // Mouse move event to drag the element
                const onMouseMove = (e) => {
                    deskDiv.style.left = (e.clientX - offsetX) + 'px';
                    deskDiv.style.top = (e.clientY - offsetY) + 'px';
                    xInput.value = parseInt(deskDiv.style.left);
                    yInput.value = parseInt(deskDiv.style.top);
                    deskMoved(deskCircle);
                };

                // Mouse up event to end dragging
                const onMouseUp = () => {
                    document.removeEventListener('mousemove', onMouseMove);
                    document.removeEventListener('mouseup', onMouseUp);
                    console.log(`New position - x: ${deskDiv.style.left}, y: ${deskDiv.style.top}`);
                };

                deskDiv.appendChild(deskCircle);
                imageContainer.appendChild(deskDiv);
            });

            // addRecurringEventListeners();
        })
        .catch(error => {
            if (error.name === 'AbortError') {
                console.log('Fetch aborted');
            } else {
                console.error('Error fetching desk data:', error);
            }
        });
}

function deskMoved(deskCircle) {
    console.log(deskCircle.classList);
    if (deskCircle.classList.contains('desk-unmoved')) {
        deskCircle.classList.remove('desk-unmoved');
        deskCircle.classList.add('desk-moved');
    }
}