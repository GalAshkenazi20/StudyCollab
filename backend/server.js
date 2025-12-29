const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

// IMPORTS
const studyGroupsRouter = require('./src/routes/studyGroups');
const authRouter = require('./src/routes/auth'); // <--- ADD THIS

const app = express();
const PORT = process.env.PORT || 3000; // <--- CHANGED TO 3000 to match your Android Constants.kt

// Connect to MongoDB
mongoose.connect('mongodb://localhost:27017/studycollab') // Ensure DB name matches
  .then(() => console.log('✅ Connected to MongoDB'))
  .catch(err => console.error('❌ MongoDB Connection Error:', err));

app.use(express.json());
app.use(cors());

// ROUTES
app.use('/api/groups', studyGroupsRouter);
app.use('/auth', authRouter); // <--- ADD THIS (Matches the Android call: BASE_URL + "auth/login")

app.listen(PORT, () => {
  console.log(`🚀 API running on http://localhost:${PORT}`);
});