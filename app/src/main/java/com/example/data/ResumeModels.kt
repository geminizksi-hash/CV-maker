package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

// Main Resume Profile Entity
@Entity(tableName = "resume_profiles")
data class ResumeProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileName: String = "",
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val website: String = "",
    val linkedin: String = "",
    val github: String = "",
    val summary: String = "",
    val photoPath: String? = null,
    val photoFilterIndex: Int = 0,
    val photoCropScale: Float = 1.0f,
    val resumeType: String = "student", // "student" or "professional"
    val selectedTemplateId: String = "template_modern",
    val customColorHex: String = "#2196F3",
    val skillsJson: String = "[]",
    val experienceJson: String = "[]",
    val educationJson: String = "[]",
    val projectsJson: String = "[]",
    val languagesJson: String = "[]",
    val certificationsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)

// Individual data models for parsing/editing
data class SkillEntry(
    val name: String,
    val level: Float // 0.0 to 1.0
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("name", name)
        obj.put("level", level.toDouble())
        return obj
    }
    companion object {
        fun fromJsonObject(obj: JSONObject): SkillEntry {
            return SkillEntry(
                name = obj.optString("name", ""),
                level = obj.optDouble("level", 0.5).toFloat()
            )
        }
    }
}

data class ExperienceEntry(
    val company: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val description: String
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("company", company)
        obj.put("role", role)
        obj.put("startDate", startDate)
        obj.put("endDate", endDate)
        obj.put("description", description)
        return obj
    }
    companion object {
        fun fromJsonObject(obj: JSONObject): ExperienceEntry {
            return ExperienceEntry(
                company = obj.optString("company", ""),
                role = obj.optString("role", ""),
                startDate = obj.optString("startDate", ""),
                endDate = obj.optString("endDate", ""),
                description = obj.optString("description", "")
            )
        }
    }
}

data class EducationEntry(
    val school: String,
    val degree: String,
    val startDate: String,
    val endDate: String,
    val result: String // CGPA/Grade
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("school", school)
        obj.put("degree", degree)
        obj.put("startDate", startDate)
        obj.put("endDate", endDate)
        obj.put("result", result)
        return obj
    }
    companion object {
        fun fromJsonObject(obj: JSONObject): EducationEntry {
            return EducationEntry(
                school = obj.optString("school", ""),
                degree = obj.optString("degree", ""),
                startDate = obj.optString("startDate", ""),
                endDate = obj.optString("endDate", ""),
                result = obj.optString("result", "")
            )
        }
    }
}

data class ProjectEntry(
    val title: String,
    val role: String,
    val description: String,
    val link: String
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("title", title)
        obj.put("role", role)
        obj.put("description", description)
        obj.put("link", link)
        return obj
    }
    companion object {
        fun fromJsonObject(obj: JSONObject): ProjectEntry {
            return ProjectEntry(
                title = obj.optString("title", ""),
                role = obj.optString("role", ""),
                description = obj.optString("description", ""),
                link = obj.optString("link", "")
            )
        }
    }
}

data class LanguageEntry(
    val language: String,
    val proficiency: String // Fluent, Intermediate, Basic
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("language", language)
        obj.put("proficiency", proficiency)
        return obj
    }
    companion object {
        fun fromJsonObject(obj: JSONObject): LanguageEntry {
            return LanguageEntry(
                language = obj.optString("language", ""),
                proficiency = obj.optString("proficiency", "")
            )
        }
    }
}

// Utility extension function to parse JSON
object JsonHelpers {
    fun parseSkills(json: String): List<SkillEntry> {
        val list = mutableListOf<SkillEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(SkillEntry.fromJsonObject(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeSkills(list: List<SkillEntry>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJsonObject())
        }
        return array.toString()
    }

    fun parseExperience(json: String): List<ExperienceEntry> {
        val list = mutableListOf<ExperienceEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(ExperienceEntry.fromJsonObject(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeExperience(list: List<ExperienceEntry>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJsonObject())
        }
        return array.toString()
    }

    fun parseEducation(json: String): List<EducationEntry> {
        val list = mutableListOf<EducationEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(EducationEntry.fromJsonObject(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeEducation(list: List<EducationEntry>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJsonObject())
        }
        return array.toString()
    }

    fun parseProjects(json: String): List<ProjectEntry> {
        val list = mutableListOf<ProjectEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(ProjectEntry.fromJsonObject(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeProjects(list: List<ProjectEntry>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJsonObject())
        }
        return array.toString()
    }

    fun parseLanguages(json: String): List<LanguageEntry> {
        val list = mutableListOf<LanguageEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(LanguageEntry.fromJsonObject(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeLanguages(list: List<LanguageEntry>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJsonObject())
        }
        return array.toString()
    }

    fun parseCertifications(json: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeCertifications(list: List<String>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item)
        }
        return array.toString()
    }
}

// Room Data Access Object
@Dao
interface ResumeDao {
    @Query("SELECT * FROM resume_profiles ORDER BY lastUpdated DESC")
    fun getAllProfilesFlow(): Flow<List<ResumeProfile>>

    @Query("SELECT * FROM resume_profiles ORDER BY lastUpdated DESC")
    suspend fun getAllProfiles(): List<ResumeProfile>

    @Query("SELECT * FROM resume_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): ResumeProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ResumeProfile): Long

    @Query("DELETE FROM resume_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)
}

// Room Database
@Database(entities = [ResumeProfile::class], version = 1, exportSchema = false)
abstract class ResumeDatabase : RoomDatabase() {
    abstract fun resumeDao(): ResumeDao
}
