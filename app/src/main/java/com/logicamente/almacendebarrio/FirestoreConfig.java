package com.logicamente.almacendebarrio;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
public class FirestoreConfig {

        public static FirebaseFirestore getFirestoreInstance() {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            firebaseFirestore.setFirestoreSettings(settings);
            return firebaseFirestore;
        }
}
