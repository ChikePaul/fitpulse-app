package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.FitnessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitPulseApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: FitnessRepository
        private set

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = FitnessRepository(database.fitnessDao())

        applicationScope.launch {
            repository.ensureInitialData()
        }
    }
}
