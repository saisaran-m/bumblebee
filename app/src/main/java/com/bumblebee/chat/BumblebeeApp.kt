package com.bumblebee.chat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Bumblebee Application class.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class BumblebeeApp : Application()
