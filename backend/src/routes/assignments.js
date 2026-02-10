const express = require('express');
const router = express.Router();
const Assignment = require('../models/Assignment');

// 1. Create a course assignment (Lecturer)
router.post('/', async (req, res) => {
    try {
        const { courseId, title, description, fileUrl, dueAt, creatorId } = req.body;
        const newAssignment = new Assignment({
            courseId, title, description, fileUrl, dueAt, createdBy: creatorId
        });
        await newAssignment.save();
        res.status(201).json(newAssignment);
    } catch (error) {
        res.status(500).json({ message: "Failed to create assignment", error: error.message });
    }
});

// 2. Get all assignments for a course
router.get('/course/:courseId', async (req, res) => {
    try {
        const assignments = await Assignment.find({ courseId: req.params.courseId })
            .sort({ dueAt: 1 });
        res.json(assignments);
    } catch (error) {
        res.status(500).json({ message: "Failed to fetch assignments" });
    }
});

module.exports = router;