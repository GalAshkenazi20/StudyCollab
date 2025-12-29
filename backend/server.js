const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

// IMPORTS
const studyGroupsRouter = require('./src/routes/studyGroups');
const authRouter = require('./src/routes/auth');
const coursesRouter = require('./src/routes/courses'); // <--- ADD THIS

const app = express();
const PORT = process.env.PORT || 3000;

// Connect to MongoDB
mongoose.connect('mongodb://localhost:27017/studycollab')
  .then(() => console.log('✅ Connected to MongoDB'))
  .catch(err => console.error('❌ MongoDB Connection Error:', err));

app.use(express.json());
app.use(cors());

// ROUTES
app.use('/api/groups', studyGroupsRouter);
app.use('/api/courses', coursesRouter); // <--- ADD THIS
app.use('/auth', authRouter);

app.listen(PORT, () => {
  console.log(`🚀 API running on http://localhost:${PORT}`);
});