const express = require('express');
const router = express.Router();
const GroupAssignmentWork = require('../models/GroupAssignmentWork');
const StudyGroup = require('../models/StudyGroup');

// 1. Get or Initialize work for a group on a specific assignment
router.get('/:groupId/:assignmentId', async (req, res) => {
    try {
        let work = await GroupAssignmentWork.findOne({ 
            groupId: req.params.groupId, 
            assignmentId: req.params.assignmentId 
        }).populate('subTasks.assignedTo', 'profile.fullName');

        if (!work) {
            work = new GroupAssignmentWork({
                groupId: req.params.groupId,
                assignmentId: req.params.assignmentId,
                subTasks: []
            });
            await work.save();
        }
        res.json(work);
    } catch (error) {
        res.status(500).json({ message: "Server error" });
    }
});

// 2. Add/Edit sub-tasks (Admin Only)
router.post('/:workId/subtasks', async (req, res) => {
    const { adminId, title, assignedTo } = req.body;
    try {
        const work = await GroupAssignmentWork.findById(req.params.workId);
        const group = await StudyGroup.findById(work.groupId);
        
        // Check if requester is admin
        const member = group.members.find(m => m.userId.toString() === adminId);
        if (!member || member.role !== 'admin') {
            return res.status(403).json({ message: "Only group admins can manage tasks" });
        }

        work.subTasks.push({ title, assignedTo, status: 'todo' });
        await work.save();
        res.json(work);
    } catch (error) {
        res.status(500).json({ message: "Failed to add task" });
    }
});

// 3. Member marks task as done -> Pending Approval
router.patch('/:workId/subtasks/:subTaskId/complete', async (req, res) => {
    try {
        const work = await GroupAssignmentWork.findById(req.params.workId);
        const task = work.subTasks.id(req.params.subTaskId);
        task.status = 'pending_approval';
        task.completedAt = new Date();
        await work.save();
        res.json(work);
    } catch (error) {
        res.status(500).json({ message: "Failed to update task" });
    }
});

// 4. Admin approves task -> Completed
router.patch('/:workId/subtasks/:subTaskId/approve', async (req, res) => {
    const { adminId } = req.body;
    try {
        const work = await GroupAssignmentWork.findById(req.params.workId);
        const group = await StudyGroup.findById(work.groupId);
        
        const member = group.members.find(m => m.userId.toString() === adminId);
        if (!member || member.role !== 'admin') {
            return res.status(403).json({ message: "Unauthorized" });
        }

        const task = work.subTasks.id(req.params.subTaskId);
        task.status = 'completed';
        await work.save();
        res.json(work);
    } catch (error) {
        res.status(500).json({ message: "Approval failed" });
    }
});

module.exports = router;