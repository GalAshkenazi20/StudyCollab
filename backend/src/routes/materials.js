const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const Material = require('../models/Material');

// Ensure upload directory exists
const uploadDir = 'uploads/materials/';
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, uploadDir),
    filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});

const upload = multer({
    storage,
    fileFilter: (req, file, cb) => {
        const allowed = /pdf|doc|docx|ppt|pptx/;
        const ext = path.extname(file.originalname).toLowerCase();
        if (allowed.test(ext)) return cb(null, true);
        cb(new Error("Only document files are allowed"));
    }
});

// GET /api/materials/course/:courseId — List all materials for a course
router.get('/course/:courseId', async (req, res) => {
    try {
        const materials = await Material.find({ courseId: req.params.courseId })
            .sort({ createdAt: -1 });
        res.json(materials);
    } catch (error) {
        res.status(500).json({ message: "Failed to fetch materials", error: error.message });
    }
});

// POST /api/materials/upload — Upload a new material PDF
router.post('/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ message: "No file uploaded" });

        const material = new Material({
            courseId: req.body.courseId,
            title: req.body.title || req.file.originalname,
            fileUrl: `/uploads/materials/${req.file.filename}`,
            uploadedBy: req.body.lecturerId
        });

        await material.save();
        res.status(201).json(material);
    } catch (error) {
        res.status(500).json({ message: "Upload failed", error: error.message });
    }
});

// DELETE /api/materials/:materialId — Delete a material
router.delete('/:materialId', async (req, res) => {
    try {
        const material = await Material.findById(req.params.materialId);
        if (!material) return res.status(404).json({ message: "Material not found" });

        // Delete the actual file from disk
        const filePath = path.join(__dirname, '../../', material.fileUrl);
        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }

        await Material.findByIdAndDelete(req.params.materialId);
        res.json({ message: "Material deleted" });
    } catch (error) {
        res.status(500).json({ message: "Delete failed", error: error.message });
    }
});

// GET /api/materials/download/:materialId — Serve/download a file
router.get('/download/:materialId', async (req, res) => {
    try {
        const material = await Material.findById(req.params.materialId);
        if (!material) return res.status(404).json({ message: "Material not found" });

        const filePath = path.join(__dirname, '../../', material.fileUrl);
        if (!fs.existsSync(filePath)) {
            return res.status(404).json({ message: "File not found on disk" });
        }

        res.download(filePath);
    } catch (error) {
        res.status(500).json({ message: "Download failed", error: error.message });
    }
});

module.exports = router;