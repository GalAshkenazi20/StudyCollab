const express = require('express');
const router = express.Router();
const StudyGroup = require('../models/StudyGroup');
// const Conversation = require('../models/Conversation'); // נצטרך את זה בהמשך ליצירת צ'אט

// 1. פתיחת קבוצה חדשה
// תרחיש: סטודנט לוחץ "פתח קבוצה" -> הופך למנהל אוטומטית
router.post('/create', async (req, res) => {
    const { groupName, courseId, creatorId, description, purpose } = req.body;

    try {
        // שלב א': יצירת אובייקט הקבוצה
        const newGroup = new StudyGroup({
            name: groupName,
            courseId: courseId,
            description: description,
            purpose: purpose || 'general', // לפי ה-Use Case
            members: [{
                userId: creatorId,
                role: 'admin', // המגדיר הוא המנהל
                status: 'active',
                joinedAt: new Date()
            }]
        });

        // הערה: כאן בעתיד תוסיף לוגיקה ליצירת מסמך ב-conversations
        // const newChat = await Conversation.create({ ... });
        // newGroup.chatId = newChat._id;

        const savedGroup = await newGroup.save();

        res.status(201).json({
            message: "Group created successfully",
            group: savedGroup
        });

    } catch (error) {
        console.error("Error creating group:", error);
        res.status(500).json({ message: "Failed to create group", error: error.message });
    }
});

// 2. הוספת משתתפים לקבוצה
// תרחיש: מנהל הקבוצה בוחר סטודנטים ולוחץ "הוסף"
router.post('/add-members', async (req, res) => {
    const { groupId, newMemberIds, requesterId } = req.body; 
    // requesterId = מי שמבצע את הבקשה (כדי לוודא שהוא אדמין אם צריך)

    try {
        const group = await StudyGroup.findById(groupId);
        if (!group) return res.status(404).json({ message: "Group not found" });

        // בדיקה אופציונלית: האם המבקש הוא חבר בקבוצה/מנהל?
        // כרגע נשאיר פתוח לפי הדרישה הפשוטה שלך

        // סינון כפילויות
        const existingIds = group.members.map(m => m.userId.toString());
        const membersToAdd = newMemberIds
            .filter(id => !existingIds.includes(id))
            .map(userId => ({
                userId: userId,
                role: 'student',
                // אם רוצים לממש הזמנה: status: 'invited'
                // אם רוצים הוספה ישירה (כמו בתיאור שלך): status: 'active'
                status: 'active', 
                joinedAt: new Date()
            }));

        if (membersToAdd.length === 0) {
            return res.status(400).json({ message: "No new members to add (all exist)" });
        }

        // דחיפה למערך
        group.members.push(...membersToAdd);
        await group.save();

        res.status(200).json({
            message: `Successfully added ${membersToAdd.length} members`,
            updatedGroup: group
        });

    } catch (error) {
        console.error("Error adding members:", error);
        res.status(500).json({ message: "Failed to add members", error: error.message });
    }
});

module.exports = router;