package com.example.data

import android.content.Context
import androidx.room.Room

object MyDatabaseProvider {
    private var database: ResumeDatabase? = null
    private var repository: ResumeRepository? = null

    fun getDatabase(context: Context): ResumeDatabase {
        return database ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                ResumeDatabase::class.java,
                "design_cv_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            database = db
            db
        }
    }

    fun getRepository(context: Context): ResumeRepository {
        return repository ?: synchronized(this) {
            val repo = ResumeRepository(getDatabase(context).resumeDao())
            repository = repo
            repo
        }
    }
}
