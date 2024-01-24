package it.viborg.capacitor.video.recorder;

// Based on: com.capacitorjs.plugins.camera.CameraSettings

public class VideoRecorderSettings {
    private VideoSource source = VideoSource.PROMPT;

    public VideoSource getSource() {
        return this.source;
    }

    public void setSource(VideoSource source) {
        this.source = source;
    }
}
