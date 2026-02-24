const express = require('express');
const router = express.Router();
const StudyGroup = require('../models/StudyGroup');
const Notification = require('../models/Notification');
const Course = require('../models/Course');
const User = require('../models/User');
const ChatRoom = require('../models/ChatRoom');

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

// NEW: Get or Create the Standard (Student-only) Chat Room
router.get('/:groupId/standard-room', async (req, res) => {
    try {
        let room = await ChatRoom.findOne({ parentGroupId: req.params.groupId, type: 'standard' });
        if (!room) {
            room = new ChatRoom({ type: 'standard', parentGroupId: req.params.groupId });
            await room.save();
        }
        res.json({ chatRoomId: room._id });
    } catch (error) {
        res.status(500).json({ message: "Server error" });
    }
});

// REFACTORED: Open Consultation Room
router.post('/:groupId/consult', async (req, res) => {
  try {
    const { groupId } = req.params;
    const group = await StudyGroup.findById(groupId);
    if (!group) return res.status(404).json({ message: "Group not found" });

    const course = await Course.findById(group.courseId).populate('lecturer');
    
    // Find lecturer logic
    let lecturerId = course.lecturer?._id;
    if (!lecturerId) {
      const mem = await CourseMembership.findOne({ courseId: course._id, role: 'lecturer' });
      lecturerId = mem?.userId;
    }

    const lecturer = await User.findById(lecturerId);
    if (!lecturer) return res.status(404).json({ message: "Lecturer not found" });

    // Find or Create ChatRoom (Prevents the "Ghost Group" symptom)
    let chatRoom = await ChatRoom.findOne({ parentGroupId: groupId, type: 'lecturer_consultation' });

    if (!chatRoom) {
      chatRoom = new ChatRoom({
        type: 'lecturer_consultation',
        parentGroupId: groupId,
        metadata: {
          lecturerId: lecturerId,
          lecturerName: lecturer.profile.fullName
        }
      });
      await chatRoom.save();
    }

    // Return the specific keys the Android ViewModel expects
    return res.json({
      chatRoomId: chatRoom._id.toString(),
      lecturerName: chatRoom.metadata.lecturerName || "Lecturer"
    });

  } catch (error) {
    res.status(500).json({ message: "Server error" });
  }
});

router.post('/:groupId/peer-consult/:targetGroupId', async (req, res) => {
    try {
        const { groupId, targetGroupId } = req.params;

        // 1. Force IDs to strings before sorting to prevent "Server Error"
        const id1 = groupId.toString();
        const id2 = targetGroupId.toString();
        const pairKey = [id1, id2].sort().join('_');

        console.log(`🔍 Resolving Peer Room for Key: ${pairKey}`);

        let chatRoom = await ChatRoom.findOne({ 
            type: 'peer_group_consultation', 
            'metadata.pairKey': pairKey 
        });

        if (!chatRoom) {
            console.log("🆕 Creating new Peer Room");
            chatRoom = new ChatRoom({
                type: 'peer_group_consultation',
                metadata: { 
                    pairKey: pairKey, 
                    groupIds: [id1, id2] // Storing as strings for easier matching later
                }
            });
            await chatRoom.save();
        }

        res.json({ chatRoomId: chatRoom._id.toString() });
    } catch (error) {
        console.error("❌ Peer Consult Route Error:", error); // Check Node logs for specific error
        res.status(500).json({ message: "Server error", details: error.message });
    }
});



module.exports = router;