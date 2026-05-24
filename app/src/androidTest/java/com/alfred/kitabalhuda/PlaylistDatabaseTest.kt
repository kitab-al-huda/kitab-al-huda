package com.alfred.kitabalhuda

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alfred.kitabalhuda.database.AppDatabase
import com.alfred.kitabalhuda.database.dao.PlaylistDao
import com.alfred.kitabalhuda.database.dao.SourateDao
import com.alfred.kitabalhuda.database.dao.ReciteurDao
import com.alfred.kitabalhuda.database.dao.AudioDao
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import androidx.lifecycle.Observer

@RunWith(AndroidJUnit4::class)
class PlaylistDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var playlistDao: PlaylistDao
    private lateinit var sourateDao: SourateDao
    private lateinit var reciteurDao: ReciteurDao
    private lateinit var audioDao: AudioDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playlistDao = db.playlistDao()
        sourateDao = db.sourateDao()
        reciteurDao = db.reciteurDao()
        audioDao = db.audioDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testSaveAndRetrieveSurahInPlaylist() = runBlocking {
        // 1. Insert a Reciter
        val reciter = ReciteurEntity(id = 1, nom = "Mishary Alafasy", imageUrl = "https://example.com/alafasy.jpg", description = "Alafasy recitations")
        reciteurDao.insertAll(listOf(reciter))

        // 2. Insert a Surah
        val surah = SourateEntity(numero = 1, nomArabe = "الفاتحة", nomPhonetique = "Al-Fatihah", nombreVersets = 7, lieuRevelation = "MECCA")
        sourateDao.insertAll(listOf(surah))

        // 3. Insert an Audio (the Surah track for the reciter)
        val audio = AudioEntity(id = 100L, reciteurId = 1, sourateNumero = 1, duree = 120L, urlWeb = "https://example.com/audio1.mp3")
        audioDao.insertAll(listOf(audio))

        // 4. Create a Playlist
        val playlist = PlaylistEntity(id = 1, name = "My Favorite Surahs", description = "Daily recitation list")
        val playlistIdLong = playlistDao.createPlaylist(playlist)
        val playlistId = playlistIdLong.toInt()

        // 5. Add the Surah audio track to the Playlist
        val playlistItem = PlaylistItemEntity(id = 10L, playlistId = playlistId, audioId = 100L, orderIndex = 0)
        playlistDao.addPlaylistItem(playlistItem)

        // 6. Retrieve playlist tracks and verify
        // Run on the main/instrumentation thread to observe LiveData
        var resultTracks: List<PlaylistDao.PlaylistTrack>? = null
        val latch = CountDownLatch(1)
        
        val liveData = playlistDao.getPlaylistTracks(playlistId)
        val observer = Observer<List<PlaylistDao.PlaylistTrack>> { tracks ->
            resultTracks = tracks
            latch.countDown()
        }

        // We must observe on main thread
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            liveData.observeForever(observer)
        }

        latch.await(2, TimeUnit.SECONDS)

        instrumentation.runOnMainSync {
            liveData.removeObserver(observer)
        }

        // Assert that the surah was successfully saved and retrieved with all joined details!
        assertNotNull(resultTracks)
        assertEquals(1, resultTracks!!.size)
        
        val track = resultTracks!![0]
        assertEquals(10L, track.item.id)
        assertEquals(playlistId, track.item.playlistId)
        assertEquals(100L, track.item.audioId)
        
        // Joined entities verification
        assertEquals("Al-Fatihah", track.sourate.nomPhonetique)
        assertEquals("الفاتحة", track.sourate.nomArabe)
        assertEquals("Mishary Alafasy", track.reciteur.nom)
        assertEquals("https://example.com/audio1.mp3", track.audio.urlWeb)
    }
}
