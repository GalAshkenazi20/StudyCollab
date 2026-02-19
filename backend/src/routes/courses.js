const express = require('express');
const router = express.Router();
const Course = require('../models/Course');
const CourseMembership = require('../models/CourseMembership');
const User = require('../models/User');

// GET /api/courses/user/:userId - Get courses for a specific user
router.get('/user/:userId', async (req, res) => {
    console.log("🔍 Request received for User ID:", req.params.userId); 

    try {
        const memberships = await CourseMembership.find({ userId: req.params.userId })
            .populate('courseId');
        
        console.log("✅ Found memberships count:", memberships.length);

        // Extract the course objects
        const courses = memberships.map(m => m.courseId).filter(c => c != null);
        
        console.log("📦 Returning courses to app:", courses);
        
        res.json(courses);
    } catch (error) {
        console.error("❌ Error fetching courses:", error);
        res.status(500).json({ message: error.message });
    }
});

// GET /api/courses/:courseId/students
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

// GET /api/courses/:courseId — Get a single course by ID
router.get('/:courseId', async (req, res) => {
    try {
        const course = await Course.findById(req.params.courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });
        res.json(course);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// PUT /api/courses/:courseId/topics/:topicIndex/toggle
// Lecturer toggles a topic's completion status
router.put('/:courseId/topics/:topicIndex/toggle', async (req, res) => {
    try {
        const { courseId, topicIndex } = req.params;
        const course = await Course.findById(courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });

        const index = parseInt(topicIndex);
        if (index < 0 || index >= course.topics.length) {
            return res.status(400).json({ message: "Invalid topic index" });
        }

        // Toggle the completion status
        course.topics[index].isCompleted = !course.topics[index].isCompleted;

        // Recalculate completedLectures count
        course.completedLectures = course.topics.filter(t => t.isCompleted).length;

        await course.save();
        res.json(course);
    } catch (error) {
        console.error("Toggle topic error:", error);
        res.status(500).json({ message: "Failed to toggle topic" });
    }
});

// PUT /api/courses/:courseId/topics
// Lecturer adds a new topic to the syllabus
router.put('/:courseId/topics', async (req, res) => {
    try {
        const { title } = req.body;
        const course = await Course.findById(courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });

        course.topics.push({ title, isCompleted: false });
        course.totalLectures = course.topics.length;
        await course.save();
        res.json(course);
    } catch (error) {
        res.status(500).json({ message: "Failed to add topic" });
    }
});

// DELETE /api/courses/:courseId/topics/:topicIndex
// Lecturer removes a topic from the syllabus
router.delete('/:courseId/topics/:topicIndex', async (req, res) => {
    try {
        const { courseId, topicIndex } = req.params;
        const course = await Course.findById(courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });

        const index = parseInt(topicIndex);
        course.topics.splice(index, 1);
        course.totalLectures = course.topics.length;
        course.completedLectures = course.topics.filter(t => t.isCompleted).length;

        await course.save();
        res.json(course);
    } catch (error) {
        res.status(500).json({ message: "Failed to remove topic" });
    }
});

// GET /api/courses/:courseId/peer-groups
router.get('/:courseId/peer-groups', async (req, res) => {
    try {
        const groups = await StudyGroup.find({ courseId: req.params.courseId });
        res.json(groups);
    } catch (error) {
        res.status(500).json({ message: "Error finding peers" });
    }
});

module.exports = router;