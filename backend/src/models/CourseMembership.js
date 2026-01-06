const mongoose = require('mongoose');

const CourseMembershipSchema = new mongoose.Schema({
    courseId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'Course', 
        required: true 
    },
    userId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'User', 
        required: true 
    },
    role: { type: String, default: 'student' },
    status: { type: String, default: 'active' }
});

// Prevents a student from being registered to the same course twice
CourseMembershipSchema.index({ courseId: 1, userId: 1 }, { unique: true });

module.exports = mongoose.model('CourseMembership', CourseMembershipSchema, "course_memberships");