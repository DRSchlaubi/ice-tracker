package dev.schlaubi.icetracker.util

import android.content.Intent
import android.os.Build

inline fun <reified T : Any> Intent.getParcelable(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name) as T?
    }
}
