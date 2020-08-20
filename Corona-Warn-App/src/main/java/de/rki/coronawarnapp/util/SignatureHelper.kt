package de.rki.coronawarnapp.util

import KeyExportFormat.SignatureInfo

object SignatureHelper {
    val clientSig: SignatureInfo = SignatureInfo.newBuilder()
        .setAndroidPackage("be.sciensano.coronalert")
        .setAppBundleId("be.sciensano.coronalert")
        .setSignatureAlgorithm("ECDSA")
        .build()
}
