import SwiftUI
import FirebaseCore
import GoogleSignIn
import ComposeApp

private class SwiftGoogleSignInDelegate: GoogleSignInDelegate {
    func signIn(completion: @escaping (String?, String?) -> Void) {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let rootVC = scene.windows.first?.rootViewController
        else {
            completion(nil, nil)
            return
        }
        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            guard
                error == nil,
                let user = result?.user,
                let idToken = user.idToken?.tokenString
            else {
                completion(nil, nil)
                return
            }
            completion(idToken, user.accessToken.tokenString)
        }
    }
}

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        GoogleSignInDelegateHolder.shared.delegate = SwiftGoogleSignInDelegate()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
