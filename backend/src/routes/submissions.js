const express = require('express');
const router = express.Router();
const Submission = require('../models/Submission');
// --- הוספנו את המודלים שחייבים בשביל ההתראות ---
const Assignment = require('../models/Assignment');
const Notification = require('../models/Notification');
const User = require('../models/User'); 

// =================================================================
// 1. POST: Student Submits an Assignment (יצירת הגשה + התראה למרצה)
// =================================================================
router.post('/', async (req, res) => {
    try {
        // מקבלים את פרטי ההגשה מהאפליקציה
        const { assignmentId, studentId, groupId, fileUrl, comments } = req.body;

        const newSubmission = new Submission({
            assignmentId,
            studentId,
            groupId, // אופציונלי, אם זה הגשה קבוצתית
            fileUrl,
            comments,
            submittedAt: new Date()
        });

        const savedSubmission = await newSubmission.save();

        // --- התראה למרצה (Lecturer Notification) ---
        try {
            // 1. מוצאים את המטלה כדי לדעת מי המרצה שיצר אותה
            const assignment = await Assignment.findById(assignmentId);
            // 2. מוצאים את הסטודנט כדי לדעת את שמו
            const student = await User.findById(studentId);

            if (assignment && student) {
                // יצירת ההתראה
                const notification = new Notification({
                    userId: assignment.createdBy, // המזהה של המרצה
                    title: "New Submission",
                    message: `${student.name} submitted: ${assignment.title}`,
                    type: "submission",
                    relatedId: savedSubmission._id,
                    createdAt: new Date()
                });

                await notification.save();
                console.log(`🔔 Notification sent to lecturer about submission from ${student.name}`);
            }
        } catch (notifError) {
            console.error("Error sending notification to lecturer:", notifError);
        }
        // ---------------------------------------------

        res.status(201).json(savedSubmission);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Failed to submit assignment" });
    }
});

// =================================================================
// 2. GET: Get all submissions for assignment (Lecturer View)
// =================================================================
router.get('/assignment/:assignmentId', async (req, res) => {
    try {
        const submissions = await Submission.find({ assignmentId: req.params.assignmentId })
            .populate('groupId', 'groupName') // מראה את שם הקבוצה (אם יש)
            .populate('studentId', 'name email') // מראה את פרטי הסטודנט
            .sort({ submittedAt: -1 });
        res.json(submissions);
    } catch (error) {
        res.status(500).json({ message: "Error fetching submissions" });
    }
});

// =================================================================
// 3. PATCH: Update Grade (מתן ציון + התראה לסטודנט)
// =================================================================
router.patch('/:submissionId/grade', async (req, res) => {
    const { grade, feedback } = req.body;
    try {
        const updatedSubmission = await Submission.findByIdAndUpdate(
            req.params.submissionId,
            { grade, feedback, gradedAt: new Date() },
            { new: true }
        );

        if (!updatedSubmission) return res.status(404).json({ message: "Submission not found" });

        // --- התראה לסטודנט (Student Notification) ---
        try {
            // שולחים התראה לסטודנט שהגיש את העבודה
            const notification = new Notification({
                userId: updatedSubmission.studentId,
                title: "Grade Posted",
                message: `You received a grade: ${grade}`,
                type: "grade",
                relatedId: updatedSubmission.assignmentId,
                createdAt: new Date()
            });

            await notification.save();
            console.log(`🔔 Notification sent to student regarding grade`);
        } catch (notifError) {
            console.error("Error sending grade notification:", notifError);
        }
        // --------------------------------------------

        res.json(updatedSubmission);
    } catch (error) {
        res.status(500).json({ message: "Failed to update grade" });
    }
});

module.exports = router;