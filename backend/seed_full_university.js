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

function calculateCourseSchedule(courseIndex) {
    const dayIndex = courseIndex % 5; 
    const timeIndex = Math.floor(courseIndex / 5) % 4;
    
    return {
        day: DAYS[dayIndex],
        startTime: TIME_SLOTS[timeIndex].start,
        endTime: TIME_SLOTS[timeIndex].end,
        location: `Building ${Math.floor(Math.random() * 8) + 1}, Room ${Math.floor(Math.random() * 20) + 100}`
    };
}

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
// COMPREHENSIVE SYLLABUS DATA
// ---------------------------------------------------------
const SYLLABUS_DB = {
    "CS-101": ["Introduction to Computing", "Binary & Data Representation", "Control Flow & Logic", "Functional Decomposition", "Arrays and Collections", "String Manipulation", "Pointers & Memory", "Recursion Depth", "Structs & Unions", "File I/O Systems", "Dynamic Allocation", "Algorithmic Thinking", "Final Review"],
    "MATH-101": ["Limits & Continuity", "The Derivative", "Rules of Differentiation", "Mean Value Theorem", "Integration Methods", "Fundamental Theorem of Calculus", "Applications of Integrals", "Sequences & Series", "Power Series", "Taylor Polynomials", "Improper Integrals", "Multivariable Intro"],
    "MATH-102": ["Systems of Linear Equations", "Matrix Algebra", "Determinants", "Vector Spaces", "Subspaces & Bases", "Linear Transformations", "Eigenvalues & Eigenvectors", "Inner Product Spaces", "Gram-Schmidt Process", "Diagonalization", "Quadratic Forms", "Canonical Forms"],
    "MATH-103": ["Logic & Proofs", "Set Theory", "Functions & Relations", "Number Theory", "Induction & Recursion", "Counting & Combinatorics", "Discrete Probability", "Graph Theory Intro", "Trees & Forest", "Boolean Algebra", "Automata Theory", "Complexity Classes"],
    "CS-102": ["OOP Paradigms", "Classes & Objects", "Encapsulation", "Inheritance Hierarchies", "Polymorphism", "Abstract Classes", "Interfaces", "Exception Handling", "Generics", "Design Patterns", "Unit Testing", "UML Modeling", "Stream API"],
    "CS-201": ["Abstract Data Types", "Complexity Analysis", "Linked Lists", "Stacks & Queues", "Binary Search Trees", "AVL & Red-Black Trees", "Hashing & Hash Tables", "Priority Queues", "Sorting Algorithms", "Graph Representations", "BFS & DFS", "Disjoint Sets"],
    "CS-202": ["Divide & Conquer", "Greedy Algorithms", "Dynamic Programming", "Network Flow", "Max-Cut Min-Flow", "String Matching", "NP-Completeness", "Approximation Algorithms", "Backtracking", "Randomized Algorithms", "Computational Geometry", "Amortized Analysis"],
    "CS-206": ["SDLC Models", "Requirement Engineering", "Software Architecture", "Clean Code Principles", "SOLID Principles", "Version Control (Git)", "CI/CD Pipelines", "Agile & Scrum", "Software Testing", "Refactoring Techniques", "Project Management", "Documentation"],
    "CS-302": ["OSI & TCP/IP Models", "Physical Layer", "Data Link Layer", "Ethernet & Switching", "IP Addressing", "Routing Protocols", "Transport Layer (TCP/UDP)", "Congestion Control", "Application Layer", "Network Security", "Wireless Networking", "Cloud Computing"],
    "PROJ-300": ["Research Methods", "Proposal Writing", "Feasibility Study", "System Design", "Backend Development", "Frontend Implementation", "Database Schema", "API Documentation", "Security Audit", "Performance Tuning", "User Testing", "Final Presentation", "Legacy Handoff"]
};

// Default syllabus for courses not in SYLLABUS_DB
const DEFAULT_SYLLABUS = ["Introduction", "Historical Context", "Core Foundations", "Theoretical Framework", "Practical Application I", "Practical Application II", "Intermediate Concepts", "Case Studies", "Advanced Methodologies", "Critical Analysis", "Modern Trends", "Ethics & Industry", "Final Synthesis"];

const CS_CURRICULUM = [
    { name: "Intro to Computer Science", code: "CS-101", year: 1 },
    { name: "Calculus 1", code: "MATH-101", year: 1 },
    { name: "Linear Algebra 1", code: "MATH-102", year: 1 },
    { name: "Discrete Math", code: "MATH-103", year: 1 },
    { name: "Object Oriented Programming", code: "CS-102", year: 1 },
    { name: "Calculus 2", code: "MATH-104", year: 1 },
    { name: "Linear Algebra 2", code: "MATH-105", year: 1 },
    { name: "Digital Systems", code: "HW-101", year: 1 },
    { name: "Data Structures", code: "CS-201", year: 2 },
    { name: "Algorithms 1", code: "CS-202", year: 2 },
    { name: "Computer Organization", code: "HW-201", year: 2 },
    { name: "Probability", code: "MATH-201", year: 2 },
    { name: "Operating Systems", code: "CS-203", year: 2 },
    { name: "Algorithms 2", code: "CS-204", year: 2 },
    { name: "Database Systems", code: "CS-205", year: 2 },
    { name: "Software Engineering", code: "CS-206", year: 2 },
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

        await User.deleteMany({});
        await Course.deleteMany({});
        await CourseMembership.deleteMany({});
        await StudyGroup.deleteMany({});
        await Assignment.deleteMany({});
        await GroupAssignmentWork.deleteMany({});

        const hashedPassword = await bcrypt.hash("123456", 10);
        const currentSemester = "2026A";

        console.log("👨‍🏫 Creating 20 Lecturers...");
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

        const lecturerScheduleTracker = {};
        lecturers.forEach(l => lecturerScheduleTracker[l._id.toString()] = new Set());

        console.log("📚 Creating Courses with Distinct Lecturers...");
        const coursesDocs = [];
        let countYear1 = 0, countYear2 = 0, countYear3 = 0;

        // Guaranteed distribution: ensure every lecturer gets at least one course
        const lecturerPool = [...lecturers].sort(() => 0.5 - Math.random());

        for (let i = 0; i < CS_CURRICULUM.length; i++) {
            const cInfo = CS_CURRICULUM[i];
            let schedule;
            if (cInfo.year === 1) schedule = calculateCourseSchedule(countYear1++);
            else if (cInfo.year === 2) schedule = calculateCourseSchedule(countYear2++);
            else schedule = calculateCourseSchedule(countYear3++);

            const timeSlotKey = `${schedule.day}_${schedule.startTime}`;
            
            // Assign from the guaranteed pool first to ensure 1 course per lecturer
            let assignedLecturer = lecturerPool[i % lecturerPool.length];
            lecturerScheduleTracker[assignedLecturer._id.toString()].add(timeSlotKey);

            const rawTopics = SYLLABUS_DB[cInfo.code] || DEFAULT_SYLLABUS;
            
            coursesDocs.push({
                name: cInfo.name,
                code: cInfo.code,
                lecturer: assignedLecturer._id, // FIXED: Using 'lecturer' field as ObjectId
                semester: currentSemester,
                schedule: schedule, 
                totalLectures: rawTopics.length,
                completedLectures: 0,
                topics: rawTopics.map(t => ({ title: t, isCompleted: false })),
                _yearRef: cInfo.year
            });
        }
        const savedCourses = await Course.insertMany(coursesDocs);

        console.log("🔗 Creating Lecturer Memberships...");
        const lecturerMemberships = savedCourses.map((c, idx) => ({
            userId: coursesDocs[idx].lecturer,
            courseId: c._id,
            role: 'lecturer',
            status: 'active'
        }));
        await CourseMembership.insertMany(lecturerMemberships);

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
        console.log("   - Every Lecturer has at least 1 course.");
        console.log("   - All Courses have 12-13 Academic Topics.");
        console.log("   - Roles are strictly defined to prevent lecturers in student lists.");
        process.exit(0);
    } catch (e) { console.error(e); process.exit(1); }
}

seed();