const express = require('express');
const router = express.Router();
const Course = require('../models/Course');
const CourseMembership = require('../models/CourseMembership');
const User = require('../models/User');
const ChatRoom = require('../models/ChatRoom');
const StudyGroup = require('../models/StudyGroup');

// GET /api/courses/user/:userId - Get courses for a specific user
router.get('/user/:userId', async (req, res) => {
    try {
        const memberships = await CourseMembership.find({ userId: req.params.userId })
            .populate({
                path: 'courseId',
                // Populate the lecturer field inside the course and get the fullName
                populate: { path: 'lecturer', select: 'profile.fullName' }
            });

        const courses = memberships.map(m => {
            const course = m.courseId;
            if (course) {
                // Flatten the lecturer name into a top-level field for the Android model
                const courseObj = course.toObject();
                return {
                    ...courseObj,
                    lecturerName: course.lecturer?.profile?.fullName || "TBD"
                };
            }
            return null;
        }).filter(c => c != null);

        res.json(courses);
    } catch (error) {
        console.error("Error fetching courses:", error);
        res.status(500).json({ message: error.message });
    }
});

// GET /api/courses/:courseId/students
router.get('/:courseId/students', async (req, res) => {
    try {
        const memberships = await CourseMembership.find({
            courseId: req.params.courseId,
            role: 'student'
        }).populate('userId');

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

// GET /api/courses/:courseId/lecturer — Get the lecturer for a course
router.get('/:courseId/lecturer', async (req, res) => {
    try {
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

router.get('/:courseId/forum', async (req, res) => {
    try {
        const { courseId } = req.params;
        const course = await Course.findById(courseId);
        
        let forumRoom = await ChatRoom.findOne({ 
            parentCourseId: courseId, 
            type: 'course_forum' 
        });

        if (!forumRoom) {
            forumRoom = new ChatRoom({
                type: 'course_forum',
                parentCourseId: courseId,
                metadata: { title: `${course.name} Forum` }
            });
            await forumRoom.save();
        }

        res.json({ chatRoomId: forumRoom._id });
    } catch (error) {
        res.status(500).json({ message: "Error loading forum" });
    }
});

// backend/src/routes/courses.js

router.get('/lecturer/:lecturerId/consultations', async (req, res) => {
    try {
        const { lecturerId } = req.params;
        console.log("🔍 Querying DB for Lecturer ID:", lecturerId);

        // We use .lean() to get a plain JavaScript object
        // We use .populate() to get the group name from the StudyGroup model
        const rooms = await ChatRoom.find({
            type: 'lecturer_consultation',
            'metadata.lecturerId': lecturerId
        })
        .populate('parentGroupId', 'name')
        .lean();

        console.log(`✅ Found ${rooms.length} rooms.`);

        // Format the data to match your Kotlin 'ConsultationRoom' data class
        const formattedRooms = rooms.map(room => ({
            chatRoomId: room._id.toString(),
            groupName: room.parentGroupId?.name || "Deleted Group",
            type: room.type,
            createdAt: room.createdAt ? room.createdAt.toString() : ""
        }));

        console.log("📦 Sending to App:", formattedRooms);
        res.json(formattedRooms);
    } catch (error) {
        console.error("❌ Route Error:", error);
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;