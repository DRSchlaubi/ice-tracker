package dev.schlaubi.icetracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import dev.schlaubi.icetracker.R
import dev.schlaubi.icetracker.fetcher.FetchingTask
import dev.schlaubi.icetracker.fetcher.Journey
import dev.schlaubi.icetracker.models.TrainStatus
import dev.schlaubi.icetracker.models.TripInfo
import dev.schlaubi.icetracker.ui.tracker.TrackerActivity
import dev.schlaubi.icetracker.ui.tracker.TrackerState
import dev.schlaubi.icetracker.util.getParcelable
import dev.schlaubi.icetracker.util.icePortalClient
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.div
import kotlin.io.path.writeText


const val TRACKER_SERVICE_ID = "dev.schlaubi.icetracker.service.TRACKER_SERVICE"
const val NOTIFICATION_ID = 1
const val TRACKER_SERVICE_NAME = "Tracker Status"
const val TRACKER_SERVICE_REQUEST_CODE = 1
const val TRACKER_INITIAL_DATA_EXTRA = "initial_data"

class TrackerService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val safeIntent = intent ?: return super.onStartCommand(null, flags, startId)
        val action = enumValues<Command>().firstOrNull { it.name == safeIntent.action }
            ?: return super.onStartCommand(null, flags, startId)
        val currentState = state.value
            ?: return super.onStartCommand(null, flags, startId)

        Log.d("ICETRK", "Got start command: $intent")

        when (action) {
            Command.STOP -> {
                currentState.requireRunning()
                val lastData = currentState.fetchingTask.stopAndSave()
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
                stopSelf()
                _state.postValue(TrackingServiceState.Stopping(lastData))
            }
            Command.PAUSE -> {
                currentState.requireRunning()

                require(!currentState.paused) { "Already paused" }
                currentState.fetchingTask.pause()
                _state.postValue(currentState.copy(paused = true))
            }
            Command.RESUME -> {
                currentState.requireRunning()
                require(currentState.paused) { "Not paused" }
                currentState.fetchingTask.start()
                _state.postValue(currentState.copy(paused = false))
            }
            Command.START -> {
                currentState.requireSleeping()
                val tempFile =
                    cacheDir.toPath() / "current_ongoing_journeys" / "journey_${generateNonce()}.journey.json"
                val task = FetchingTask(
                    scope = lifecycleScope,
                    client = icePortalClient,
                    onUpdate = ::updateState
                )
                val initialData = safeIntent.getParcelable<TrackerState>(TRACKER_INITIAL_DATA_EXTRA)
                    ?: error("Missing initial data")
                task.start()
                val state = TrackingServiceState.Running(false, initialData, task, tempFile)
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                startForeground(NOTIFICATION_ID, state.toNotification(notificationManager))
                _state.postValue(state)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateState(status: TrainStatus, tripInfo: TripInfo) {
        val state = _state.value!!
        state.requireRunning()
        val data = TrackerState(status, tripInfo.trip)
        _state.postValue(state.copy(data = data))
        state.tempFile.writeText(
            Json.encodeToString(data),
            Charsets.UTF_8,
            StandardOpenOption.CREATE
        )
    }

    private fun setupNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        _state.observe(this) {
            if (it is TrackingServiceState.Running) {
                notificationManager.notify(
                    NOTIFICATION_ID,
                    it.toNotification(notificationManager)
                )
            }
        }
    }

    private fun TrackingServiceState.Running.toNotification(notificationManager: NotificationManager): Notification {
        val channel = NotificationChannel(
            TRACKER_SERVICE_ID,
            TRACKER_SERVICE_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this@TrackerService, TRACKER_SERVICE_ID)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_train)
            .setContentTitle(resources.getString(R.string.tracking_notification_title))
            .setContentText(
                resources.getString(
                    R.string.tracking_notification_description,
                    data.speed
                )
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this@TrackerService,
                    TRACKER_SERVICE_REQUEST_CODE,
                    Intent(this@TrackerService, TrackerActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .apply {
                if (paused) {
                    addAction(
                        R.drawable.ic_play,
                        resources.getString(R.string.resume),
                        pendingCommandIntent(this@TrackerService, Command.RESUME)
                    )
                } else {
                    addAction(
                        R.drawable.ic_pause,
                        resources.getString(R.string.pause),
                        pendingCommandIntent(this@TrackerService, Command.PAUSE)
                    )
                }
            }
            .addAction(
                R.drawable.ic_stop,
                resources.getString(R.string.stop_and_save),
                pendingCommandIntent(this@TrackerService, Command.STOP)
            )
            .build()
    }

    companion object {
        @Suppress("ObjectPropertyName")
        private val _state = MutableLiveData<TrackingServiceState>(TrackingServiceState.Sleeping)
        val state: LiveData<TrackingServiceState>
            get() = _state

        fun reset() {
            require(_state.value is TrackingServiceState.Stopping) { "Not Stopping" }
            _state.postValue(TrackingServiceState.Sleeping)
        }

        inline fun commandIntent(
            context: Context,
            command: Command,
            builder: Intent.() -> Unit = {}
        ): Intent = Intent(
            context,
            TrackerService::class.java
        ).also {
            it.action = command.name
        }.apply(builder)

        inline fun pendingCommandIntent(
            context: Context,
            command: Command,
            builder: Intent.() -> Unit = {}
        ): PendingIntent =
            PendingIntent.getService(
                context,
                2,
                commandIntent(context, command, builder),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
    }

    enum class Command {
        STOP,
        PAUSE,
        RESUME,
        START
    }
}

sealed interface TrackingServiceState {
    object Sleeping : TrackingServiceState
    data class Stopping(val data: Journey) : TrackingServiceState
    data class Running(
        val paused: Boolean,
        val data: TrackerState,
        val fetchingTask: FetchingTask,
        val tempFile: Path
    ) : TrackingServiceState
}

@OptIn(ExperimentalContracts::class)
private fun TrackingServiceState.requireRunning() {
    contract {
        returns() implies (this@requireRunning is TrackingServiceState.Running)
    }
    require(this is TrackingServiceState.Running) { "Not running" }
}

@OptIn(ExperimentalContracts::class)
private fun TrackingServiceState.requireSleeping() {
    contract {
        returns() implies (this@requireSleeping is TrackingServiceState.Sleeping)
    }
    require(this is TrackingServiceState.Sleeping) { "Not sleeping" }
}
