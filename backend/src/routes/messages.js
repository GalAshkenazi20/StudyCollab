const express = require('express');
const router = express.Router();
const Message = require('../models/Message');
const StudyGroup = require('../models/StudyGroup'); 
const Notification = require('../models/Notification'); 

// 1. Get messages for a group
router.get('/:groupId', async (req, res) => {
  try {
    const { groupId } = req.params;
    const messages = await Message.find({ groupId }).sort({ timestamp: 1 });
    res.json(messages);
  } catch (error) {
    console.error('Error fetching messages:', error);
    res.status(500).json({ message: 'Server Error' });
  }
});

// 2. Send a new message + Create Notifications
router.post('/', async (req, res) => {
  try {
    const { groupId, senderId, senderName, content } = req.body;

    // 1. יצירת ושמירת ההודעה
    const newMessage = new Message({
      groupId,
      senderId,
      senderName,
      content
    });
    const savedMessage = await newMessage.save();

    // =======================================================
    // 2. לוגיקת ההתראות המתוקנת עם מפתח ייחודי לכל הודעה
    // =======================================================
    try {
        const group = await StudyGroup.findById(groupId);
        
        if (group) {
            // סינון: שולחים לכל חברי הקבוצה חוץ מהשולח
            const recipients = group.members.filter(m => 
                m.userId.toString() !== senderId.toString()
            );

            // יצירת רשימת התראות עם dedupeKey ייחודי לכל הודעה
            const notifications = recipients.map(member => ({
                userId: member.userId,
                title: `New Message in ${group.name}`,
                message: `${senderName}: ${content.substring(0, 30)}${content.length > 30 ? '...' : ''}`,
                type: 'new_message',
                relatedId: groupId,
                // פתרון הבעיה: שילוב מזהה ההודעה והמשתמש כדי לעקוף את ה-Unique Index
                dedupeKey: `msg_${savedMessage._id}_${member.userId}`, 
                createdAt: new Date()
            }));

            if (notifications.length > 0) {
                // insertMany יצליח כעת כי לכל התראה יש dedupeKey שונה
                await Notification.insertMany(notifications);
                console.log(`Successfully created ${notifications.length} unique notifications`);
            }
        }
    } catch (notifError) {
        // אם יש שגיאת Duplicate Key (קוד 11000), זה אומר שההתראה כבר קיימת
        console.error("Notification logic error:", notifError.message);
    }
    // =======================================================

    res.status(201).json(savedMessage);
  } catch (error) {
    console.error('Error sending message:', error);
    res.status(500).json({ message: 'Server Error' });
  }
});

module.exports = router;