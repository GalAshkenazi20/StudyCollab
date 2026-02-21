const express = require("express");
const router = express.Router();
const Assignment = require("../models/Assignment");

const multer = require("multer");
const path = require("path");
const fs = require("fs");

const uploadDir = "uploads/assignments/";
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => cb(null, Date.now() + "-" + file.originalname),
});

const upload = multer({ storage });

// POST /api/assignments/upload — Create assignment with file upload
router.post("/upload", upload.single("file"), async (req, res) => {
  try {
    const newAssignment = new Assignment({
      courseId: req.body.courseId,
      title: req.body.title,
      description: req.body.description || "",
      fileUrl: req.file ? `/uploads/assignments/${req.file.filename}` : null,
      dueAt: req.body.dueAt,
      createdBy: req.body.creatorId,
    });
    await newAssignment.save();
    res.status(201).json(newAssignment);
  } catch (error) {
    res
      .status(500)
      .json({ message: "Failed to create assignment", error: error.message });
  }
});

// 1. Create a course assignment (Lecturer)
router.post("/", async (req, res) => {
  try {
    const { courseId, title, description, fileUrl, dueAt, creatorId } =
      req.body;
    const newAssignment = new Assignment({
      courseId,
      title,
      description,
      fileUrl,
      dueAt,
      createdBy: creatorId,
    });
    await newAssignment.save();
    // After saving the assignment, notify all students in the course
    const CourseMembership = require("../models/CourseMembership");
    const Notification = require("../models/Notification");

    const memberships = await CourseMembership.find({
      courseId,
      role: "student",
    });
    const notifications = memberships.map((m) => ({
      userId: m.userId,
      title: "New Assignment",
      message: `New assignment: "${title}" - Due: ${new Date(dueAt).toLocaleDateString()}`,
      type: "new_assignment",
      relatedId: newAssignment._id,
      dedupeKey: `new_assignment_${newAssignment._id}_${m.userId}`,
    }));
    if (notifications.length > 0) {
      await Notification.insertMany(notifications, { ordered: false }).catch(
        (e) => console.error(e),
      );
    }
    res.status(201).json(newAssignment);
  } catch (error) {
    res
      .status(500)
      .json({ message: "Failed to create assignment", error: error.message });
  }
});

// 2. Get all assignments for a course
router.get("/course/:courseId", async (req, res) => {
  try {
    const assignments = await Assignment.find({
      courseId: req.params.courseId,
    }).sort({ dueAt: 1 });
    res.json(assignments);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch assignments" });
  }
});

module.exports = router;
