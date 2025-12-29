const mongoose = require('mongoose');

const CourseSchema = new mongoose.Schema({
    name: { type: String, required: true },
    code: { type: String, required: true },
    semester: { type: String, required: true }
});

// Ensure unique combination of code and semester
CourseSchema.index({ code: 1, semester: 1 }, { unique: true }); 

module.exports = mongoose.model("Course", CourseSchema);