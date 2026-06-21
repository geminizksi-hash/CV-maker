package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.EducationEntry
import com.example.data.ExperienceEntry
import com.example.data.JsonHelpers
import com.example.data.LanguageEntry
import com.example.data.ProjectEntry
import com.example.data.ResumeProfile
import com.example.data.SkillEntry
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateResumePdf(context: Context, profile: ResumeProfile): File {
        val pdfDocument = PdfDocument()
        
        // standard A4 page layout (595 x 842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Parse lists from JSON
        val skills = JsonHelpers.parseSkills(profile.skillsJson)
        val experiences = JsonHelpers.parseExperience(profile.experienceJson)
        val education = JsonHelpers.parseEducation(profile.educationJson)
        val projects = JsonHelpers.parseProjects(profile.projectsJson)
        val languages = JsonHelpers.parseLanguages(profile.languagesJson)
        val certifications = JsonHelpers.parseCertifications(profile.certificationsJson)

        // Decode and filter the photo, if available
        var photoBitmap: Bitmap? = null
        if (!profile.photoPath.isNullOrEmpty()) {
            try {
                val original = BitmapFactory.decodeFile(profile.photoPath)
                if (original != null) {
                    photoBitmap = applyFilterAndScale(original, profile.photoFilterIndex, profile.photoCropScale)
                }
            } catch (_: Exception) {}
        }

        val primaryColorInt = try {
            Color.parseColor(profile.customColorHex)
        } catch (_: Exception) {
            Color.parseColor("#2196F3")
        }

        // Draw the resume based on the selected template
        when (profile.selectedTemplateId) {
            "template_creative" -> {
                drawCreativeTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
            "template_minimal" -> {
                drawMinimalTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
            "template_executive" -> {
                drawExecutiveTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
            "template_stylish" -> {
                drawStylishTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
            "template_shoriful" -> {
                drawShorifulTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
            else -> {
                // Default: template_modern
                drawModernTemplate(canvas, profile, primaryColorInt, photoBitmap, skills, experiences, education, projects, languages, certifications)
            }
        }

        pdfDocument.finishPage(page)

        // Save file to the application's cache directory first for safety
        val outputDir = context.cacheDir
        val file = File(outputDir, "${profile.fullName.replace(" ", "_")}_CV.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        // Also duplicate to external downloads folder if permission allowed/accessible
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val externalFile = File(downloadsDir, "${profile.fullName.replace(" ", "_")}_CV.pdf")
            file.inputStream().use { input ->
                externalFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {}

        return file
    }

    private fun applyFilterAndScale(src: Bitmap, filterIndex: Int, scale: Float): Bitmap {
        val minDim = Math.min(src.width, src.height)
        val safeScale = if (scale <= 0.1f) 1.0f else scale
        val cropSize = (minDim / safeScale).toInt().coerceIn(20, minDim)
        
        val startX = ((src.width - cropSize) / 2).coerceIn(0, src.width - cropSize)
        val startY = ((src.height - cropSize) / 2).coerceIn(0, src.height - cropSize)
        
        val cropped = Bitmap.createBitmap(src, startX, startY, cropSize, cropSize)
        val targetResolution = 512
        val scaled = Bitmap.createScaledBitmap(cropped, targetResolution, targetResolution, true)
        
        val output = Bitmap.createBitmap(targetResolution, targetResolution, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        
        when (filterIndex) {
            1 -> { // B&W / Noir
                val matrix = android.graphics.ColorMatrix()
                matrix.setSaturation(0f)
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
            }
            2 -> { // Warm Glow / Amber
                val matrix = android.graphics.ColorMatrix(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 15f,
                    0f, 1.0f, 0f, 0f, 10f,
                    0f, 0f, 0.8f, 0f, -10f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
            }
            3 -> { // Cool Blue / Cyber
                val matrix = android.graphics.ColorMatrix(floatArrayOf(
                    0.8f, 0f, 0f, 0f, -15f,
                    0f, 1.0f, 0f, 0f, 0f,
                    0f, 0f, 1.3f, 0f, 25f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
            }
            4 -> { // Vintage Sepia
                val matrix = android.graphics.ColorMatrix(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
            }
        }
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        return output
    }

    // Helper to draw text with standard wrapped lines
    private fun drawText(
        canvas: Canvas, 
        text: String, 
        x: Float, 
        y: Float, 
        paint: TextPaint, 
        width: Int, 
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): Float {
        if (text.isEmpty()) return y
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
        return y + staticLayout.height
    }

    // Modern Corporate Template Style
    private fun drawModernTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }
        
        // Draw Left sidebar background (Grey)
        paintInstance.color = Color.parseColor("#F5F7FA")
        canvas.drawRect(RectF(0f, 0f, 195f, 842f), paintInstance)

        // Draw Right background (White)
        paintInstance.color = Color.WHITE
        canvas.drawRect(RectF(195f, 0f, 595f, 842f), paintInstance)

        // Photo circle on top left
        var leftY = 40f
        if (photo != null) {
            paintInstance.color = Color.WHITE
            canvas.drawCircle(97.5f, leftY + 50f, 43f, paintInstance)
            paintInstance.color = primaryColor
            canvas.drawCircle(97.5f, leftY + 50f, 40f, paintInstance)

            val rect = RectF(97.5f - 38f, leftY + 50f - 38f, 97.5f + 38f, leftY + 50f + 38f)
            canvas.save()
            val clipPath = android.graphics.Path().apply {
                addCircle(97.5f, leftY + 50f, 38f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(photo, null, rect, paintInstance)
            canvas.restore()
            leftY += 115f
        } else {
            leftY += 20f
        }

        // Contact info on sidebar
        val sidebarWidth = 165
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.parseColor("#333333")
        }

        val headingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // CONTACT INFO
        leftY = drawText(canvas, "CONTACT ME", 15f, leftY, headingPaint, sidebarWidth) + 8f

        val details = listOf(
            "✉  ${profile.email}",
            "📞  ${profile.phone}",
            "📍  ${profile.address}",
            "🔗  ${profile.website}",
            "in  ${profile.linkedin}",
            "git  ${profile.github}"
        )

        for (detail in details) {
            if (detail.substring(3).trim().isNotEmpty()) {
                leftY = drawText(canvas, detail, 15f, leftY, textPaint, sidebarWidth) + 6f
            }
        }
        leftY += 15f

        // SKILLS
        if (skills.isNotEmpty()) {
            leftY = drawText(canvas, "SKILLS", 15f, leftY, headingPaint, sidebarWidth) + 12f
            for (skill in skills) {
                leftY = drawText(canvas, skill.name, 15f, leftY, textPaint, sidebarWidth) + 2f
                // Draw tiny mastery indicator
                paintInstance.color = Color.parseColor("#E0E0E0")
                canvas.drawRoundRect(RectF(15f, leftY, 15f + 140f, leftY + 4f), 2f, 2f, paintInstance)
                paintInstance.color = primaryColor
                canvas.drawRoundRect(RectF(15f, leftY, 15f + (140f * skill.level), leftY + 4f), 2f, 2f, paintInstance)
                leftY += 12f
            }
            leftY += 15f
        }

        // LANGUAGES
        if (languages.isNotEmpty()) {
            leftY = drawText(canvas, "LANGUAGES", 15f, leftY, headingPaint, sidebarWidth) + 8f
            for (lang in languages) {
                val formatted = "${lang.language} - ${lang.proficiency}"
                leftY = drawText(canvas, formatted, 15f, leftY, textPaint, sidebarWidth) + 6f
            }
        }

        // Draw Right Side Data
        var rightY = 40f
        val rightWidth = 360

        // Title and Name
        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 24f
            color = Color.parseColor("#1F2937")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val jobTitlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 13f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val rightBodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.parseColor("#4B5563")
        }

        rightY = drawText(canvas, profile.fullName.uppercase(), 215f, rightY, namePaint, rightWidth) + 4f
        rightY = drawText(canvas, profile.jobTitle, 215f, rightY, jobTitlePaint, rightWidth) + 18f

        // PROFILE SUMMARY
        if (profile.summary.isNotEmpty()) {
            rightY = drawText(canvas, "PROFESSIONAL SUMMARY", 215f, rightY, headingPaint, rightWidth) + 6f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(215f, rightY, 215f + 40f, rightY + 2f), paintInstance)
            rightY += 10f
            rightY = drawText(canvas, profile.summary, 215f, rightY, rightBodyPaint, rightWidth) + 20f
        }

        // EXPERIENCE (ONLY show if professional path OR has experience data)
        val showExp = profile.resumeType == "professional" || experiences.isNotEmpty()
        if (showExp && experiences.isNotEmpty()) {
            rightY = drawText(canvas, "WORK EXPERIENCE", 215f, rightY, headingPaint, rightWidth) + 6f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(215f, rightY, 215f + 40f, rightY + 2f), paintInstance)
            rightY += 10f

            for (exp in experiences) {
                val headerText = "${exp.role} @ ${exp.company}"
                val dateText = "${exp.startDate} - ${exp.endDate}"
                
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                
                rightY = drawText(canvas, headerText, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                rightY = drawText(canvas, dateText, 215f, rightY, TextPaint(rightBodyPaint).apply { textSize = 9f }, rightWidth) + 4f
                rightY = drawText(canvas, exp.description, 215f, rightY, rightBodyPaint, rightWidth) + 14f
            }
        }

        // EDUCATION
        if (education.isNotEmpty()) {
            rightY = drawText(canvas, "EDUCATION", 215f, rightY, headingPaint, rightWidth) + 6f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(215f, rightY, 215f + 40f, rightY + 2f), paintInstance)
            rightY += 10f

            for (edu in education) {
                val headerText = "${edu.degree}  |  ${edu.school}"
                val dateResult = "${edu.startDate} - ${edu.endDate}   •   Grade: ${edu.result}"
                
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }

                rightY = drawText(canvas, headerText, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                rightY = drawText(canvas, dateResult, 215f, rightY, TextPaint(rightBodyPaint).apply { textSize = 9f }, rightWidth) + 12f
            }
        }

        // PROJECTS (ideal for both, specially students)
        if (projects.isNotEmpty()) {
            rightY = drawText(canvas, "PROJECTS", 215f, rightY, headingPaint, rightWidth) + 6f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(215f, rightY, 215f + 40f, rightY + 2f), paintInstance)
            rightY += 10f

            for (proj in projects) {
                val header = if (proj.link.isNotEmpty()) "${proj.title} [${proj.link}]" else proj.title
                
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }

                rightY = drawText(canvas, header, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                if (proj.role.isNotEmpty()) {
                    rightY = drawText(canvas, "Role: ${proj.role}", 215f, rightY, TextPaint(rightBodyPaint).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC) }, rightWidth) + 3f
                }
                rightY = drawText(canvas, proj.description, 215f, rightY, rightBodyPaint, rightWidth) + 14f
            }
        }

        // CERTIFICATIONS
        if (certifications.isNotEmpty()) {
            rightY = drawText(canvas, "CERTIFICATIONS & ACTIVITIES", 215f, rightY, headingPaint, rightWidth) + 6f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(215f, rightY, 215f + 40f, rightY + 2f), paintInstance)
            rightY += 10f

            for (cert in certifications) {
                rightY = drawText(canvas, "•  $cert", 215f, rightY, rightBodyPaint, rightWidth) + 6f
            }
        }
    }

    // Creative Tech Template Style
    private fun drawCreativeTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }

        // Top banner (Primary Accent Color)
        paintInstance.color = primaryColor
        canvas.drawRect(RectF(0f, 0f, 595f, 130f), paintInstance)

        // Sidebar Panel Background (White with fine left margin line)
        paintInstance.color = Color.parseColor("#FAFAFA")
        canvas.drawRect(RectF(0f, 130f, 595f, 842f), paintInstance)

        // Left vertical line separator
        paintInstance.color = primaryColor
        paintInstance.strokeWidth = 2f
        canvas.drawLine(200f, 130f, 200f, 842f, paintInstance)

        // Name and Title inside the colorful banner
        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 26f
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 14f
            color = Color.parseColor("#E0E0E0")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        var headerY = 30f
        headerY = drawText(canvas, profile.fullName, 215f, headerY, namePaint, 360) + 4f
        drawText(canvas, profile.jobTitle, 215f, headerY, titlePaint, 360)

        // Overlapping Rounded Profile Photo
        if (photo != null) {
            paintInstance.color = Color.WHITE
            canvas.drawRoundRect(RectF(40f, 30f, 170f, 160f), 12f, 12f, paintInstance)
            
            val rect = RectF(43f, 33f, 167f, 157f)
            canvas.save()
            val clipPath = android.graphics.Path().apply {
                addRoundRect(rect, 8f, 8f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(photo, null, rect, paintInstance)
            canvas.restore()
        }

        val headingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 11f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val sidebarBodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.parseColor("#374151")
        }

        // Left sidebar section: (Y starts at 180f to leave space for photo overlap)
        var leftY = 180f
        val sidebarWidth = 160

        // CONT ME
        leftY = drawText(canvas, "CONTACT DETAILS", 25f, leftY, headingPaint, sidebarWidth) + 8f
        val details = listOf(
            "✉  ${profile.email}",
            "📞  ${profile.phone}",
            "📍  ${profile.address}",
            "🔗  ${profile.website}",
            "in  ${profile.linkedin}",
            "git  ${profile.github}"
        )
        for (det in details) {
            if (det.substring(3).trim().isNotEmpty()) {
                leftY = drawText(canvas, det, 25f, leftY, sidebarBodyPaint, sidebarWidth) + 6f
            }
        }
        leftY += 15f

        // TECH SKILLS
        if (skills.isNotEmpty()) {
            leftY = drawText(canvas, "POWER SKILLS", 25f, leftY, headingPaint, sidebarWidth) + 10f
            for (skill in skills) {
                leftY = drawText(canvas, "■  ${skill.name}", 25f, leftY, sidebarBodyPaint, sidebarWidth) + 5f
            }
            leftY += 15f
        }

        // LANGUAGES
        if (languages.isNotEmpty()) {
            leftY = drawText(canvas, "LANGUAGES", 25f, leftY, headingPaint, sidebarWidth) + 8f
            for (lang in languages) {
                leftY = drawText(canvas, "• ${lang.language} (${lang.proficiency})", 25f, leftY, sidebarBodyPaint, sidebarWidth) + 5f
            }
        }

        // Right side:
        var rightY = 150f
        val rightWidth = 350
        val rightBodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.parseColor("#374151")
        }

        // SUMMARY
        if (profile.summary.isNotEmpty()) {
            rightY = drawText(canvas, "ABOUT ME", 215f, rightY, headingPaint, rightWidth) + 8f
            rightY = drawText(canvas, profile.summary, 215f, rightY, rightBodyPaint, rightWidth) + 20f
        }

        // EXPERIENCE
        if ((profile.resumeType == "professional" || experiences.isNotEmpty()) && experiences.isNotEmpty()) {
            rightY = drawText(canvas, "WORK EXPERIENCE", 215f, rightY, headingPaint, rightWidth) + 8f
            for (exp in experiences) {
                val header = "${exp.role}  |  ${exp.company}"
                val date = "${exp.startDate} - ${exp.endDate}"
                
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }

                rightY = drawText(canvas, header, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                rightY = drawText(canvas, date, 215f, rightY, TextPaint(rightBodyPaint).apply { textSize = 9f }, rightWidth) + 4f
                rightY = drawText(canvas, exp.description, 215f, rightY, rightBodyPaint, rightWidth) + 14f
            }
        }

        // PROJECTS
        if (projects.isNotEmpty()) {
            rightY = drawText(canvas, "PROJECTS & CORNERSTONES", 215f, rightY, headingPaint, rightWidth) + 8f
            for (proj in projects) {
                val header = "${proj.title} [${proj.role}]"
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                rightY = drawText(canvas, header, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                rightY = drawText(canvas, proj.description, 215f, rightY, rightBodyPaint, rightWidth) + 12f
            }
        }

        // EDUCATION
        if (education.isNotEmpty()) {
            rightY = drawText(canvas, "ACADEMIC BACKGROUND", 215f, rightY, headingPaint, rightWidth) + 8f
            for (edu in education) {
                val header = "${edu.degree}  -  ${edu.school}"
                val detailsText = "${edu.startDate} - ${edu.endDate}   •   Result: ${edu.result}"
                val itemTitlePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#1F2937")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                rightY = drawText(canvas, header, 215f, rightY, itemTitlePaint, rightWidth) + 2f
                rightY = drawText(canvas, detailsText, 215f, rightY, rightBodyPaint, rightWidth) + 10f
            }
        }
    }

    // Minimalist Slate Template Style
    private fun drawMinimalTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }

        // Clean white background, no blocks
        paintInstance.color = Color.WHITE
        canvas.drawRect(RectF(0f, 0f, 595f, 842f), paintInstance)

        var yPos = 40f
        val margin = 45f
        val printWidth = 505

        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 28f
            color = Color.parseColor("#111827")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val subtitlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Full width header
        yPos = drawText(canvas, profile.fullName.uppercase(), margin, yPos, namePaint, printWidth) + 4f
        yPos = drawText(canvas, profile.jobTitle, margin, yPos, subtitlePaint, printWidth) + 12f

        // Contact info block (single horizontal line format)
        val infoPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.parseColor("#6B7280")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val infoString = ArrayList<String>()
        if (profile.email.isNotEmpty()) infoString.add(profile.email)
        if (profile.phone.isNotEmpty()) infoString.add(profile.phone)
        if (profile.address.isNotEmpty()) infoString.add(profile.address)
        if (profile.website.isNotEmpty()) infoString.add(profile.website)
        
        yPos = drawText(canvas, infoString.joinToString("   |   "), margin, yPos, infoPaint, printWidth) + 8f

        // Draw top dividing line
        paintInstance.color = Color.parseColor("#E5E7EB")
        paintInstance.strokeWidth = 1f
        canvas.drawLine(margin, yPos, 595f - margin, yPos, paintInstance)
        yPos += 18f

        val headingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.parseColor("#111827")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        // SUMMARY
        if (profile.summary.isNotEmpty()) {
            yPos = drawText(canvas, "SUMMARY", margin, yPos, headingPaint, printWidth) + 5f
            yPos = drawText(canvas, profile.summary, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 10f; color = Color.parseColor("#374151") }, printWidth) + 18f
        }

        // EXPERIENCE
        if ((profile.resumeType == "professional" || experiences.isNotEmpty()) && experiences.isNotEmpty()) {
            yPos = drawText(canvas, "EXPERIENCE", margin, yPos, headingPaint, printWidth) + 6f
            for (exp in experiences) {
                val header = "${exp.role}  —  ${exp.company}"
                val date = "${exp.startDate} - ${exp.endDate}"
                
                val headerPaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#111827")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                
                yPos = drawText(canvas, header, margin, yPos, headerPaint, printWidth) + 2f
                yPos = drawText(canvas, date, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 9f; color = primaryColor }, printWidth) + 4f
                yPos = drawText(canvas, exp.description, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 9.5f; color = Color.parseColor("#4B5563") }, printWidth) + 14f
            }
        }

        // PROJECTS
        if (projects.isNotEmpty()) {
            yPos = drawText(canvas, "KEY PROJECTS", margin, yPos, headingPaint, printWidth) + 6f
            for (proj in projects) {
                val headerPaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#111827")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                yPos = drawText(canvas, proj.title, margin, yPos, headerPaint, printWidth) + 2f
                yPos = drawText(canvas, proj.description, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 9.5f; color = Color.parseColor("#4B5563") }, printWidth) + 12f
            }
        }

        // EDUCATION
        if (education.isNotEmpty()) {
            yPos = drawText(canvas, "EDUCATION", margin, yPos, headingPaint, printWidth) + 6f
            for (edu in education) {
                val text = "${edu.degree}  •  ${edu.school} (${edu.startDate} - ${edu.endDate})"
                yPos = drawText(canvas, text, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 9.5f; color = Color.parseColor("#374151") }, printWidth) + 5f
            }
            yPos += 10f
        }

        // SKILLS & LANGUAGES (Draw side by side simple text)
        yPos = drawText(canvas, "SKILLS & LANGUAGES", margin, yPos, headingPaint, printWidth) + 6f
        val listSkills = skills.joinToString { it.name }
        val listLangs = languages.joinToString { "${it.language} (${it.proficiency})" }
        val finalDetailsStr = "Skills: $listSkills\nLanguages: $listLangs"
        yPos = drawText(canvas, finalDetailsStr, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 9.5f; color = Color.parseColor("#4B5563") }, printWidth) + 10f
    }

    // Executive Classic Template Style
    private fun drawExecutiveTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }

        // Classic formal borders
        paintInstance.color = Color.WHITE
        canvas.drawRect(RectF(0f, 0f, 595f, 842f), paintInstance)

        // Subtle thin frame inside
        paintInstance.color = Color.parseColor("#CCCCCC")
        paintInstance.style = Paint.Style.STROKE
        paintInstance.strokeWidth = 1f
        canvas.drawRect(RectF(25f, 25f, 570f, 817f), paintInstance)
        paintInstance.style = Paint.Style.FILL // revert

        var yPos = 40f
        val printWidth = 500
        val margin = 48f

        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 24f
            color = Color.parseColor("#1B2A4A") // Rich navy
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        // Centered header
        yPos = drawText(canvas, profile.fullName, margin, yPos, namePaint, printWidth, Layout.Alignment.ALIGN_CENTER) + 4f
        
        val subtitlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.parseColor("#555555")
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val infoString = "${profile.jobTitle}   |   ${profile.email}   |   ${profile.phone}   |   ${profile.address}"
        yPos = drawText(canvas, infoString, margin, yPos, subtitlePaint, printWidth, Layout.Alignment.ALIGN_CENTER) + 12f

        // Solid underline with primary custom color
        paintInstance.color = primaryColor
        canvas.drawRect(RectF(margin, yPos, 595f - margin, yPos + 2f), paintInstance)
        yPos += 15f

        val sectionHeadingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.parseColor("#1B2A4A")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9.5f
            color = Color.parseColor("#222222")
        }

        // EXECUTIVE SUMMARY
        if (profile.summary.isNotEmpty()) {
            yPos = drawText(canvas, "EXECUTIVE SUMMARY", margin, yPos, sectionHeadingPaint, printWidth) + 6f
            yPos = drawText(canvas, profile.summary, margin, yPos, bodyPaint, printWidth) + 15f
        }

        // PROFESSIONAL EXPERIENCE
        if ((profile.resumeType == "professional" || experiences.isNotEmpty()) && experiences.isNotEmpty()) {
            yPos = drawText(canvas, "PROFESSIONAL EXPERIENCE", margin, yPos, sectionHeadingPaint, printWidth) + 6f
            for (exp in experiences) {
                val header = "${exp.role.uppercase()}   •   ${exp.company}"
                val dates = "${exp.startDate} - ${exp.endDate}"
                
                val titleFont = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#111111")
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }

                yPos = drawText(canvas, header, margin, yPos, titleFont, printWidth) + 2f
                yPos = drawText(canvas, dates, margin, yPos, TextPaint().apply { isAntiAlias = true; textSize = 8.5f; color = primaryColor }, printWidth) + 4f
                yPos = drawText(canvas, "Job description:\n${exp.description}", margin, yPos, bodyPaint, printWidth) + 12f
            }
        }

        // PROJECTS
        if (projects.isNotEmpty()) {
            yPos = drawText(canvas, "NOTABLE PROJECT INITIATIVES", margin, yPos, sectionHeadingPaint, printWidth) + 6f
            for (proj in projects) {
                val titleFont = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    color = Color.parseColor("#111111")
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                yPos = drawText(canvas, proj.title, margin, yPos, titleFont, printWidth) + 2f
                yPos = drawText(canvas, proj.description, margin, yPos, bodyPaint, printWidth) + 12f
            }
        }

        // EDUCATION
        if (education.isNotEmpty()) {
            yPos = drawText(canvas, "ACADEMIC CREDENTIALS", margin, yPos, sectionHeadingPaint, printWidth) + 6f
            for (edu in education) {
                val text = "${edu.degree}  -  ${edu.school}  [Grade: ${edu.result}]"
                yPos = drawText(canvas, text, margin, yPos, bodyPaint, printWidth) + 6f
            }
            yPos += 10f
        }

        // SKILLS, CERTIFICATIONS & LANGUAGES
        yPos = drawText(canvas, "CORE COMPETENCIES & LANGUAGES", margin, yPos, sectionHeadingPaint, printWidth) + 6f
        val skillsStr = skills.joinToString { it.name }
        val langsStr = languages.joinToString { "${it.language} (${it.proficiency})" }
        val fullLine = "Expertise in: $skillsStr\nMultilingual proficiencies: $langsStr"
        drawText(canvas, fullLine, margin, yPos, bodyPaint, printWidth)
    }

    // Stylish Premium Template Style
    private fun drawStylishTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }

        // Top Banner background
        paintInstance.color = primaryColor
        canvas.drawRect(RectF(0f, 0f, 595f, 130f), paintInstance)

        // Draw header text
        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 20f
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 11f
            color = Color.parseColor("#E5E7EB")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val contactPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 8.5f
            color = Color.parseColor("#F3F4F6")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        var headerY = 30f
        headerY = drawText(canvas, profile.fullName.uppercase(), 40f, headerY, namePaint, 380) + 4f
        headerY = drawText(canvas, profile.jobTitle, 40f, headerY, titlePaint, 380) + 12f
        
        val contactItems = listOfNotNull(
            profile.email.takeIf { it.isNotEmpty() },
            profile.phone.takeIf { it.isNotEmpty() },
            profile.address.takeIf { it.isNotEmpty() }
        )
        drawText(canvas, contactItems.joinToString("   •   "), 40f, headerY, contactPaint, 380)

        // Draw overlapping rounded Profile Photo inside Header banner
        if (photo != null) {
            paintInstance.color = Color.WHITE
            canvas.drawRoundRect(RectF(465f, 25f, 555f, 115f), 10f, 10f, paintInstance)

            val rect = RectF(468f, 28f, 552f, 112f)
            canvas.save()
            val clipPath = android.graphics.Path().apply {
                addRoundRect(rect, 8f, 8f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(photo, null, rect, paintInstance)
            canvas.restore()
        }

        // Two split columns below banner (Starting at yPos = 145f)
        val headingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val linkPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 7.5f
            color = Color.parseColor("#374151")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val leftColWidth = 150
        val rightColWidth = 330
        
        var leftY = 155f
        
        // Draw Links
        val links = listOf(
            profile.website,
            profile.linkedin,
            profile.github
        ).filter { it.isNotEmpty() }
        
        if (links.isNotEmpty()) {
            leftY = drawText(canvas, "LINKS", 40f, leftY, headingPaint, leftColWidth) + 4f
            for (link in links) {
                leftY = drawText(canvas, "• $link", 40f, leftY, linkPaint, leftColWidth) + 3f
            }
            leftY += 10f
        }

        // Draw Technical Skills
        if (skills.isNotEmpty()) {
            leftY = drawText(canvas, "TECH SKILLS", 40f, leftY, headingPaint, leftColWidth) + 6f
            val skillNamePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.parseColor("#1F2937")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            
            for (sk in skills) {
                leftY = drawText(canvas, sk.name, 40f, leftY, skillNamePaint, leftColWidth) + 1.5f
                
                // Draw thin skill indicator bar
                val barRect = RectF(40f, leftY, 40f + (leftColWidth * sk.level), leftY + 3.5f)
                paintInstance.color = primaryColor
                canvas.drawRect(barRect, paintInstance)
                leftY += 8.5f
            }
            leftY += 8f
        }

        // Draw Languages below Technical Skills (as requested: lengus selection ta skill ar pore jate thake)
        if (languages.isNotEmpty()) {
            leftY = drawText(canvas, "LANGUAGES", 40f, leftY, headingPaint, leftColWidth) + 6f
            val langNamePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.parseColor("#1F2937")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val langProfPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 7f
                color = Color.GRAY
            }
            for (lang in languages) {
                leftY = drawText(canvas, "• ${lang.language}", 40f, leftY, langNamePaint, leftColWidth) + 1f
                leftY = drawText(canvas, lang.proficiency, 44f, leftY, langProfPaint, leftColWidth) + 5f
            }
        }

        // Draw thin elegant vertical separating line
        paintInstance.color = Color.parseColor("#E5E7EB")
        canvas.drawRect(RectF(202f, 150f, 203f, 810f), paintInstance)

        // Draw Right Column
        var rightY = 155f
        val summaryHeadingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 8.5f
            color = Color.parseColor("#4B5563")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        // About Me Section
        if (profile.summary.isNotEmpty()) {
            rightY = drawText(canvas, "ABOUT ME", 215f, rightY, summaryHeadingPaint, rightColWidth) + 4f
            rightY = drawText(canvas, profile.summary, 215f, rightY, textPaint, rightColWidth) + 12f
        }

        // Work Experience
        if (experiences.isNotEmpty()) {
            rightY = drawText(canvas, "WORK EXPERIENCE", 215f, rightY, summaryHeadingPaint, rightColWidth) + 6f
            val expRolePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = Color.parseColor("#111827")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val expCompanyPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.GRAY
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            for (exp in experiences) {
                rightY = drawText(canvas, exp.role, 215f, rightY, expRolePaint, rightColWidth) + 1f
                val subtitle = "${exp.company}  |  ${exp.startDate} - ${exp.endDate}"
                rightY = drawText(canvas, subtitle, 215f, rightY, expCompanyPaint, rightColWidth) + 3f
                rightY = drawText(canvas, exp.description, 215f, rightY, textPaint, rightColWidth) + 8f
            }
            rightY += 4f
        }

        // Key Projects
        if (projects.isNotEmpty()) {
            rightY = drawText(canvas, "KEY PROJECTS", 215f, rightY, summaryHeadingPaint, rightColWidth) + 6f
            val projTitlePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = Color.parseColor("#111827")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            for (proj in projects) {
                rightY = drawText(canvas, proj.title, 215f, rightY, projTitlePaint, rightColWidth) + 1.5f
                rightY = drawText(canvas, proj.description, 215f, rightY, textPaint, rightColWidth) + 8f
            }
            rightY += 4f
        }

        // Education
        if (education.isNotEmpty()) {
            rightY = drawText(canvas, "EDUCATION AND LEARNING", 215f, rightY, summaryHeadingPaint, rightColWidth) + 6f
            val eduDegreePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = Color.parseColor("#111827")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            for (edu in education) {
                val eduTitle = "${edu.degree}  -  ${edu.school}"
                rightY = drawText(canvas, eduTitle, 215f, rightY, eduDegreePaint, rightColWidth) + 1f
                val eduMeta = "Graduated: ${edu.startDate}  |  Result: ${edu.result}"
                rightY = drawText(canvas, eduMeta, 215f, rightY, textPaint, rightColWidth) + 7f
            }
        }
    }

    private fun drawShorifulTemplate(
        canvas: Canvas,
        profile: ResumeProfile,
        primaryColor: Int,
        photo: Bitmap?,
        skills: List<SkillEntry>,
        experiences: List<ExperienceEntry>,
        education: List<EducationEntry>,
        projects: List<ProjectEntry>,
        languages: List<LanguageEntry>,
        certifications: List<String>
    ) {
        val paintInstance = Paint().apply { isAntiAlias = true }

        // Draw sidebar background on the left (Light Blue)
        paintInstance.color = Color.parseColor("#EBF4FA")
        canvas.drawRect(RectF(0f, 0f, 195f, 842f), paintInstance)

        // Draw main body background (White)
        paintInstance.color = Color.WHITE
        canvas.drawRect(RectF(195f, 0f, 595f, 842f), paintInstance)

        var leftY = 35f
        val sidebarWidth = 165

        // Photo top left in sidebar
        if (photo != null) {
            paintInstance.color = Color.WHITE
            canvas.drawRoundRect(RectF(40f, leftY, 150f, leftY + 110f), 12f, 12f, paintInstance)
            paintInstance.color = primaryColor
            paintInstance.style = Paint.Style.STROKE
            paintInstance.strokeWidth = 2f
            canvas.drawRoundRect(RectF(40f, leftY, 150f, leftY + 110f), 12f, 12f, paintInstance)
            paintInstance.style = Paint.Style.FILL

            val rect = RectF(42f, leftY + 2f, 148f, leftY + 108f)
            canvas.save()
            val clipPath = android.graphics.Path().apply {
                addRoundRect(rect, 10f, 10f, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(photo, null, rect, paintInstance)
            canvas.restore()
            leftY += 130f
        } else {
            leftY += 20f
        }

        val sidebarHeadingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.parseColor("#1B365D") // deep navy shoriful style
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val sidebarTextPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 8.5f
            color = Color.parseColor("#2C3E50")
        }

        // Certifications
        if (certifications.isNotEmpty()) {
            leftY = drawText(canvas, "CERTIFICATIONS", 15f, leftY, sidebarHeadingPaint, sidebarWidth) + 2f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(15f, leftY, 150f, leftY + 1.5f), paintInstance)
            leftY += 8f
            for (cert in certifications) {
                leftY = drawText(canvas, "• $cert", 15f, leftY, sidebarTextPaint, sidebarWidth) + 4f
            }
            leftY += 15f
        }

        // Contact info
        leftY = drawText(canvas, "CONTACT", 15f, leftY, sidebarHeadingPaint, sidebarWidth) + 2f
        paintInstance.color = primaryColor
        canvas.drawRect(RectF(15f, leftY, 150f, leftY + 1.5f), paintInstance)
        leftY += 8f

        val details = listOf(
            "📍  " + profile.address.ifBlank { "Sherpur, Bangladesh" },
            "📞  " + profile.phone.ifBlank { "01558118588" },
            "✉  " + profile.email.ifBlank { "info.shorif0000@gmail.com" }
        )
        for (det in details) {
            leftY = drawText(canvas, det, 15f, leftY, sidebarTextPaint, sidebarWidth) + 5f
        }
        leftY += 15f

        // Links
        if (profile.website.isNotEmpty() || profile.linkedin.isNotEmpty() || profile.github.isNotEmpty()) {
            leftY = drawText(canvas, "LINKS", 15f, leftY, sidebarHeadingPaint, sidebarWidth) + 2f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(15f, leftY, 150f, leftY + 1.5f), paintInstance)
            leftY += 8f

            val links = listOf(
                "🌐  " + profile.website,
                "in  " + profile.linkedin,
                "git  " + profile.github
            )
            for (link in links) {
                if (link.substring(4).trim().isNotEmpty()) {
                    leftY = drawText(canvas, link, 15f, leftY, sidebarTextPaint, sidebarWidth) + 4f
                }
            }
            leftY += 15f
        }

        // Skills
        if (skills.isNotEmpty()) {
            leftY = drawText(canvas, "SKILLS", 15f, leftY, sidebarHeadingPaint, sidebarWidth) + 2f
            paintInstance.color = primaryColor
            canvas.drawRect(RectF(15f, leftY, 150f, leftY + 1.5f), paintInstance)
            leftY += 8f
            for (sk in skills) {
                leftY = drawText(canvas, "• ${sk.name}", 15f, leftY, sidebarTextPaint, sidebarWidth) + 4f
            }
        }

        // Right column details
        var rightY = 40f
        val rightColWidth = 355

        val namePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 24f
            color = Color.parseColor("#1B365D")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val jobTitlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 10f
            color = primaryColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val bodyTextPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.parseColor("#34495E")
        }

        // Draw Name
        rightY = drawText(canvas, profile.fullName, 215f, rightY, namePaint, rightColWidth) + 3f
        
        // Draw Job Title
        rightY = drawText(canvas, profile.jobTitle.uppercase(), 215f, rightY, jobTitlePaint, rightColWidth) + 8f

        // Summary
        if (profile.summary.isNotEmpty()) {
            rightY = drawText(canvas, profile.summary, 215f, rightY, bodyTextPaint, rightColWidth) + 12f
        }

        // Right panel bold heading paint
        val sectionHeadingPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 11f
            color = Color.parseColor("#1B365D")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Draw Top thick divider line
        paintInstance.color = primaryColor
        canvas.drawRect(RectF(215f, rightY, 565f, rightY + 2f), paintInstance)
        rightY += 12f

        // Experience Section
        if (experiences.isNotEmpty()) {
            rightY = drawText(canvas, "EXPERIENCE", 215f, rightY, sectionHeadingPaint, rightColWidth) + 6f
            
            val itemRolePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 9f
                color = Color.parseColor("#1B365D")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val itemCompanyPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = primaryColor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val datePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.parseColor("#566573")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            for (exp in experiences) {
                // timeline dot and vertical line representation on PDF
                val dotX = 295f
                paintInstance.color = primaryColor
                canvas.drawCircle(dotX, rightY + 5f, 3.5f, paintInstance)

                // Date is printed on left (215f to 285f)
                val dateStr = "${exp.startDate} - ${exp.endDate}"
                drawText(canvas, dateStr, 215f, rightY, datePaint, 75)

                // Role and company under right offset (310f to 565f)
                var expOffsetDelta = drawText(canvas, exp.role, 310f, rightY, itemRolePaint, 255) + 2f
                expOffsetDelta = drawText(canvas, exp.company, 310f, expOffsetDelta, itemCompanyPaint, 255) + 4f
                rightY = drawText(canvas, exp.description, 310f, expOffsetDelta, bodyTextPaint, 255) + 10f
            }
            rightY += 10f
        }

        // Divider
        paintInstance.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(RectF(215f, rightY, 565f, rightY + 1f), paintInstance)
        rightY += 12f

        // Education Section
        if (education.isNotEmpty()) {
            rightY = drawText(canvas, "EDUCATION", 215f, rightY, sectionHeadingPaint, rightColWidth) + 6f

            val itemDegreePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 9f
                color = Color.parseColor("#1B365D")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val itemSchoolPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = primaryColor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val datePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 8f
                color = Color.parseColor("#566573")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            for (edu in education) {
                // timeline dot
                val dotX = 295f
                paintInstance.color = primaryColor
                canvas.drawCircle(dotX, rightY + 5f, 3.5f, paintInstance)

                // Date
                val dateStr = "${edu.startDate} - ${edu.endDate}"
                drawText(canvas, dateStr, 215f, rightY, datePaint, 75)

                // Degree and School
                var eduOffsetDelta = drawText(canvas, edu.degree, 310f, rightY, itemDegreePaint, 255) + 2f
                eduOffsetDelta = drawText(canvas, edu.school, 310f, eduOffsetDelta, itemSchoolPaint, 255) + 3f
                if (edu.result.isNotEmpty()) {
                    eduOffsetDelta = drawText(canvas, edu.result, 310f, eduOffsetDelta, bodyTextPaint, 255) + 4f
                }
                rightY = eduOffsetDelta + 8f
            }
        }
    }

    fun savePdfToPublicDownloads(context: Context, file: File, displayName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val destination = File(downloadsDir, displayName)
                file.inputStream().use { inputStream ->
                    destination.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
