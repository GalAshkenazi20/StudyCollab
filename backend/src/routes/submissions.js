const express = require('express');
const router = express.Router();
const Submission = require('../models/Submission');
const Assignment = require('../models/Assignment');
const Notification = require('../models/Notification');
const User = require('../models/User'); 

const multer = require('multer');
const path = require('path');
const fs = require('fs');

const uploadDir = 'uploads/submissions/';
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, uploadDir),
    filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});

const upload = multer({ storage });

// POST with file — Student submits assignment with PDF
router.post('/upload', upload.single('file'), async (req, res) => {
    try {
        const newSubmission = new Submission({
            assignmentId: req.body.assignmentId,
            groupId: req.body.groupId || null,
            courseId: req.body.courseId,
            fileUrl: `/uploads/submissions/${req.file.filename}`
        });
        const saved = await newSubmission.save();
        res.status(201).json(saved);
    } catch (error) {
        res.status(500).json({ message: "Submission failed", error: error.message });
    }
});

// POST: Student Submits an Assignment
router.post('/', async (req, res) => {
    try {
        const { assignmentId, studentId, groupId, fileUrl, comments } = req.body;

        const newSubmission = new Submission({
            assignmentId,
            studentId,
            groupId,
            fileUrl,
            comments,
            submittedAt: new Date()
        });

        const savedSubmission = await newSubmission.save();

        // Notify the lecturer
        try {
            const assignment = await Assignment.findById(assignmentId);
            const student = await User.findById(studentId);

            if (assignment && student) {
                const notification = new Notification({
                    userId: assignment.createdBy,
                    title: "New Submission",
                    message: `${student.profile?.fullName || 'A student'} submitted: ${assignment.title}`,
                    type: "submission",
                    relatedId: savedSubmission._id,
                    createdAt: new Date()
                });
                await notification.save();
            }
        } catch (notifError) {
            console.error("Error sending notification to lecturer:", notifError);
        }

        res.status(201).json(savedSubmission);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Failed to submit assignment" });
    }
});

// GET: Get all submissions for assignment (Lecturer View)
router.get('/assignment/:assignmentId', async (req, res) => {
    try {
        const submissions = await Submission.find({ assignmentId: req.params.assignmentId })
            .populate('groupId', 'name')
            .sort({ submittedAt: -1 });
        res.json(submissions);
    } catch (error) {
        res.status(500).json({ message: "Error fetching submissions" });
    }
});

// PATCH: Update Grade + notify student
router.patch('/:submissionId/grade', async (req, res) => {
    const { grade, feedback } = req.body;
    try {
        const updatedSubmission = await Submission.findByIdAndUpdate(
            req.params.submissionId,
            { grade, feedback, gradedAt: new Date() },
            { new: true }
        );

        if (!updatedSubmission) return res.status(404).json({ message: "Submission not found" });

        // Notify the student about the grade
        try {
            const notification = new Notification({
                userId: updatedSubmission.studentId,
                title: "Grade Posted",
                message: `You received a grade: ${grade}`,
                type: "grade",
                relatedId: updatedSubmission.assignmentId,
                createdAt: new Date()
            });
            await notification.save();
        } catch (notifError) {
            console.error("Error sending grade notification:", notifError);
        }

        res.json(updatedSubmission);
    } catch (error) {
        res.status(500).json({ message: "Failed to update grade" });
    }
});

module.exports = router;