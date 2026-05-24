package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GospelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GospelDatabase.getDatabase(application)
    private val dao = db.dao()
    private val coachManager = PracticeCoachManager()

    // --- State Flows ---
    val lessons: StateFlow<List<Lesson>> = dao.getAllLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val songs: StateFlow<List<Song>> = dao.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forumPosts: StateFlow<List<ForumPost>> = dao.getAllForumPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats> = dao.getUserStats()
        .map { it ?: UserStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    // --- Coach Chat History State ---
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Welcome, upcoming Gospel Giant! I'm Coach Barnabas, your personal Practice Coach. Let's get your fingers moving. Ask me anything about tritones, passing chords, the number system, or type 'learn' to get started!" to false
        )
    )
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    // --- Selected Post Comments ---
    private val _currentComments = MutableStateFlow<List<ForumComment>>(emptyList())
    val currentComments: StateFlow<List<ForumComment>> = _currentComments.asStateFlow()

    // --- Elevated Keyboard Highlight & Practice State ---
    private val _highlightedNotes = MutableStateFlow<List<String>>(emptyList())
    val highlightedNotes: StateFlow<List<String>> = _highlightedNotes.asStateFlow()

    private val _selectedVoicingName = MutableStateFlow("C Major")
    val selectedVoicingName: StateFlow<String> = _selectedVoicingName.asStateFlow()

    fun highlightNotes(notes: List<String>, name: String) {
        _highlightedNotes.value = notes
        _selectedVoicingName.value = name
    }

    init {
        // Run pre-seeding so database arrives populated with gospel tutorials, chord sheets, and warm conversations
        seedDatabaseIfNeeded()
    }

    private fun seedDatabaseIfNeeded() {
        viewModelScope.launch {
            // Seed missing lessons while preserving existing user completion statistics
            lessons.take(1).collect { existingLessons ->
                val existingIds = existingLessons.map { it.id }.toSet()
                val toSeed = initialLessons.filter { it.id !in existingIds }
                if (toSeed.isNotEmpty()) {
                    dao.insertLessons(toSeed)
                }
            }
            // Seed missing songs while preserving existing favorites
            songs.take(1).collect { existingSongs ->
                val existingIds = existingSongs.map { it.id }.toSet()
                val toSeed = initialSongs.filter { it.id !in existingIds }
                if (toSeed.isNotEmpty()) {
                    dao.insertSongs(toSeed)
                }
            }
            // Check if posts are empty, seed if so
            forumPosts.take(1).collect { existingPosts ->
                if (existingPosts.isEmpty()) {
                    initialForumPosts.forEach { dao.insertForumPost(it) }
                }
            }
            // Query stats to initialize default user stats
            userStats.take(1).collect { currentStats ->
                dao.insertOrUpdateUserStats(currentStats)
            }
        }
    }

    // --- Business Actions ---

    fun completeLesson(lesson: Lesson) {
        viewModelScope.launch {
            if (!lesson.isCompleted) {
                dao.updateLessonCompleted(lesson.id, true)
                // Add XP (Points) to student profile
                val stats = userStats.value
                val newXp = stats.xp + lesson.points
                val levelUpgrade = if (newXp > 300 && stats.currentLevel == "Beginner") {
                    "Intermediate"
                } else if (newXp > 800 && stats.currentLevel == "Intermediate") {
                    "Pro"
                } else {
                    stats.currentLevel
                }
                dao.insertOrUpdateUserStats(
                    stats.copy(
                        xp = newXp,
                        currentLevel = levelUpgrade,
                        lastPracticeTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun toggleSongFavorite(song: Song) {
        viewModelScope.launch {
            dao.updateSongFavorite(song.id, !song.isFavorite)
        }
    }

    fun changeUserLevel(level: String) {
        viewModelScope.launch {
            val stats = userStats.value
            dao.insertOrUpdateUserStats(stats.copy(currentLevel = level))
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val stats = userStats.value
            dao.insertOrUpdateUserStats(stats.copy(userName = name))
        }
    }

    // --- Forum Actions ---

    fun createForumPost(content: String, category: String = "General") {
        viewModelScope.launch {
            val stats = userStats.value
            val newPost = ForumPost(
                author = stats.userName,
                authorLevel = stats.currentLevel,
                content = content,
                category = category
            )
            dao.insertForumPost(newPost)
        }
    }

    fun likePost(postId: Int) {
        viewModelScope.launch {
            dao.likeForumPost(postId)
        }
    }

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            dao.getCommentsForPost(postId).collect { comments ->
                _currentComments.value = comments
            }
        }
    }

    fun addComment(postId: Int, content: String) {
        viewModelScope.launch {
            val stats = userStats.value
            val newComment = ForumComment(
                postId = postId,
                author = stats.userName,
                authorLevel = stats.currentLevel,
                content = content
            )
            dao.insertForumComment(newComment)
            // Refresh comments
            loadComments(postId)
        }
    }

    // --- Coach Chat Actions ---

    fun askCoach(message: String) {
        if (message.isBlank()) return

        // Append user turn immediately to list
        _chatMessages.update { it + (message to true) }

        viewModelScope.launch {
            _isCoachTyping.value = true
            val currentLevel = userStats.value.currentLevel
            val response = coachManager.getCoachResponse(
                userMessage = message,
                userSkill = currentLevel,
                chatHistory = chatMessages.value
            )
            _isCoachTyping.value = false
            _chatMessages.update { it + (response to false) }
        }
    }

    fun clearCoachChat() {
        val initialText = "Great session, let's start fresh! Ask me any questions about chord structures, progressions or worship songs."
        _chatMessages.value = listOf(initialText to false)
    }

    // --- Initial Seed Data ---

    private val initialLessons = listOf(
        // --- BEGINNER LESSONS ---
        Lesson(
            id = "lesson_b1",
            title = "The Keyboard & Number System",
            description = "Learn how to find each note on the keys, play your first Major Scale, and translate chords to numbers (I-IV-V).",
            level = "Beginner",
            category = "Basics",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "12mins",
            points = 50,
            keyConcepts = "Major Scale;White & Black Keys;The Nashville Numbers System;C Major Scale Exercises"
        ),
        Lesson(
            id = "lesson_b2",
            title = "Left Hand Shell Chords",
            description = "Master robust base shells (Root + 5th or Root + Octave) that form the solid rhythmic foundation of traditional worship playing.",
            level = "Beginner",
            category = "Chords",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "15mins",
            points = 60,
            keyConcepts = "Root-5th Anchors;Steady Tempo Control;Smooth Voice Leading;Left-Hand Independence"
        ),
        Lesson(
            id = "lesson_b3",
            title = "Diatonic Triads & Inversions",
            description = "Master C, F, and G Major triads. Learn how inversions keep your movements close and professional without jumping across the octave.",
            level = "Beginner",
            category = "Basics",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "14mins",
            points = 65,
            keyConcepts = "Triad Inversions;Voice-Leading Economy;C-F-G Common Notes;Smooth Chord Linking"
        ),
        Lesson(
            id = "lesson_b4",
            title = "The 1-5-6-4 Worship Engine",
            description = "Master the legendary progression behind 90% of modern worship anthems. Unlock seamless transitions in C major and D major.",
            level = "Beginner",
            category = "Chords",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "16mins",
            points = 70,
            keyConcepts = "Pop Worship Progressions;C-G-Am-F Layout;Chord Strum Timings;Worship Lead Patterns"
        ),

        // --- INTERMEDIATE LESSONS ---
        Lesson(
            id = "lesson_i1",
            title = "The Essential 2-5-1 Progression",
            description = "Take your play from simple to soulful. We explore minor 9th and dominant 13th shapes that define urban gospel and contemporary style.",
            level = "Intermediate",
            category = "Progressions",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "18mins",
            points = 80,
            keyConcepts = "Minor 9th Chords;Dominant 13th Voicings;Major 9th Resolution;Soulful Ear Training"
        ),
        Lesson(
            id = "lesson_i2",
            title = "7-3-6 Passing Chord Walk",
            description = "Precede minor sections beautifully with this gospel signature! We break down how to slide smoothly into relative minors.",
            level = "Intermediate",
            category = "Progressions",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "20mins",
            points = 90,
            keyConcepts = "Minor 7b5 Voicings;Dominant Altered 7ths;Minor vi Resolution;Passing Chords Harmony"
        ),
        Lesson(
            id = "lesson_i3",
            title = "Anointed 4-Bar Secondary Dominants",
            description = "Learn how to use secondary dominant chords to build a smooth harmonic bridge before jumping into choruses.",
            level = "Intermediate",
            category = "Progressions",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "18mins",
            points = 95,
            keyConcepts = "Secondary Dominants;Bridge Chord Linking;Tension Builds;Gospel Verse Bridges"
        ),
        Lesson(
            id = "lesson_i4",
            title = "Trident Drop & Tritone Chords",
            description = "Unlock the mystery of tritones. Learn how two simple notes in your left hand can completely transform your right-hand chords.",
            level = "Intermediate",
            category = "Voicings",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "19mins",
            points = 100,
            keyConcepts = "Tritone Placement;3rd and 7th Foundations;Left Hand Spacing;Gospel Compression"
        ),

        // --- PRO LESSONS ---
        Lesson(
            id = "lesson_p1",
            title = "Dominant Tritones & Shout Chops",
            description = "Pro territory! Learn how to stack 3rd and 7th intervals to play upbeat Praise Drives and shout music backing up a preacher.",
            level = "Pro",
            category = "Voicings",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "25mins",
            points = 120,
            keyConcepts = "Tritone Intervals;The Preacher Shout Rhythm;Left-Hand Walkdowns;Tritone Substitutions"
        ),
        Lesson(
            id = "lesson_p2",
            title = "Grace Slides & Chord Pentatonics",
            description = "Decorate your gospel chords with quick grace-note slides and fast melodic runs like a true pro musician.",
            level = "Pro",
            category = "Voicings",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "22mins",
            points = 150,
            keyConcepts = "Grace Note Micro-Slides;Melodic Pentatonic Cascades;Gospel Runs;Advanced Tritone Play"
        ),
        Lesson(
            id = "lesson_p3",
            title = "Preacher's Key-Slinging (F# Shout)",
            description = "Unlock full preacher backing chord movements. Walk the bassline, hit left-hand tenths, and land powerful right-hand blues chops.",
            level = "Pro",
            category = "Voicings",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "28mins",
            points = 160,
            keyConcepts = "F# Shout Patterns;Fast Walking Bass;Preacher Climax Backing;Blues Scale Infusions"
        ),
        Lesson(
            id = "lesson_p4",
            title = "Diminished Passing Walkdowns",
            description = "Use fully diminished 7th chords to walk down minor and major progressions. Add high tension and professional gospel slides.",
            level = "Pro",
            category = "Progressions",
            videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            durationText = "26mins",
            points = 180,
            keyConcepts = "Diminished 7th Voicings;Chromatic Walkdowns;Tension Resolutions;Slid Multi-chords"
        )
    )

    private val initialSongs = listOf(
        Song(
            id = "song1",
            title = "Amazing Grace (Worship Soul)",
            artist = "Traditional Hymn (Gospel Arrangement)",
            style = "Worship",
            originalKey = "C Major",
            recommendedVoicings = "Cmaj9, Fmaj9, C7#9, Db13",
            lyricsAndChords = "[Cmaj9] Amazing [Fmaj9] grace, how [Cmaj9] sweet the [C7#9] sound\n" +
                    "That [Cmaj9] saved a [Am7] wretch like [D9] me [G13]\n" +
                    "I [Cmaj9] once was [Fmaj9] lost, but [C7#9] now am [Db13] found\n" +
                    "Was [Cmaj9] blind, but [G13] now I [Cmaj9] see.",
            tempoBpm = 64,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song2",
            title = "As The Deer (Tritone Upgrade)",
            artist = "Martin J. Nystrom (Modern Gospel)",
            style = "Worship",
            originalKey = "D Major",
            recommendedVoicings = "Dmaj9, F#m11, Gmaj9, A13, B7b9",
            lyricsAndChords = "[Dmaj9] As the [A/C#] deer panteth [Bm7] for the [D7#9] water\n" +
                    "So my [Gmaj9] soul longeth [A13] after [Dmaj9] Thee\n" +
                    "You a-[Dmaj9]lone are my [A/C#] heart\'s de-[Bm7]sire [D7#9]\n" +
                    "And I [Gmaj9] long to [A13] worship [Dmaj9] Thee.",
            tempoBpm = 68,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song3",
            title = "What A Mighty God (Praise Drive)",
            artist = "Traditional Praise (Upbeat Shout)",
            style = "Praise",
            originalKey = "F Major",
            recommendedVoicings = "F13, Bb9, G9/B, C13, C7#9",
            lyricsAndChords = "[F13] What a mighty God we [Bb9] serve!\n" +
                    "[F13] What a mighty God we [C13] serve! [C7#9]\n" +
                    "[F13] Angels bow be-[Bb9]fore Him,\n" +
                    "[F13] Heaven and earth a-[Bb9]dore Him,\n" +
                    "[F13] What a mighty [C13] God we [F13] serve!",
            tempoBpm = 118,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song4",
            title = "Holy, Holy, Holy (Worship Walkup)",
            artist = "Reginald Heber (Gospel Progression)",
            style = "Worship",
            originalKey = "C Major",
            recommendedVoicings = "Cmaj9, Am11, Dm9, G13, E7#9",
            lyricsAndChords = "[Cmaj9] Holy, [Am11] Holy, [Dm9] Ho-[G13]ly!\n" +
                    "[Cmaj9] Lord God [Am11] Al-[D9]migh-[G13]ty!\n" +
                    "[Cmaj9] Early [Am11] in the [Dm9] mor-[G13]ning\n" +
                    "Our [Cmaj9] song shall [Am11] rise [G13] to [Cmaj9] Thee.",
            tempoBpm = 65,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song5",
            title = "Let Your Glory Fall (Praise Shout)",
            artist = "Traditional (Uptempo Drive)",
            style = "Praise",
            originalKey = "Ab Major",
            recommendedVoicings = "Ab13, Db9, Eb13, Bb9",
            lyricsAndChords = "[Ab13] Let Your glory [Db9] fall in this [Ab13] place!\n" +
                    "[Ab13] Let Your glory [Db9] fall in this [Eb13] place!\n" +
                    "We [Ab13] lift our voice in [Db9] adoration,\n" +
                    "[Ab13] Fill this house with [Eb13] divine sal-[Ab13]vation!",
            tempoBpm = 125,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song6",
            title = "Total Praise (Majestic Anointing)",
            artist = "Richard Smallwood (Gospel Masterpiece)",
            style = "Worship",
            originalKey = "Db Major",
            recommendedVoicings = "Db13, Fmaj9, C7#9, Bbmaj7",
            lyricsAndChords = "[Db13] Lord, I will [Fmaj9] lift mine [C7#9] eyes to the [Db13] hills\n" +
                    "Knowing my [Bbmaj7] help is [Fmaj9] coming from [Db13] You\n" +
                    "You are my [Db13] strength when [C7#9] I am [Fmaj9] weak\n" +
                    "You are the [Db13] strength and [Bbmaj7] portion of my [Db13] life.",
            tempoBpm = 58,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song7",
            title = "Blessed Assurance (Sweet Organ)",
            artist = "Fanny Crosby (Gospel Hymnal)",
            style = "Worship",
            originalKey = "G Major",
            recommendedVoicings = "Gmaj9, Cmaj9, Am7, D9, G13",
            lyricsAndChords = "[Gmaj9] Blessed as-[Cmaj9]surance, Je-sus is [Gmaj9] mine!\n" +
                    "Oh, what a [Am7] fore-taste of [D9] glo-ry di-[G13]vine!\n" +
                    "[Gmaj9] Heir of sal-[Cmaj9]va-tion, pur-chase of [Gmaj9] God\n" +
                    "Born of His [Am7] Spi-rit, [D9] washed in His [Gmaj9] blood.",
            tempoBpm = 60,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        ),
        Song(
            id = "song8",
            title = "Shackles (Praise Shout Drive)",
            artist = "Mary Mary (Contemporary Uptempo)",
            style = "Praise",
            originalKey = "Gm / Bb Major",
            recommendedVoicings = "Gm7, C9, Fmaj9, Bbmaj7",
            lyricsAndChords = "Take the [Gm7] shackles off my [C9] feet so I can dance\n" +
                    "I just [Fmaj9] want to praise [Bbmaj7] You\n" +
                    "I broke the [Gm7] chains now [C9] I can lift my hands\n" +
                    "And I [Fmaj9] just want to praise [Bbmaj7] You!",
            tempoBpm = 114,
            videoLessonUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        )
    )

    private val initialForumPosts = listOf(
        ForumPost(
            author = "Deacon Steve",
            authorLevel = "Pro",
            content = "For those working on the 7-3-6 progression we learned today, try voicing your 3 chord (e.g. E7#9) as LHS E-B, RHS G#-D-G. It creates an incredible tension before resolving to Amin9! Let me know how it sounds on your pianos.",
            category = "Progressions",
            likesCount = 12
        ),
        ForumPost(
            author = "Sister Keisha",
            authorLevel = "Beginner",
            content = "Hey everyone! I just completed my first week on the basics. Learning the Numbers system has changed my life. I can finally transpose from C to Db without sweating! Love this app.",
            category = "Feedback",
            likesCount = 8
        ),
        ForumPost(
            author = "Brother John",
            authorLevel = "Intermediate",
            content = "Quick question for the AI Coach: how can I transition smoothly from a 4 chord to a 5 chord using a gospel sliding tritones in the key of Ab? Any tips?",
            category = "Help",
            likesCount = 3
        )
    )
}
