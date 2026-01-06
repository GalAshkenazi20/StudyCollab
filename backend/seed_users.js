require('dotenv').config();
const mongoose = require('mongoose');
const User = require('./src/models/User'); 
const Course = require('./src/models/Course');
const CourseMembership = require('./src/models/CourseMembership');

const MONGO_URI = process.env.MONGODB_URI;

async function seed() {
    try {
        await mongoose.connect(MONGO_URI);
        console.log("Connected to MongoDB Atlas...");

        // 1. Clear existing data
        await User.deleteMany({});
        await Course.deleteMany({});
        await CourseMembership.deleteMany({});

        const commonPassword = "123456";
        const emailDomain = "@msmail.uni.ac.il";
        const currentSemester = "2026A";

        // 2. Create 6 Lecturers
        const lecturersData = [
            { name: "Dr. Yossi Cohen", prefix: "yossi.c", id: "LEC001" },
            { name: "Prof. Sarah Levy", prefix: "sarah.l", id: "LEC002" },
            { name: "Dr. Avraham Mizrahi", prefix: "avraham.m", id: "LEC003" },
            { name: "Dr. Dina Silberman", prefix: "dina.s", id: "LEC004" },
            { name: "Prof. Ronen Bar", prefix: "ronen.b", id: "LEC005" },
            { name: "Dr. Maya Ziv", prefix: "maya.z", id: "LEC006" }
        ];

        const lecturers = await User.insertMany(lecturersData.map(l => ({
            role: 'lecturer',
            university: {
                provider: 'University',
                externalUserId: l.id,
                email: `${l.prefix}${emailDomain}`
            },
            password: commonPassword,
            profile: { fullName: l.name }
        })));

        // 3. Create 6 Courses
        const coursesData = [
            { name: "Computer Vision", code: "CV-101" },
            { name: "Network Security", code: "NET-202" },
            { name: "Algorithms", code: "ALG-303" },
            { name: "Operating Systems", code: "OS-404" },
            { name: "Data Compression", code: "COMP-505" },
            { name: "Mobile App Dev", code: "MOB-606" }
        ];

        const courses = await Course.insertMany(coursesData.map((c, i) => ({
            name: c.name,
            code: c.code,
            lecturerId: lecturers[i]._id,
            semester: currentSemester
        })));

        // 4. Create 10 Students (Fixed .length property)
        const studentsData = [
            "Elad", "Noa", "Itay", "Maya", "Amit", 
            "Roni", "Daniel", "Shira", "Yuval", "Tamar"
        ];

        for (let i = 0; i < studentsData.length; i++) {
            const name = studentsData[i];
            
            // Pick 4 courses using a rotating window
            const assignedCourseIds = [
                courses[i % 6]._id,
                courses[(i + 1) % 6]._id,
                courses[(i + 2) % 6]._id,
                courses[(i + 3) % 6]._id
            ];

            // Create Student
            const student = await User.create({
                role: 'student',
                university: {
                    provider: 'University',
                    externalUserId: `STU${100 + i}`,
                    email: `${name.toLowerCase()}${i}${emailDomain}`
                },
                password: commonPassword,
                profile: { fullName: `${name} Student` },
                enrolledCourses: assignedCourseIds
            });

            // Create Membership Records
            const memberships = assignedCourseIds.map(courseId => ({
                userId: student._id,
                courseId: courseId,
                role: 'student',
                semester: currentSemester // Added if required by your schema
            }));
            
            await CourseMembership.insertMany(memberships);
        }

        console.log(`
Seeding Complete!
---------------------------------
Users: 6 Lecturers, 10 Students
Courses: 6 created
Memberships: 40 created
---------------------------------
`);
        process.exit();
    } catch (err) {
        console.error("Seeding Error:", err);
        process.exit(1);
    }
}

seed();