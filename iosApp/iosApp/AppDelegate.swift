import UIKit
import FirebaseCore
import Shared

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // GoogleService-Info.plist is copied into the bundle per environment by the "Copy Firebase Config" build phase.
        FirebaseApp.configure()
        // APP_ENV comes from the active xcconfig through Info.plist (dev | prod). Starts telemetry switch + Koin.
        let env = Bundle.main.object(forInfoDictionaryKey: "APP_ENV") as? String ?? "prod"
        AppStartupKt.startApp(environmentName: env, googleSignIn: GoogleSignInBridgeImpl())
        return true
    }
}
