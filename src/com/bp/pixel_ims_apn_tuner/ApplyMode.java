package com.bp.pixel_ims_apn_tuner;

import android.os.Build;

/**
 * Selects how {@link VolteInstrumentation} is launched over local loopback ADB:
 * <ul>
 *   <li><b>Attached</b> (Android 14+ / API 34+): {@code am instrument -w --no-restart} attaches
 *       to the running process without killing the UI or JobService.</li>
 *   <li><b>Detached</b> (Android 12–13 / API 31–33): {@code --no-restart} is unavailable and
 *       {@code am instrument} force-stops the caller process on start and finish, so it is
 *       dispatched via {@code nohup ... &} and reports completion via notification.</li>
 * </ul>
 */
public final class ApplyMode {

    public static boolean isDetached() {
        return Build.VERSION.SDK_INT < 34;
    }

    private ApplyMode() {}
}
