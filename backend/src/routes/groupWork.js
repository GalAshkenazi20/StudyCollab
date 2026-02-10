const express = require('express');
const router = express.Router();
const GroupAssignmentWork = require('../models/GroupAssignmentWork');
const StudyGroup = require('../models/StudyGroup');
const mongoose = require('mongoose');

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
        console.error("GET Work Error:", error);
        res.status(500).json({ message: "Server error" });
    }
});

// 2. Add sub-tasks (Handles "new" work initialization)
router.post('/:workId/subtasks', async (req, res) => {
    const { workId } = req.params;
    const { adminId, title, assignedTo, groupId, assignmentId } = req.body;

    try {
        let work;

        // FIXED: Logic to handle initialization if workId is "new"
        if (workId === 'new') {
            if (!groupId || !assignmentId) {
                return res.status(400).json({ message: "Missing groupId or assignmentId for new work initialization" });
            }
            
            // Use findOneAndUpdate with upsert to avoid duplicate key errors
            work = await GroupAssignmentWork.findOneAndUpdate(
                { groupId, assignmentId },
                { $setOnInsert: { subTasks: [] } },
                { upsert: true, new: true }
            );
        } else {
            work = await GroupAssignmentWork.findById(workId);
        }

        if (!work) return res.status(404).json({ message: "Group work record not found" });

        const group = await StudyGroup.findById(work.groupId);
        if (!group) return res.status(404).json({ message: "Study group not found" });

        // Check if requester is admin
        const member = group.members.find(m => m.userId.toString() === adminId);
        if (!member || member.role !== 'admin') {
            return res.status(403).json({ message: "Only group admins can manage tasks" });
        }

        // Add the task
        work.subTasks.push({ 
            title, 
            assignedTo: assignedTo || null, 
            status: 'todo' 
        });
        
        await work.save();
        res.json(work);
    } catch (error) {
        console.error("POST Subtask Error:", error);
        res.status(500).json({ message: "Failed to add task", error: error.message });
    }
});

// 3. Member marks task as done -> Pending Approval
router.patch('/:workId/subtasks/:subTaskId/complete', async (req, res) => {
    try {
        const work = await GroupAssignmentWork.findById(req.params.workId);
        const task = work.subTasks.id(req.params.subTaskId);
        if (!task) return res.status(404).json({ message: "Task not found" });

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
        if (!task) return res.status(404).json({ message: "Task not found" });

        task.status = 'completed';
        await work.save();
        res.json(work);
    } catch (error) {
        res.status(500).json({ message: "Approval failed" });
    }
});

module.exports = router;