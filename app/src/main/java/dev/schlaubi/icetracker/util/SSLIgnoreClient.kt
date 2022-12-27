package dev.schlaubi.icetracker.util

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.internal.platform.Platform
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

private val emptyTrustManager by lazy { TrustAllTrustManager() }
private val emptySllContext by lazy {
    SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(emptyTrustManager), null)
    }
}

val sslIgnoringClient = HttpClient(OkHttp) {
    engine {
        config {
            sslSocketFactory(emptySllContext.socketFactory, emptyTrustManager)
        }
    }
}

@SuppressLint("CustomX509TrustManager") // I know what I am doing!
private class TrustAllTrustManager : X509ExtendedTrustManager() {
    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?
    ) = Unit

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?
    ) = Unit

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?
    ) = Unit

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?
    ) = Unit

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        Platform.get().platformTrustManager().acceptedIssuers

    @Throws(CertificateException::class)
    fun checkServerTrusted(
        chain: Array<X509Certificate>, authType: String, host: String
    ): List<X509Certificate> = chain.asList()
}
