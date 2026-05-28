package com.ifochka.m14n.rest.integrity.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.auth.oauth2.GoogleCredentials
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.io.File

@Service
class PlayIntegrityService(
    @Value("\${play.integrity.allowed-packages:}") allowedPackages: String,
) : IntegrityService {

    private val log = LoggerFactory.getLogger(PlayIntegrityService::class.java)
    private val restClient = RestClient.create()
    private val useEmulator = System.getenv("USE_FIREBASE_EMULATOR") == "true"
    private val credentials: GoogleCredentials? = if (useEmulator) null else loadCredentials()
    private val allowedSet = allowedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    override fun verify(token: String, packageName: String): IntegrityVerdict {
        if (useEmulator) {
            log.info("[PlayIntegrity] Emulator mode — returning stub pass verdict")
            return IntegrityVerdict(
                pass = true,
                appRecognized = true,
                deviceIntegrity = listOf("MEETS_DEVICE_INTEGRITY"),
                licensingVerdict = "LICENSED",
            )
        }

        if (packageName !in allowedSet) {
            log.warn("[PlayIntegrity] Rejected unlisted packageName={}", packageName)
            return IntegrityVerdict(
                pass = false,
                appRecognized = false,
                deviceIntegrity = emptyList(),
                licensingVerdict = "UNEVALUATED",
            )
        }

        return runCatching {
            credentials!!.refreshIfExpired()
            val accessToken = credentials.accessToken.tokenValue

            val response = restClient.post()
                .uri("https://playintegrity.googleapis.com/v1/$packageName:decodeIntegrityToken")
                .header("Authorization", "Bearer $accessToken")
                .body(DecodeRequest(integrityToken = token))
                .retrieve()
                .body(DecodeResponse::class.java)
                ?: error("Empty response from Play Integrity API")

            val payload = response.tokenPayloadExternal
            val appVerdict = payload.appIntegrity?.appRecognitionVerdict ?: "UNEVALUATED"
            val deviceVerdicts = payload.deviceIntegrity?.deviceRecognitionVerdict ?: emptyList()
            val licenseVerdict = payload.accountDetails?.appLicensingVerdict ?: "UNEVALUATED"

            IntegrityVerdict(
                pass = appVerdict == "PLAY_RECOGNIZED" && deviceVerdicts.contains("MEETS_DEVICE_INTEGRITY"),
                appRecognized = appVerdict == "PLAY_RECOGNIZED",
                deviceIntegrity = deviceVerdicts,
                licensingVerdict = licenseVerdict,
            )
        }.onFailure {
            log.warn("[PlayIntegrity] Verification failed: ${it.message}", it)
        }.getOrElse {
            IntegrityVerdict(
                pass = false,
                appRecognized = false,
                deviceIntegrity = emptyList(),
                licensingVerdict = "UNEVALUATED",
            )
        }
    }

    private fun loadCredentials(): GoogleCredentials {
        val json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        val path = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
        return when {
            !json.isNullOrEmpty() -> GoogleCredentials.fromStream(json.byteInputStream())
                .createScoped("https://www.googleapis.com/auth/playintegrity")
            !path.isNullOrEmpty() -> GoogleCredentials.fromStream(File(path).inputStream())
                .createScoped("https://www.googleapis.com/auth/playintegrity")
            else -> GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/playintegrity")
        }
    }

    private data class DecodeRequest(
        @JsonProperty("integrity_token") val integrityToken: String,
    )

    private data class DecodeResponse(
        val tokenPayloadExternal: TokenPayload = TokenPayload(),
    )

    private data class TokenPayload(
        val appIntegrity: AppIntegrity? = null,
        val deviceIntegrity: DeviceIntegrity? = null,
        val accountDetails: AccountDetails? = null,
    )

    private data class AppIntegrity(
        val appRecognitionVerdict: String = "UNEVALUATED",
    )

    private data class DeviceIntegrity(
        val deviceRecognitionVerdict: List<String> = emptyList(),
    )

    private data class AccountDetails(
        val appLicensingVerdict: String = "UNEVALUATED",
    )
}
