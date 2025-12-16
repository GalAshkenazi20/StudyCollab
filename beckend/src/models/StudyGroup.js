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
    // נדרש לפי ה-Use Case diagram (Set group purpose)
    purpose: {
        type: String,
        enum: ['assignment_submission', 'exam_study', 'general', 'other'],
        default: 'general'
    },
    description: String,
    // קישור לקולקציית השיחות (לפי מבנה התיקיות שלך)
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
        // תוספת קריטית לפי FR-4.2 (הזמנה ואישור)
        status: {
            type: String,
            enum: ['active', 'pending', 'invited'], 
            default: 'active' // ברירת מחדל לפיילוט: מוסיף ישר
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