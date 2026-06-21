package com.example.data

import kotlinx.coroutines.flow.Flow

class ResumeRepository(private val resumeDao: ResumeDao) {
    val allProfiles: Flow<List<ResumeProfile>> = resumeDao.getAllProfilesFlow()

    suspend fun getProfileById(id: Int): ResumeProfile? {
        return resumeDao.getProfileById(id)
    }

    suspend fun getAllProfilesList(): List<ResumeProfile> {
        return resumeDao.getAllProfiles()
    }

    suspend fun saveProfile(profile: ResumeProfile): Long {
        return resumeDao.insertProfile(profile.copy(lastUpdated = System.currentTimeMillis()))
    }

    suspend fun deleteProfile(id: Int) {
        resumeDao.deleteProfileById(id)
    }
}
