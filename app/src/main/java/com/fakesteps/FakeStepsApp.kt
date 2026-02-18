package com.fakesteps

import android.app.Application
import androidx.work.Configuration

class FakeStepsApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
