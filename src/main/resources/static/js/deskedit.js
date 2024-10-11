document.addEventListener('DOMContentLoaded', function () {
    fetchAndDisplayDesks();
});

function fetchAndDisplayDesks() {
    const imageContainer = document.querySelector('.image-container');

    fetch('/api/desk/all' )
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

                const resetButton = document.createElement('button');
                resetButton.textContent = 'Reset Position';
                const originalTop = parseInt(deskDiv.style.top);
                const originalLeft = parseInt(deskDiv.style.left);
                resetButton.onclick = function() {
                    resetDeskPosition(deskDiv, deskCircle, originalLeft, originalTop);
                };
                popupInfo.appendChild(resetButton);
                deskDiv.appendChild(popupInfo);

                // Variables to hold the initial position
                let offsetX, offsetY;
                let originalZIndex = deskDiv.style.zIndex;

                // Mouse down event to start dragging
                deskDiv.addEventListener('mousedown', (e) => {
                    offsetX = e.clientX - parseInt(deskDiv.style.left);
                    offsetY = e.clientY - parseInt(deskDiv.style.top);
                    deskDiv.style.zIndex = 1000;
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
                    deskDiv.style.zIndex = originalZIndex;
                    document.removeEventListener('mousemove', onMouseMove);
                    document.removeEventListener('mouseup', onMouseUp);
                };

                deskDiv.addEventListener('mouseenter', function () {
                    let viewportRight = window.scrollX + window.innerWidth;
                    let viewportBottom = window.scrollY + window.innerHeight;

                    popupInfo.style.display = 'inline-block';
                    let popupRect = popupInfo.getBoundingClientRect();
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
                        popupInfo.style.left = (-1 * overhang) + 'px';
                    }

                    if (popupBottom > viewportBottom) {
                        //If falling off the bottom of the viewport, move it to just inside the viewport
                        let overhang = popupBottom - viewportBottom;
                        popupInfo.style.top = (-1 * overhang) + 'px';
                    }
                });
                deskDiv.addEventListener('mouseleave', function () {
                    popupInfo.style.display = 'none';
                    popupInfo.style.left = '';
                    popupInfo.style.top = '';
                });

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
    if (deskCircle.classList.contains('desk-unmoved')) {
        deskCircle.classList.remove('desk-unmoved');
        deskCircle.classList.add('desk-moved');
    }
}

function resetDeskPosition(deskDiv, deskCircle, leftPosition, topPosition) {
    deskCircle.classList.remove('desk-moved');
    deskCircle.classList.add('desk-unmoved');
    deskDiv.style.top = topPosition + 'px';
    deskDiv.style.left = leftPosition + 'px';
}