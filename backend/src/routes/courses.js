const express = require('express');
const router = express.Router();
const Course = require('../models/Course');
const CourseMembership = require('../models/CourseMembership');
const User = require('../models/User');
const StudyGroup = require('../models/StudyGroup'); // הוספתי את זה כדי שנתיב peer-groups יעבוד

// =================================================================
// 1. Create a New Course (Updated for 'lecturer' field)
// =================================================================
router.post('/', async (req, res) => {
    try {
        // שים לב: אנחנו מצפים ל-lecturer (ולא lecturerId) כדי שיהיה אחיד עם המודל
        const { name, code, semester, schedule, lecturer, description } = req.body;

        if (!lecturer) {
            return res.status(400).json({ message: "Lecturer ID is required." });
        }

        const newCourse = new Course({
            name,
            code,
            semester,
            description,
            schedule,
            lecturer: lecturer, // שמירה בשדה הנכון (lecturer)
            topics: [],
            totalLectures: 13
        });

        const savedCourse = await newCourse.save();
        
        // רישום המרצה לקורס בטבלת החברויות (Memberships)
        await new CourseMembership({
            courseId: savedCourse._id,
            userId: lecturer,
            role: 'lecturer'
        }).save();

        res.status(201).json(savedCourse);
    } catch (error) {
        console.error("Error creating course:", error);
        res.status(500).json({ message: "Failed to create course", error: error.message });
    }
});

// =================================================================
// 2. Get Courses for a Specific User
// =================================================================
router.get('/user/:userId', async (req, res) => {
    console.log("🔍 Request received for User ID:", req.params.userId); 

    try {
        const memberships = await CourseMembership.find({ userId: req.params.userId })
            .populate('courseId');
        
        console.log("✅ Found memberships count:", memberships.length);

        // Extract the course objects and filter out nulls (deleted courses)
        const courses = memberships.map(m => m.courseId).filter(c => c != null);
        
        console.log("📦 Returning courses to app:", courses);
        
        res.json(courses);
    } catch (error) {
        console.error("❌ Error fetching courses:", error);
        res.status(500).json({ message: error.message });
    }
});

// =================================================================
// 3. Get Students in a Course
// =================================================================
router.get('/:courseId/students', async (req, res) => {
    try {
        const memberships = await CourseMembership.find({
            courseId: req.params.courseId,
            role: 'student'  // Filter: only return students
        }).populate('userId');

        const students = memberships.map(m => m.userId).filter(u => u != null);
        res.json(students);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// =================================================================
// 4. Get a Single Course by ID
// =================================================================
router.get('/:courseId', async (req, res) => {
    try {
        const course = await Course.findById(req.params.courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });
        res.json(course);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// =================================================================
// 5. Get the Lecturer for a Course
// =================================================================
router.get('/:courseId/lecturer', async (req, res) => {
    try {
        // שלב 1: נסה למצוא את המרצה ישירות מהקורס (השיטה החדשה)
        const course = await Course.findById(req.params.courseId).populate('lecturer', 'profile.fullName');
        
        if (course && course.lecturer) {
            return res.json({ 
                lecturerId: course.lecturer._id, 
                name: course.lecturer.profile.fullName 
            });
        }

        // שלב 2 (גיבוי): נסה למצוא דרך טבלת ה-Memberships (לקורסים ישנים)
        const membership = await CourseMembership.find({
            courseId: req.params.courseId,
            role: 'lecturer'
        }).populate('userId', 'profile.fullName');

        if (membership.length === 0) return res.status(404).json({ message: "No lecturer found" });

        const lecturer = membership[0].userId;
        res.json({ lecturerId: lecturer._id, name: lecturer.profile.fullName });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// =================================================================
// 6. Toggle Topic Completion
// =================================================================
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

// =================================================================
// 7. Add a New Topic
// =================================================================
router.put('/:courseId/topics', async (req, res) => {
    try {
        const { title } = req.body;
        const course = await Course.findById(req.params.courseId);
        if (!course) return res.status(404).json({ message: "Course not found" });

        course.topics.push({ title, isCompleted: false });
        course.totalLectures = course.topics.length;
        await course.save();
        res.json(course);
    } catch (error) {
        res.status(500).json({ message: "Failed to add topic" });
    }
});

// =================================================================
// 8. Remove a Topic
// =================================================================
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

// =================================================================
// 9. Get Peer Groups for a Course
// =================================================================
router.get('/:courseId/peer-groups', async (req, res) => {
    try {
        const groups = await StudyGroup.find({ courseId: req.params.courseId });
        res.json(groups);
    } catch (error) {
        console.error("Error finding peers:", error);
        res.status(500).json({ message: "Error finding peers" });
    }
});

module.exports = router;