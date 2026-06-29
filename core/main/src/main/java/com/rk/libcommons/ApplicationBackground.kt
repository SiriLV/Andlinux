package com.rk.libcommons

// Reserved for future use. The previous `var isAppInBackground = false` was
// never read or written anywhere in the project, so it has been removed in
// AndLinux 1.5. When you actually need a global background-flag (e.g. for
// pausing terminal rendering when the app goes background), reintroduce it
// here and wire it up in App.kt's onTrimMemory / lifecycle callbacks.
