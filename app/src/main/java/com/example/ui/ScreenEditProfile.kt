package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEditProfile(
    viewModel: ResumeViewModel,
    onNavigateToPreview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeSectionTab by remember { mutableStateOf(0) }

    var showApiKeyInput by remember { mutableStateOf(false) }
    var apiKeyText by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCustomApiKey(context)
        apiKeyText = viewModel.customGeminiApiKey
    }

    // Setup Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.savePickedPhoto(context, uri)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("সিভি তথ্য এডিট করুন", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                viewModel.saveCurrentProfileToDb {
                                    onNavigateToPreview()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Preview CV", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Visibility, contentDescription = "Preview", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Scrollable custom Category Selectors
            ScrollableTabRow(
                selectedTabIndex = activeSectionTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                val tabHeaders = listOf("সাধারণ তথ্য", "যোগাযোগ", "কাজের অভিজ্ঞতা", "শিক্ষা", "প্রজেক্ট", "দক্ষতা ও ভাষা")
                tabHeaders.forEachIndexed { index, title ->
                    Tab(
                        selected = activeSectionTab == index,
                        onClick = { activeSectionTab = index },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = title, 
                            fontSize = 11.sp, 
                            fontWeight = if (activeSectionTab == index) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 0: GENERAL & PHOTO
                if (activeSectionTab == 0) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("প্রধান এবং প্রোফাইল তথ্য", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = viewModel.editedProfileName,
                                    onValueChange = { viewModel.editedProfileName = it },
                                    label = { Text("সিভি ড্রাফট নাম (e.g., My Software Resume)") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedFullName,
                                    onValueChange = { viewModel.editedFullName = it },
                                    label = { Text("পূর্ণ নাম (e.g., Dev Shoriful)") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedJobTitle,
                                    onValueChange = { viewModel.editedJobTitle = it },
                                    label = { Text("পদবী / টাইটেল (e.g., Mobile App Specialist)") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                )

                                // CV Type Mode Selector
                                Text("সিভি মোড সিলেক্ট করুন (Student / Pro):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ElevatedFilterChip(
                                        selected = viewModel.editedResumeType == "student",
                                        onClick = { viewModel.editedResumeType = "student" },
                                        label = { Text("Student flow") },
                                        leadingIcon = if (viewModel.editedResumeType == "student") {
                                            { Icon(Icons.Default.Check, contentDescription = "", modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )

                                    ElevatedFilterChip(
                                        selected = viewModel.editedResumeType == "professional",
                                        onClick = { viewModel.editedResumeType = "professional" },
                                        label = { Text("Professional experience flow") },
                                        leadingIcon = if (viewModel.editedResumeType == "professional") {
                                            { Icon(Icons.Default.Check, contentDescription = "", modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("সিভি ফটো ও এডিটর প্যানেল", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Upload a photo and adjust standard crops or custom filters instantly.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Visual Photo Box with Filters applied
                                    TemplatePhotoHeader(
                                        photoPath = viewModel.editedPhotoPath,
                                        filterIndex = viewModel.editedPhotoFilterIndex,
                                        cropScale = viewModel.editedPhotoCropScale,
                                        borderAccent = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("সিলেক্ট করুন", fontSize = 11.sp)
                                            }
                                        }

                                        if (viewModel.editedPhotoPath != null) {
                                            TextButton(
                                                onClick = { viewModel.removePhoto() },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("ফটো মুছে ফেলুন", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                if (viewModel.editedPhotoPath != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("ছবি জুম সিলেক্ট (Crop Crop Ratio):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = viewModel.editedPhotoCropScale,
                                        onValueChange = { viewModel.editedPhotoCropScale = it },
                                        valueRange = 0.5f..2.5f,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("ছবিতে আধুনিক ফিল্টার বসান:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val filters = listOf("Normal", "Noir", "Warm", "Cool", "Vintage")
                                        filters.forEachIndexed { index, name ->
                                            OutlinedButton(
                                                onClick = { viewModel.editedPhotoFilterIndex = index },
                                                colors = if (viewModel.editedPhotoFilterIndex == index) {
                                                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                                } else ButtonDefaults.outlinedButtonColors(),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(name, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("প্রোফাইল সামারি (About Me)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = viewModel.editedSummary,
                                    onValueChange = { viewModel.editedSummary = it },
                                    label = { Text("নিজ সম্পর্কে সংক্ষেপে লিখুন (Objective details)") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    maxLines = 6
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // AI Helper Section inside the summary card
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(modifier = Modifier.height(4.dp))

                                if (viewModel.isAiAnalyzing) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("জেমিনি এআই আপনার তথ্যসমূহ এনালাইসিস করে রি-রাইট করছে...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (viewModel.aiSuggestionTitle.isNotEmpty() || viewModel.aiSuggestionSummary.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("জেমিনি এআই এর পরামর্শ বা সাজেশন সমূহ:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (viewModel.aiSuggestionTitle.isNotEmpty()) {
                                                Text("প্রস্তাবিত টাইটেল (Suggested Title):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                                Text(viewModel.aiSuggestionTitle, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                            }

                                            if (viewModel.aiSuggestionSummary.isNotEmpty()) {
                                                Text("প্রস্তাবিত সামারি (Suggested Summary):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                                Text(viewModel.aiSuggestionSummary, fontSize = 12.sp, lineHeight = 16.sp)
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { viewModel.dismissAiSuggestions() }) {
                                                    Text("বাতিল", fontSize = 12.sp)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = { viewModel.applyAiSuggestions() },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Check, contentDescription = "", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("যুক্ত করুন (Apply)", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "জেমিনি এআই দিয়ে আকর্ষণীয় করুন:",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { showApiKeyInput = !showApiKeyInput },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "API Settings",
                                                        tint = if (showApiKeyInput) MaterialTheme.colorScheme.primary else Color.Gray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Button(
                                                onClick = { viewModel.fetchAiSuggestions(context) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = "", modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("AI Improve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        
                                        if (showApiKeyInput) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        "জেমিনি API কী সেট করুনঃ",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        "আপনার নিজের ফ্রি Gemini API key এখানে দিয়ে ‘সংরক্ষণ’ করুন। কীটি লোকাল স্টোরেজে সুরক্ষিত থাকবে।",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray,
                                                        lineHeight = 13.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedTextField(
                                                        value = apiKeyText,
                                                        onValueChange = { apiKeyText = it },
                                                        label = { Text("Gemini API Key", fontSize = 11.sp) },
                                                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                        trailingIcon = {
                                                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                                Icon(
                                                                    imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.Lock,
                                                                    contentDescription = "Toggle Key Visibility",
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true,
                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        TextButton(onClick = { showApiKeyInput = false }) {
                                                            Text("বন্ধ করুন", fontSize = 11.sp)
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(
                                                            onClick = {
                                                                viewModel.saveCustomApiKey(context, apiKeyText)
                                                                showApiKeyInput = false
                                                            },
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(30.dp)
                                                        ) {
                                                            Text("সংরক্ষণ করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        viewModel.aiErrorStatus?.let { err ->
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = err,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 1: CONTACT
                if (activeSectionTab == 1) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("যোগাযোগের লিংক এবং ঠিকানা", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = viewModel.editedEmail,
                                    onValueChange = { viewModel.editedEmail = it },
                                    label = { Text("ইমেইল ঠিকানা") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedPhone,
                                    onValueChange = { viewModel.editedPhone = it },
                                    label = { Text("ফোন নম্বর") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedAddr,
                                    onValueChange = { viewModel.editedAddr = it },
                                    label = { Text("ঠিকানা / বাসস্থান") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedWeb,
                                    onValueChange = { viewModel.editedWeb = it },
                                    label = { Text("ওয়েবসাইট লিংক") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedLinkedin,
                                    onValueChange = { viewModel.editedLinkedin = it },
                                    label = { Text("LinkedIn লিংক") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = viewModel.editedGithub,
                                    onValueChange = { viewModel.editedGithub = it },
                                    label = { Text("GitHub লিংক") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Section 2: EXPERIENCE
                if (activeSectionTab == 2) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("অভিজ্ঞতার বিবরণ সমূহ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = {
                                    viewModel.editingExperience.add(ExperienceEntry("নতুন কোম্পানি", "পদবী", "শুরু সাল/মাস", "শেষ সাল/মাস", "কাজের বর্ণনা"))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন যুক্ত করুন", fontSize = 11.sp)
                            }
                        }
                    }

                    if (viewModel.editingExperience.isEmpty()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("অভিজ্ঞতার কোনো এন্ট্রি নেই। 'নতুন যুক্ত করুন' এ ক্লিক করুন।", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(viewModel.editingExperience.size) { index ->
                            val exp = viewModel.editingExperience[index]
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("এন্ট্রি ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        IconButton(
                                            onClick = { viewModel.editingExperience.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = exp.company,
                                        onValueChange = { viewModel.editingExperience[index] = exp.copy(company = it) },
                                        label = { Text("প্রতিষ্ঠান / কোম্পানি") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = exp.role,
                                        onValueChange = { viewModel.editingExperience[index] = exp.copy(role = it) },
                                        label = { Text("পদের নাম / রোল") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = exp.startDate,
                                            onValueChange = { viewModel.editingExperience[index] = exp.copy(startDate = it) },
                                            label = { Text("শুরুর তারিখ") },
                                            modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                                        )

                                        OutlinedTextField(
                                            value = exp.endDate,
                                            onValueChange = { viewModel.editingExperience[index] = exp.copy(endDate = it) },
                                            label = { Text("শেষের তারিখ") },
                                            modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = exp.description,
                                        onValueChange = { viewModel.editingExperience[index] = exp.copy(description = it) },
                                        label = { Text("অভিজ্ঞতার ভূমিকা ও কাজের সংক্ষিপ্ত বর্ণনা") },
                                        modifier = Modifier.fillMaxWidth().height(80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: EDUCATION
                if (activeSectionTab == 3) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("শিক্ষাগত বিবরণী", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = {
                                    viewModel.editingEducation.add(EducationEntry("বিশ্ববিদ্যালয়/স্কুল", "ডিগ্রী", "শুরু সাল", "শেষ সাল", "CGPA/Grade"))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন যুক্ত করুন", fontSize = 11.sp)
                            }
                        }
                    }

                    if (viewModel.editingEducation.isEmpty()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("শিক্ষার বিবরণী ফাঁকা! নতুন শিক্ষাগত যোগ্যতা যোগ করুন।", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(viewModel.editingEducation.size) { index ->
                            val edu = viewModel.editingEducation[index]
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("শিক্ষা স্তর ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        IconButton(
                                            onClick = { viewModel.editingEducation.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = edu.school,
                                        onValueChange = { viewModel.editingEducation[index] = edu.copy(school = it) },
                                        label = { Text("প্রতিষ্ঠান / স্কুল / কলেজ / বিশ্ববিদ্যালয়ের নাম") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = edu.degree,
                                        onValueChange = { viewModel.editingEducation[index] = edu.copy(degree = it) },
                                        label = { Text("ডিগ্রী / শিক্ষার স্তর (e.g., Higher Secondary / BSC)") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = edu.startDate,
                                            onValueChange = { viewModel.editingEducation[index] = edu.copy(startDate = it) },
                                            label = { Text("শুরুর বছর") },
                                            modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                                        )

                                        OutlinedTextField(
                                            value = edu.endDate,
                                            onValueChange = { viewModel.editingEducation[index] = edu.copy(endDate = it) },
                                            label = { Text("পাসের বছর") },
                                            modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = edu.result,
                                        onValueChange = { viewModel.editingEducation[index] = edu.copy(result = it) },
                                        label = { Text("ফলাফল (CGPA / GPA)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: PROJECT
                if (activeSectionTab == 4) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("প্রজেক্টের তথ্য সমূহ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = {
                                    viewModel.editingProjects.add(ProjectEntry("নতুন প্রজেক্ট নাম", "আমার ভূমিকা (e.g., Team Lead)", "প্রজেক্টের কাজের বিবরণ", "প্রজেক্টের ডেমো/গিটহাব লিংক"))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন যুক্ত করুন", fontSize = 11.sp)
                            }
                        }
                    }

                    if (viewModel.editingProjects.isEmpty()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("কোনো প্রজেক্ট যুক্ত করা হয়নি। প্রজেক্ট সিভি ভারী করে!", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(viewModel.editingProjects.size) { index ->
                            val proj = viewModel.editingProjects[index]
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("প্রজেক্ট ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        IconButton(
                                            onClick = { viewModel.editingProjects.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = proj.title,
                                        onValueChange = { viewModel.editingProjects[index] = proj.copy(title = it) },
                                        label = { Text("প্রজেক্টের শিরোনাম (e.g., E-commerce Store)") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = proj.role,
                                        onValueChange = { viewModel.editingProjects[index] = proj.copy(role = it) },
                                        label = { Text("আপনার ভূমিকা / রোল (e.g., Backend Developer)") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = proj.link,
                                        onValueChange = { viewModel.editingProjects[index] = proj.copy(link = it) },
                                        label = { Text("প্রজেক্ট লিংক (GitHub/Live Demo)") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = proj.description,
                                        onValueChange = { viewModel.editingProjects[index] = proj.copy(description = it) },
                                        label = { Text("প্রজেক্টের বিস্তারিত কাজের বিবরণ") },
                                        modifier = Modifier.fillMaxWidth().height(80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 5: SKILLS & LANGUAGES
                if (activeSectionTab == 5) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("সিভি স্কিল বা দক্ষতা", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    IconButton(
                                        onClick = { viewModel.editingSkills.add(SkillEntry("নতুন দক্ষতা", 0.7f)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Skill", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                for (i in viewModel.editingSkills.indices) {
                                    val sk = viewModel.editingSkills[i]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = sk.name,
                                            onValueChange = { viewModel.editingSkills[i] = sk.copy(name = it) },
                                            modifier = Modifier.weight(1.5f),
                                            label = { Text("Skill name") },
                                            singleLine = true
                                        )

                                        Column(
                                            modifier = Modifier.weight(2f).padding(horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("দক্ষতা স্তর: ${(sk.level * 100).toInt()}%", fontSize = 10.sp)
                                            Slider(
                                                value = sk.level,
                                                onValueChange = { viewModel.editingSkills[i] = sk.copy(level = it) },
                                                valueRange = 0.1f..1.0f
                                            )
                                        }

                                        IconButton(onClick = { viewModel.editingSkills.removeAt(i) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ভাষাগত যোগ্যতা (Languages)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    IconButton(
                                        onClick = { viewModel.editingLanguages.add(LanguageEntry("ভাষা", "Fluent")) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add language", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                for (i in viewModel.editingLanguages.indices) {
                                    val ln = viewModel.editingLanguages[i]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = ln.language,
                                            onValueChange = { viewModel.editingLanguages[i] = ln.copy(language = it) },
                                            modifier = Modifier.weight(1.5f),
                                            label = { Text("Language") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = ln.proficiency,
                                            onValueChange = { viewModel.editingLanguages[i] = ln.copy(proficiency = it) },
                                            modifier = Modifier.weight(1.5f),
                                            label = { Text("Proficiency (e.g. Fluent)") },
                                            singleLine = true
                                        )

                                        IconButton(onClick = { viewModel.editingLanguages.removeAt(i) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save and Continue bottom bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("হোমে ফিরে যান")
                    }

                    Button(
                        onClick = {
                            viewModel.saveCurrentProfileToDb {
                                onNavigateToPreview()
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("সেভ ও সিভির প্রিভিউ")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "")
                        }
                    }
                }
            }
        }
    }
}
