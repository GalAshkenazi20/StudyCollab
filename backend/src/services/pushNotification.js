const admin = require('firebase-admin');
const User = require('../models/User');

// Initialize Firebase Admin (do this once)
const serviceAccount = require('../../firebase-service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

/**
 * Sends a push notification to a specific user via FCM.
 * Falls back silently if user has no FCM token.
 *
 * @param {string} userId - MongoDB user ID
 * @param {string} title - Notification title
 * @param {string} body - Notification message
 * @param {object} data - Optional extra data (e.g., { type: "task_assigned", relatedId: "..." })
 */
async function sendPushToUser(userId, title, body, data = {}) {
  try {
    const user = await User.findById(userId).select('fcmToken');
    if (!user || !user.fcmToken) {
      return; // User hasn't registered for push — skip silently
    }

    const message = {
      token: user.fcmToken,
      notification: {
        title: title,
        body: body
      },
      data: {
        // All values must be strings for FCM data payload
        type: String(data.type || ''),
        relatedId: String(data.relatedId || ''),
        click_action: 'OPEN_NOTIFICATIONS'
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'studycollab_notifications',
          sound: 'default'
        }
      }
    };

    await admin.messaging().send(message);
    console.log(`📱 Push sent to user ${userId}`);
  } catch (error) {
    if (error.code === 'messaging/registration-token-not-registered') {
      // Token is stale — clear it from DB
      await User.findByIdAndUpdate(userId, { fcmToken: null });
      console.log(`🧹 Cleared stale FCM token for user ${userId}`);
    } else {
      console.error(`❌ Push notification failed for user ${userId}:`, error.message);
    }
  }
}

/**
 * Sends push notification to multiple users.
 */
async function sendPushToUsers(userIds, title, body, data = {}) {
  const promises = userIds.map(id => sendPushToUser(id, title, body, data));
  await Promise.allSettled(promises);
}

module.exports = { sendPushToUser, sendPushToUsers };