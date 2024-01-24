import type { PermissionState } from '@capacitor/core';
export declare type VideoPermissionState = PermissionState | 'limited';
export declare type VideoPermissionType = 'camera' | 'videos';

export interface PermissionStatus {
    camera: VideoPermissionState;
    photos: VideoPermissionState;
}
export interface VideoRecorderPluginPermissions {
    permissions: VideoPermissionType[];
}

export interface VideoRecorderPlugin {
    /**
     * Prompt the user to pick a video from an album, or take a new video
     * with the camera.
     *
     * @since 0.0.1
     */
    getVideo(options: VideoOptions): Promise<Video>;
    /**
     * Allows the user to pick multiple videos from the gallery.
     * On iOS 13 and older it only allows to pick one video.
     *
     * @since 0.0.1
     */
    pickVideos(options: GalleryVideoOptions): Promise<Videos>;
    /**
     * Check camera and video album permissions
     *
     * @since 0.0.1
     */
    checkPermissions(): Promise<PermissionStatus>;
    /**
     * Request camera and video album permissions
     *
     * @since 0.0.1
     */
    requestPermissions(permissions?: VideoRecorderPluginPermissions): Promise<PermissionStatus>;
    /**
     * Text value to use when displaying the prompt.
     * @default: 'Video'
     *
     * @since 0.0.1
     *
     */
    promptLabelHeader?: string;

    /**
     * Text value to use when displaying the prompt.
     * iOS only: The label of the 'cancel' button.
     * @default: 'Cancel'
     *
     * @since 0.0.1
     */
    promptLabelCancel?: string;

    /**
     * Text value to use when displaying the prompt.
     * The label of the button to select a saved image.
     * @default: 'From Videos'
     *
     * @since 0.0.1
     */
    promptLabelVideos?: string;

    /**
     * Text value to use when displaying the prompt.
     * The label of the button to open the camera.
     * @default: 'Record Video'
     *
     * @since 0.0.1
     */
    promptLabelVideo?: string;
}

export interface VideoOptions {
    /**
    * The source to get the video from. By default this prompts the user to select
    * either the video album or take a video.
    * @default: VideoSource.Prompt
    *
    * @since 0.0.1
    */
    source?: VideoSource;
}

export interface Video {
    /**
     * If using CameraResultType.Uri, the path will contain a full,
     * platform-specific file URL that can be read later using the Filesystem API.
     *
     * @since 0.0.1
     */
    path?: string;
    /**
     * webPath returns a path that can be used to set the src attribute of an video for efficient
     * loading and rendering.
     *
     * @since 0.0.1
     */
    webPath?: string;
}

export interface Videos {
    /**
     * Array of all the picked videos.
     *
     * @since 0.0.1
     */
    photos: Video[];
}

export interface GalleryVideoOptions {
    /**
     * iOS only: The presentation style of the Camera.
     * @default: 'fullscreen'
     *
     * @since 0.0.1
     */
    presentationStyle?: 'fullscreen' | 'popover';
    /**
     * iOS only: Maximum number of videos the user will be able to choose.
     * @default 0 (unlimited)
     *
     * @since 0.0.1
     */
    limit?: number;
}

export declare enum VideoSource {
    /**
     * Prompts the user to select either the video album or take a video.
     */
    Prompt = "PROMPT",
    /**
     * Take a new video using the camera.
     */
    Camera = "CAMERA",
    /**
     * Pick an existing video from the gallery or video album.
     */
    Videos = "VIDEOS"
}