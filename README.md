# capacitor-video-recorder

Library that enables video recording in native Capacitor apps using the built in vindeo recorder abilities on Android and iOS

## Install

```bash
npm install capacitor-video-recorder
npx cap sync
```

## API

<docgen-index>

* [`getVideo(...)`](#getvideo)
* [`pickVideos(...)`](#pickvideos)
* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions(...)`](#requestpermissions)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getVideo(...)

```typescript
getVideo(options: VideoOptions) => Promise<Video>
```

Prompt the user to pick a video from an album, or take a new video
with the camera.

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#videooptions">VideoOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#video">Video</a>&gt;</code>

**Since:** 0.0.1

--------------------


### pickVideos(...)

```typescript
pickVideos(options: GalleryVideoOptions) => Promise<Videos>
```

Allows the user to pick multiple videos from the gallery.
On iOS 13 and older it only allows to pick one video.

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#galleryvideooptions">GalleryVideoOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#videos">Videos</a>&gt;</code>

**Since:** 0.0.1

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

Check camera and video album permissions

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 0.0.1

--------------------


### requestPermissions(...)

```typescript
requestPermissions(permissions?: VideoRecorderPluginPermissions | undefined) => Promise<PermissionStatus>
```

Request camera and video album permissions

| Param             | Type                                                                                      |
| ----------------- | ----------------------------------------------------------------------------------------- |
| **`permissions`** | <code><a href="#videorecorderpluginpermissions">VideoRecorderPluginPermissions</a></code> |

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 0.0.1

--------------------


### Interfaces


#### Video

| Prop          | Type                | Description                                                                                                                              | Since |
| ------------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`path`**    | <code>string</code> | If using CameraResultType.Uri, the path will contain a full, platform-specific file URL that can be read later using the Filesystem API. | 0.0.1 |
| **`webPath`** | <code>string</code> | webPath returns a path that can be used to set the src attribute of an video for efficient loading and rendering.                        | 0.0.1 |


#### VideoOptions

| Prop         | Type                                                | Description                                                                                                          | Default                            | Since |
| ------------ | --------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | ---------------------------------- | ----- |
| **`source`** | <code><a href="#videosource">VideoSource</a></code> | The source to get the photo from. By default this prompts the user to select either the photo album or take a photo. | <code>: CameraSource.Prompt</code> | 0.0.1 |


#### Videos

| Prop         | Type                  | Description                     | Since |
| ------------ | --------------------- | ------------------------------- | ----- |
| **`photos`** | <code>Videos[]</code> | Array of all the picked photos. | 0.0.1 |


#### GalleryVideoOptions

| Prop                    | Type                                   | Description                                                           | Default                     | Since |
| ----------------------- | -------------------------------------- | --------------------------------------------------------------------- | --------------------------- | ----- |
| **`presentationStyle`** | <code>'fullscreen' \| 'popover'</code> | iOS only: The presentation style of the Camera.                       | <code>: 'fullscreen'</code> | 0.0.1 |
| **`limit`**             | <code>number</code>                    | iOS only: Maximum number of pictures the user will be able to choose. | <code>0 (unlimited)</code>  | 0.0.1 |


#### PermissionStatus

| Prop         | Type                                                                  |
| ------------ | --------------------------------------------------------------------- |
| **`camera`** | <code><a href="#videopermissionstate">VideoPermissionState</a></code> |
| **`photos`** | <code><a href="#videopermissionstate">VideoPermissionState</a></code> |


#### VideoRecorderPluginPermissions

| Prop              | Type                               |
| ----------------- | ---------------------------------- |
| **`permissions`** | <code>VideoPermissionType[]</code> |


### Type Aliases


#### VideoPermissionState

<code><a href="#permissionstate">PermissionState</a> | 'limited'</code>


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### VideoPermissionType

<code>'camera' | 'videos'</code>


### Enums


#### VideoSource

| Members      | Value                 | Description                                                        |
| ------------ | --------------------- | ------------------------------------------------------------------ |
| **`Prompt`** | <code>"PROMPT"</code> | Prompts the user to select either the photo album or take a photo. |
| **`Camera`** | <code>"CAMERA"</code> | Take a new photo using the camera.                                 |
| **`Videos`** | <code>"VIDEOS"</code> | Pick an existing photo from the gallery or photo album.            |

</docgen-api>
