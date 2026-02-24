const admin = require('firebase-admin');
const serviceAccount = require('./firebase-service-account.json');

admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

admin.messaging().send({
  token: 'fOU7QVkwQHeSEhwiNCqJEr:APA91bHZqIEujgFUrOSpBmPDpcFiFT_ffMWS1DwE5QHRzLs-GAA5PzMw7tCwcpYSAlB0a8Z6YMjONwdVMJ_ofDP_sMDTwp51B5sLt7yVKfd7lHlXbV0_U0g',
  notification: {
    title: 'Test Push',
    body: 'Push notifications are working! 🎉'
  }
}).then(response => {
  console.log('✅ Message sent:', response);
}).catch(error => {
  console.error('❌ Error:', error);
});