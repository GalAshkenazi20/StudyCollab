const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const path = require('path'); // Added for handling file paths
require('dotenv').config();

// IMPORTS
const studyGroupsRouter = require('./src/routes/studyGroups');
const authRouter = require('./src/routes/auth');
const coursesRouter = require('./src/routes/courses');
const notificationsRouter = require('./src/routes/notifications');
const messageRoutes = require('./src/routes/messages');
const assignmentsRouter = require('./src/routes/assignments');
const groupWorkRouter = require('./src/routes/groupWork');
const officeHoursRouter = require('./src/routes/office-hours'); // NEW
const submissionsRouter = require('./src/routes/submissions'); // NEW
const materialsRouter = require('./src/routes/materials'); // NEW

const app = express();
const PORT = process.env.PORT || 3000;

// CONNECTION LOGIC
const dbURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/studycollab';

mongoose.connect(dbURI)
  .then(() => {
    const connType = process.env.MONGODB_URI ? 'Atlas (Cloud)' : 'Localhost';
    console.log(`✅ Connected to MongoDB: ${connType}`);
  })
  .catch(err => {
    console.error('❌ MongoDB Connection Error:', err);
    process.exit(1);
  });

// MIDDLEWARE
app.use(express.json());
app.use(cors());

app.use('/uploads', express.static('uploads'));


// STATIC FOLDER FOR UPLOADS
// This allows the Android app to view PDFs via http://localhost:3000/uploads/...
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// ROUTES
app.use('/api/groups', studyGroupsRouter);
app.use('/api/courses', coursesRouter);
app.use('/auth', authRouter);
app.use('/api/notifications', notificationsRouter);
app.use('/api/messages', messageRoutes);
app.use('/api/assignments', assignmentsRouter);
app.use('/api/group-work', groupWorkRouter);

// NEW LECTURER & SCHEDULING ROUTES
app.use('/api/office-hours', officeHoursRouter); // Added for scheduler/booking
app.use('/api/submissions', submissionsRouter); // Added for grading
app.use('/api/materials', materialsRouter);     // Added for course slides/notes
app.use("/uploads", express.static("uploads"));

// Run deadline reminders every hour
const checkDeadlines = require('./src/jobs/deadlineReminder');
setInterval(checkDeadlines, 60 * 60 * 1000); // Every hour
checkDeadlines(); // Run once on startup

app.listen(PORT, () => {
  console.log(`🚀 API running on http://localhost:${PORT}`);
});