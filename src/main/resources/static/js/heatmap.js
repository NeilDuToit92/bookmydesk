document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/booking/heatmap')
        .then(response => response.json())
        .then(bookings => {
            let coordinates = [];

            bookings.forEach(booking => {
                coordinates.push({ x: booking.x, y: booking.y });
            });

            console.log(coordinates);

            const heatmapInstance = h337.create({
                container: document.getElementById('heatmap')
            });

            const heatmapData = {
                max: 10, // maximum value of the intensity
                data: coordinates.map(coord => ({
                    x: coord.x,
                    y: coord.y,
                    value: 1 // intensity value, adjust as necessary
                }))
            };

            heatmapInstance.setData(heatmapData);

            document.getElementById("backgroundImage").style.position = 'absolute';

        })
        .catch(error => console.error('Error fetching desk data:', error));
});