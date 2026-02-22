const mongoose = require('mongoose');

const TopicSchema = new mongoose.Schema({
    title: { type: String, required: true },
    isCompleted: { type: Boolean, default: false }
});

const CourseSchema = new mongoose.Schema({
    name: { type: String, required: true },
    code: { type: String, required: true },
    semester: { type: String, required: true },
    
    // --- התיקון: הגדרה רשמית של השדה lecturer ---
    lecturer: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'User',
        required: true 
    },
    // ---------------------------------------------

    schedule: {
        day: { type: String, required: true },
        startTime: { type: String, required: true },
        endTime: { type: String, required: true },
        location: { type: String, default: "TBD" }
    },
    
    totalLectures: { type: Number, default: 13 },
    completedLectures: { type: Number, default: 0 },
    topics: [TopicSchema],
    
    materials: [{
        title: String,
        url: String,
        type: { type: String, enum: ['link', 'file', 'zoom'] },
        uploadedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }
    }]
});

CourseSchema.index({ code: 1, semester: 1 }, { unique: true }); 

module.exports = mongoose.model("Course", CourseSchema);