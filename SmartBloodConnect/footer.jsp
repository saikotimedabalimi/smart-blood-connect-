<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<footer class="site-footer">
    <div class="container footer-inner">
        <div>
            <h3>Smart Blood Connect</h3>
            <p>Public donor discovery, fast request routing, and a cleaner interface for emergency support.</p>
        </div>
        <div class="footer-note">
            <p>Colors kept minimal, pages readable, and the site usable before login and after login.</p>
        </div>
    </div>
</footer>
<script>
(function () {
    const root = document.documentElement;
    const button = document.getElementById('theme-toggle');
    const saved = localStorage.getItem('sbc-theme');
    const preferredDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const theme = saved || (preferredDark ? 'dark' : 'light');
    root.setAttribute('data-theme', theme);
    if (button) {
        button.textContent = theme === 'dark' ? 'Light' : 'Dark';
        button.addEventListener('click', function () {
            const next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            root.setAttribute('data-theme', next);
            localStorage.setItem('sbc-theme', next);
            button.textContent = next === 'dark' ? 'Light' : 'Dark';
        });
    }

    const notificationButton = document.querySelector('[data-enable-notifications]');
    const notificationFeed = document.body ? document.body.getAttribute('data-notification-feed') : null;
    const notificationList = document.getElementById('notification-list');
    const seenIds = new Set();

    if (notificationList) {
        notificationList.querySelectorAll('[data-notification-id]').forEach(function (item) {
            seenIds.add(item.getAttribute('data-notification-id'));
        });
    }

    function updateNotificationButton() {
        if (!notificationButton) {
            return;
        }
        if (!('Notification' in window)) {
            notificationButton.textContent = 'Browser Alerts Unsupported';
            notificationButton.disabled = true;
            return;
        }
        if (Notification.permission === 'granted') {
            notificationButton.textContent = 'Browser Alerts Enabled';
        } else if (Notification.permission === 'denied') {
            notificationButton.textContent = 'Alerts Blocked';
        } else {
            notificationButton.textContent = 'Enable Browser Alerts';
        }
    }

    function prependNotification(item) {
        if (!notificationList || !item || seenIds.has(String(item.id))) {
            return;
        }

        const row = document.createElement('article');
        row.className = 'notification-item';
        row.setAttribute('data-notification-id', String(item.id));
        row.innerHTML =
            '<div><strong>' + escapeHtml(item.title) + '</strong><p>' + escapeHtml(item.message) + '</p></div>' +
            '<span>' + escapeHtml(item.createdAt || '') + '</span>';
        notificationList.prepend(row);
        seenIds.add(String(item.id));
    }

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    async function pollNotifications() {
        if (!notificationFeed) {
            return;
        }
        try {
            const response = await fetch(notificationFeed, { credentials: 'same-origin' });
            if (!response.ok) {
                return;
            }
            const payload = await response.json();
            const notifications = Array.isArray(payload.notifications) ? payload.notifications : [];
            notifications.forEach(function (item) {
                prependNotification(item);
                if ('Notification' in window && Notification.permission === 'granted') {
                    new Notification(item.title || 'Smart Blood Connect', {
                        body: item.message || 'New blood request update received.'
                    });
                }
            });
        } catch (error) {
            console.error('Notification polling failed', error);
        }
    }

    if (notificationButton) {
        updateNotificationButton();
        notificationButton.addEventListener('click', async function () {
            if (!('Notification' in window)) {
                return;
            }
            if (Notification.permission === 'default') {
                await Notification.requestPermission();
            }
            updateNotificationButton();
        });
    }

    if (notificationFeed) {
        window.setInterval(pollNotifications, 20000);
    }
})();
</script>
