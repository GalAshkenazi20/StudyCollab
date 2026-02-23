const express = require('express');
const router = express.Router();
const Message = require('../models/Message');
const ChatRoom = require('../models/ChatRoom');
const StudyGroup = require('../models/StudyGroup'); 
const Notification = require('../models/Notification'); 

// Helper to resolve an ID to a ChatRoomId
const resolveRoom = async (targetId) => {
    // 1. Try finding as a ChatRoom ID
    let room = await ChatRoom.findById(targetId);
    if (room) return room;

    // 2. If not found, assume it's a StudyGroup ID and find/create the 'standard' room
    room = await ChatRoom.findOne({ parentGroupId: targetId, type: 'standard' });
    if (!room) {
        room = new ChatRoom({ type: 'standard', parentGroupId: targetId });
        await room.save();
    }
    return room;
};

// 1. Get messages for a specific room (or resolve from Group ID)
router.get('/:id', async (req, res) => {
  try {
    const room = await resolveRoom(req.params.id);
    const messages = await Message.find({ chatRoomId: room._id }).sort({ timestamp: 1 });
    res.json(messages);
  } catch (error) {
    res.status(500).json({ message: 'Server Error' });
  }
});

// 2. Send a new message
router.post('/', async (req, res) => {
  try {
    const { chatRoomId: targetId, senderId, senderName, content } = req.body;
    const room = await resolveRoom(targetId);

    const newMessage = new Message({
      chatRoomId: room._id,
      senderId,
      senderName,
      content
    });
    const savedMessage = await newMessage.save();

    // Notification Logic
    if (room.parentGroupId) {
        const group = await StudyGroup.findById(room.parentGroupId);
        if (group) {
            let recipientIds = group.members.map(m => m.userId.toString());
            
            // If consultation, include the lecturer in the loop
            if (room.type === 'lecturer_consultation' && room.metadata.lecturerId) {
                recipientIds.push(room.metadata.lecturerId.toString());
            }

            const notifications = recipientIds
                .filter(id => id !== senderId.toString())
                .map(userId => ({
                    userId,
                    title: room.type === 'standard' ? `Group: ${group.name}` : `Consultation: ${group.name}`,
                    message: `${senderName}: ${content.substring(0, 30)}...`,
                    type: 'new_message',
                    relatedId: room._id,
                    dedupeKey: `msg_${savedMessage._id}_${userId}`
                }));

            if (notifications.length > 0) await Notification.insertMany(notifications, { ordered: false });
        }
    }

    res.status(201).json(savedMessage);
  } catch (error) {
    res.status(500).json({ message: 'Server Error' });
  }
});

module.exports = router;