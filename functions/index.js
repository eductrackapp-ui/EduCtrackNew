const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {setGlobalOptions} = require("firebase-functions/v2/options");
const admin = require("firebase-admin");

admin.initializeApp();

// Limiter les ressources globales
setGlobalOptions({maxInstances: 10});

/**
 * Déclenchée à chaque création d’un document dans /admins/{adminId}
 * Attribue automatiquement le rôle "admin" à l’utilisateur correspondant
 */
exports.setAdminRole = onDocumentCreated("admins/{adminId}", async (event) => {
  const uid = event.params.adminId;

  try {
    // Vérifier que l’utilisateur existe dans Firebase Auth
    const user = await admin.auth().getUser(uid);
    if (!user) {
      console.error(`❌ Utilisateur ${uid} introuvable`);
      return;
    }

    // Récupérer les claims existants
    const existingClaims = user.customClaims || {};

    // Ajouter/mettre à jour le rôle admin sans écraser les autres claims
    await admin.auth().setCustomUserClaims(uid, {
      ...existingClaims,
      role: "admin",
    });

    console.log(`✅ Rôle admin attribué à l’utilisateur ${uid}`);
  } catch (error) {
    console.error("❌ Erreur lors de l’attribution du rôle admin :", error);
  }
});
