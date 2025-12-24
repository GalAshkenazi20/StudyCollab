const express = require('express');
const router = express.Router();
const User = require('../models/User');

// POST /auth/login
router.post('/login', async (req, res) => {
    const { email, password } = req.body;
    
    console.log(`Login attempt for: ${email}`); // Debug log

    try {
        // 1. Find the user by their university email
        const user = await User.findOne({ "university.email": email });

        if (!user) {
            return res.status(404).json({ message: "User not found. Please sign up first." });
        }

        // 2. (Optional) Check password here if you added a password field later.
        // For now, we trust the email exists for the prototype.

        // 3. Return the user object (Android needs this to navigate to Home)
        res.json(user);

    } catch (error) {
        console.error("Login error:", error);
        res.status(500).json({ message: "Server error during login" });
    }
});

module.exports = router;