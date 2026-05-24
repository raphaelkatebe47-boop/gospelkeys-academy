package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val level: String, // "Beginner", "Intermediate", "Pro"
    val category: String, // "Basics", "Chords", "Progressions", "Voicings"
    val videoUrl: String,
    val durationText: String,
    val points: Int,
    val isCompleted: Boolean = false,
    val keyConcepts: String // Semicolon-separated concepts (e.g. "I-IV-V Progressions;Number System")
)

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val style: String, // "Worship" or "Praise"
    val originalKey: String,
    val recommendedVoicings: String, // e.g. "C7#9, Bbmaj7"
    val lyricsAndChords: String, // Detailed chart with lyrics and chord tags
    val tempoBpm: Int,
    val isFavorite: Boolean = false,
    val videoLessonUrl: String = ""
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val authorLevel: String, // "Beginner", "Intermediate", "Pro"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val category: String = "General" // "Help", "Progressions", "Showcase", "Feedback"
)

@Entity(tableName = "forum_comments")
data class ForumComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val author: String,
    val authorLevel: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String = "default_user",
    val userName: String = "Upcoming Maestro",
    val currentLevel: String = "Beginner", // "Beginner", "Intermediate", "Pro"
    val xp: Int = 0,
    val streakDays: Int = 1,
    val lastPracticeTimestamp: Long = System.currentTimeMillis(),
    val keyboardLabelsEnabled: Boolean = true
)
