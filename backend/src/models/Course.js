const mongoose = require('mongoose');

const TopicSchema = new mongoose.Schema({
    title: { type: String, required: true },
    isCompleted: { type: Boolean, default: false }
});

const CourseSchema = new mongoose.Schema({
    name: { type: String, required: true },
    code: { type: String, required: true },
    semester: { type: String, required: true },
    
    // Progress fields - Shared by all members of this instance
    totalLectures: { type: Number, default: 13 },
    completedLectures: { type: Number, default: 0 },
    topics: [TopicSchema],
    
    // Global materials for this semester's course
    materials: [{
        title: String,
        url: String,
        type: { type: String, enum: ['link', 'file', 'zoom'] }
    }]
});

// Unique index remains to prevent duplicate instances
CourseSchema.index({ code: 1, semester: 1 }, { unique: true }); 

module.exports = mongoose.model("Course", CourseSchema);