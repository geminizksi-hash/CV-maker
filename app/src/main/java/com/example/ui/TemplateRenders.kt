package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link

// Color filters mapping
object PhotoFilters {
    val NORMAL = ColorMatrix()
    
    val NOIR = ColorMatrix().apply { setToSaturation(0f) }
    
    val WARM = ColorMatrix(floatArrayOf(
        1.15f, 0f, 0f, 0f, 15f,
        0f, 1.0f, 0f, 0f, 10f,
        0f, 0f, 0.85f, 0f, -10f,
        0f, 0f, 0f, 1.0f, 0f
    ))
    
    val COOL = ColorMatrix(floatArrayOf(
        0.85f, 0f, 0f, 0f, -10f,
        0f, 1.0f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 20f,
        0f, 0f, 0f, 1.0f, 0f
    ))
    
    val VINTAGE = ColorMatrix(floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1.0f, 0f
    ))

    fun getFilterMatrix(index: Int): ColorMatrix {
        return when (index) {
            1 -> NOIR
            2 -> WARM
            3 -> COOL
            4 -> VINTAGE
            else -> NORMAL
        }
    }
}

@Composable
fun RenderOnScreenResume(
    profile: ResumeProfile,
    modifier: Modifier = Modifier
) {
    val skills = JsonHelpers.parseSkills(profile.skillsJson)
    val experiences = JsonHelpers.parseExperience(profile.experienceJson)
    val education = JsonHelpers.parseEducation(profile.educationJson)
    val projects = JsonHelpers.parseProjects(profile.projectsJson)
    val languages = JsonHelpers.parseLanguages(profile.languagesJson)
    val certifications = JsonHelpers.parseCertifications(profile.certificationsJson)

    // Parse primary color
    val accentColor = try {
        Color(android.graphics.Color.parseColor(profile.customColorHex))
    } catch (_: Exception) {
        Color(0xFF2196F3)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (profile.selectedTemplateId) {
            "template_creative" -> {
                CreativeTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
            "template_minimal" -> {
                MinimalTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
            "template_executive" -> {
                ExecutiveTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
            "template_stylish" -> {
                StylishTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
            "template_shoriful" -> {
                ShorifulTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
            else -> {
                ModernTemplateView(profile, accentColor, skills, experiences, education, projects, languages, certifications)
            }
        }
    }
}

@Composable
fun TemplatePhotoHeader(
    photoPath: String?,
    filterIndex: Int,
    cropScale: Float,
    borderAccent: Color,
    isSquare: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageBitmap = remember(photoPath) {
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                bitmap?.asImageBitmap()
            } else null
        } else null
    }

    if (imageBitmap != null) {
        val shape = if (isSquare) RoundedCornerShape(12.dp) else CircleShape
        Box(
            modifier = modifier
                .size(90.dp)
                .clip(shape)
                .border(2.dp, borderAccent, shape)
                .background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(PhotoFilters.getFilterMatrix(filterIndex)),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 0.dp) // crop offset placeholder
            )
        }
    }
}

// 1. Template: Modern Corporate
@Composable
fun ModernTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column (Sidebar)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(130.dp)
                .background(Color(0xFFF3F4F6))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TemplatePhotoHeader(
                photoPath = profile.photoPath,
                filterIndex = profile.photoFilterIndex,
                cropScale = profile.photoCropScale,
                borderAccent = accent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info
            Text(
                "CONTACT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            
            val infoItems = listOf(
                profile.email,
                profile.phone,
                profile.address,
                profile.website,
                profile.linkedin,
                profile.github
            )
            for (item in infoItems) {
                if (item.isNotEmpty()) {
                    Text(
                        item,
                        fontSize = 8.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        lineHeight = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skills
            if (skills.isNotEmpty()) {
                Text(
                    "SKILLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.fillMaxWidth()
                )
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                for (skill in skills) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(skill.name, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { skill.level },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = accent,
                            trackColor = Color(0xFFD1D5DB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Languages
            if (languages.isNotEmpty()) {
                Text(
                    "LANGUAGES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.fillMaxWidth()
                )
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                for (lang in languages) {
                    Text(
                        "${lang.language} (${lang.proficiency})",
                        fontSize = 8.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                    )
                }
            }
        }

        // Right Column (Details)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                profile.fullName.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                profile.jobTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Professional Summary
            if (profile.summary.isNotEmpty()) {
                Text("PROFESSIONAL SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                Text(
                    profile.summary,
                    fontSize = 8.5.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Work Experience (show based on profile path or availability)
            val showExp = profile.resumeType == "professional" || experiences.isNotEmpty()
            if (showExp && experiences.isNotEmpty()) {
                Text("WORK EXPERIENCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                for (exp in experiences) {
                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${exp.role} @ ${exp.company}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text("${exp.startDate} - ${exp.endDate}", fontSize = 8.sp, color = accent)
                        }
                        Text(exp.description, fontSize = 8.sp, color = Color(0xFF4B5563), lineHeight = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Projects
            if (projects.isNotEmpty()) {
                Text("PROJECTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                for (proj in projects) {
                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(proj.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        if (proj.role.isNotEmpty()) {
                            Text("Role: ${proj.role}", fontSize = 8.sp, color = Color(0xFF6B7280), fontStyle = FontStyle.Italic)
                        }
                        Text(proj.description, fontSize = 8.sp, color = Color(0xFF4B5563), lineHeight = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Education
            if (education.isNotEmpty()) {
                Text("EDUCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Divider(color = accent, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                for (edu in education) {
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${edu.degree}  |  ${edu.school}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text("${edu.startDate} - ${edu.endDate}", fontSize = 8.sp, color = Color(0xFF4B5563))
                        }
                        Text("Grade: ${edu.result}", fontSize = 8.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}

// 2. Template: Creative Tech
@Composable
fun CreativeTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TemplatePhotoHeader(
                photoPath = profile.photoPath,
                filterIndex = profile.photoFilterIndex,
                cropScale = profile.photoCropScale,
                borderAccent = Color.White,
                isSquare = true
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    profile.fullName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    profile.jobTitle,
                    fontSize = 12.sp,
                    color = Color(0xFFE5E7EB)
                )
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left sidebar inside layout
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(135.dp)
                    .background(Color(0xFFFAFAFA))
                    .padding(12.dp)
            ) {
                Text("CONTACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(modifier = Modifier.height(4.dp))
                val list = listOf(profile.email, profile.phone, profile.address, profile.website)
                for (item in list) {
                    if (item.isNotEmpty()) {
                        Text(item, fontSize = 8.sp, color = Color(0xFF374151), modifier = Modifier.padding(vertical = 1.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("TECH SKILLS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(modifier = Modifier.height(4.dp))
                for (sk in skills) {
                    Text("▪ ${sk.name}", fontSize = 8.sp, color = Color(0xFF374151), modifier = Modifier.padding(vertical = 1.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("LANGUAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(modifier = Modifier.height(4.dp))
                for (ln in languages) {
                    Text("• ${ln.language} (${ln.proficiency})", fontSize = 8.sp, color = Color(0xFF374151))
                }
            }

            // Vertical divider line
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(accent))

            // Details on right
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                if (profile.summary.isNotEmpty()) {
                    Text("ABOUT ME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile.summary, fontSize = 8.5.sp, color = Color(0xFF374151), lineHeight = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (experiences.isNotEmpty()) {
                    Text("EXPERIENCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    for (exp in experiences) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("${exp.role} | ${exp.company}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text("${exp.startDate} - ${exp.endDate}", fontSize = 7.5.sp, color = Color(0xFF6B7280))
                            Text(exp.description, fontSize = 8.sp, color = Color(0xFF4B5563))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (projects.isNotEmpty()) {
                    Text("CORNERSTONE PROJECTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    for (proj in projects) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(proj.title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            Text(proj.description, fontSize = 8.sp, color = Color(0xFF4B5563))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (education.isNotEmpty()) {
                    Text("EDUCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    for (edu in education) {
                        Text("${edu.degree} - ${edu.school} (${edu.startDate})", fontSize = 8.sp, color = Color(0xFF374151))
                    }
                }
            }
        }
    }
}

// 3. Template: Minimalist Slate
@Composable
fun MinimalTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            profile.fullName.uppercase(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color(0xFF111827)
        )
        Text(
            profile.jobTitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        val contactLine = listOfNotNull(
            profile.email.takeIf { it.isNotEmpty() },
            profile.phone.takeIf { it.isNotEmpty() },
            profile.address.takeIf { it.isNotEmpty() },
            profile.website.takeIf { it.isNotEmpty() }
        ).joinToString("   |   ")

        Text(contactLine, fontSize = 8.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
        Spacer(modifier = Modifier.height(12.dp))

        // Profile summary
        if (profile.summary.isNotEmpty()) {
            Text("SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(2.dp))
            Text(profile.summary, fontSize = 8.5.sp, color = Color(0xFF4B5563), lineHeight = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Experience
        if (experiences.isNotEmpty()) {
            Text("CAREER HISTORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(3.dp))
            for (exp in experiences) {
                Column(modifier = Modifier.padding(vertical = 3.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${exp.role}  —  ${exp.company}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text("${exp.startDate} - ${exp.endDate}", fontSize = 8.sp, color = accent)
                    }
                    Text(exp.description, fontSize = 8.sp, color = Color(0xFF4B5563))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Projects
        if (projects.isNotEmpty()) {
            Text("SELECTED PROJECTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(3.dp))
            for (proj in projects) {
                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(proj.title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(proj.description, fontSize = 8.sp, color = Color(0xFF4B5563))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Education
        if (education.isNotEmpty()) {
            Text("ACADEMIC HISTORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(3.dp))
            for (edu in education) {
                Text(
                    "${edu.degree}  •  ${edu.school} (${edu.startDate} - ${edu.endDate})",
                    fontSize = 8.5.sp,
                    color = Color(0xFF374151)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Skills Footer
        Text("COMPETENCIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(2.dp))
        val listSkills = skills.joinToString { it.name }
        Text(listSkills, fontSize = 8.5.sp, color = Color(0xFF4B5563))

        if (languages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("LANGUAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(2.dp))
            val listLangs = languages.joinToString { "${it.language} (${it.proficiency})" }
            Text(listLangs, fontSize = 8.5.sp, color = Color(0xFF4B5563))
        }
    }
}

// 4. Template: Executive Classic
@Composable
fun ExecutiveTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .border(1.dp, Color(0xFFCCCCCC))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Centered Header
            Text(
                profile.fullName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1A2530),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            val info = "${profile.jobTitle}  •  ${profile.email}  •  ${profile.phone}  •  ${profile.address}"
            Text(
                info,
                fontSize = 8.sp,
                color = Color(0xFF555555),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(accent))
            Spacer(modifier = Modifier.height(10.dp))

            // Summary
            if (profile.summary.isNotEmpty()) {
                Text("EXECUTIVE STATEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2530), fontFamily = FontFamily.Serif)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray).padding(vertical = 1.dp))
                Text(profile.summary, fontSize = 8.sp, color = Color.Black, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
            }

            // Experience
            if (experiences.isNotEmpty()) {
                Text("CHRONOLOGICAL CAREER REVIEWS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2530), fontFamily = FontFamily.Serif)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray).padding(vertical = 1.dp))
                Spacer(modifier = Modifier.height(2.dp))
                for (exp in experiences) {
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${exp.role.uppercase()}  -  ${exp.company}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("${exp.startDate} - ${exp.endDate}", fontSize = 8.sp, color = accent)
                        }
                        Text(exp.description, fontSize = 8.sp, color = Color(0xFF111111))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Projects
            if (projects.isNotEmpty()) {
                Text("NOTABLE PORTFOLIOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2530), fontFamily = FontFamily.Serif)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray).padding(vertical = 1.dp))
                Spacer(modifier = Modifier.height(2.dp))
                for (proj in projects) {
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(proj.title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(proj.description, fontSize = 8.sp, color = Color(0xFF333333))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Education
            if (education.isNotEmpty()) {
                Text("ACADEMIC CREDENTIALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2530), fontFamily = FontFamily.Serif)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray).padding(vertical = 1.dp))
                Spacer(modifier = Modifier.height(2.dp))
                for (edu in education) {
                    Text(
                        "${edu.degree}  -  ${edu.school} (Grade: ${edu.result})",
                        fontSize = 8.5.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Skills & Languages (Executive Style)
            if (skills.isNotEmpty() || languages.isNotEmpty()) {
                Text("CORE COMPETENCIES & LANGUAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2530), fontFamily = FontFamily.Serif)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray).padding(vertical = 1.dp))
                Spacer(modifier = Modifier.height(2.dp))
                if (skills.isNotEmpty()) {
                    val listSkills = skills.joinToString { it.name }
                    Text("Skills: $listSkills", fontSize = 8.5.sp, color = Color.Black)
                }
                if (languages.isNotEmpty()) {
                    val listLangs = languages.joinToString { "${it.language} (${it.proficiency})" }
                    Text("Languages: $listLangs", fontSize = 8.5.sp, color = Color.Black, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

// 5. Grid Overlay lines helper to support precise alignment checking!
// 5. Template: Stylish Premium
@Composable
fun StylishTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Banner Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.fullName.uppercase(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        profile.jobTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                
                // Rounded corner photo in header
                TemplatePhotoHeader(
                    photoPath = profile.photoPath,
                    filterIndex = profile.photoFilterIndex,
                    cropScale = profile.photoCropScale,
                    borderAccent = Color.White,
                    isSquare = true,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))
            
            // Subtitle Info
            val contactItems = listOfNotNull(
                profile.email.takeIf { it.isNotEmpty() },
                profile.phone.takeIf { it.isNotEmpty() },
                profile.address.takeIf { it.isNotEmpty() }
            )
            Text(
                contactItems.joinToString("  •  "),
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        // Two Column Body
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Left Column (Sidebar Contact & Skills & Languages)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .background(Color(0xFFF9FAFB))
                    .padding(10.dp)
            ) {
                // Personal links
                Text("LINKS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(modifier = Modifier.height(4.dp))
                val links = listOf(
                    profile.website to Icons.Default.Language,
                    profile.linkedin to Icons.Default.Link,
                    profile.github to Icons.Default.Code
                )
                for (link in links) {
                    if (link.first.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Icon(link.second, contentDescription = "", tint = accent, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(link.first, fontSize = 7.sp, color = Color(0xFF374151), maxLines = 1)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Skills
                if (skills.isNotEmpty()) {
                    Text("TECH SKILLS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (sk in skills) {
                        Column(modifier = Modifier.padding(vertical = 1.dp)) {
                            Text(sk.name, fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                            LinearProgressIndicator(
                                progress = { sk.level },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(1.dp)),
                                color = accent,
                                trackColor = Color(0xFFE5E7EB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Languages
                if (languages.isNotEmpty()) {
                    Text("LANGUAGES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (lang in languages) {
                        Text("• ${lang.language}", fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                        Text(lang.proficiency, fontSize = 7.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }

            // Divider vertical line
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFE5E7EB)))

            // Right Column (Main details of Career, Projects, Edu)
            Column(
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .padding(10.dp)
            ) {
                // About Me
                if (profile.summary.isNotEmpty()) {
                    Text("ABOUT ME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile.summary, fontSize = 8.sp, color = Color(0xFF4B5563), lineHeight = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Experiences
                if (experiences.isNotEmpty()) {
                    Text("WORK EXPERIENCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (exp in experiences) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(exp.role, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("${exp.startDate} - ${exp.endDate}", fontSize = 7.sp, color = accent)
                            }
                            Text(exp.company, fontSize = 7.5.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(exp.description, fontSize = 7.5.sp, color = Color(0xFF4B5563), lineHeight = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Projects
                if (projects.isNotEmpty()) {
                    Text("KEY PROJECTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (proj in projects) {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(proj.title, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text(proj.description, fontSize = 7.5.sp, color = Color(0xFF4B5563), lineHeight = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Education
                if (education.isNotEmpty()) {
                    Text("EDUCATION AND LEARNING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (edu in education) {
                        Text("${edu.degree}  •  ${edu.school} (${edu.startDate})", fontSize = 7.5.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GridAlignmentOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeColor = Color(0x332196F3) // Highly transparent blue lines
        val stepX = 16.dp.toPx()
        val stepY = 16.dp.toPx()

        // Vertical Gridlines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = strokeColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.5.dp.toPx()
            )
            x += stepX
        }

        // Horizontal Gridlines
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = strokeColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5.dp.toPx()
            )
            y += stepY
        }
    }
}

@Composable
fun ShorifulTemplateView(
    profile: ResumeProfile,
    accent: Color,
    skills: List<SkillEntry>,
    experiences: List<ExperienceEntry>,
    education: List<EducationEntry>,
    projects: List<ProjectEntry>,
    languages: List<LanguageEntry>,
    certifications: List<String>
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column (Sidebar with Light Blue/Soft background)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(135.dp)
                .background(Color(0xFFEBF4FA)) // Light Blue / Steel blue tint representation
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            // Photo container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TemplatePhotoHeader(
                    photoPath = profile.photoPath,
                    filterIndex = profile.photoFilterIndex,
                    cropScale = profile.photoCropScale,
                    borderAccent = accent,
                    isSquare = true
                )
            }

            // Certifications
            if (certifications.isNotEmpty()) {
                Text(
                    text = "CERTIFICATIONS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B365D), // Navy Blue
                    letterSpacing = 0.5.sp
                )
                Divider(color = accent, thickness = 1.5.dp, modifier = Modifier.padding(top = 1.dp, bottom = 4.dp))
                certifications.forEach { cert ->
                    Text(
                        text = "• $cert",
                        fontSize = 8.sp,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(vertical = 1.dp),
                        lineHeight = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Contact info
            Text(
                text = "CONTACT",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B365D),
                letterSpacing = 0.5.sp
            )
            Divider(color = accent, thickness = 1.5.dp, modifier = Modifier.padding(top = 1.dp, bottom = 4.dp))
            
            val contacts = listOf(
                "📍" to profile.address.ifBlank { "Sherpur, Bangladesh" },
                "📞" to profile.phone.ifBlank { "01558118588" },
                "✉" to profile.email.ifBlank { "info.shorif0000@gmail.com" }
            )
            contacts.forEach { (icon, text) ->
                if (text.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(icon, fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = text,
                            fontSize = 8.sp,
                            color = Color(0xFF2C3E50),
                            lineHeight = 10.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Web/Links
            if (profile.website.isNotEmpty() || profile.linkedin.isNotEmpty() || profile.github.isNotEmpty()) {
                Text(
                    text = "LINKS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B365D),
                    letterSpacing = 0.5.sp
                )
                Divider(color = accent, thickness = 1.5.dp, modifier = Modifier.padding(top = 1.dp, bottom = 4.dp))
                
                val links = listOf(
                    "🌐" to profile.website,
                    "in" to profile.linkedin,
                    "git" to profile.github
                )
                links.forEach { (label, url) ->
                    if (url.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[$label] ",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = url,
                                fontSize = 8.sp,
                                color = Color(0xFF2C3E50),
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Skills
            if (skills.isNotEmpty()) {
                Text(
                    text = "SKILLS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B365D),
                    letterSpacing = 0.5.sp
                )
                Divider(color = accent, thickness = 1.5.dp, modifier = Modifier.padding(top = 1.dp, bottom = 4.dp))
                skills.forEach { skill ->
                    Text(
                        text = "• ${skill.name}",
                        fontSize = 8.sp,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(vertical = 1.5.dp),
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // Right Column (Details with elegant timeline styling)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header: Name
            Text(
                text = profile.fullName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1B365D), // Classic deep navy color
                lineHeight = 26.sp
            )
            
            // Subtitle
            Text(
                text = profile.jobTitle.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            // Professional Summary
            if (profile.summary.isNotEmpty()) {
                Text(
                    text = profile.summary,
                    fontSize = 9.sp,
                    color = Color(0xFF34495E),
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Divider(color = accent, thickness = 2.dp, modifier = Modifier.padding(bottom = 12.dp))

            // Experience Section
            if (experiences.isNotEmpty()) {
                Text(
                    text = "EXPERIENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B365D),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                experiences.forEach { exp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // timeline dates column
                        Column(modifier = Modifier.width(70.dp)) {
                            Text(
                                text = "${exp.startDate} - ${exp.endDate}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF566573)
                            )
                        }
                        
                        // timeline dot
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(8.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(5.dp)
                                    .background(accent, CircleShape)
                            )
                        }

                        // experience info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.role,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B365D)
                            )
                            Text(
                                text = exp.company,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accent
                            )
                            Text(
                                text = exp.description,
                                fontSize = 8.sp,
                                color = Color(0xFF2C3E50),
                                lineHeight = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Divider(color = accent.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))

            // Education Section
            if (education.isNotEmpty()) {
                Text(
                    text = "EDUCATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B365D),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                education.forEach { edu ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.width(70.dp)) {
                            Text(
                                text = "${edu.startDate} - ${edu.endDate}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF566573)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(8.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(5.dp)
                                    .background(accent, CircleShape)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = edu.degree,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B365D)
                            )
                            Text(
                                text = edu.school,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accent
                            )
                            if (edu.result.isNotEmpty()) {
                                Text(
                                    text = edu.result,
                                    fontSize = 8.sp,
                                    color = Color(0xFF2C3E50),
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
