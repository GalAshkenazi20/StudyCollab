const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');

// Configure storage logic
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/materials/'); // Ensure this folder exists
    },
    filename: (req, file, cb) => {
        // Renaming the file to avoid duplicates: timestamp-originalname
        cb(null, Date.now() + '-' + file.originalname);
    }
});

const upload = multer({ 
    storage: storage,
    fileFilter: (req, file, cb) => {
        const filetypes = /pdf|doc|docx|ppt|pptx/;
        const extname = filetypes.test(path.extname(file.originalname).toLowerCase());
        if (extname) return cb(null, true);
        cb(new Error("Only documents are allowed!"));
    }
});

// POST route to upload course material
router.post('/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) return res.status(400).send("No file uploaded.");

        // Save metadata to your MongoDB (Course name, File Path, Uploader ID)
        const fileData = {
            title: req.body.title,
            courseId: req.body.courseId,
            fileUrl: `/uploads/materials/${req.file.filename}`,
            uploadedBy: req.body.lecturerId
        };

        // TODO: Save fileData to your MongoDB 'Material' model here
        
        res.status(201).json({ message: "Upload successful", data: fileData });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;