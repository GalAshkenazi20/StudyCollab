// use studycollab;

db.createCollection("users", {
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
db.users.createIndex({ "university.externalUserId": 1 }, { unique: true });
db.users.createIndex({ "university.email": 1 }, { unique: true });

db.createCollection("courses");
db.courses.createIndex({ code: 1, semester: 1 }, { unique: true });

db.createCollection("course_memberships");
db.course_memberships.createIndex({ courseId: 1, userId: 1 }, { unique: true });
db.course_memberships.createIndex({ userId: 1, status: 1 });
db.course_memberships.createIndex({ courseId: 1, status: 1 });

db.createCollection("study_groups");
db.study_groups.createIndex({ courseId: 1 });
db.study_groups.createIndex({ "members.userId": 1 });

db.createCollection("conversations");
db.conversations.createIndex({ type: 1, courseId: 1 });
db.conversations.createIndex({ type: 1, groupId: 1 });
db.conversations.createIndex({ type: 1, participants: 1 });

db.createCollection("messages");
db.messages.createIndex({ conversationId: 1, createdAt: -1 });
db.messages.createIndex({ senderId: 1, createdAt: -1 });

db.createCollection("materials");
db.materials.createIndex({ courseId: 1, createdAt: -1 });

db.createCollection("schedule_events");
db.schedule_events.createIndex({ courseId: 1, startAt: 1 });

db.createCollection("assignments");
db.assignments.createIndex({ courseId: 1, dueAt: 1 });

db.createCollection("group_assignment_work");
db.group_assignment_work.createIndex({ assignmentId: 1, groupId: 1 }, { unique: true });
db.group_assignment_work.createIndex({ groupId: 1 });

db.createCollection("notifications");
db.notifications.createIndex({ userId: 1, scheduledFor: 1 });
db.notifications.createIndex({ dedupeKey: 1 }, { unique: true });