const Assignment = require('../models/Assignment');
const CourseMembership = require('../models/CourseMembership');
const User = require('../models/User');
const Notification = require('../models/Notification');
const { sendPushToUser } = require('../services/pushNotification');

async function checkDeadlines() {
    try {
        const now = new Date();
        // Find assignments due in the next 48 hours
        const upcoming = await Assignment.find({
            dueAt: {
                $gt: now,
                $lt: new Date(now.getTime() + 48 * 60 * 60 * 1000)
            }
        });

        for (const assignment of upcoming) {
            const memberships = await CourseMembership.find({
                courseId: assignment.courseId,
                role: 'student'
            });

            for (const m of memberships) {
                const user = await User.findById(m.userId);
                const reminderHours = user?.notificationPreferences?.deadlineReminderHours || 24;
                const reminderTime = new Date(assignment.dueAt.getTime() - reminderHours * 60 * 60 * 1000);

                // Only create reminder if we're within the reminder window
                if (now >= reminderTime) {
                    const hoursLeft = Math.round((assignment.dueAt - now) / (1000 * 60 * 60));
                    await new Notification({
                        userId: m.userId,
                        title: "Deadline Approaching",
                        message: `"${assignment.title}" is due in ~${hoursLeft} hours`,
                        type: "deadline_reminder",
                        relatedId: assignment._id,
                        dedupeKey: `deadline_${assignment._id}_${m.userId}`
                    }).save().catch(() => {}); // Dedupe key prevents duplicates
                    // Send push notification for deadline
                    await sendPushToUser(m.userId, "Deadline Approaching", `"${assignment.title}" is due in ~${hoursLeft} hours`, {
                        type: 'deadline_reminder',
                        relatedId: assignment._id
                    }).catch(() => {});
                }
            }
        }
        console.log(`⏰ Deadline check complete. Checked ${upcoming.length} assignments.`);
    } catch (e) {
        console.error("Deadline reminder error:", e);
    }
}

module.exports = checkDeadlines;