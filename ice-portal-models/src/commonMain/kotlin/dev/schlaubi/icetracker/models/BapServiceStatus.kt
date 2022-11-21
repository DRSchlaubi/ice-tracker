package dev.schlaubi.icetracker.models

import kotlinx.serialization.Serializable

@Serializable
public data class BapServiceStatus(val bapServiceStatus: String, val status: Boolean)
