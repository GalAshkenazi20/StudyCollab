const express = require('express');
const router = express.Router();
const Submission = require('../models/Submission');

// 1. Get all submissions for a specific assignment (Lecturer View)
router.get('/assignment/:assignmentId', async (req, res) => {
    try {
        const submissions = await Submission.find({ assignmentId: req.params.assignmentId })
            .populate('groupId', 'groupName') // Shows the name of the Study Group
            .sort({ submittedAt: -1 });
        res.json(submissions);
    } catch (error) {
        res.status(500).json({ message: "Error fetching submissions" });
    }
});

// 2. Update Grade and Feedback for a group
router.patch('/:submissionId/grade', async (req, res) => {
    const { grade, feedback } = req.body;
    try {
        const updatedSubmission = await Submission.findByIdAndUpdate(
            req.params.submissionId,
            { grade, feedback },
            { new: true }
        );
        if (!updatedSubmission) return res.status(404).json({ message: "Submission not found" });
        res.json(updatedSubmission);
    } catch (error) {
        res.status(500).json({ message: "Failed to update grade" });
    }
});

module.exports = router;