import UIKit
import FirebaseCore
import GoogleSignIn
import Shared

/// Google sign-in for the shared `GoogleIdTokenProvider` (STORY-01-02-03). Returns Google tokens only; Firebase
/// sign-in happens in Kotlin. Never logs: the Kotlin side logs the failure category.
final class GoogleSignInBridgeImpl: NSObject, GoogleSignInBridge {

    func signIn(callback: GoogleSignInCallback) {
        DispatchQueue.main.async {
            self.start(callback)
        }
    }

    private func start(_ callback: GoogleSignInCallback) {
        // CLIENT_ID comes from the injected GoogleService-Info.plist. Without it, or without the reversed-id URL
        // scheme (added by the "Register Google URL Scheme" build phase), the SDK would raise: report instead.
        guard let clientID = FirebaseApp.app()?.options.clientID, !clientID.isEmpty,
              Self.isURLSchemeRegistered(Self.reversed(clientID)) else {
            callback.onFailure(reason: "not_configured")
            return
        }
        guard let presenter = Self.topViewController() else {
            callback.onFailure(reason: "failed")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error {
                if let signInError = error as? GIDSignInError, signInError.code == .canceled {
                    callback.onCancelled()
                } else {
                    callback.onFailure(reason: (error as NSError).domain == NSURLErrorDomain ? "network" : "failed")
                }
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                callback.onFailure(reason: "failed")
                return
            }
            callback.onToken(idToken: idToken, accessToken: user.accessToken.tokenString)
        }
    }

    /// "123-abc.apps.googleusercontent.com" → "com.googleusercontent.apps.123-abc".
    private static func reversed(_ clientID: String) -> String {
        clientID.split(separator: ".").reversed().joined(separator: ".")
    }

    private static func isURLSchemeRegistered(_ scheme: String) -> Bool {
        let types = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes") as? [[String: Any]] ?? []
        return types.contains { type in
            (type["CFBundleURLSchemes"] as? [String] ?? []).contains { $0.caseInsensitiveCompare(scheme) == .orderedSame }
        }
    }

    private static func topViewController() -> UIViewController? {
        let window = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }
        var top = window?.rootViewController
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }
}
