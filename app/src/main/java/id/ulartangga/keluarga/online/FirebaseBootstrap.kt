package id.ulartangga.keluarga.online

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

private const val FIREBASE_APP_NAME = "ular-tangga-keluarga"

/** Inisialisasi FirebaseApp manual dari FirebaseConfig, tanpa google-services.json. */
object FirebaseBootstrap {
    private var database: FirebaseDatabase? = null

    fun database(context: Context): FirebaseDatabase? {
        if (!FirebaseConfig.isConfigured) return null
        database?.let { return it }

        val app = FirebaseApp.getApps(context).firstOrNull { it.name == FIREBASE_APP_NAME }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApiKey(FirebaseConfig.API_KEY)
                    .setApplicationId(FirebaseConfig.APPLICATION_ID)
                    .setProjectId(FirebaseConfig.PROJECT_ID)
                    .setDatabaseUrl(FirebaseConfig.DATABASE_URL)
                    .build(),
                FIREBASE_APP_NAME
            )

        return FirebaseDatabase.getInstance(app).also { database = it }
    }
}
