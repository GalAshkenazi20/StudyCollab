require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const User = require('./src/models/User'); 
const Course = require('./src/models/Course');
const CourseMembership = require('./src/models/CourseMembership');
const StudyGroup = require('./src/models/StudyGroup');
const Assignment = require('./src/models/Assignment');
const GroupAssignmentWork = require('./src/models/GroupAssignmentWork');

const MONGO_URI = process.env.MONGODB_URI;


const TIME_SLOTS = [
    { start: "08:00", end: "11:00" },
    { start: "11:00", end: "14:00" },
    { start: "14:00", end: "17:00" },
    { start: "17:00", end: "20:00" }
];

const DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"];

// פונקציה לקביעת לו"ז הקורס (כדי למנוע חפיפות לסטודנטים)
function calculateCourseSchedule(courseIndex) {
    // מפזר את הקורסים על פני השבוע בצורה רוחבית
    const dayIndex = courseIndex % 5; 
    const timeIndex = Math.floor(courseIndex / 5) % 4;
    
    return {
        day: DAYS[dayIndex],
        startTime: TIME_SLOTS[timeIndex].start,
        endTime: TIME_SLOTS[timeIndex].end,
        location: `Building ${Math.floor(Math.random() * 8) + 1}, Room ${Math.floor(Math.random() * 20) + 100}`
    };
}

// שמות אנשים
const FIRST_NAMES = ["Noa", "Maya", "Tamar", "Yael", "Adi", "Ronit", "Gal", "Michal", "Dana", "David", "Yossi", "Omer", "Itay", "Daniel", "Guy", "Eitan", "Moshe", "Avi", "Tom", "Ben", "Lior", "Shir","Gal","Elad"];
const LAST_NAMES = ["Cohen", "Levi", "Mizrahi", "Peretz", "Biton", "Dahan", "Avraham", "Friedman", "Katz", "Yosef", "Amar", "Ohana", "Golan", "Bar", "Segal", "Shapira", "Azoulay","Noah","Avni","Lorman"];

function getRandomName(isLecturer) {
    const f = FIRST_NAMES[Math.floor(Math.random() * FIRST_NAMES.length)];
    const l = LAST_NAMES[Math.floor(Math.random() * LAST_NAMES.length)];
    return isLecturer ? `${Math.random()>0.5?'Dr.':'Prof.'} ${f} ${l}` : `${f} ${l}`;
}

function generateRealEmail(name, i, domain) {
    return `${name.replace(/^(Dr\.|Prof\.)\s+/,"").trim().toLowerCase().replace(/\s+/g,".")}${i}${domain}`;
}

// ---------------------------------------------------------
// תוכנית הלימודים
// ---------------------------------------------------------
const SYLLABUS_DB = {
    "CS-101": ["Intro to Prog", "Variables", "Loops", "Functions", "Arrays", "Pointers", "Strings", "Recursion", "Structs", "Files", "Memory Alloc", "Complexity"],
    // ... המערכת תשלים אוטומטית אם חסר
};

const CS_CURRICULUM = [
    // Year 1
    { name: "Intro to Computer Science", code: "CS-101", year: 1 },
    { name: "Calculus 1", code: "MATH-101", year: 1 },
    { name: "Linear Algebra 1", code: "MATH-102", year: 1 },
    { name: "Discrete Math", code: "MATH-103", year: 1 },
    { name: "Object Oriented Programming", code: "CS-102", year: 1 },
    { name: "Calculus 2", code: "MATH-104", year: 1 },
    { name: "Linear Algebra 2", code: "MATH-105", year: 1 },
    { name: "Digital Systems", code: "HW-101", year: 1 },
    // Year 2
    { name: "Data Structures", code: "CS-201", year: 2 },
    { name: "Algorithms 1", code: "CS-202", year: 2 },
    { name: "Computer Organization", code: "HW-201", year: 2 },
    { name: "Probability", code: "MATH-201", year: 2 },
    { name: "Operating Systems", code: "CS-203", year: 2 },
    { name: "Algorithms 2", code: "CS-204", year: 2 },
    { name: "Database Systems", code: "CS-205", year: 2 },
    { name: "Software Engineering", code: "CS-206", year: 2 },
    // Year 3
    { name: "Machine Learning", code: "CS-301", year: 3 },
    { name: "Computer Networks", code: "CS-302", year: 3 },
    { name: "Complexity Theory", code: "CS-303", year: 3 },
    { name: "Compilation", code: "CS-304", year: 3 },
    { name: "Deep Learning", code: "CS-306", year: 3 },
    { name: "Cryptography", code: "CS-307", year: 3 },
    { name: "Computer Graphics", code: "CS-309", year: 3 },
    { name: "Final Project", code: "PROJ-300", year: 3 }
];

async function seed() {
    try {
        await mongoose.connect(MONGO_URI);
        console.log("🔌 Connected to MongoDB...");

        console.log("🧹 Clearing old data...");
        await User.deleteMany({});
        await Course.deleteMany({});
        await CourseMembership.deleteMany({});
        await StudyGroup.deleteMany({});
        await Assignment.deleteMany({});
        await GroupAssignmentWork.deleteMany({});

        const hashedPassword = await bcrypt.hash("123456", 10);
        const currentSemester = "2026A";

        // 1. Create Lecturers
        console.log("👨‍🏫 Creating Lecturers...");
        const lecturersData = [];
        for (let i = 1; i <= 20; i++) {
            const name = getRandomName(true);
            lecturersData.push({
                role: 'lecturer',
                university: { 
                    provider: 'University', externalUserId: `LEC${100+i}`, 
                    email: generateRealEmail(name, i, "@uni.ac.il")
                },
                password: hashedPassword,
                profile: { fullName: name, avatarUrl: `https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(/\s/g,'')}` }
            });
        }
        const lecturers = await User.insertMany(lecturersData);

        // אובייקט למעקב אחרי הלו"ז של המרצים: { lecturerId: Set<"Sunday_08:00"> }
        const lecturerScheduleTracker = {};
        lecturers.forEach(l => lecturerScheduleTracker[l._id.toString()] = new Set());

        // 2. Create Courses with SMART SCHEDULING
        console.log("📚 Creating Courses (Conflict-Free Mode)...");
        const coursesDocs = [];
        
        let countYear1 = 0, countYear2 = 0, countYear3 = 0;

        for (let i = 0; i < CS_CURRICULUM.length; i++) {
            const cInfo = CS_CURRICULUM[i];
            
            // 2.1 קובעים זמן לקורס לפי השנה (כדי שלא יהיו התנגשויות לסטודנטים)
            let schedule;
            if (cInfo.year === 1) schedule = calculateCourseSchedule(countYear1++);
            else if (cInfo.year === 2) schedule = calculateCourseSchedule(countYear2++);
            else schedule = calculateCourseSchedule(countYear3++);

            // מפתח ייחודי לזמן הזה (למשל: "Sunday_08:00")
            const timeSlotKey = `${schedule.day}_${schedule.startTime}`;

            // 2.2 מוצאים מרצה שפנוי בזמן הזה
            let assignedLecturer = null;
            
            // מערבבים את רשימת המרצים כדי לא ליפול תמיד על אותו אחד
            const shuffledLecturers = [...lecturers].sort(() => 0.5 - Math.random());

            for (const lecturer of shuffledLecturers) {
                const lecId = lecturer._id.toString();
                // אם המרצה לא מלמד בזמן הזה -> בחר בו
                if (!lecturerScheduleTracker[lecId].has(timeSlotKey)) {
                    assignedLecturer = lecturer;
                    lecturerScheduleTracker[lecId].add(timeSlotKey); // סמן אותו כתפוס
                    break;
                }
            }

            // Fallback (לא אמור לקרות עם 20 מרצים ו-3 קורסים במקביל, אבל ליתר ביטחון)
            if (!assignedLecturer) {
                console.warn(`⚠️ Warning: Could not find free lecturer for ${timeSlotKey}, assigning random.`);
                assignedLecturer = lecturers[0];
            }

            const rawTopics = SYLLABUS_DB[cInfo.code] || ["Topic A", "Topic B", "Topic C", "Topic D", "Topic E", "Topic F", "Topic G", "Topic H", "Topic I", "Topic J", "Topic K", "Topic L"];
            
            coursesDocs.push({
                name: cInfo.name,
                code: cInfo.code,
                lecturerId: assignedLecturer._id,
                semester: currentSemester,
                schedule: schedule, 
                totalLectures: rawTopics.length,
                completedLectures: 0,
                topics: rawTopics.map(t => ({ title: t, isCompleted: false })),
                _yearRef: cInfo.year
            });
        }
        const savedCourses = await Course.insertMany(coursesDocs);

        // 2.5 Create CourseMembership for Lecturers
        console.log("🔗 Creating Lecturer Memberships...");
        const lecturerMemberships = savedCourses.map((c, idx) => ({
            userId: coursesDocs[idx].lecturerId,
            courseId: c._id,
            role: 'lecturer',
            status: 'active'
        }));
        await CourseMembership.insertMany(lecturerMemberships);
        console.log(`   ✅ Created ${lecturerMemberships.length} lecturer memberships`);

        // 3. Create Students & Enroll
        console.log("🎓 Creating Students...");
        const studentsPerYear = 20;
        
        for (let year = 1; year <= 3; year++) {
            const yearCourses = savedCourses.filter((_, idx) => CS_CURRICULUM[idx].year === year);
            
            for (let i = 0; i < studentsPerYear; i++) {
                const globalIdx = (year - 1) * studentsPerYear + i;
                const name = getRandomName(false);
                
                const student = await User.create({
                    role: 'student',
                    university: { 
                        provider: 'University', externalUserId: `STU${2000+globalIdx}`,
                        email: generateRealEmail(name, globalIdx, "@student.uni.ac.il")
                    },
                    password: hashedPassword,
                    profile: { fullName: name, avatarUrl: `https://api.dicebear.com/7.x/notionists/svg?seed=${name.replace(/\s/g,'')}` },
                    enrolledCourses: yearCourses.map(c => c._id)
                });

                await CourseMembership.insertMany(yearCourses.map(c => ({
                    userId: student._id, courseId: c._id, role: 'student', semester: currentSemester
                })));
            }
        }

        console.log("✅ Seeding Complete!");
        console.log("   - Students have spread schedules (Mon-Thu).");
        console.log("   - Lecturers are NOT double-booked.");
        process.exit(0);
    } catch (e) { console.error(e); process.exit(1); }
}

seed();