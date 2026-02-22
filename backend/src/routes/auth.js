const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs'); 

// GET /auth/lecturers — Get all lecturers (for student office hours screen)
router.get('/lecturers', async (req, res) => {
    try {
        const lecturers = await User.find({ role: 'lecturer' })
            .select('profile.fullName _id');
        res.json(lecturers);
    } catch (error) {
        res.status(500).json({ message: "Failed to fetch lecturers", error: error.message });
    }
});

// GET /auth/preferences/:userId — Get notification preferences
router.get('/preferences/:userId', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId)
            .select('notificationPreferences');
        if (!user) return res.status(404).json({ message: "User not found" });
        res.json(user.notificationPreferences || {});
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// PUT /auth/preferences/:userId — Update notification preferences
router.put('/preferences/:userId', async (req, res) => {
    try {
        const user = await User.findByIdAndUpdate(
            req.params.userId,
            { notificationPreferences: req.body },
            { new: true }
        );
        res.json(user.notificationPreferences);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.post('/login', async (req, res) => {
    const { email, password } = req.body;
    
    console.log(`Login attempt for: ${email}`); 

    try {
        const user = await User.findOne({ "university.email": email });

        if (!user) {
            console.log("User not found in DB");
            return res.status(404).json({ message: "User not found." });
        }

        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            console.log("Password mismatch");
            return res.status(401).json({ message: "Invalid password." });
        }

        res.json(user);

    } catch (error) {
        console.error("Login error:", error);
        res.status(500).json({ message: "Server error during login" });
    }
});

// Add this route near your other login routes
router.post('/microsoft-login', async (req, res) => {
    const { email } = req.body;
    
    console.log(`Microsoft login link attempt for: ${email}`); 

    try {
        // Find the user in your mock database using the university email returned by Microsoft
        const user = await User.findOne({ "university.email": email });

        if (!user) {
            console.log("Microsoft user not found in mock database");
            return res.status(404).json({ 
                message: "No university account found matching this Microsoft login." 
            });
        }

        // Return the full user object so the app inherits the mock user's data (role, groups, etc.)
        res.json(user);

    } catch (error) {
        console.error("Microsoft login error:", error);
        res.status(500).json({ message: "Server error during Microsoft authentication." });
    }
});

module.exports = router;