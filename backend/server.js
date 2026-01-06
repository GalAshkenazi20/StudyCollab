const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config(); // 👈 1. Load your .env variables

// IMPORTS
const studyGroupsRouter = require('./src/routes/studyGroups');
const authRouter = require('./src/routes/auth');
const coursesRouter = require('./src/routes/courses');
const notificationsRouter = require('./src/routes/notifications');

const app = express();
const PORT = process.env.PORT || 3000;

// 2. Connection Logic: Use Atlas if available, otherwise local
const dbURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/studycollab';

mongoose.connect(dbURI)
  .then(() => {
    // Helpful log to know exactly WHICH database you are hitting
    const connType = process.env.MONGODB_URI ? 'Atlas (Cloud)' : 'Localhost';
    console.log(`✅ Connected to MongoDB: ${connType}`);
  })
  .catch(err => {
    console.error('❌ MongoDB Connection Error:', err);
    process.exit(1); // Stop the server if the DB connection fails
  });

app.use(express.json());
app.use(cors());

// ROUTES
app.use('/api/groups', studyGroupsRouter);
app.use('/api/courses', coursesRouter);
app.use('/auth', authRouter);
app.use('/api/notifications', notificationsRouter);

app.listen(PORT, () => {
  console.log(`🚀 API running on http://localhost:${PORT}`);
});