/**
 * StackOverflow Tag Details - JavaScript
 */

// WebSocket connection
let stompClient = null;

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initWebSocket();
    setupEventListeners();
});

// Initialize WebSocket connection
function initWebSocket() {
    const socket = new SockJS('/stackoverflow-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket: ' + frame);

        // Subscribe to trend updates
        stompClient.subscribe('/topic/trends', function(message) {
            console.log('Received trend update');
            // If we're on the details page, reload to show fresh data
            window.location.reload();
        });
    }, function(error) {
        console.error('WebSocket connection error: ' + error);
        // Retry connection after 5 seconds
        setTimeout(initWebSocket, 5000);
    });
}

// Setup event listeners
function setupEventListeners() {
    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', function() {
        fetch('/api/refresh', {
            method: 'POST'
        }).then(() => {
            window.location.reload();
        });
    });
}