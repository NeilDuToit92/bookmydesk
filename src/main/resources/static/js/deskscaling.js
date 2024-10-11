let originalPositions = [];

function scaleLayout() {
    // Get the natural dimensions of your background image
    const backgroundImage = document.getElementById('backgroundImage');
    const imgNaturalWidth = backgroundImage.naturalWidth;
    const imgNaturalHeight = backgroundImage.naturalHeight;
    const imgOriginalWidth = 1920;
    const imgOriginalHeight = (imgOriginalWidth / imgNaturalWidth) * imgNaturalHeight;
    const imgActualWidth = backgroundImage.width;
    const imgActualHeight = backgroundImage.height;

    // Calculate the scale factor based on the aspect ratio
    const scaleX = imgActualWidth / imgOriginalWidth;
    const scaleY = imgActualHeight / imgOriginalHeight;

    // Use the smaller scale to fit the layout within the viewport
    const scale = Math.min(scaleX, scaleY);

    // Get all desk-div elements
    const desksContainer = document.getElementById('desksContainer');

    const desks = desksContainer.querySelectorAll('.desk');
    desks.forEach((desk, index) => {
        desk.style.transform = `scale(${scale})`;
    });

    const deskDivs = desksContainer.querySelectorAll('.desk-div');

    // If it's the first time scaling, store the original positions
    if (originalPositions.length === 0) {
        deskDivs.forEach((deskDiv, index) => {
            const left = parseFloat(deskDiv.style.left);
            const top = parseFloat(deskDiv.style.top);
            originalPositions[index] = { left, top };  // Store original positions
        });
    }

    // Update the position of each desk-div based on the new scale
    deskDivs.forEach((deskDiv, index) => {
        //-12.5 to make coordinates the middle of the box
        const originalLeft = originalPositions[index].left;
        const originalTop = originalPositions[index].top - headerHeight;

        const leftFix = 5 - (scale * 5);
        const topFix = 33 - (scale * 33);
        // Apply the current scale to the original positions
        const newLeft = (originalLeft * scale) - leftFix;
        const newTop = ((originalTop * scale) - topFix) + headerHeight;

        deskDiv.style.left = `${newLeft}px`;
        deskDiv.style.top = `${newTop}px`;
        // deskDiv.style.transform = `scale(${scale})`;
    });
}

// Call the function on page load and window resize
window.addEventListener('load', scaleLayout);
window.addEventListener('resize', scaleLayout);