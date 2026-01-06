const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema({
    userId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'User', 
        required: true 
    },
    title: { type: String, required: true },
    message: { type: String, required: true },
    type: { type: String, default: 'group_invite' },
    relatedId: { type: mongoose.Schema.Types.ObjectId }, // e.g., Group ID
    
    dedupeKey: { 
        type: String, 
        unique: true,
        sparse: true // Allows multiple nulls if the index wasn't strictly created
    },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Notification', NotificationSchema, 'notifications');