const functions = require("firebase-functions");
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

// Initialisation Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// 🔐 Fonction callable pour assigner le rôle "teacher"
exports.assignTeacherRole = functions.https.onCall(async (data, context) => {
  const uid = data.uid;

  if (!uid) {
    throw new functions.https.HttpsError("invalid-argument", "UID is required");
  }

  try {
    await admin.auth().setCustomUserClaims(uid, {role: "teacher"});
    console.log(`✅ Rôle "teacher" assigné à UID: ${uid}`);
    return {success: true, message: "Rôle assigné avec succès"};
  } catch (error) {
    console.error("❌ Erreur lors de l'assignation du rôle:", error);
    throw new functions.https.HttpsError("internal", "Erreur serveur");
  }
});
