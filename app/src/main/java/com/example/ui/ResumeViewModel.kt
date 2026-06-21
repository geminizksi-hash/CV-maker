package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ResumeViewModel(private val repository: ResumeRepository) : ViewModel() {

    // All active resumes in DB
    val allProfiles: StateFlow<List<ResumeProfile>> = repository.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current global Dark Mode state of editing app
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    // Grid system alignment overlay check toggle
    var isGridOverlayEnabled by mutableStateOf(false)

    // Currently selected/edited profile
    var currentProfile by mutableStateOf<ResumeProfile?>(null)
        private set

    // Simple temporary fields for the live text editing state inside composer
    var editedProfileName by mutableStateOf("")
    var editedFullName by mutableStateOf("")
    var editedJobTitle by mutableStateOf("")
    var editedEmail by mutableStateOf("")
    var editedPhone by mutableStateOf("")
    var editedAddr by mutableStateOf("")
    var editedWeb by mutableStateOf("")
    var editedLinkedin by mutableStateOf("")
    var editedGithub by mutableStateOf("")
    var editedSummary by mutableStateOf("")
    
    var editedPhotoPath by mutableStateOf<String?>(null)
    var editedPhotoFilterIndex by mutableStateOf(0)
    var editedPhotoCropScale by mutableStateOf(1.0f)
    var editedResumeType by mutableStateOf("student") // "student" or "professional"
    var editedTemplateId by mutableStateOf("template_modern")
    var editedColorHex by mutableStateOf("#2196F3")

    // Dynamic lists that populate fields
    val editingSkills = mutableStateListOf<SkillEntry>()
    val editingExperience = mutableStateListOf<ExperienceEntry>()
    val editingEducation = mutableStateListOf<EducationEntry>()
    val editingProjects = mutableStateListOf<ProjectEntry>()
    val editingLanguages = mutableStateListOf<LanguageEntry>()
    val editingCertifications = mutableStateListOf<String>()

    // AI Suggestion state
    var isAiAnalyzing by mutableStateOf(false)
    var aiSuggestionTitle by mutableStateOf("")
    var aiSuggestionSummary by mutableStateOf("")
    var aiErrorStatus by mutableStateOf<String?>(null)
    var customGeminiApiKey by mutableStateOf("")

    fun loadCustomApiKey(context: Context) {
        val prefs = context.getSharedPreferences("design_cv_prefs", Context.MODE_PRIVATE)
        customGeminiApiKey = prefs.getString("gemini_api_key_custom", "") ?: ""
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences("design_cv_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key_custom", key.trim()).apply()
        customGeminiApiKey = key.trim()
    }

    fun fetchAiSuggestions(context: Context) {
        viewModelScope.launch {
            if (editedJobTitle.isEmpty() && editedSummary.isEmpty() && editingSkills.isEmpty()) {
                aiErrorStatus = "বিশ্লেষণ করতে প্রোফাইল টাইটেল বা সামারি দিন।"
                return@launch
            }
            isAiAnalyzing = true
            aiErrorStatus = null
            aiSuggestionTitle = ""
            aiSuggestionSummary = ""
            
            val prefs = context.getSharedPreferences("design_cv_prefs", Context.MODE_PRIVATE)
            val savedKey = prefs.getString("gemini_api_key_custom", "") ?: ""
            
            val result = GeminiAiHelper.getProfileSuggestions(
                currentTitle = editedJobTitle,
                currentSummary = editedSummary,
                skillsList = editingSkills.map { it.name },
                customApiKey = savedKey.ifBlank { null }
            )
            
            if (result.error != null) {
                aiErrorStatus = result.error
            } else {
                aiSuggestionTitle = result.jobTitle
                aiSuggestionSummary = result.summary
            }
            isAiAnalyzing = false
        }
    }
    
    fun applyAiSuggestions() {
        if (aiSuggestionTitle.isNotEmpty()) {
            editedJobTitle = aiSuggestionTitle
        }
        if (aiSuggestionSummary.isNotEmpty()) {
            editedSummary = aiSuggestionSummary
        }
        aiSuggestionTitle = ""
        aiSuggestionSummary = ""
        aiErrorStatus = null
    }

    fun dismissAiSuggestions() {
        aiSuggestionTitle = ""
        aiSuggestionSummary = ""
        aiErrorStatus = null
    }

    // Cloud sync states
    var cloudSyncStatus by mutableStateOf("Not synced")
    var isSyncing by mutableStateOf(false)

    fun startNewResume(resumeType: String) {
        val blank = ResumeProfile(
            profileName = if (resumeType == "student") "Dev Shoriful CV" else "Shoriful Islam Professional CV",
            fullName = "Dev Shoriful",
            jobTitle = if (resumeType == "student") "Creative Software Developer" else "Senior Professional Developer",
            email = "info.shorif0000@gmail.com",
            phone = "01558118588",
            address = "Sherpur, 2100, Bangladesh",
            website = "netlify.com",
            linkedin = "linkedin.com/in/shoriful",
            github = "github.com/shoriful",
            summary = "Proactive Software Developer and Customer Service Representative achieving high customer satisfaction through effective communication. Resolves complexity by employing creative problem-solving techniques.",
            resumeType = resumeType,
            selectedTemplateId = "template_shoriful",
            customColorHex = "#1E88E5"
        )
        selectProfileForEditing(blank)
    }

    fun selectProfileForEditing(profile: ResumeProfile) {
        currentProfile = profile
        
        // Populate inputs
        editedProfileName = profile.profileName
        editedFullName = profile.fullName
        editedJobTitle = profile.jobTitle
        editedEmail = profile.email
        editedPhone = profile.phone
        editedAddr = profile.address
        editedWeb = profile.website
        editedLinkedin = profile.linkedin
        editedGithub = profile.github
        editedSummary = profile.summary
        
        editedPhotoPath = profile.photoPath
        editedPhotoFilterIndex = profile.photoFilterIndex
        editedPhotoCropScale = profile.photoCropScale
        editedResumeType = profile.resumeType
        editedTemplateId = profile.selectedTemplateId
        editedColorHex = profile.customColorHex

        // Populate lists
        editingSkills.clear()
        editingSkills.addAll(JsonHelpers.parseSkills(profile.skillsJson))

        editingExperience.clear()
        editingExperience.addAll(JsonHelpers.parseExperience(profile.experienceJson))

        editingEducation.clear()
        editingEducation.addAll(JsonHelpers.parseEducation(profile.educationJson))

        editingProjects.clear()
        editingProjects.addAll(JsonHelpers.parseProjects(profile.projectsJson))

        editingLanguages.clear()
        editingLanguages.addAll(JsonHelpers.parseLanguages(profile.languagesJson))

        editingCertifications.clear()
        editingCertifications.addAll(JsonHelpers.parseCertifications(profile.certificationsJson))

        // Trigger safe default lists to give user beautiful starting template values!
        if (editingSkills.isEmpty()) {
            editingSkills.add(SkillEntry("Project management", 0.9f))
            editingSkills.add(SkillEntry("Process optimization", 0.85f))
            editingSkills.add(SkillEntry("Safety compliance", 0.8f))
            editingSkills.add(SkillEntry("Problem analysis", 0.75f))
        }
        if (editingEducation.isEmpty()) {
            editingEducation.add(EducationEntry("Daffodil International University", "B.Sc. in Software Engineering", "2026", "2030", "GPA: 3.85"))
        }
        if (editingProjects.isEmpty()) {
            editingProjects.add(ProjectEntry("DesignCV Pro App", "Lead Craft Developer", "Advanced Jetpack Compose Resume Maker app with secure local capabilities.", "github.com/shoriful/design_cv"))
        }
        if (editingLanguages.isEmpty()) {
            editingLanguages.add(LanguageEntry("Bangla", "Native"))
            editingLanguages.add(LanguageEntry("English", "Fluent"))
        }
        if (editingCertifications.isEmpty()) {
            editingCertifications.add("Chartered Certified Software Developer")
            editingCertifications.add("Executive Management Professional")
            editingCertifications.add("Certified Scrum Product Owner (CSPO)")
        }
        if (editingExperience.isEmpty()) {
            editingExperience.add(ExperienceEntry("Tech Innovations Ltd", "Associate Software Engineer", "01/2025", "01/2026", "Collaborated with team members to achieve target results. Maintained clean, safe working environments to maximize operational efficiency."))
        }
    }

    // Capture the absolute current live design and serialize it
    fun getSerializedStateProfile(): ResumeProfile {
        val base = currentProfile ?: ResumeProfile(profileName = "Temporary Resume")
        return base.copy(
            profileName = editedProfileName,
            fullName = editedFullName,
            jobTitle = editedJobTitle,
            email = editedEmail,
            phone = editedPhone,
            address = editedAddr,
            website = editedWeb,
            linkedin = editedLinkedin,
            github = editedGithub,
            summary = editedSummary,
            photoPath = editedPhotoPath,
            photoFilterIndex = editedPhotoFilterIndex,
            photoCropScale = editedPhotoCropScale,
            resumeType = editedResumeType,
            selectedTemplateId = editedTemplateId,
            customColorHex = editedColorHex,
            skillsJson = JsonHelpers.serializeSkills(editingSkills),
            experienceJson = JsonHelpers.serializeExperience(editingExperience),
            educationJson = JsonHelpers.serializeEducation(editingEducation),
            projectsJson = JsonHelpers.serializeProjects(editingProjects),
            languagesJson = JsonHelpers.serializeLanguages(editingLanguages),
            certificationsJson = JsonHelpers.serializeCertifications(editingCertifications)
        )
    }

    fun saveCurrentProfileToDb(onComplete: () -> Unit) {
        val updated = getSerializedStateProfile()
        viewModelScope.launch {
            val id = repository.saveProfile(updated)
            currentProfile = updated.copy(id = id.toInt())
            onComplete()
        }
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteProfile(id)
        }
    }

    fun duplicateProfile(profile: ResumeProfile) {
        viewModelScope.launch {
            val copy = profile.copy(
                id = 0,
                profileName = "${profile.profileName} (Copy)",
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveProfile(copy)
        }
    }

    // Handles picking and caching images inside safe app location
    fun savePickedPhoto(context: Context, uri: Uri) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val fileName = "profile_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                editedPhotoPath = file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Remove photo
    fun removePhoto() {
        editedPhotoPath = null
    }

    // Simulated cloud sync function block
    fun syncWithCloud() {
        if (isSyncing) return
        isSyncing = true
        cloudSyncStatus = "Connecting..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            cloudSyncStatus = "Uploading resumes..."
            kotlinx.coroutines.delay(1000)
            cloudSyncStatus = "Synced cleanly at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
            isSyncing = false
        }
    }

    fun importResumeFromJson(jsonString: String, onError: () -> Unit, onSuccess: () -> Unit) {
        try {
            val obj = org.json.JSONObject(jsonString)
            val profile = ResumeProfile(
                profileName = obj.optString("profileName", "Imported CV"),
                fullName = obj.optString("fullName", ""),
                jobTitle = obj.optString("jobTitle", ""),
                email = obj.optString("email", ""),
                phone = obj.optString("phone", ""),
                address = obj.optString("address", ""),
                website = obj.optString("website", ""),
                linkedin = obj.optString("linkedin", ""),
                github = obj.optString("github", ""),
                summary = obj.optString("summary", ""),
                photoPath = null,
                photoFilterIndex = obj.optInt("photoFilterIndex", 0),
                photoCropScale = obj.optDouble("photoCropScale", 1.0).toFloat(),
                resumeType = obj.optString("resumeType", "student"),
                selectedTemplateId = obj.optString("selectedTemplateId", "template_modern"),
                customColorHex = obj.optString("customColorHex", "#2196F3"),
                skillsJson = obj.optString("skillsJson", "[]"),
                experienceJson = obj.optString("experienceJson", "[]"),
                educationJson = obj.optString("educationJson", "[]"),
                projectsJson = obj.optString("projectsJson", "[]"),
                languagesJson = obj.optString("languagesJson", "[]"),
                certificationsJson = obj.optString("certificationsJson", "[]")
            )
            viewModelScope.launch {
                repository.saveProfile(profile)
                onSuccess()
            }
        } catch (e: Exception) {
            onError()
        }
    }

    fun exportResumeAsJsonString(profile: ResumeProfile): String {
        return try {
            val obj = org.json.JSONObject()
            obj.put("profileName", profile.profileName)
            obj.put("fullName", profile.fullName)
            obj.put("jobTitle", profile.jobTitle)
            obj.put("email", profile.email)
            obj.put("phone", profile.phone)
            obj.put("address", profile.address)
            obj.put("website", profile.website)
            obj.put("linkedin", profile.linkedin)
            obj.put("github", profile.github)
            obj.put("summary", profile.summary)
            obj.put("photoFilterIndex", profile.photoFilterIndex)
            obj.put("photoCropScale", profile.photoCropScale)
            obj.put("resumeType", profile.resumeType)
            obj.put("selectedTemplateId", profile.selectedTemplateId)
            obj.put("customColorHex", profile.customColorHex)
            obj.put("skillsJson", profile.skillsJson)
            obj.put("experienceJson", profile.experienceJson)
            obj.put("educationJson", profile.educationJson)
            obj.put("projectsJson", profile.projectsJson)
            obj.put("languagesJson", profile.languagesJson)
            obj.put("certificationsJson", profile.certificationsJson)
            obj.toString(2)
        } catch (_: Exception) {
            "{}"
        }
    }
}

// Factory Provider
class ResumeViewModelFactory(private val repository: ResumeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResumeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResumeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
