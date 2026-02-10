const mongoose = require('mongoose');

const AssignmentSchema = new mongoose.Schema({
    courseId: { type: mongoose.Schema.Types.ObjectId, ref: 'Course', required: true },
    title: { type: String, required: true },
    description: { type: String }, // General instructions
    fileUrl: { type: String }, // Link to the assignment PDF
    dueAt: { type: Date, required: true },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' } // The Lecturer
}, { timestamps: true });

// Align with index from setup_db.js
AssignmentSchema.index({ courseId: 1, dueAt: 1 });

module.exports = mongoose.model("Assignment", AssignmentSchema);