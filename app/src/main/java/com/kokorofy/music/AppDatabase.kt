package com.kokorofy.music

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun observeSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<Song>)

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun get(id: String): Song?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :q || '%' OR artist LIKE '%' || :q || '%' OR album LIKE '%' || :q || '%' ORDER BY title")
    fun search(q: String): Flow<List<Song>>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(entry: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSong(playlistId: String, songId: String)

    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON ps.songId = s.id
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
        """
    )
    fun observePlaylistSongs(playlistId: String): Flow<List<Song>>
}

@Database(
    entities = [Song::class, Playlist::class, PlaylistSong::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kokorofy.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
