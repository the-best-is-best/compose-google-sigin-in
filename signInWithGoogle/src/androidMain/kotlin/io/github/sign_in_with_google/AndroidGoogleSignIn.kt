
package io.github.sign_in_with_google


import android.app.Activity
import java.lang.ref.WeakReference

object AndroidGoogleSignIn {
    private var activity: WeakReference<Activity?> = WeakReference(null)

    internal fun getActivity(): Activity {
        return activity.get()!!
    }

    fun initialization(activity: Activity) {
        this.activity = WeakReference(activity)
    }
}



