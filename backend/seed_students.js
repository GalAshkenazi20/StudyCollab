require('dotenv').config(); // 👈 Load Atlas URI from .env
const mongoose = require('mongoose');

// --- SCHEMAS ---

const UserSchema = new mongoose.Schema({
  role: { type: String, required: true },
  university: {
    provider: { type: String },
    externalUserId: { type: String },
    email: { type: String, unique: true },
  },
  profile: {
    fullName: { type: String },
    avatarUrl: { type: String }
  }
});
const User = mongoose.model("User", UserSchema);

const CourseSchema = new mongoose.Schema({
    name: { type: String, required: true },
    code: { type: String, required: true },
    semester: { type: String, required: true }
});
CourseSchema.index({ code: 1, semester: 1 }, { unique: true }); 
const Course = mongoose.model("Course", CourseSchema);

const CourseMembershipSchema = new mongoose.Schema({
    courseId: { type: mongoose.Schema.Types.ObjectId, ref: 'Course', required: true },
    userId:   { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    role:     { type: String, default: 'student' },
    status:   { type: String, default: 'active' }
});
CourseMembershipSchema.index({ courseId: 1, userId: 1 }, { unique: true });
const CourseMembership = mongoose.model("CourseMembership", CourseMembershipSchema, "course_memberships");


// --- DATA TO SEED ---

const studentsData = [
    { name: "Charlie", email: "charlie@test.com", id: "1001" },
    { name: "Bob",     email: "bob@test.com",     id: "1002" },
    { name: "Jane",    email: "jane@test.com",    id: "1003" }
];

const coursesData = [
    { name: "Algorithms 1",      code: "CS-201",   semester: "2025A" },
    { name: "Linear Algebra",    code: "MATH-101", semester: "2025A" },
    { name: "Intro to CS",       code: "CS-101",   semester: "2025A" }
];


// --- MAIN LOGIC ---

// Use the Atlas URI from .env, or fallback to local if .env is missing
const dbURI = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/studycollab';

mongoose.connect(dbURI)
  .then(async () => {
    console.log('✅ Connected to MongoDB Atlas for Seeding.');

    // 1. Upsert Students
    console.log('--- Seeding Students ---');
    const studentDocs = [];
    for (const s of studentsData) {
        const user = await User.findOneAndUpdate(
            { "university.email": s.email }, 
            {
                role: "student",
                university: { provider: "sys", externalUserId: s.id, email: s.email },
                profile: { fullName: s.name, avatarUrl: "" }
            },
            { upsert: true, new: true }
        );
        studentDocs.push(user);
        console.log(`👤 Processed: ${s.name}`);
    }

    // 2. Upsert Courses
    console.log('\n--- Seeding Courses ---');
    const courseDocs = [];
    for (const c of coursesData) {
        const course = await Course.findOneAndUpdate(
            { code: c.code, semester: c.semester },
            { name: c.name, code: c.code, semester: c.semester },
            { upsert: true, new: true }
        );
        courseDocs.push(course);
        console.log(`📚 Processed: ${c.name}`);
    }

    // 3. Register Students to Courses
    console.log('\n--- Registering Students to Courses ---');
    for (const student of studentDocs) {
        for (const course of courseDocs) {
            await CourseMembership.findOneAndUpdate(
                { courseId: course._id, userId: student._id },
                { role: "student", status: "active" },
                { upsert: true, new: true }
            );
        }
        console.log(`🔗 Registered ${student.profile.fullName} to all courses.`);
    }

    console.log('\n🎉 Seed Complete on Atlas!');
    process.exit();
  })
  .catch(err => {
    console.error('❌ Seed Error:', err);
    process.exit(1);
  });