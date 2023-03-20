package com.hashone.module.textview.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import java.util.UUID

object AndroidDeviceIdentifier {

    /**
     * Returns a stable identifier for the current device.
     *
     * @param ctx The application's Context
     * @return The unique device identifier
     * @throws IllegalStateException If the device's identifier could not be determined
     */
    @Throws(IllegalStateException::class)
    fun getUniqueDeviceIdentifier(ctx: Context): String {
        try {
            return getDeviceUUID(ctx)
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("Could not determine device identifier", e)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("Could not determine device identifier", e)
        }

    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    private fun getDeviceUUID(ctx: Context): String {
        val hash = makeHash(getMac(ctx), getSerialNumber(ctx))
        return createUUIDFromHash(hash)
    }

    private fun createUUIDFromHash(hash: ByteArray): String {
        return UUID.nameUUIDFromBytes(hash).toString()
            .lowercase(Locale.getDefault()) // Server side wants lower cased UUIDs
    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    private fun makeHash(mac: String, serialNumber: String): ByteArray {
        val sha: MessageDigest

        sha = MessageDigest.getInstance("SHA-256")
        sha.reset()

        sha.update(mac.toByteArray(charset("UTF-8")))
        sha.update(serialNumber.toByteArray(charset("UTF-8")))

        return sha.digest()
    }

    private fun getSerialNumber(context: Context): String {
        var serialNumber: String? =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (serialNumber == null) {
            serialNumber = "0000000000000000"
        }

        return serialNumber
    }

    private fun getMac(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var mac: String? = wifiManager.connectionInfo.macAddress
        if (mac == null) {
            mac = "000000000000"
        }
        return mac
    }
}// hidden constructor of singleton