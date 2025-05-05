import androidx.compose.ui.window.ComposeUIViewController
import io.github.sign_in_with_google.KGoogleSignIn
import io.gituhb.demo.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    KGoogleSignIn().initialize("com.googleusercontent.apps.204571788770-rhck30jdb0321gosbi07h94s50midf52")
    return ComposeUIViewController { App() }
}
