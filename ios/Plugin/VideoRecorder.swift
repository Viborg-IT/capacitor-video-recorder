import Foundation

@objc public class VideoRecorder: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
