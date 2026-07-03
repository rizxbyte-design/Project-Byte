package com.android.rizxbyte;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Protector {
    private static final String EXPECTED_SHA256 = "e1519f389e35d143d98d2a4bfaa46a0441c11a3e1961036a05f1c7ed04c4ff2c";

    public static boolean check(Context ctx) {
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(
                ctx.getPackageName(), PackageManager.GET_SIGNATURES
            );
            for (Signature sig : pInfo.signatures) {
                X509Certificate cert = (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new java.io.ByteArrayInputStream(sig.toByteArray()));
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(cert.getEncoded());
                StringBuilder hex = new StringBuilder();
                for (byte b : digest) hex.append(String.format("%02x", b));
                if (hex.toString().equalsIgnoreCase(EXPECTED_SHA256)) return true;
            }
        } catch (Exception fuckIt) { }
        return false;
    }
}
