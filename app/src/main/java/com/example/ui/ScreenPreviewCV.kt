package com.example.ui

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.ResumeProfile
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPreviewCV(
    viewModel: ResumeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }

    // Preset color palletes
    val colorPalettes = listOf(
        "#2196F3" to "Ocean Blue",
        "#009688" to "Teal Mint",
        "#4CAF50" to "Emerald Green",
        "#E91E63" to "Crimson Rose",
        "#9C27B0" to "Royal Purple",
        "#374151" to "Slate Grey",
        "#D97706" to "Sunset Amber"
    )

    // Get current live state profile (with unsaved changes if they exist)
    val liveProfile = viewModel.getSerializedStateProfile()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("সিভি প্রিভিউ ও থিম সিলেক্টর", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Export PDF on Top bar
                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.saveCurrentProfileToDb {
                                    try {
                                        val f = PdfGenerator.generateResumePdf(context, viewModel.getSerializedStateProfile())
                                        exportedFile = f
                                        showExportSuccessDialog = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PDF Generation failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export A4 PDF", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Live Interactive A4 Aspect Ratio Board
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // The actual real-time on screen template representation!
                        RenderOnScreenResume(
                            profile = liveProfile,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Alignment Metric Guideline Grid
                        if (viewModel.isGridOverlayEnabled) {
                            GridAlignmentOverlay()
                        }
                    }
                }
            }

            // 2. Formatting Grid and Align Assist Helper Checkbox
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable { viewModel.isGridOverlayEnabled = !viewModel.isGridOverlayEnabled },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Grid Alignment Guidelines", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Toggle fine overlay to check visual symmetry of items.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = viewModel.isGridOverlayEnabled,
                            onCheckedChange = { viewModel.isGridOverlayEnabled = it }
                        )
                    }
                }
            }

            // 3. Selection of Templates (3-4 styles available as requested)
            item {
                Column {
                    Text("১. সিভির টেমপ্লেট সিলেক্ট করুন (Minimalist Design templates)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Select a modern style. Changes adapt instantly below.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    val templates = listOf(
                        Triple("template_modern", "Modern Corp", "Classic Corporate feel with clear details"),
                        Triple("template_creative", "Creative Tech", "High visual banner, matching profiles"),
                        Triple("template_minimal", "Minimal Slate", "Extreme clean serif simplicity"),
                        Triple("template_executive", "Executive", "Classy formal layout with navy accents"),
                        Triple("template_stylish", "Stylish Premium", "Vibrant colors with beautiful header sections"),
                        Triple("template_shoriful", "Shoriful Spec", "Premium blue timeline style with fine side panel")
                    )

                    val chunked = templates.chunked(3)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunked.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { temp ->
                                    val isSelected = viewModel.editedTemplateId == temp.first
                                    OutlinedCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(65.dp)
                                            .clickable { viewModel.editedTemplateId = temp.first },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = when(temp.first) {
                                                        "template_creative" -> Icons.Default.Palette
                                                        "template_minimal" -> Icons.Default.Remove
                                                        "template_executive" -> Icons.Default.StarOutline
                                                        "template_stylish" -> Icons.Default.AutoAwesome
                                                        "template_shoriful" -> Icons.Default.Verified
                                                        else -> Icons.Default.Article
                                                    },
                                                    contentDescription = "",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    temp.second,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Accent Color Match Pickers
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("২. সিভির ব্র্যান্ড রোদ সিলেক্ট (Custom Palette Accent)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Paint headings and layout dividers inside the PDF output.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorPalettes.forEach { hexPair ->
                                val paletteColor = Color(android.graphics.Color.parseColor(hexPair.first))
                                val isSelected = viewModel.editedColorHex.lowercase() == hexPair.first.lowercase()
                                
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(paletteColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.editedColorHex = hexPair.first },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom padding spacer
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Success dialog shown when PDF generated completely
    if (showExportSuccessDialog && exportedFile != null) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TaskAlt, contentDescription = "", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সিভি পিডিএফ তৈরি সম্পন্ন!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "আপনার আধুনিক সিভিটি একটি অত্যন্ত হাই-রেজোলিউশন ও প্রিন্ট করার উপযোগী A4 আকারে পিডিএফ ফাইলে রুপান্তর করা হয়েছে।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Button 1: Save directly to downloads directory
                    Button(
                        onClick = {
                            exportedFile?.let { file ->
                                val success = PdfGenerator.savePdfToPublicDownloads(context, file, file.name)
                                if (success) {
                                    Toast.makeText(context, "সরাসরি মোবাইলের Downloads ফোল্ডারে সেভ করা হয়েছে! ✅", Toast.LENGTH_LONG).show()
                                    showExportSuccessDialog = false
                                } else {
                                    Toast.makeText(context, "ফাইল সেভ করতে সমস্যা হয়েছে, অনুগ্রহ করে শেয়ার অপশনটি ট্রাই করুন।", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = "", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সরাসরি মোবাইলে সেভ করুন", fontSize = 12.sp)
                        }
                    }

                    // Button 2: Share securely with ClipData
                    OutlinedButton(
                        onClick = {
                            exportedFile?.let { file ->
                                try {
                                    val authority = "${context.packageName}.fileprovider"
                                    val uri = FileProvider.getUriForFile(context, authority, file)
                                    
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        // Set ClipData for secure permission propagation to Telegram/WhatsApp!
                                        clipData = ClipData.newRawUri("CV PDF", uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "সিভি শেয়ার বা পাঠান"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("টেলিগ্রাম বা অন্য অ্যাপে শেয়ার", fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ফাইল নাম: ${exportedFile?.name ?: ""}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportSuccessDialog = false }) {
                    Text("সম্পন্ন", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
