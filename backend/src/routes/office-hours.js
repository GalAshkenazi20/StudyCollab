const express = require('express');
const router = express.Router();
const OfficeHourSlot = require('../models/OfficeHourSlot');

// 1. GET all slots for a specific lecturer (Used by both roles)
router.get('/lecturer/:lecturerId', async (req, res) => {
    try {
        const slots = await OfficeHourSlot.find({ lecturerId: req.params.lecturerId })
            .populate('bookedBy', 'profile.fullName') // Shows student name if booked
            .sort({ dayOfWeek: 1, startTime: 1 });
        res.json(slots);
    } catch (error) {
        res.status(500).json({ message: "Error fetching office hours", error: error.message });
    }
});

// 2. POST a new availability slot (Lecturer only)
router.post('/create', async (req, res) => {
    try {
        const newSlot = new OfficeHourSlot({
            lecturerId: req.body.lecturerId,
            dayOfWeek: req.body.dayOfWeek, // e.g., "Monday"
            startTime: req.body.startTime, // e.g., "10:00"
            endTime: req.body.endTime,     // e.g., "11:00"
            meetingLink: req.body.meetingLink || ""
        });
        const savedSlot = await newSlot.save();
        res.status(201).json(savedSlot);
    } catch (error) {
        res.status(400).json({ message: "Error creating slot", error: error.message });
    }
});

// 3. PATCH to book a slot (Student only)
router.patch('/book/:slotId', async (req, res) => {
    try {
        const slot = await OfficeHourSlot.findById(req.params.slotId);
        
        if (!slot) return res.status(404).json({ message: "Slot not found" });
        if (slot.isBooked) return res.status(400).json({ message: "Slot is already booked" });

        slot.isBooked = true;
        slot.bookedBy = req.body.studentId; // Passed from Android UserSession
        await slot.save();

        res.json({ message: "Slot booked successfully", slot });
    } catch (error) {
        res.status(500).json({ message: "Booking failed", error: error.message });
    }
});

// 4. DELETE a slot (Lecturer only)
router.delete('/:slotId', async (req, res) => {
    try {
        const slot = await OfficeHourSlot.findById(req.params.slotId);
        if (!slot) return res.status(404).json({ message: "Slot not found" });
        
        await OfficeHourSlot.findByIdAndDelete(req.params.slotId);
        res.json({ message: "Slot removed" });
    } catch (error) {
        res.status(500).json({ message: "Error deleting slot", error: error.message });
    }
});

// 5. GET available (unbooked) slots for a lecturer (Student view)
router.get('/available/:lecturerId', async (req, res) => {
    try {
        const slots = await OfficeHourSlot.find({
            lecturerId: req.params.lecturerId,
            isBooked: false
        }).sort({ dayOfWeek: 1, startTime: 1 });
        res.json(slots);
    } catch (error) {
        res.status(500).json({ message: "Error fetching available slots", error: error.message });
    }
});

module.exports = router;