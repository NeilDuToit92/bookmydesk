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
    toastTimeoutId = setTimeout(function () {
        toast.classList.add('hide');

        // Remove the 'hide' class after the animation ends to reset the toast
        toast.addEventListener('transitionend', function () {
            if (toast.classList.contains('hide')) {
                toast.className = 'toast'; // Reset to base class
            }
        }, {once: true});
    }, 4000);
}