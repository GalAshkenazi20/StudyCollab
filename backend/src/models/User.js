const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  // Role matches the required enum for students and lecturers
  role: { 
    type: String, 
    enum: ["student", "lecturer", "ta", "admin"], 
    required: true,
    default: "student" 
  },
  
  // Microsoft Identity and University Data
  university: {
    // Linked to Microsoft Entra ID (externalUserId) when auth is integrated
    externalUserId: { type: String, unique: true, sparse: true }, 
    email: { type: String, required: true, unique: true }, // @msmail.uni.ac.il
    provider: { type: String, default: "Ariel University" }
  },

  // Added for current testing before Microsoft Auth is fully live
  password: { type: String, required: true }, 

  profile: {
    fullName: { type: String, required: true },
    avatarUrl: { type: String }
  },

  // Array of Course IDs for students (enrolled) or lecturers (teaching)
  enrolledCourses: [{ 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'Course' 
  }],

  settings: { type: Object }
}, { timestamps: true });

module.exports = mongoose.model("User", UserSchema);