const mongoose = require('mongoose');

const TopicSchema = new mongoose.Schema({
    title: { type: String, required: true },
    isCompleted: { type: Boolean, default: false }
});

const CourseSchema = new mongoose.Schema({
    name: { type: String, required: true },
    code: { type: String, required: true },
    semester: { type: String, required: true },
    
    // --- הוספנו את החלק הזה למערכת שעות ---
    schedule: {
        day: { type: String, required: true }, // e.g., "Sunday", "Monday"
        startTime: { type: String, required: true }, // e.g., "08:00"
        endTime: { type: String, required: true },   // e.g., "11:00"
        location: { type: String, default: "Building 3, Room 101" }
    },
    // ----------------------------------------

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