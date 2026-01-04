const mongoose = require('mongoose');

const studyGroupSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    courseId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Course',
        required: true
    },
    purpose: {
        type: String,
        enum: ['assignment_submission', 'exam_study', 'general', 'other'],
        default: 'general'
    },
    description: String,
    chatId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Conversation' 
    },
    members: [{
        userId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true
        },
        role: {
            type: String,
            enum: ['admin', 'student'], 
            default: 'student'
        },
        status: {
            type: String,
            enum: ['active', 'pending', 'invited'], 
            default: 'active' 
        },
        joinedAt: {
            type: Date,
            default: Date.now
        }
    }],
    createdAt: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('StudyGroup', studyGroupSchema);