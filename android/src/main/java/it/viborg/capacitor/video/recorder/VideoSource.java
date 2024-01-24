package it.viborg.capacitor.video.recorder;

// Based on: com.capacitorjs.plugins.camera.CameraSource

public enum VideoSource {
    PROMPT("PROMPT"),
    CAMERA("CAMERA"),
    VIDEOS("VIDEOS");

    private String source;

    VideoSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }
}
