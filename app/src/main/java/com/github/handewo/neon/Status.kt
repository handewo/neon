package com.github.handewo.neon

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import androidx.room.Database
import androidx.room.Room

// Define an entity to represent the editor status
@androidx.room.Entity(tableName = "editor_status")
data class EditorStatus(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val fontSize: Int,
    val fontColor: Int,
    val speed: Long,
    val bgColor: Int,
    val cutout: Boolean,
    val shadow: Float,
    val orientation: Int
)

// Create a DAO to access the database
@androidx.room.Dao
interface EditorStatusDao {
    @androidx.room.Insert
    suspend fun insert(editorStatus: EditorStatus)

    @androidx.room.Update
    suspend fun update(editorStatus: EditorStatus)

    @androidx.room.Query("SELECT * FROM editor_status WHERE id = 1")
    suspend fun getLastStatus(): EditorStatus?
}

@Database(entities = [EditorStatus::class], version = 4)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun editorStatusDao(): EditorStatusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "editor_database"
                ).addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adding a new column called "cutout"
        db.execSQL("ALTER TABLE editor_status ADD COLUMN cutout Boolean NOT NULL DEFAULT false")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adding a new column called "shadow"
        db.execSQL("ALTER TABLE editor_status ADD COLUMN shadow REAL NOT NULL DEFAULT 30")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adding a new column called "shadow"
        db.execSQL("ALTER TABLE editor_status ADD COLUMN orientation INT NOT NULL DEFAULT 0")
    }
}