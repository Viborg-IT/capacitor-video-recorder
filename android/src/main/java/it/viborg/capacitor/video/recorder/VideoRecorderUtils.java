package it.viborg.capacitor.video.recorder;

// Based on: com.capacitorjs.plugins.camera.CameraUtils

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoRecorderUtils {
    public static File createVideoFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File video = File.createTempFile(videoFileName, /* prefix */".mp4", /* suffix */storageDir/* directory */);

        return video;
    }
}
