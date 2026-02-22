const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const StudyGroup = require('../models/StudyGroup');
const Course = require('../models/Course'); 
const User = require('../models/User');     
const Notification = require('../models/Notification');
const CourseMembership = require('../models/CourseMembership');


// =================================================================
// 1. Create a New Study Group
// =================================================================
router.post('/create', async (req, res) => {
    const { groupName, courseId, creatorId, purpose, memberIds } = req.body;

    try {
        // Validate: no lecturers in memberIds
        if (memberIds && memberIds.length > 0) {
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

            try {
                await Notification.insertMany(notifications, { ordered: false });
            } catch (notifError) {
                console.error("Notification error (non-fatal):", notifError);
            }
        }

        res.status(201).json(populatedGroup);

    } catch (error) {
        console.error("Error creating group:", error);
        res.status(500).json({ message: "Failed to create group", error: error.message });
    }
});

// =================================================================
// 2. Get All Groups for a Specific User
// =================================================================
router.get('/user/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const groups = await StudyGroup.find({ "members.userId": userId })
            .populate('members.userId', 'name email profile')
            .populate('courseId', 'name code');

        res.json(groups);
    } catch (error) {
        console.error("Error fetching user groups:", error);
        res.status(500).json({ message: "Failed to fetch groups", error: error.message });
    }
});

// =================================================================
// 3. Get all groups for a course (Lecturer oversight)
// =================================================================
router.get('/course/:courseId', async (req, res) => {
    try {
        const groups = await StudyGroup.find({ courseId: req.params.courseId })
            .populate('members.userId', 'name email');
        res.json(groups);
    } catch (error) {
        res.status(500).json({ message: "Failed to fetch course groups", error: error.message });
    }
});

// =================================================================
// 4. Delete a Group (Includes Notifications)
// =================================================================
router.delete('/:groupId', async (req, res) => {
    try {
        const { userId } = req.body;
        const group = await StudyGroup.findById(req.params.groupId);

        if (!group) return res.status(404).json({ message: "Group not found" });

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
            try {
                await Notification.insertMany(notifications, { ordered: false });
            } catch (e) { console.error(e); }
        }

        await StudyGroup.findByIdAndDelete(req.params.groupId);
        res.json({ message: "Group deleted successfully" });
    } catch (error) {
        res.status(500).json({ message: "Deletion failed", error: error.message });
    }
});

// =================================================================
// 5. Get participants for a specific group
// =================================================================
router.get('/:groupId/participants', async (req, res) => {
    try {
        const group = await StudyGroup.findById(req.params.groupId)
            .populate('members.userId', 'name email profile role university');

        if (!group) return res.status(404).json({ message: "Group not found" });

        res.json(group.members);
    } catch (error) {
        console.error("Error fetching participants:", error);
        res.status(500).json({ message: "Server error" });
    }
});

// =================================================================
// 6. OPEN CONSULTATION (Fixed + Backward Compatible)
// =================================================================
router.post('/:groupId/consult', async (req, res) => {
  try {
    const { groupId } = req.params;

    const originalGroup = await StudyGroup.findById(groupId);
    if (!originalGroup) return res.status(404).json({ message: "Group not found" });

    const course = await Course.findById(originalGroup.courseId);
    if (!course) return res.status(404).json({ message: "Course not found" });

    // ✅ Find lecturer (new field OR fallback to memberships)
    let lecturerId = course.lecturer;

    if (!lecturerId) {
      const membership = await CourseMembership.findOne({
        courseId: course._id,
        role: 'lecturer'
      });
      lecturerId = membership?.userId;
    }

    if (!lecturerId) {
      console.error(`Course '${course.name}' has no lecturer (missing lecturer + no membership).`);
      return res.status(404).json({ message: "Lecturer not found for this course" });
    }

    // ✅ Check if consultation group already exists
    const consultName = `Consultation: ${originalGroup.name}`;
    let consultGroup = await StudyGroup.findOne({
      name: consultName,
      courseId: course._id,
      purpose: 'lecturer_consultation'
    });

    if (consultGroup) {
      // ✅ חשוב: להחזיר תמיד String
      return res.json({ consultationGroupId: consultGroup._id.toString() });
    }

    // ✅ Build members list
    const newMembers = originalGroup.members.map(m => ({
      userId: m.userId,
      role: 'member',
      status: 'active',
      joinedAt: new Date()
    }));

    const lecturerAlreadyInside = newMembers.some(m => m.userId.toString() === lecturerId.toString());
    if (!lecturerAlreadyInside) {
      newMembers.push({
        userId: lecturerId,
        role: 'admin',
        status: 'active',
        joinedAt: new Date()
      });
    }

    consultGroup = new StudyGroup({
      name: consultName,
      courseId: course._id,
      description: `Official consultation chat for group "${originalGroup.name}" with the lecturer.`,
      capacity: newMembers.length + 5,
      members: newMembers,
      purpose: 'lecturer_consultation'
    });

    const savedGroup = await consultGroup.save();

    try {
      await new Notification({
        userId: lecturerId,
        title: "New Consultation",
        message: `Study group "${originalGroup.name}" started a consultation chat with you.`,
        type: "consultation_start",
        relatedId: savedGroup._id,
        dedupeKey: `consult_start_${savedGroup._id}`
      }).save();
    } catch (e) {
      console.error("Failed to notify lecturer (non-fatal):", e);
    }

    // ✅ חשוב: להחזיר תמיד String
    return res.status(201).json({ consultationGroupId: savedGroup._id.toString() });

  } catch (error) {
    console.error("Error creating consultation group:", error);
    return res.status(500).json({
      message: "Failed to open consultation",
      error: error.message
    });
  }
});

module.exports = router;