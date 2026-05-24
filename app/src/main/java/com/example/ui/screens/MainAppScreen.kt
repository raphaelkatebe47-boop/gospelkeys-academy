@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.audio.AudioSynth
import com.example.data.ForumPost
import com.example.data.Lesson
import com.example.data.Song
import com.example.ui.components.PianoKeyboard
import com.example.ui.viewmodel.GospelViewModel

@Composable
fun MainAppScreen(
    viewModel: GospelViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Learn, 1: Chords, 2: AI Coach, 3: Forum, 4: Progress
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    var showWebCompanionMode by remember { mutableStateOf(false) }

    if (showWebCompanionMode) {
        WebAppView(onBack = { showWebCompanionMode = false })
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = "Logo",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Gospel Keys Pro",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    "Level: ${userStats.currentLevel} • XP: ${userStats.xp}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showWebCompanionMode = true },
                            modifier = Modifier.padding(end = 4.dp).testTag("action_web_mode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Switch to Web App Version",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥 ${userStats.streakDays} Day Streak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
                        label = { Text("Learn", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_learn")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Chords") },
                        label = { Text("Chords", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_chords")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Coach") },
                        label = { Text("AI Coach", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_coach")
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Forum") },
                        label = { Text("Forum", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_forum")
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Stats") },
                        label = { Text("Progress", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_progress")
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (activeTab) {
                    0 -> LearnTab(viewModel, onLaunchWebCompanion = { showWebCompanionMode = true })
                    1 -> ChordsTab(viewModel)
                    2 -> AiCoachTab(viewModel)
                    3 -> ForumTab(viewModel)
                    4 -> ProgressTab(viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. LEARN TAB (Lessons from Scratch to Pro)
// ==========================================
// --- structured Course Model ---
data class GospelCourse(
    val id: String,
    val title: String,
    val description: String,
    val level: String,
    val lessonsCount: Int,
    val totalXpValue: Int,
    val requiredXp: Int,
    val gradientStartColor: Color,
    val gradientEndColor: Color
)

val predesignedCourses = listOf(
    GospelCourse(
        id = "course_beg",
        title = "Gospel Piano Foundations",
        description = "Go from absolute zero to playing solid diatonic worship frameworks. Master major scales, the Nashville Number System, key hand coordination, and triads.",
        level = "Beginner",
        lessonsCount = 4,
        totalXpValue = 245,
        requiredXp = 0,
        gradientStartColor = Color(0xFF1B5E20),
        gradientEndColor = Color(0xFF2E7D32)
    ),
    GospelCourse(
        id = "course_int",
        title = "Anointed Passing Progressions",
        description = "Enhance your harmony with soulful transitions. Master the essential major/minor 2-5-1 progression, secondary dominants, and 7-3-6 relative minor walks.",
        level = "Intermediate",
        lessonsCount = 4,
        totalXpValue = 365,
        requiredXp = 300,
        gradientStartColor = Color(0xFFE65100),
        gradientEndColor = Color(0xFFEF6C00)
    ),
    GospelCourse(
        id = "course_pro",
        title = "Urban Worship & Shout Master",
        description = "Learn to accompany preacher climaxes. Master rapid rhythmic F# preacher shouts, fully diminished walking lines, and flashy pentatonic grace runs.",
        level = "Pro",
        lessonsCount = 4,
        totalXpValue = 610,
        requiredXp = 800,
        gradientStartColor = Color(0xFF880E4F),
        gradientEndColor = Color(0xFFAD1457)
    )
)

@Composable
fun LearnTab(viewModel: GospelViewModel, onLaunchWebCompanion: () -> Unit) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    var showFlatLessonsList by remember { mutableStateOf(false) }
    var selectedCourseId by remember { mutableStateOf<String?>(null) }
    var selectedLevelFilter by remember { mutableStateOf("All") }
    var expandedLessonId by remember { mutableStateOf<String?>(null) }

    val filteredLessons = remember(lessons, selectedLevelFilter) {
        if (selectedLevelFilter == "All") lessons else lessons.filter { it.level == selectedLevelFilter }
    }

    // Safely precalculate course-specific stats outside lazy layout composable invocation
    val selectedCourse = remember(selectedCourseId) { predesignedCourses.find { it.id == selectedCourseId } }
    val courseLessons = remember(lessons, selectedCourse) {
        if (selectedCourse != null) {
            lessons.filter { it.level.lowercase() == selectedCourse.level.lowercase() }
        } else {
            emptyList()
        }
    }
    val completedInCourse = remember(courseLessons) {
        courseLessons.count { it.isCompleted }
    }
    val courseProgressPercent = remember(courseLessons, completedInCourse) {
        if (courseLessons.isNotEmpty()) completedInCourse.toFloat() / courseLessons.size else 0f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcome and piano keyboard element
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Interactive Piano Sandbox",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Visualize gospel voicings in real-time. Select a voicing to highlight the keys, then touch them or click 'Strum' to play absolute tones.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Connect Keyboard with our Centralized highlighting flows
                    PianoKeyboard(viewModel = viewModel, labelsEnabled = userStats.keyboardLabelsEnabled)
                }
            }
        }

        // Web Companion Promo Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaunchWebCompanion() }
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Globe",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New! Gospel Keys Web Edition",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to explore the fully functional interactive Web App version with custom voice synthesizer!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (selectedCourseId != null && selectedCourse != null) {
            // Course Header Card back routing
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedCourseId = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Pathways")
                    }
                    Text(
                        "Back to Pathways",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Gorgeous Gradient Course Metadata Block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(selectedCourse.gradientStartColor, selectedCourse.gradientEndColor)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "COURSE LEVEL: ${selectedCourse.level.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedCourse.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedCourse.description,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Course Completion: $completedInCourse / ${selectedCourse.lessonsCount} Mastered",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { courseProgressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Structured Core Lessons",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Course Lessons
            items(courseLessons) { lesson ->
                LessonCard(
                    lesson = lesson,
                    viewModel = viewModel,
                    isExpanded = expandedLessonId == lesson.id,
                    onToggleExpand = { expandedLessonId = if (expandedLessonId == lesson.id) null else lesson.id },
                    onComplete = { viewModel.completeLesson(lesson) }
                )
            }
        } else {
            // Display Toggle Navigation header
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showFlatLessonsList) "All Practice Tutorials" else "Gospel Course pathways",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Switch path
                        TextButton(onClick = { showFlatLessonsList = !showFlatLessonsList }) {
                            Text(
                                text = if (showFlatLessonsList) "See Courses View 📁" else "Browse Flat List 🔍",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (showFlatLessonsList) {
                        // Level Filter Chips for Search
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Beginner", "Intermediate", "Pro").forEach { level ->
                                FilterChip(
                                    selected = selectedLevelFilter == level,
                                    onClick = { selectedLevelFilter = level },
                                    label = { Text(level, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (showFlatLessonsList) {
                // Flat list items
                items(filteredLessons) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        viewModel = viewModel,
                        isExpanded = expandedLessonId == lesson.id,
                        onToggleExpand = { expandedLessonId = if (expandedLessonId == lesson.id) null else lesson.id },
                        onComplete = { viewModel.completeLesson(lesson) }
                    )
                }
            } else {
                // Display the 3 structured predesigned courses as gorgeous clickable grids
                items(predesignedCourses) { course ->
                    val completedInCourse = remember(lessons) {
                        lessons.count { it.level.lowercase() == course.level.lowercase() && it.isCompleted }
                    }
                    val isLocked = userStats.xp < course.requiredXp
                    val progressPercent = completedInCourse.toFloat() / course.lessonsCount

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isLocked) selectedCourseId = course.id }
                            .border(
                                1.dp,
                                if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("course_card_${course.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isLocked) Color.Gray.copy(alpha = 0.2f)
                                            else when (course.level) {
                                                "Beginner" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                "Intermediate" -> Color(0xFFFF9800)
                                                    .copy(alpha = 0.15f)
                                                else -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = course.level.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocked) Color.Gray
                                        else when (course.level) {
                                            "Beginner" -> Color(0xFF4CAF50)
                                            "Intermediate" -> Color(0xFFFF9800)
                                            else -> Color(0xFFE91E63)
                                        }
                                    )
                                }

                                if (isLocked) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked Course",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Requires ${course.requiredXp} XP (Locked)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (progressPercent >= 1.0f) Icons.Default.CheckCircle else Icons.Default.LockOpen,
                                            contentDescription = "Status",
                                            tint = if (progressPercent >= 1.0f) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (progressPercent >= 1.0f) "Mastered!" else "Academic Track Active",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (progressPercent >= 1.0f) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = course.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Course Path Progress: $completedInCourse / ${course.lessonsCount} Playlists",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "+${course.totalXpValue} XP Course Weight",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: Lesson,
    viewModel: GospelViewModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (lesson.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (lesson.level) {
                                "Beginner" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                "Intermediate" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                else -> Color(0xFFE91E63).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = lesson.level + " • " + lesson.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (lesson.level) {
                            "Beginner" -> Color(0xFF4CAF50)
                            "Intermediate" -> Color(0xFFFF9800)
                            else -> Color(0xFFE91E63)
                        }
                    )
                }

                if (lesson.isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        "+${lesson.points} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lesson.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = lesson.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Animated expansion block
            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Simulated High-fidelity Interactive video player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Beautiful animated mock sound visualizer waves
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Visual tutorial video",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gospel Piano Classroom Simulation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Time code: 00:00 / ${lesson.durationText} • Click keys below to practice along",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )

                        // Visual volume wave lines decoration
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(10.dp, 24.dp, 16.dp, 32.dp, 20.dp, 12.dp, 28.dp).forEach { height ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(height)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recommended practice chord blocks inside expanded lesson card
                val lessonPracticeVoicing = remember(lesson.id) {
                    when (lesson.id) {
                        "lesson_b1" -> "C Major"
                        "lesson_b2" -> "Walkup C/E"
                        "lesson_b3" -> "Fmaj9"
                        "lesson_b4" -> "Cmaj9"
                        "lesson_i1" -> "2-5-1 (C Major)"
                        "lesson_i2" -> "7-3-6 (C key)"
                        "lesson_i3" -> "Abmaj9"
                        "lesson_i4" -> "C7#9"
                        "lesson_p1" -> "Passing Tritone C"
                        "lesson_p2" -> "Db13"
                        "lesson_p3" -> "F13"
                        "lesson_p4" -> "Bb9"
                        else -> "C Major"
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Lesson Practice Voicing", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Target Chord: $lessonPracticeVoicing", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val notes = AudioSynth.chordPresets[lessonPracticeVoicing] ?: listOf("C", "E", "G", "C2")
                                viewModel.highlightNotes(notes, lessonPracticeVoicing)
                                AudioSynth.playChord(notes)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Visualize on Piano", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Concept chips
                Text(
                    "Key Concepts Taught in this tutorial:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lesson.keyConcepts.split(";").forEach { concept ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(concept, fontSize = 10.sp) }
                        )
                    }
                }

                // Completion trigger action button
                if (!lesson.isCompleted) {
                    Button(
                        onClick = { onComplete() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("complete_lesson_button_${lesson.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Mark Complete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("I've Mastered This Lesson (+${lesson.points} XP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Duration: ${lesson.durationText}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "Tap card to open tutorial ▾",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. CHORDS TAB (Praise & Worship Chart Sheets)
// ==========================================
@Composable
fun ChordsTab(viewModel: GospelViewModel) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    var searchPhrase by remember { mutableStateOf("") }
    var selectedStyleFilter by remember { mutableStateOf("All") }
    var selectedSongForChart by remember { mutableStateOf<Song?>(null) }

    val filteredSongs = remember(songs, searchPhrase, selectedStyleFilter) {
        songs.filter { song ->
            (selectedStyleFilter == "All" || song.style == selectedStyleFilter) &&
                    (song.title.contains(searchPhrase, ignoreCase = true) ||
                            song.artist.contains(searchPhrase, ignoreCase = true) ||
                            song.originalKey.contains(searchPhrase, ignoreCase = true))
        }
    }

    if (selectedSongForChart != null) {
        // Detailed Chord Chart page - Pass the ViewModel for visual keyboard feedback
        ChordChartDetailView(
            song = selectedSongForChart!!,
            viewModel = viewModel,
            onBack = { selectedSongForChart = null },
            onFavoriteToggle = { viewModel.toggleSongFavorite(it) }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Offline Chord Sheets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Search our pre-cached Praise and Worship index. Teach yourself with lyrics overlaid on chord charts on the go.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search text box
            OutlinedTextField(
                value = searchPhrase,
                onValueChange = { searchPhrase = it },
                placeholder = { Text("Search songs, artists, keys...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("song_search_field"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Category choice row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Worship", "Praise").forEach { style ->
                    FilterChip(
                        selected = selectedStyleFilter == style,
                        onClick = { selectedStyleFilter = style },
                        label = { Text(style, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Songs List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No songs found for '$searchPhrase'",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(filteredSongs) { song ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSongForChart = song }
                            .testTag("song_card_${song.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = song.artist,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(song.style, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Key: ${song.originalKey}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleSongFavorite(song) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (song.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordChartDetailView(
    song: Song,
    viewModel: GospelViewModel,
    onBack: () -> Unit,
    onFavoriteToggle: (Song) -> Unit
) {
    var transposeShift by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to list")
            }
            Text(
                text = "Interactive Sheet",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            IconButton(onClick = { onFavoriteToggle(song) }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (song.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Header Card detailing key and tempo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(song.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("by ${song.artist}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("Style", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(song.style, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Original Key", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(song.originalKey, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Tempo", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("${song.tempoBpm} BPM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Strum block for recommended voicings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Recommended Voicings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        song.recommendedVoicings,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        val firstVoicing = song.recommendedVoicings.split(", ").firstOrNull() ?: "C Major"
                        val notes = AudioSynth.chordPresets[firstVoicing] ?: listOf("C", "E", "G", "C2")
                        // Highlight on the sandbox and play sound
                        viewModel.highlightNotes(notes, firstVoicing)
                        AudioSynth.playChord(notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Hear Base Voicing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Transposition Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Transpose", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (transposeShift > -6) transposeShift-- }, modifier = Modifier.size(36.dp)) {
                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (transposeShift == 0) "Original" else (if (transposeShift > 0) "+$transposeShift" else "$transposeShift"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { if (transposeShift < 6) transposeShift++ }, modifier = Modifier.size(36.dp)) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Lyric Sheet Scroll box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "CHORD & LYRIC COMPOSITION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Process chords and markup lyrics
                val lines = song.lyricsAndChords.split("\n")
                items(lines) { line ->
                    if (line.isNotBlank()) {
                        GospelMarkupLine(line = line, transShift = transposeShift, viewModel = viewModel)
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GospelMarkupLine(line: String, transShift: Int, viewModel: GospelViewModel) {
    // Elegant line parsing showing chord elements on top of lyrics beautifully in a rich Compose block
    val chordRegex = "\\[(.*?)\\]".toRegex()
    val matches = chordRegex.findAll(line).toList()

    if (matches.isEmpty()) {
        Text(text = line, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
    } else {
        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            // Raw text representation with spaced chord symbols aligned is best done by parsing parts:
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // To keep it perfectly reliable, let's show chords inside highlight buttons followed by lyric chunks
                // Or simply beautiful inline highlighted tags!
                var lastIndex = 0
                val parsedElements = mutableListOf<@Composable () -> Unit>()

                matches.forEach { match ->
                    val start = match.range.first
                    val end = match.range.last

                    if (start > lastIndex) {
                        val textPart = line.substring(lastIndex, start)
                        parsedElements.add {
                            Text(
                                textPart,
                                fontSize = 14.sp,
                                modifier = Modifier.alignByBaseline(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    val rawChord = match.groupValues[1]
                    val transposedChord = transposeChordString(rawChord, transShift)

                    parsedElements.add {
                        Box(
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                .clickable {
                                    val notes = AudioSynth.chordPresets[transposedChord]
                                        ?: listOf("C", "E", "G")
                                    // Visual highlight on virtual keyboard & playing audio
                                    viewModel.highlightNotes(notes, transposedChord)
                                    AudioSynth.playChord(notes)
                                }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = transposedChord,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    lastIndex = end + 1
                }

                if (lastIndex < line.length) {
                    val remaining = line.substring(lastIndex)
                    parsedElements.add {
                        Text(
                            remaining,
                            fontSize = 14.sp,
                            modifier = Modifier.alignByBaseline(),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Render in row flow
                // FlowRow works perfect here!
                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Start
                ) {
                    parsedElements.forEach { it() }
                }
            }
        }
    }
}

// Simple transposition helper which shifts chord roots by specified halfsteps
fun transposeChordString(chord: String, halfSteps: Int): String {
    if (halfSteps == 0) return chord
    val roots = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
    val flatsToSharps = mapOf("Db" to "C#", "D#" to "Eb", "Gb" to "F#", "G#" to "Ab", "A#" to "Bb")

    // Extract root
    var rootPart = ""
    var restPart = ""
    if (chord.length >= 2 && (chord[1] == '#' || chord[1] == 'b')) {
        rootPart = chord.substring(0, 2)
        restPart = chord.substring(2)
    } else if (chord.isNotEmpty()) {
        rootPart = chord.substring(0, 1)
        restPart = chord.substring(1)
    }

    // Convert flats
    val normalizedRoot = flatsToSharps[rootPart] ?: rootPart
    val rootIdx = roots.indexOf(normalizedRoot)
    if (rootIdx == -1) return chord // Keep unchanged if unrecognized

    var newIdx = (rootIdx + halfSteps) % 12
    if (newIdx < 0) newIdx += 12

    return roots[newIdx] + restPart
}


// ==========================================
// 3. AI PRACTICE COACH TAB (Coach Barnabas)
// ==========================================
@Composable
fun AiCoachTab(viewModel: GospelViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isCoachTyping.collectAsStateWithLifecycle()
    val stats by viewModel.userStats.collectAsStateWithLifecycle()

    var coachQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val coachPresets = remember(stats.currentLevel) {
        when (stats.currentLevel) {
            "Beginner" -> listOf(
                "Explain Nashville Number system 🔢",
                "How do white/black key layouts work? 🎹",
                "Give me a simple worship progression ✝️"
            )
            "Intermediate" -> listOf(
                "Show me 2-5-1 chord voicings 🎶",
                "Explain the 7-3-6 passing chord step 🎵",
                "How do I spice up Minor 9ths? 🌟"
            )
            else -> listOf(
                "Teach me a pro preaching shout run 🎹",
                "Explain Triton substitutions 🌀",
                "Show some grace note slides ✨"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Coach Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SpatialAudio,
                    contentDescription = "Coach Barnabas",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Coach Barnabas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Gospel Piano Mentor • Adapting to: ${stats.currentLevel} student",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.clearCoachChat() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = Color.Gray)
            }
        }

        // Messages Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = false
            ) {
                items(messages) { (text, isUser) ->
                    ChatBubble(text = text, isUser = isUser)
                }

                if (isTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Barnabas is playing chords...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Coach Helper presets clickers
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                "Ask Barnabas instantly:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                coachPresets.forEach { preset ->
                    OutlinedButton(
                        onClick = { viewModel.askCoach(preset.dropLast(2)) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(preset, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Chat Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = coachQuery,
                onValueChange = { coachQuery = it },
                placeholder = { Text("Ask about voicings, walk downs, praise drive...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("coach_chat_input_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (coachQuery.isNotBlank()) {
                        viewModel.askCoach(coachQuery)
                        coachQuery = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .testTag("coach_send_button"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    val textColor = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurface
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}


// ==========================================
// 4. FORUM TAB (Peer Support Community)
// ==========================================
@Composable
fun ForumTab(viewModel: GospelViewModel) {
    val posts by viewModel.forumPosts.collectAsStateWithLifecycle()
    val comments by viewModel.currentComments.collectAsStateWithLifecycle()

    var expandedPostForCommentId by remember { mutableStateOf<Int?>(null) }
    var forumPostCategoryFilter by remember { mutableStateOf("All") }

    var isCreatingNewTopic by remember { mutableStateOf(false) }
    var newPostContent by remember { mutableStateOf("") }
    var newPostCategory by remember { mutableStateOf("General") }

    val filteredPosts = remember(posts, forumPostCategoryFilter) {
        if (forumPostCategoryFilter == "All") posts else posts.filter { it.category == forumPostCategoryFilter }
    }

    if (isCreatingNewTopic) {
        // Create Post View overlays
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isCreatingNewTopic = false }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text("Start Gospel Discussion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = {
                        if (newPostContent.isNotBlank()) {
                            viewModel.createForumPost(newPostContent, newPostCategory)
                            newPostContent = ""
                            isCreatingNewTopic = false
                        }
                    },
                    modifier = Modifier.testTag("submit_topic_button")
                ) {
                    Text("Post")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Category Dropdown simulation
            Text("Select Discussion Hub:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                listOf("General", "Progressions", "Help", "Feedback").forEach { cat ->
                    FilterChip(
                        selected = newPostCategory == cat,
                        onClick = { newPostCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = newPostContent,
                onValueChange = { newPostContent = it },
                label = { Text("What are you practicing? Ask for feedback, share voicings...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("new_post_content_field"),
                shape = RoundedCornerShape(8.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Maestro Forum 🏛️",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Peer support and gospel progressions sharing.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { isCreatingNewTopic = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_post_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Post", modifier = Modifier.size(16.dp))
                    Text("Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Forum Thread Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Progressions", "Help", "Feedback").forEach { cat ->
                    FilterChip(
                        selected = forumPostCategoryFilter == cat,
                        onClick = { forumPostCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            // Post list scrollable
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts) { post ->
                    ForumPostCard(
                        post = post,
                        isSelected = expandedPostForCommentId == post.id,
                        comments = comments,
                        onSelectForComments = {
                            if (expandedPostForCommentId == post.id) {
                                expandedPostForCommentId = null
                            } else {
                                expandedPostForCommentId = post.id
                                viewModel.loadComments(post.id)
                            }
                        },
                        onLike = { viewModel.likePost(post.id) },
                        onSubmitComment = { text -> viewModel.addComment(post.id, text) }
                    )
                }
            }
        }
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    isSelected: Boolean,
    comments: List<com.example.data.ForumComment>,
    onSelectForComments: () -> Unit,
    onLike: () -> Unit,
    onSubmitComment: (String) -> Unit
) {
    var draftComment by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_post_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            post.author.take(1).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(post.author, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Level: ${post.authorLevel} • Hub: ${post.category}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            // Bottom interactivity buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Like", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount} Helpful", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Expand Comments indicator
                OutlinedButton(
                    onClick = onSelectForComments,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Comment, contentDescription = "Comments", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isSelected) "Hide replies" else "Open replies",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanding Comments Subsection
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Render inner comment rows
                comments.forEach { comment ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(comment.author, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(comment.authorLevel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(comment.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                    }
                }

                if (comments.isEmpty()) {
                    Text(
                        "No replies yet. Be the first to start the chord analysis!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                // Add comment row input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draftComment,
                        onValueChange = { draftComment = it },
                        placeholder = { Text("Add helpful advice...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (draftComment.isNotBlank()) {
                                onSubmitComment(draftComment)
                                draftComment = ""
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Add comment", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. PROGRESS & SETTINGS TAB
// ==========================================
@Composable
fun ProgressTab(viewModel: GospelViewModel) {
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()

    var editingName by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(stats.userName) }

    val completedCount = remember(lessons) { lessons.count { it.isCompleted } }
    val progressFraction = remember(lessons, completedCount) {
        if (lessons.isEmpty()) 0f else completedCount.toFloat() / lessons.size
    }

    // Badge configuration list
    val achievements = remember(stats.xp, completedCount) {
        listOf(
            Triple("First Scales Played", "Complete any piano lesson successfully.", completedCount > 0),
            Triple("Tritone Gladiator", "Gain 150 total XP on gospel progressions.", stats.xp >= 150),
            Triple("Preacher Shout Titan", "Complete at least 3 lessons on pro arrangements.", completedCount >= 3),
            Triple("Barnabas' Favorite", "Chat with your AI Coach and master any intermediate voicings.", stats.xp >= 300)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Profile Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Musician Profile Dashboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (editingName) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("Display Name", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (inputName.isNotBlank()) {
                                        viewModel.updateUserName(inputName)
                                        editingName = false
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stats.userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { editingName = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit name", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Assigned Skill: ${stats.currentLevel}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Custom Skill Adaptation settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Manual AI Adaptation Pivot", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Pivot your proficiency bracket. This instructs the Coach and modifies your curriculum automatically.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Beginner", "Intermediate", "Pro").forEach { level ->
                            val isSelected = stats.currentLevel == level
                            Button(
                                onClick = { viewModel.changeUserLevel(level) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("set_level_$level")
                            ) {
                                Text(level, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Stats tracking row progress percentages
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lessons Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$completedCount / ${lessons.size} Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }

        // Achievement Badges Grid list
        item {
            Column {
                Text(
                    "Spiritual Badges & Milestones",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    achievements.forEach { (title, desc, isUnlocked) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) MaterialTheme.colorScheme.primary
                                        else Color.DarkGray
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.Stars else Icons.Default.Lock,
                                    contentDescription = "Badge Status",
                                    tint = if (isUnlocked) Color.Black else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                Text(
                                    desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebAppView(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    webViewClient = WebViewClient()
                    loadUrl("file:///android_asset/web/index.html")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Elegant overlay back button to escape webview mode
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Web App",
                tint = Color.White
            )
        }
    }
}
