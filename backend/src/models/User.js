const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  role: { type: String, enum: ["student", "lecturer", "ta", "admin"], required: true },
  university: {
    provider: { type: String, required: true },
    externalUserId: { type: String, required: true, unique: true }, // e.g., Student ID
    email: { type: String, required: true, unique: true },
  },
  profile: {
    fullName: { type: String, required: true },
    avatarUrl: { type: String }
  },
  settings: { type: Object }
}, { timestamps: true });

module.exports = mongoose.model("User", UserSchema);