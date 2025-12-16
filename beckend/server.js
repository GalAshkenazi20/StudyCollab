const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

// 1. ייבוא הראוטר שיצרנו
const studyGroupsRouter = require('./src/routes/studyGroups');

const app = express();
const PORT = process.env.PORT || 5000;

// חיבור ל-MongoDB
mongoose.connect('mongodb://localhost:27017/StudyCollabDB')
  .then(() => console.log('✅ Connected to MongoDB'))
  .catch(err => console.error('❌ MongoDB Connection Error:', err));

app.use(express.json());
app.use(cors());

// 2. הגדרת הנתיב - זו השורה שחסרה לך כנראה!
// היא אומרת: כל פנייה שמתחילה ב-/api/groups תלך לקובץ studyGroups.js
app.use('/api/groups', studyGroupsRouter);

app.get('/', (req, res) => {
  res.send('API is running...');
});

app.listen(PORT, () => {
  console.log(`🚀 API running on http://localhost:${PORT}`);
});