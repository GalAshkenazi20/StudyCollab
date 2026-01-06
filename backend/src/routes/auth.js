const express = require('express');
const router = express.Router();
const User = require('../models/User');

// POST /auth/login
router.post('/login', async (req, res) => {
    const { email, password } = req.body;
    
    console.log(`Login attempt for: ${email}`); 

    try {
        // 1. Find the user by their university email
        const user = await User.findOne({ "university.email": email });

        if (!user) {
            return res.status(404).json({ message: "User not found." });
        }

        // 2. CHECK PASSWORD
        // Since you seeded with "123456", we compare the plain text here.
        // Once you move to Microsoft Auth, this whole block will change.
        if (user.password !== password) {
            return res.status(401).json({ message: "Invalid password." });
        }

        // 3. Return the user object
        res.json(user);

    } catch (error) {
        console.error("Login error:", error);
        res.status(500).json({ message: "Server error during login" });
    }
});

module.exports = router;