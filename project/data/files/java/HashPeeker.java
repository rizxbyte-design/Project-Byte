package com.android.rizxbyte;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HashPeeker {

    private static final String TAG = "HASH";

    public static void logCurrentHash(Context ctx) {
        try {
            Signature[] signatures;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo info = ctx.getPackageManager().getPackageInfo(
                        ctx.getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES
                );

                if (info.signingInfo.hasMultipleSigners()) {
                    signatures = info.signingInfo.getApkContentsSigners();
                } else {
                    signatures = info.signingInfo.getSigningCertificateHistory();
                }
            } else {
                PackageInfo info = ctx.getPackageManager().getPackageInfo(
                        ctx.getPackageName(),
                        PackageManager.GET_SIGNATURES
                );
                signatures = info.signatures;
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            for (Signature sig : signatures) {
                X509Certificate cert = (X509Certificate)
                        cf.generateCertificate(new ByteArrayInputStream(sig.toByteArray()));

                byte[] digest = md.digest(cert.getEncoded());

                StringBuilder hex = new StringBuilder();
                for (byte b : digest) {
                    hex.append(String.format("%02x", b & 0xff));
                }

                Log.d(TAG, "SHA256 = " + hex.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to get signature hash", e);
        }
    }
}