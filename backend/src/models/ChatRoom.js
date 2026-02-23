// backend/src/models/ChatRoom.js
const mongoose = require('mongoose');

const chatRoomSchema = new mongoose.Schema({
    type: {
        type: String,
        enum: ['standard', 'lecturer_consultation', 'course_forum'],
        required: true
    },
    parentGroupId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'StudyGroup' 
    },
    // Stores extra info like the lecturer's name for the header
    metadata: {
        lecturerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
        lecturerName: String
    },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('ChatRoom', chatRoomSchema);