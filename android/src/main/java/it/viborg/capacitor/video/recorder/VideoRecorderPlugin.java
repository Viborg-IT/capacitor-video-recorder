package it.viborg.capacitor.video.recorder;

// Based on: com.capacitorjs.plugins.camera.CameraPlugin

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.getcapacitor.FileUtils;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@CapacitorPlugin(
        name = "VideoRecorder",
        permissions = {
                @Permission(strings = {Manifest.permission.CAMERA}, alias = VideoRecorderPlugin.CAMERA),
                @Permission(strings = {Manifest.permission.RECORD_AUDIO}, alias = VideoRecorderPlugin.AUDIO),
                // SDK Version <= 29
                @Permission(strings = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, alias = VideoRecorderPlugin.VIDEOS),
                // SDK Version 30-32
                @Permission(strings = {Manifest.permission.READ_EXTERNAL_STORAGE}, alias = VideoRecorderPlugin.READ_EXTERNAL_STORAGE),
        /*
        SDK Version >= 33
         */
                @Permission(strings = {Manifest.permission.READ_MEDIA_VIDEO}, alias = VideoRecorderPlugin.MEDIA)
        }
)
public class VideoRecorderPlugin extends Plugin {

    // Permission alias constants
    static final String CAMERA = "camera";
    static final String AUDIO = "audio";
    static final String VIDEOS = "videos";
    static final String READ_EXTERNAL_STORAGE = "readExternalStorage";
    static final String MEDIA = "media";

    // Message constants
    private static final String INVALID_RESULT_TYPE_ERROR = "Invalid resultType option";
    private static final String PERMISSION_DENIED_ERROR_CAMERA = "User denied access to camera";
    private static final String PERMISSION_DENIED_ERROR_VIDEOS = "User denied access to videos";
    private static final String NO_CAMERA_ERROR = "Device doesn't have a camera available";
    private static final String NO_CAMERA_ACTIVITY_ERROR = "Unable to resolve camera activity";
    private static final String NO_VIDEO_ACTIVITY_ERROR = "Unable to resolve video activity";
    private static final String VIDEO_FILE_SAVE_ERROR = "Unable to create video on disk";
    private static final String VIDEO_PROCESS_NO_FILE_ERROR = "Unable to process video, file not found on disk";
    private static final String UNABLE_TO_PROCESS_VIDEO = "Unable to process video";
    private static final String VIDEO_EDIT_ERROR = "Unable to edit video";
    private static final String VIDE_GALLERY_SAVE_ERROR = "Unable to save the video in the gallery";

    private String videoFileSavePath;
    private String videoEditedFileSavePath;
    private Uri videoFileUri;
    private Uri videoPickedContentUri;
    private boolean isEdited = false;
    private boolean isFirstRequest = true;
    private boolean isSaved = false;

    private VideoRecorderSettings settings = new VideoRecorderSettings();
    @PluginMethod
    public void getVideo(PluginCall call) {
        isEdited = false;
        settings = getSettings(call);
        doShow(call);
    }

    private void doShow(PluginCall call) {
        switch (settings.getSource()) {
            case CAMERA:
                showCamera(call);
                break;
            case VIDEOS:
                showVideos(call);
                break;
            default:
                showPrompt(call);
                break;
        }
    }

    private void showPrompt(final PluginCall call) {
        // We have all necessary permissions, open the camera
        List<String> options = new ArrayList<>();
        options.add(call.getString("promptLabelPhoto", "From Videos"));
        options.add(call.getString("promptLabelPicture", "Take Video"));

        final VideoRecorderBottomSheetDialogFragment fragment = new VideoRecorderBottomSheetDialogFragment();
        fragment.setTitle(call.getString("promptLabelHeader", "Video"));
        fragment.setOptions(
                options,
                index -> {
                    if (index == 0) {
                        settings.setSource(VideoSource.VIDEOS);
                        openVideos(call);
                    } else if (index == 1) {
                        settings.setSource(VideoSource.CAMERA);
                        openCamera(call);
                    }
                },
                () -> call.reject("User cancelled photos app")
        );
        fragment.show(getActivity().getSupportFragmentManager(), "capacitorModalsActionSheet");
    }

    private void showVideos(final PluginCall call) {
        openVideos(call);
    }

    private void showCamera(final PluginCall call) {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }
        openCamera(call);
    }

    @PluginMethod
    public void pickVideos(PluginCall call) {
        settings = getSettings(call);
        openVideos(call, true, false);
    }

    private boolean checkCameraPermissions(PluginCall call) {
        // if the manifest does not contain the camera permissions key, we don't need to ask the user
        boolean needCameraPerms = isPermissionDeclared(CAMERA);
        boolean hasCameraPerms = !needCameraPerms || getPermissionState(CAMERA) == PermissionState.GRANTED;

        // If we want to save to the gallery, we need two permissions
        if (!hasCameraPerms) {
            requestPermissionForAlias(CAMERA, call, "videoPermissionsCallback");
            return false;
        }
        return true;
    }

    private boolean checkVideosPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (getPermissionState(VIDEOS) != PermissionState.GRANTED) {
                requestPermissionForAlias(VIDEOS, call, "videoPermissionsCallback");
                return false;
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (getPermissionState(READ_EXTERNAL_STORAGE) != PermissionState.GRANTED) {
                requestPermissionForAlias(READ_EXTERNAL_STORAGE, call, "videoPermissionsCallback");
                return false;
            }
        } else if (getPermissionState(MEDIA) != PermissionState.GRANTED) {
            requestPermissionForAlias(MEDIA, call, "videoPermissionsCallback");
            return false;
        }

        return true;
    }

    /**
     * Completes the plugin call after a camera permission request
     *
     * @see #getVideo(PluginCall)
     * @param call the plugin call
     */
    @PermissionCallback
    private void videoPermissionsCallback(PluginCall call) {
        if (call.getMethodName().equals("pickVideos")) {
            openVideos(call, true, true);
        } else {
            if (settings.getSource() == VideoSource.CAMERA && getPermissionState(CAMERA) != PermissionState.GRANTED) {
                Logger.debug(getLogTag(), "User denied camera permission: " + getPermissionState(CAMERA).toString());
                call.reject(PERMISSION_DENIED_ERROR_CAMERA);
                return;
            } else if (settings.getSource() == VideoSource.VIDEOS) {
                String alias = MEDIA;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    alias = VIDEOS;
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    alias = READ_EXTERNAL_STORAGE;
                }
                PermissionState permissionState = getPermissionState(alias);
                if (permissionState != PermissionState.GRANTED) {
                    Logger.debug(getLogTag(), "User denied photos permission: " + permissionState.toString());
                    call.reject(PERMISSION_DENIED_ERROR_VIDEOS);
                    return;
                }
            }
            doShow(call);
        }
    }


    @Override
    protected void requestPermissionForAliases(@NonNull String[] aliases, @NonNull PluginCall call, @NonNull String callbackName) {
        // If the SDK version is 33 or higher, use the MEDIA alias permissions instead.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (int i = 0; i < aliases.length; i++) {
                if (aliases[i].equals(VIDEOS)) {
                    aliases[i] = MEDIA;
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (int i = 0; i < aliases.length; i++) {
                if (aliases[i].equals(VIDEOS)) {
                    aliases[i] = READ_EXTERNAL_STORAGE;
                }
            }
        }
        super.requestPermissionForAliases(aliases, call, callbackName);
    }

    private VideoRecorderSettings getSettings(PluginCall call) {
        VideoRecorderSettings settings = new VideoRecorderSettings();

        try {
            settings.setSource(VideoSource.valueOf(call.getString("source", VideoSource.PROMPT.getSource())));
        } catch (IllegalArgumentException ex) {
            settings.setSource(VideoSource.PROMPT);
        }

        return settings;
    }

    public void openCamera(final PluginCall call) {
        if (checkCameraPermissions(call)) {
            Intent captureVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (captureVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
                // If we will be saving the video, send the target file along
                try {
                    String appId = getAppId();
                    File photoFile = VideoRecorderUtils.createVideoFile(getActivity());
                    videoFileSavePath = photoFile.getAbsolutePath();
                    // TODO: Verify provider config exists
                    videoFileUri = FileProvider.getUriForFile(getActivity(), appId + ".fileprovider", photoFile);
                    captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);
                } catch (Exception ex) {
                    call.reject(VIDEO_FILE_SAVE_ERROR, ex);
                    return;
                }

                startActivityForResult(call, captureVideoIntent, "processCameraVideo");
            } else {
                call.reject(NO_CAMERA_ACTIVITY_ERROR);
            }
        }
    }

    public void openVideos(final PluginCall call) {
        openVideos(call, false, false);
    }

    private void openVideos(final PluginCall call, boolean multiple, boolean skipPermission) {
        if (skipPermission || checkVideosPermission(call)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
            intent.setType("video/*");
            try {
                if (multiple) {
                    intent.putExtra("multi-pick", multiple);
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "video/*" });
                    startActivityForResult(call, intent, "processPickedVideos");
                } else {
                    startActivityForResult(call, intent, "processPickedVideo");
                }
            } catch (ActivityNotFoundException ex) {
                call.reject(NO_VIDEO_ACTIVITY_ERROR);
            }
        }
    }

    @ActivityCallback
    public void processCameraVideo(PluginCall call, ActivityResult result) {
        settings = getSettings(call);
        if (videoFileSavePath == null) {
            call.reject(VIDEO_PROCESS_NO_FILE_ERROR);
            return;
        }
        // Load the image as a Bitmap
        File f = new File(videoFileSavePath);
        Uri contentUri = Uri.fromFile(f);

        if (!f.exists()) {
            call.reject("User cancelled videos app");
            return;
        }

        returnResult(call, contentUri);
    }

    @ActivityCallback
    public void processPickedVideo(PluginCall call, ActivityResult result) {
        settings = getSettings(call);
        Intent data = result.getData();
        if (data == null) {
            call.reject("No image picked");
            return;
        }

        Uri u = data.getData();

        videoPickedContentUri = u;

        processPickedVideo(u, call);
    }

    @ActivityCallback
    public void processPickedVideos(PluginCall call, ActivityResult result) {
        Intent data = result.getData();
        if (data != null) {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(
                    () -> {
                        JSObject ret = new JSObject();
                        JSArray photos = new JSArray();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri videoUri = data.getClipData().getItemAt(i).getUri();
                                JSObject processResult = processPickedVideos(videoUri);
                                if (processResult.getString("error") != null && !processResult.getString("error").isEmpty()) {
                                    call.reject(processResult.getString("error"));
                                    return;
                                } else {
                                    photos.put(processResult);
                                }
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            JSObject processResult = processPickedVideos(imageUri);
                            if (processResult.getString("error") != null && !processResult.getString("error").isEmpty()) {
                                call.reject(processResult.getString("error"));
                                return;
                            } else {
                                photos.put(processResult);
                            }
                        } else if (data.getExtras() != null) {
                            Bundle bundle = data.getExtras();
                            if (bundle.keySet().contains("selectedItems")) {
                                ArrayList<Parcelable> fileUris;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    fileUris = bundle.getParcelableArrayList("selectedItems", Parcelable.class);
                                } else {
                                    fileUris = getLegacyParcelableArrayList(bundle, "selectedItems");
                                }
                                if (fileUris != null) {
                                    for (Parcelable fileUri : fileUris) {
                                        if (fileUri instanceof Uri) {
                                            Uri videoUri = (Uri) fileUri;
                                            try {
                                                JSObject processResult = processPickedVideos(videoUri);
                                                if (processResult.getString("error") != null && !processResult.getString("error").isEmpty()) {
                                                    call.reject(processResult.getString("error"));
                                                    return;
                                                } else {
                                                    photos.put(processResult);
                                                }
                                            } catch (SecurityException ex) {
                                                call.reject("SecurityException");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ret.put("photos", photos);
                        call.resolve(ret);
                    }
            );
        } else {
            call.reject("No images picked");
        }
    }

    @SuppressWarnings("deprecation")
    private ArrayList<Parcelable> getLegacyParcelableArrayList(Bundle bundle, String key) {
        return bundle.getParcelableArrayList(key);
    }

    private void processPickedVideo(Uri videoUri, PluginCall call) {
        returnResult(call, videoUri);
    }

    private JSObject processPickedVideos(Uri videoUri) {
        JSObject ret = new JSObject();

        if (videoUri != null) {
            ret.put("path", videoUri.toString());
            ret.put("webPath", FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), videoUri));
        } else {
            ret.put("error", UNABLE_TO_PROCESS_VIDEO);
        }
        return ret;
    }



    /**
     * After processing the image, return the final result back to the caller.
     * @param call
     * @param uri
     */
    @SuppressWarnings("deprecation")
    private void returnResult(PluginCall call, Uri uri) {

        if (uri != null) {
            JSObject ret = new JSObject();
            ret.put("path", uri.toString());
            ret.put("webPath", FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), uri));
            call.resolve(ret);
        }

        videoFileSavePath = null;
        videoFileUri = null;
        videoPickedContentUri = null;
        videoEditedFileSavePath = null;
    }



    @Override
    @PluginMethod
    public void requestPermissions(PluginCall call) {
        // If the camera permission is defined in the manifest, then we have to prompt the user
        // or else we will get a security exception when trying to present the camera. If, however,
        // it is not defined in the manifest then we don't need to prompt and it will just work.
        if (isPermissionDeclared(CAMERA)) {
            // just request normally
            super.requestPermissions(call);
        } else {
            // the manifest does not define camera permissions, so we need to decide what to do
            // first, extract the permissions being requested
            JSArray providedPerms = call.getArray("permissions");
            List<String> permsList = null;
            if (providedPerms != null) {
                try {
                    permsList = providedPerms.toList();
                } catch (JSONException e) {}
            }

            if (permsList != null && permsList.size() == 1 && permsList.contains(CAMERA)) {
                // the only thing being asked for was the camera so we can just return the current state
                checkPermissions(call);
            } else {
                // we need to ask about vidoes so request storage permissions
                requestPermissionForAlias(VIDEOS, call, "checkPermissions");
            }
        }
    }

    private void deleteVideoFIle() {
        if (videoFileSavePath != null) {
            File videoFile = new File(videoFileSavePath);
            if (videoFile.exists()) {
                videoFile.delete();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private List<ResolveInfo> legacyQueryIntentActivities(Intent intent) {
        return getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    @Override
    protected Bundle saveInstanceState() {
        Bundle bundle = super.saveInstanceState();
        if (bundle != null) {
            bundle.putString("cameraImageFileSavePath", videoFileSavePath);
        }
        return bundle;
    }

    @Override
    protected void restoreState(Bundle state) {
        String storedImageFileSavePath = state.getString("cameraImageFileSavePath");
        if (storedImageFileSavePath != null) {
            videoFileSavePath = storedImageFileSavePath;
        }
    }
}
