const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');

// 1. Get all notifications for a specific user
// This will be called by your NotificationViewModel in Android
router.get('/user/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        // Fetch notifications and sort by newest first
        const notifications = await Notification.find({ userId: userId })
            .sort({ createdAt: -1 });
        
        res.json(notifications);
    } catch (error) {
        console.error("Error fetching notifications:", error);
        res.status(500).json({ message: "Failed to fetch notifications", error: error.message });
    }
});

// 2. Delete a notification (Mark as Read)
// Whenever a user clicks a notification in the app, this endpoint is called
router.delete('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const deletedNotification = await Notification.findByIdAndDelete(id);
        
        if (!deletedNotification) {
            return res.status(404).json({ message: "Notification not found" });
        }

        res.status(200).json({ message: "Notification cleared successfully" });
    } catch (error) {
        console.error("Error deleting notification:", error);
        res.status(500).json({ message: "Failed to delete notification", error: error.message });
    }
});

module.exports = router;