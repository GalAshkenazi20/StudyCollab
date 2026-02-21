const express = require('express');
const router = express.Router();
const StudyGroup = require('../models/StudyGroup');
const Notification = require('../models/Notification');

// 1. Create a New Study Group
router.post('/create', async (req, res) => {
    const { groupName, courseId, creatorId, purpose, memberIds } = req.body;

    try {
        // Validate: no lecturers in memberIds
        if (memberIds && memberIds.length > 0) {
            const User = require('../models/User');
            const members = await User.find({ _id: { $in: memberIds } });
            const lecturerInList = members.find(m => m.role === 'lecturer');
            if (lecturerInList) {
                return res.status(400).json({
                    message: "Lecturers cannot be added as study group members"
                });
            }
        }

        const initialMembers = [{
            userId: creatorId,
            role: 'admin',
            status: 'active',
            joinedAt: new Date()
        }];

        if (memberIds && Array.isArray(memberIds)) {
            memberIds.forEach(id => {
                if (id !== creatorId) {
                    initialMembers.push({
                        userId: id,
                        role: 'student',
                        status: 'active',
                        joinedAt: new Date()
                    });
                }
            });
        }

        const newGroup = new StudyGroup({
            name: groupName,
            courseId: courseId,
            purpose: purpose || 'general',
            members: initialMembers
        });

        const savedGroup = await newGroup.save();

        const populatedGroup = await StudyGroup.findById(savedGroup._id)
            .populate('members.userId', 'profile role university')
            .populate('courseId', 'name');

        // Notification logic
        if (memberIds && memberIds.length > 0) {
            const notifications = memberIds
                .filter(id => id !== creatorId)
                .map(userId => ({
                    userId: userId,
                    title: "New Study Group!",
                    message: `You've been invited to join ${groupName}.`,
                    type: "group_invite",
                    relatedId: savedGroup._id,
                    dedupeKey: `invite_${savedGroup._id}_${userId}`
                }));

            await Notification.insertMany(notifications, { ordered: false });
        }

        res.status(201).json(populatedGroup);

    } catch (error) {
        console.error("Error creating group:", error);
        res.status(500).json({ message: "Failed to create group", error: error.message });
    }
});

// 2. Get All Groups for a Specific User
router.get('/user/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const groups = await StudyGroup.find({ "members.userId": userId })
            .populate('members.userId', 'profile role university')
            .populate('courseId', 'name');

        res.json(groups);
    } catch (error) {
        console.error("Error fetching user groups:", error);
        res.status(500).json({ message: "Failed to fetch groups", error: error.message });
    }
});

// 3. Get all groups for a course (Lecturer oversight)
router.get('/course/:courseId', async (req, res) => {
    try {
        const groups = await StudyGroup.find({ courseId: req.params.courseId })
            .populate('members.userId', 'profile.fullName role');
        res.json(groups);
    } catch (error) {
        res.status(500).json({ message: "Failed to fetch course groups", error: error.message });
    }
});

// 4. Delete a Group (Includes Notifications)
router.delete('/:groupId', async (req, res) => {
    try {
        const { userId } = req.body;
        const group = await StudyGroup.findById(req.params.groupId);

        if (!group) return res.status(404).json({ message: "Group not found" });

        const requester = group.members.find(m => m.userId.toString() === userId);
        if (!requester || requester.role !== 'admin') {
            return res.status(403).json({ message: "Only admins can delete groups" });
        }

        const otherMembers = group.members.filter(m => m.userId.toString() !== userId);
        if (otherMembers.length > 0) {
            const notifications = otherMembers.map(member => ({
                userId: member.userId,
                title: "Group Deleted",
                message: `The study group "${group.name}" has been closed by the admin.`,
                type: "group_deleted",
                relatedId: group._id,
                dedupeKey: `deleted_${group._id}_${member.userId}_${Date.now()}`
            }));
            await Notification.insertMany(notifications, { ordered: false });
        }

        await StudyGroup.findByIdAndDelete(req.params.groupId);
        res.json({ message: "Group deleted successfully" });
    } catch (error) {
        res.status(500).json({ message: "Deletion failed", error: error.message });
    }
});

// 5. Get participants for a specific group (Populated)
router.get('/:groupId/participants', async (req, res) => {
    try {
        const group = await StudyGroup.findById(req.params.groupId)
            .populate('members.userId', 'profile role university');

        if (!group) return res.status(404).json({ message: "Group not found" });

        res.json(group.members);
    } catch (error) {
        console.error("Error fetching participants:", error);
        res.status(500).json({ message: "Server error" });
    }
});

// 6. Consultation with Lecturer - Create or get a "ghost group"
router.post('/consultation', async (req, res) => {
    const { originGroupId, lecturerId } = req.body;

    try {
        // Check if consultation group already exists
        let consultGroup = await StudyGroup.findOne({
            purpose: 'lecturer_consultation',
            _consultationOrigin: originGroupId,
            'members.userId': lecturerId
        });

        if (consultGroup) return res.json(consultGroup);

        // Fetch the original group to copy members
        const original = await StudyGroup.findById(originGroupId);
        if (!original) return res.status(404).json({ message: "Original group not found" });

        // Create consultation group with all original members + lecturer
        const allMembers = original.members.map(m => ({
            userId: m.userId,
            role: m.role,
            status: 'active',
            joinedAt: new Date()
        }));

        // Add lecturer as admin
        allMembers.push({
            userId: lecturerId,
            role: 'admin',
            status: 'active',
            joinedAt: new Date()
        });

        const newGroup = new StudyGroup({
            name: `Consultation: ${original.name}`,
            courseId: original.courseId,
            purpose: 'lecturer_consultation',
            members: allMembers,
            _consultationOrigin: originGroupId
        });

        const saved = await newGroup.save();

        // Notify the lecturer
        await new Notification({
            userId: lecturerId,
            title: "Consultation Request",
            message: `Group "${original.name}" wants to consult with you`,
            type: "consultation_request",
            relatedId: saved._id,
            dedupeKey: `consult_${saved._id}_${Date.now()}`
        }).save().catch(e => console.error(e));

        res.status(201).json(saved);
    } catch (error) {
        res.status(500).json({ message: "Failed to set up consultation", error: error.message });
    }
});

module.exports = router;