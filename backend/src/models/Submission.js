const mongoose = require('mongoose');

const SubmissionSchema = new mongoose.Schema({
    assignmentId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'Assignment', 
        required: true 
    },
    groupId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'StudyGroup', 
        required: true 
    },
    courseId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'Course', 
        required: true 
    },
    fileUrl: { 
        type: String, 
        required: true
    },
    grade: { 
        type: String, 
        default: null 
    },
    feedback: { 
        type: String, 
        default: "" 
    },
    taskReport: [{
        studentId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
        studentName: { type: String },
        contribution: { type: String }
    }],
    submittedAt: { 
        type: Date, 
        default: Date.now 
    }
});

module.exports = mongoose.model('Submission', SubmissionSchema);