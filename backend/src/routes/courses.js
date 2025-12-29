const express = require('express');
const router = express.Router();
const CourseMembership = require('../models/CourseMembership');
const User = require('../models/User');

// GET /api/courses/user/:userId - Get courses for a specific user
router.get('/user/:userId', async (req, res) => {
    try {
        const memberships = await CourseMembership.find({ userId: req.params.userId })
            .populate('courseId');
        
        // Extract the course objects
        const courses = memberships.map(m => m.courseId).filter(c => c != null);
        res.json(courses);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// GET /api/courses/:courseId/students - Get all students in a course (for classmate selection)
router.get('/:courseId/students', async (req, res) => {
    try {
        const memberships = await CourseMembership.find({ courseId: req.params.courseId })
            .populate('userId');
        
        const students = memberships.map(m => m.userId).filter(u => u != null);
        res.json(students);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;