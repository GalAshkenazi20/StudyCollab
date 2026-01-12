const express = require('express');
const router = express.Router();
const Message = require('../models/Message');

// 1. קבלת כל ההודעות של קבוצה מסוימת (לפי Group ID)
// GET /api/messages/:groupId
router.get('/:groupId', async (req, res) => {
  try {
    const { groupId } = req.params;
    
    // שליפת הודעות וסידור מהישן לחדש (כרונולוגי)
    const messages = await Message.find({ groupId }).sort({ timestamp: 1 });
    
    res.json(messages);
  } catch (error) {
    console.error('Error fetching messages:', error);
    res.status(500).json({ message: 'Server Error' });
  }
});

// 2. שליחת הודעה חדשה
// POST /api/messages
router.post('/', async (req, res) => {
  try {
    const { groupId, senderId, senderName, content } = req.body;

    // יצירת הודעה חדשה
    const newMessage = new Message({
      groupId,
      senderId,
      senderName,
      content
    });

    // שמירה בדאטה-בייס
    const savedMessage = await newMessage.save();

    res.status(201).json(savedMessage);
  } catch (error) {
    console.error('Error sending message:', error);
    res.status(500).json({ message: 'Server Error' });
  }
});

module.exports = router;