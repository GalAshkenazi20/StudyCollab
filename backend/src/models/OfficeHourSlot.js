const mongoose = require('mongoose');

const OfficeHourSlotSchema = new mongoose.Schema({
    lecturerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    dayOfWeek: { type: String, required: true }, // e.g., "Monday"
    startTime: { type: String, required: true }, // e.g., "10:00"
    endTime: { type: String, required: true },   // e.g., "11:00"
    isBooked: { type: Boolean, default: false },
    bookedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null },
    meetingLink: { type: String, default: "" } // Optional: For Zoom/Google Meet
});

module.exports = mongoose.model('OfficeHourSlot', OfficeHourSlotSchema);