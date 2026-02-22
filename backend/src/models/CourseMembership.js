const mongoose = require('mongoose');

const CourseMembershipSchema = new mongoose.Schema(
  {
    courseId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Course',
      required: true
    },
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true
    },

    // ✅ enforce allowed roles + normalize
    role: {
      type: String,
      enum: ['student', 'lecturer', 'ta', 'admin'],
      default: 'student',
      trim: true,
      lowercase: true
    },

    // ✅ enforce allowed status values
    status: {
      type: String,
      enum: ['active', 'pending', 'inactive', 'banned'],
      default: 'active',
      trim: true,
      lowercase: true
    }
  },
  {
    timestamps: true
  }
);

// one membership per user per course
CourseMembershipSchema.index({ courseId: 1, userId: 1 }, { unique: true });

// helpful query indexes (optional but useful)
CourseMembershipSchema.index({ courseId: 1, role: 1 });
CourseMembershipSchema.index({ userId: 1, role: 1 });

module.exports = mongoose.model(
  'CourseMembership',
  CourseMembershipSchema,
  'course_memberships'
);