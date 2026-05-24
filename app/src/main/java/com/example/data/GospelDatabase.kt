package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GospelDao {
    // --- Lessons ---
    @Query("SELECT * FROM lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("UPDATE lessons SET isCompleted = :completed WHERE id = :lessonId")
    suspend fun updateLessonCompleted(lessonId: String, completed: Boolean)

    // --- Songs ---
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :songId")
    suspend fun updateSongFavorite(songId: String, isFav: Boolean)

    // --- Forum Posts ---
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllForumPosts(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPost)

    @Query("UPDATE forum_posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun likeForumPost(postId: Int)

    // --- Forum Comments ---
    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<ForumComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumComment(comment: ForumComment)

    // --- User Stats ---
    @Query("SELECT * FROM user_stats WHERE userId = :id")
    fun getUserStats(id: String = "default_user"): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStats)
}

@Database(
    entities = [
        Lesson::class,
        Song::class,
        ForumPost::class,
        ForumComment::class,
        UserStats::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GospelDatabase : RoomDatabase() {
    abstract fun dao(): GospelDao

    companion object {
        @Volatile
        private var INSTANCE: GospelDatabase? = null

        fun getDatabase(context: Context): GospelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GospelDatabase::class.java,
                    "gospel_piano_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
