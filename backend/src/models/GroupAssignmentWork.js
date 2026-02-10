const mongoose = require('mongoose');

const SubTaskSchema = new mongoose.Schema({
    title: { type: String, required: true },
    assignedTo: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    status: { 
        type: String, 
        enum: ['todo', 'pending_approval', 'completed'], 
        default: 'todo' 
    },
    completedAt: { type: Date }
});

const GroupAssignmentWorkSchema = new mongoose.Schema({
    assignmentId: { type: mongoose.Schema.Types.ObjectId, ref: 'Assignment', required: true },
    groupId: { type: mongoose.Schema.Types.ObjectId, ref: 'StudyGroup', required: true },
    subTasks: [SubTaskSchema]
}, { timestamps: true });

// Align with unique index from setup_db.js
GroupAssignmentWorkSchema.index({ assignmentId: 1, groupId: 1 }, { unique: true });

module.exports = mongoose.model("GroupAssignmentWork", GroupAssignmentWorkSchema);