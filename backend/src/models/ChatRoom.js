// backend/src/models/ChatRoom.js
const mongoose = require('mongoose');

const chatRoomSchema = new mongoose.Schema({
    type: { 
        type: String, 
        enum: ['standard', 'lecturer_consultation', 'peer_group_consultation'], 
        default: 'standard' 
    },
    parentGroupId: { type: mongoose.Schema.Types.ObjectId, ref: 'StudyGroup' },
    metadata: {
        // Lecturer fields
        lecturerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
        lecturerName: { type: String },
        // Peer fields
        pairKey: { type: String }, 
        groupIds: [{ type: String }]
    },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('ChatRoom', chatRoomSchema);