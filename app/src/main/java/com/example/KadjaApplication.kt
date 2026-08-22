package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.KadjaDatabase
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerEngine

class KadjaApplication : Application() {

    lateinit var database: KadjaDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var playerEngine: AudioPlayerEngine
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            KadjaDatabase::class.java,
            "kadja_player.db"
        ).fallbackToDestructiveMigration().build()

        musicRepository = MusicRepository(applicationContext, database)
        playerEngine = AudioPlayerEngine(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        playerEngine.release()
    }
}
