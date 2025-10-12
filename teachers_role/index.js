const functions = require("firebase-functions");
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

// Initialisation Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// 🔐 Universal function to assign user roles
exports.assignUserRole = functions.https.onCall(async (data, context) => {
  const {uid, role} = data;

  // Validation
  if (!uid) {
    throw new functions.https.HttpsError("invalid-argument", "UID is required");
  }

  if (!role) {
    throw new functions.https.HttpsError("invalid-argument", "Role is required");
  }

  const validRoles = ["teacher", "admin", "parent", "student"];
  if (!validRoles.includes(role)) {
    throw new functions.https.HttpsError("invalid-argument", `Invalid role: ${role}. Valid roles: ${validRoles.join(", ")}`);
  }

  try {
    const db = admin.firestore();
    
    // Set custom claims for authentication
    await admin.auth().setCustomUserClaims(uid, {role: role});
    
    // Determine the correct collection based on role
    let collection;
    switch (role) {
      case "teacher":
        collection = "teachers";
        break;
      case "admin":
        collection = "admins";
        break;
      case "parent":
        collection = "users";
        break;
      case "student":
        collection = "students";
        break;
      default:
        collection = "users";
    }
    
    // Update the Firestore document to ensure consistency
    await db.collection(collection).doc(uid).update({
      role: role,
      customClaimsSet: true,
      roleAssignedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log(`✅ Role "${role}" assigned to UID: ${uid} in collection: ${collection}`);
    return {
      success: true, 
      message: `${role.charAt(0).toUpperCase() + role.slice(1)} role assigned successfully`,
      role: role,
      collection: collection
    };
  } catch (error) {
    console.error(`❌ Error assigning role "${role}" to UID: ${uid}`, error);
    throw new functions.https.HttpsError("internal", `Server error: ${error.message}`);
  }
});

// 🔐 Legacy function for backward compatibility
exports.assignTeacherRole = functions.https.onCall(async (data, context) => {
  console.log("⚠️ Using legacy assignTeacherRole function. Consider using assignUserRole instead.");
  
  const uid = data.uid;
  if (!uid) {
    throw new functions.https.HttpsError("invalid-argument", "UID is required");
  }

  // Call the new universal function
  return exports.assignUserRole.handler({uid: uid, role: "teacher"}, context);
});

// 🔍 Function to verify user role
exports.verifyUserRole = functions.https.onCall(async (data, context) => {
  const {uid} = data;

  if (!uid) {
    throw new functions.https.HttpsError("invalid-argument", "UID is required");
  }

  try {
    // Get user record to check custom claims
    const userRecord = await admin.auth().getUser(uid);
    const customClaims = userRecord.customClaims || {};
    
    // Also check Firestore for role consistency
    const db = admin.firestore();
    const collections = ["users", "teachers", "students", "admins"];
    
    for (const collection of collections) {
      const doc = await db.collection(collection).doc(uid).get();
      if (doc.exists) {
        const userData = doc.data();
        return {
          success: true,
          role: userData.role,
          collection: collection,
          customClaims: customClaims,
          isActive: userData.isActive || false,
          lastLogin: userData.lastLogin || null
        };
      }
    }
    
    throw new functions.https.HttpsError("not-found", "User not found in any collection");
  } catch (error) {
    console.error(`❌ Error verifying role for UID: ${uid}`, error);
    throw new functions.https.HttpsError("internal", `Server error: ${error.message}`);
  }
});

// 🔄 Function to migrate existing users (admin only)
exports.migrateUserRoles = functions.https.onCall(async (data, context) => {
  // Check if the caller is an admin
  if (!context.auth || !context.auth.token.role || context.auth.token.role !== "admin") {
    throw new functions.https.HttpsError("permission-denied", "Only admins can migrate user roles");
  }

  try {
    const db = admin.firestore();
    let migrated = 0;
    let errors = 0;

    // Migrate teachers
    const teachersSnapshot = await db.collection("teachers").get();
    for (const doc of teachersSnapshot.docs) {
      try {
        const data = doc.data();
        if (!data.role) {
          await doc.ref.update({
            role: "teacher",
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          await admin.auth().setCustomUserClaims(doc.id, {role: "teacher"});
          migrated++;
        }
      } catch (error) {
        console.error(`Error migrating teacher ${doc.id}:`, error);
        errors++;
      }
    }

    // Migrate parents in users collection
    const usersSnapshot = await db.collection("users").get();
    for (const doc of usersSnapshot.docs) {
      try {
        const data = doc.data();
        if (!data.role) {
          await doc.ref.update({
            role: "parent",
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          await admin.auth().setCustomUserClaims(doc.id, {role: "parent"});
          migrated++;
        }
      } catch (error) {
        console.error(`Error migrating user ${doc.id}:`, error);
        errors++;
      }
    }

    return {
      success: true,
      message: `Migration completed. ${migrated} users migrated, ${errors} errors.`,
      migrated: migrated,
      errors: errors
    };
  } catch (error) {
    console.error("❌ Migration error:", error);
    throw new functions.https.HttpsError("internal", `Migration failed: ${error.message}`);
  }
});
