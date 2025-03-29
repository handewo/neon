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
    val speed:Long,
    val bgColor:Int,
    val cutout:Boolean,
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

@Database(entities = [EditorStatus::class], version = 2)
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
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Adding a new column called "cutout"
        db.execSQL("ALTER TABLE editor_status ADD COLUMN cutout Boolean")
    }
}