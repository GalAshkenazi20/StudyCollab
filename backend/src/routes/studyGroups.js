const express = require('express');
const router = express.Router();
const StudyGroup = require('../models/StudyGroup');

// 1. Create a New Study Group
// Scenario: A student fills in details, selects classmates, and clicks "Create Group"
router.post('/create', async (req, res) => {
    // Included memberIds to allow adding classmates selected in the CreateGroupScreen
    const { groupName, courseId, creatorId, description, purpose, memberIds } = req.body;

    try {
        // Prepare initial member list - the creator is assigned the 'admin' role
        const initialMembers = [{
            userId: creatorId,
            role: 'admin',
            status: 'active',
            joinedAt: new Date()
        }];

        // Add other students invited from the selection list in the app
        if (memberIds && Array.isArray(memberIds)) {
            memberIds.forEach(id => {
                // Prevent duplicating the creator in the members list
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
            description: description || "",
            purpose: purpose || 'general',
            members: initialMembers
        });

        const savedGroup = await newGroup.save();

        // Return the saved object directly so the Android app can process it
        res.status(201).json(savedGroup);

    } catch (error) {
        console.error("Error creating group:", error);
        res.status(500).json({ message: "Failed to create group", error: error.message });
    }
});

// 2. Get All Groups for a Specific User
// This is the critical missing route needed for StudyGroupScreen to display real data
router.get('/user/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        
        // Find groups where the userId exists within the members array
        const groups = await StudyGroup.find({ "members.userId": userId })
            .populate('courseId'); // Populate full course details if needed

        res.json(groups);
    } catch (error) {
        console.error("Error fetching user groups:", error);
        res.status(500).json({ message: "Failed to fetch groups", error: error.message });
    }
});

// 3. Add Members to an Existing Group
router.post('/add-members', async (req, res) => {
    const { groupId, newMemberIds } = req.body;

    try {
        const group = await StudyGroup.findById(groupId);
        if (!group) return res.status(404).json({ message: "Group not found" });

        const existingIds = group.members.map(m => m.userId.toString());
        
        const membersToAdd = newMemberIds
            .filter(id => !existingIds.includes(id))
            .map(userId => ({
                userId: userId,
                role: 'student',
                status: 'active', 
                joinedAt: new Date()
            }));

        if (membersToAdd.length === 0) {
            return res.status(400).json({ message: "No new members to add" });
        }

        group.members.push(...membersToAdd);
        await group.save();

        res.status(200).json(group);

    } catch (error) {
        console.error("Error adding members:", error);
        res.status(500).json({ message: "Failed to add members", error: error.message });
    }
});

module.exports = router;