require('dotenv').config(); 
const mongoose = require('mongoose');

const uri = process.env.MONGODB_URI;

async function runSetup() {
  try {
    await mongoose.connect(uri);
    console.log('✅ Connected to MongoDB Atlas');

    const db = mongoose.connection.db;
    console.log('--- Initializing All Collections and Indexes ---');

    // Helper function to create collection only if it doesn't exist
    async function createSafe(name, options = {}) {
      const collections = await db.listCollections({ name }).toArray();
      if (collections.length === 0) {
        await db.createCollection(name, options);
        console.log(`📦 Created collection: ${name}`);
      } else {
        console.log(`ℹ️ Collection ${name} already exists. Updating...`);
      }
      return db.collection(name);
    }

    // 1. Users
    const users = await createSafe("users", {
      validator: {
        $jsonSchema: {
          bsonType: "object",
          required: ["role", "university", "profile"],
          properties: {
            role: { enum: ["student", "lecturer", "ta", "admin"] },
            university: {
              bsonType: "object",
              required: ["provider", "externalUserId", "email"],
              properties: {
                provider: { bsonType: "string" },
                externalUserId: { bsonType: "string" },
                email: { bsonType: "string" }
              }
            },
            profile: {
              bsonType: "object",
              required: ["fullName"],
              properties: { fullName: { bsonType: "string" }, avatarUrl: { bsonType: "string" } }
            },
            settings: { bsonType: "object" }
          }
        }
      }
    });
    await users.createIndex({ "university.externalUserId": 1 }, { unique: true });
    await users.createIndex({ "university.email": 1 }, { unique: true });

    // 2. Courses
    const courses = await createSafe("courses");
    await courses.createIndex({ code: 1, semester: 1 }, { unique: true });

    // 3. Course Memberships
    const memberships = await createSafe("course_memberships");
    await memberships.createIndex({ courseId: 1, userId: 1 }, { unique: true });
    await memberships.createIndex({ userId: 1, status: 1 });
    await memberships.createIndex({ courseId: 1, status: 1 });

    // 4. Study Groups
    const groups = await createSafe("study_groups");
    await groups.createIndex({ courseId: 1 });
    await groups.createIndex({ "members.userId": 1 });

    // 5. Conversations
    const convos = await createSafe("conversations");
    await convos.createIndex({ type: 1, courseId: 1 });
    await convos.createIndex({ type: 1, groupId: 1 });
    await convos.createIndex({ type: 1, participants: 1 });

    // 6. Messages
    const msgs = await createSafe("messages");
    await msgs.createIndex({ conversationId: 1, createdAt: -1 });
    await msgs.createIndex({ senderId: 1, createdAt: -1 });

    // 7. Materials
    const materials = await createSafe("materials");
    await materials.createIndex({ courseId: 1, createdAt: -1 });

    // 8. Schedule Events
    const events = await createSafe("schedule_events");
    await events.createIndex({ courseId: 1, startAt: 1 });

    // 9. Assignments
    const assignments = await createSafe("assignments");
    await assignments.createIndex({ courseId: 1, dueAt: 1 });

    // 10. Group Assignment Work
    const work = await createSafe("group_assignment_works");
    await work.createIndex({ assignmentId: 1, groupId: 1 }, { unique: true });
    await work.createIndex({ groupId: 1 });

    // 11. Notifications
    const notifications = await createSafe("notifications");
    await notifications.createIndex({ userId: 1, scheduledFor: 1 });
    await notifications.createIndex({ dedupeKey: 1 }, { unique: true });

    console.log('\n🎉 Global Database Setup Fully Restored and Complete!');
    process.exit(0);

  } catch (err) {
    console.error('❌ Error during setup:', err);
    process.exit(1);
  }
}

runSetup();