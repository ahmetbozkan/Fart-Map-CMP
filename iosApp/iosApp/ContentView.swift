import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        // APP_ENV comes from the active xcconfig through Info.plist (dev | prod).
        let env = Bundle.main.object(forInfoDictionaryKey: "APP_ENV") as? String ?? "prod"
        return MainViewControllerKt.MainViewController(environmentName: env)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
