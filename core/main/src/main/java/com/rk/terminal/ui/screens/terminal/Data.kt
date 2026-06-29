package com.rk.terminal.ui.screens.terminal

// Note: the fake /proc/cpuinfo, /proc/stat and /proc/vmstat strings that used
// to live here were leftovers from the btop fake-procfs shim that was removed
// in commit df109e8 ("stability: remove btop-specific fake procfs sysfs shims").
// They had no remaining callers and were just dead bytes in the APK — deleted
// in AndLinux 1.5.
//
// If you ever need host-side static data for a future fake-procfs shim, add it
// here as a `private const val` so it stays scoped to this file.
