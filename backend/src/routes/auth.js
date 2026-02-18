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

module.exports = router;