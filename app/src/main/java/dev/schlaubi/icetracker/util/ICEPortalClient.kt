package dev.schlaubi.icetracker.util

import dev.schlaubi.icetracker.BuildConfig
import dev.schlaubi.icetracker.client.ICEPortalClient
import io.ktor.http.*

val icePortalClient = ICEPortalClient(url = Url(BuildConfig.ICE_PORTAL_URL))