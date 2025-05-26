/**
 * StackOverflow Tag Trend Analyzer - Main JavaScript
 */

// Chart objects
let mainChart;
let miniCharts = {};

// WebSocket connection
let stompClient = null;

// Color palette for charts
const colors = [
    'rgb(75, 192, 192)',   // Teal
    'rgb(255, 99, 132)',   // Pink
    'rgb(54, 162, 235)',   // Blue
    'rgb(255, 159, 64)',   // Orange
    'rgb(153, 102, 255)',  // Purple
    'rgb(255, 205, 86)',   // Yellow
    'rgb(201, 203, 207)',  // Grey
    'rgb(255, 99, 71)',    // Tomato
    'rgb(46, 139, 87)',    // Sea Green
    'rgb(106, 90, 205)'    // Slate Blue
];

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initWebSocket();
    initMainChart();
    initMiniCharts();
    setupEventListeners();
    loadInitialData();
});

// Initialize WebSocket connection
function initWebSocket() {
    const socket = new SockJS('/stackoverflow-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket: ' + frame);

        // Subscribe to trend updates
        stompClient.subscribe('/topic/trends', function(message) {
            const trendData = JSON.parse(message.body);
            updateCharts(trendData);
        });
    }, function(error) {
        console.error('WebSocket connection error: ' + error);
        // Retry connection after 5 seconds
        setTimeout(initWebSocket, 5000);
    });
}

// Initialize the main trend chart
function initMainChart() {
    const ctx = document.getElementById('trendChart').getContext('2d');

    mainChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Question Count'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Date'
                    }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            }
        }
    });
}

// Initialize mini charts for each tag
function initMiniCharts() {
    document.querySelectorAll('.tag-trend-mini').forEach((element, index) => {
        const tag = element.dataset.tag;
        const canvas = document.getElementById(`miniChart-${tag}`);

        if (canvas) {
            const ctx = canvas.getContext('2d');

            miniCharts[tag] = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: tag,
                        data: [],
                        borderColor: colors[index % colors.length],
                        borderWidth: 2,
                        pointRadius: 0,
                        tension: 0.4,
                        fill: true,
                        backgroundColor: `${colors[index % colors.length].replace('rgb', 'rgba').replace(')', ', 0.1)')}`,
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            enabled: false
                        }
                    },
                    scales: {
                        x: {
                            display: false
                        },
                        y: {
                            display: false
                        }
                    }
                }
            });
        }
    });
}

// Setup event listeners
function setupEventListeners() {
    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', function() {
        fetch('/api/refresh', {
            method: 'POST'
        }).then(() => {
            loadInitialData();
        });
    });

    // Time period buttons
    document.querySelectorAll('[data-period]').forEach(button => {
        button.addEventListener('click', function() {
            // Update active state
            document.querySelectorAll('[data-period]').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');

            // Get the selected period
            const period = this.dataset.period;
            let days = 7;

            if (period === 'day') {
                days = 1;
            } else if (period === 'month') {
                days = 30;
            }

            // Reload data for the selected period
            loadTrendData(days);
        });
    });
}

// Load initial data
function loadInitialData() {
    // Default to a 7-day period
    loadTrendData(7);
}

// Load trend data for the given time period
function loadTrendData(days) {
    // Get top tags
    fetch('/api/tags/top?limit=10')
        .then(response => response.json())
        .then(tags => {
            // Fetch trend data for each tag
            const trendPromises = tags.map(tag =>
                fetch(`/api/tags/${tag}/trend?days=${days}`)
                    .then(response => response.json())
            );

            Promise.all(trendPromises)
                .then(trendData => {
                    updateCharts(trendData);
                });
        });
}

// Update charts with new trend data
function updateCharts(trendData) {
    if (!trendData || trendData.length === 0) return;

    // Clear current datasets
    mainChart.data.datasets = [];

    // Extract timestamps for the x-axis (from the first trend dataset)
    const timestamps = trendData[0].timePoints.map(point => {
        const date = new Date(point.timestamp * 1000);
        return date.toLocaleDateString();
    });

    mainChart.data.labels = timestamps;

    // Add each tag's dataset to the main chart
    trendData.forEach((trend, index) => {
        const tagName = trend.tagName;
        const counts = trend.timePoints.map(point => point.count);

        mainChart.data.datasets.push({
            label: tagName,
            data: counts,
            borderColor: colors[index % colors.length],
            backgroundColor: `${colors[index % colors.length].replace('rgb', 'rgba').replace(')', ', 0.1)')}`,
            borderWidth: 2,
            tension: 0.4,
            fill: true
        });

        // Update mini chart if it exists
        if (miniCharts[tagName]) {
            miniCharts[tagName].data.labels = timestamps;
            miniCharts[tagName].data.datasets[0].data = counts;
            miniCharts[tagName].update();
        }
    });

    mainChart.update();
}