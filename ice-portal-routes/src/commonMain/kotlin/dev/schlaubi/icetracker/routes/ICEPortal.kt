package dev.schlaubi.icetracker.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

public class ICEPortal {
    @Resource("api1/rs")
    @Serializable
    public class API1 {
        @Resource("status")
        @Serializable
        public data class Status(val api1: API1 = API1())

        @Resource("tripInfo/trip")
        @Serializable
        public data class Trip(val api1: API1 = API1())
    }

    @Resource("bap/api/bap-service-status")
    @Serializable
    public class BapServiceStatus
}
