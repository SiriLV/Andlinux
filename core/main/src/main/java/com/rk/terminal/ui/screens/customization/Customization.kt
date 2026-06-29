package com.rk.terminal.ui.screens.customization

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.compose.ui.res.stringResource
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.components.compose.preferences.switch.PreferenceSwitch
import com.rk.resources.strings
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.localDir
import com.rk.libcommons.dpToPx
import com.rk.settings.Settings
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.navHosts.horizontal_statusBar
import com.rk.terminal.ui.navHosts.showStatusBar
import com.rk.terminal.ui.screens.terminal.bitmap
import com.rk.terminal.ui.screens.terminal.darkText
import com.rk.terminal.ui.screens.terminal.setFont
import com.rk.terminal.ui.screens.terminal.showHorizontalToolbar
import com.rk.terminal.ui.screens.terminal.showToolbar
import com.rk.terminal.ui.screens.terminal.showVirtualKeys
import com.rk.terminal.ui.screens.terminal.terminalView
import com.rk.terminal.ui.screens.terminal.wallAlpha
import com.rk.terminal.ui.screens.terminal.ShortcutAction
import com.rk.terminal.ui.screens.terminal.ShortcutCaptureDialog
import com.termux.terminal.TerminalColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt


private const val min_text_size = 10f
private const val max_text_size = 20f

private fun andLinuxThemeProperties(name: String): String = when (name) {
    "Dracula" -> """
color0=#21222c
color1=#ff5555
color2=#50fa7b
color3=#f1fa8c
color4=#bd93f9
color5=#ff79c6
color6=#8be9fd
color7=#f8f8f2
color8=#6272a4
color9=#ff6e6e
color10=#69ff94
color11=#ffffa5
color12=#d6acff
color13=#ff92df
color14=#a4ffff
color15=#ffffff
foreground=#f8f8f2
background=#282a36
cursor=#f8f8f2
""".trimIndent()
    "Nord" -> """
color0=#3b4252
color1=#bf616a
color2=#a3be8c
color3=#ebcb8b
color4=#81a1c1
color5=#b48ead
color6=#88c0d0
color7=#e5e9f0
color8=#4c566a
color9=#bf616a
color10=#a3be8c
color11=#ebcb8b
color12=#81a1c1
color13=#b48ead
color14=#8fbcbb
color15=#eceff4
foreground=#d8dee9
background=#2e3440
cursor=#d8dee9
""".trimIndent()
    "Solarized Dark" -> """
color0=#073642
color1=#dc322f
color2=#859900
color3=#b58900
color4=#268bd2
color5=#d33682
color6=#2aa198
color7=#eee8d5
color8=#002b36
color9=#cb4b16
color10=#586e75
color11=#657b83
color12=#839496
color13=#6c71c4
color14=#93a1a1
color15=#fdf6e3
foreground=#839496
background=#002b36
cursor=#839496
""".trimIndent()
    "Solarized Light" -> """
color0=#073642
color1=#dc322f
color2=#859900
color3=#b58900
color4=#268bd2
color5=#d33682
color6=#2aa198
color7=#eee8d5
color8=#002b36
color9=#cb4b16
color10=#586e75
color11=#657b83
color12=#839496
color13=#6c71c4
color14=#93a1a1
color15=#fdf6e3
foreground=#657b83
background=#fdf6e3
cursor=#657b83
""".trimIndent()
    "Gruvbox Dark" -> """
color0=#282828
color1=#cc241d
color2=#98971a
color3=#d79921
color4=#458588
color5=#b16286
color6=#689d6a
color7=#a89984
color8=#928374
color9=#fb4934
color10=#b8bb26
color11=#fabd2f
color12=#83a598
color13=#d3869b
color14=#8ec07c
color15=#ebdbb2
foreground=#ebdbb2
background=#282828
cursor=#ebdbb2
""".trimIndent()
    "Gruvbox Light" -> """
color0=#fbf1c7
color1=#cc241d
color2=#98971a
color3=#d79921
color4=#458588
color5=#b16286
color6=#689d6a
color7=#7c6f64
color8=#928374
color9=#9d0006
color10=#79740e
color11=#b57614
color12=#076678
color13=#8f3f71
color14=#427b58
color15=#3c3836
foreground=#3c3836
background=#fbf1c7
cursor=#3c3836
""".trimIndent()
    "One Dark" -> """
color0=#282c34
color1=#e06c75
color2=#98c379
color3=#e5c07b
color4=#61afef
color5=#c678dd
color6=#56b6c2
color7=#abb2bf
color8=#5c6370
color9=#e06c75
color10=#98c379
color11=#e5c07b
color12=#61afef
color13=#c678dd
color14=#56b6c2
color15=#ffffff
foreground=#abb2bf
background=#282c34
cursor=#528bff
""".trimIndent()
    "Tokyo Night" -> """
color0=#15161e
color1=#f7768e
color2=#9ece6a
color3=#e0af68
color4=#7aa2f7
color5=#bb9af7
color6=#7dcfff
color7=#a9b1d6
color8=#414868
color9=#f7768e
color10=#9ece6a
color11=#e0af68
color12=#7aa2f7
color13=#bb9af7
color14=#7dcfff
color15=#c0caf5
foreground=#a9b1d6
background=#1a1b26
cursor=#c0caf5
""".trimIndent()
    "Tokyo Night Light" -> """
color0=#0f0f14
color1=#8c4351
color2=#485e30
color3=#8f5e15
color4=#34548a
color5=#5a4a78
color6=#0f4b6e
color7=#343b58
color8=#9699a3
color9=#8c4351
color10=#485e30
color11=#8f5e15
color12=#34548a
color13=#5a4a78
color14=#0f4b6e
color15=#343b58
foreground=#343b58
background=#d5d6db
cursor=#343b58
""".trimIndent()
    "Catppuccin Mocha" -> """
color0=#45475a
color1=#f38ba8
color2=#a6e3a1
color3=#f9e2af
color4=#89b4fa
color5=#f5c2e7
color6=#94e2d5
color7=#bac2de
color8=#585b70
color9=#f38ba8
color10=#a6e3a1
color11=#f9e2af
color12=#89b4fa
color13=#f5c2e7
color14=#94e2d5
color15=#a6adc8
foreground=#cdd6f4
background=#1e1e2e
cursor=#f5e0dc
""".trimIndent()
    "Catppuccin Latte" -> """
color0=#5c5f77
color1=#d20f39
color2=#40a02b
color3=#df8e1d
color4=#1e66f5
color5=#ea76cb
color6=#179299
color7=#acb0be
color8=#6c6f85
color9=#d20f39
color10=#40a02b
color11=#df8e1d
color12=#1e66f5
color13=#ea76cb
color14=#179299
color15=#bcc0cc
foreground=#4c4f69
background=#eff1f5
cursor=#dc8a78
""".trimIndent()
    "Monokai" -> """
color0=#272822
color1=#f92672
color2=#a6e22e
color3=#f4bf75
color4=#66d9ef
color5=#ae81ff
color6=#a1efe4
color7=#f8f8f2
color8=#75715e
color9=#f92672
color10=#a6e22e
color11=#f4bf75
color12=#66d9ef
color13=#ae81ff
color14=#a1efe4
color15=#f9f8f5
foreground=#f8f8f2
background=#272822
cursor=#f8f8f2
""".trimIndent()
    "Material Dark" -> """
color0=#212121
color1=#f07178
color2=#c3e88d
color3=#ffcb6b
color4=#82aaff
color5=#c792ea
color6=#89ddff
color7=#eeffff
color8=#545454
color9=#f07178
color10=#c3e88d
color11=#ffcb6b
color12=#82aaff
color13=#c792ea
color14=#89ddff
color15=#ffffff
foreground=#eeffff
background=#212121
cursor=#ffcc00
""".trimIndent()
    "Ayu Dark" -> """
color0=#0a0e14
color1=#ff3333
color2=#b8cc52
color3=#e7c547
color4=#36a3d9
color5=#f07178
color6=#95e6cb
color7=#b3b1ad
color8=#626a73
color9=#ff6565
color10=#eafe84
color11=#fff779
color12=#68d5ff
color13=#ffa3aa
color14=#c7fffd
color15=#ffffff
foreground=#b3b1ad
background=#0a0e14
cursor=#e6b450
""".trimIndent()
    "Ayu Light" -> """
color0=#000000
color1=#ff3333
color2=#86b300
color3=#f29718
color4=#41a6d9
color5=#f07178
color6=#4dbf99
color7=#ffffff
color8=#323232
color9=#ff6565
color10=#b8e532
color11=#ffc94a
color12=#73d8ff
color13=#ffa3aa
color14=#7ff1cb
color15=#ffffff
foreground=#5c6166
background=#fafafa
cursor=#ff6a00
""".trimIndent()
    else -> ""
}

private fun andLinuxThemeIsLight(name: String): Boolean = name in setOf(
    "Solarized Light", "Gruvbox Light", "Tokyo Night Light", "Catppuccin Latte", "Ayu Light"
)


@Composable
fun Customization(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    PreferenceLayout(label = stringResource(strings.customizations)) {
        var sliderPosition by remember { mutableFloatStateOf(Settings.terminal_font_size.toFloat()) }
        PreferenceGroup {
            PreferenceTemplate(title = { Text(stringResource(strings.text_size)) }) {
                Text(sliderPosition.toInt().toString())
            }
            PreferenceTemplate(title = {}) {
                Slider(
                    modifier = modifier,
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        Settings.terminal_font_size = it.toInt()
                        terminalView.get()?.setTextSize(dpToPx(it.toFloat(), context))
                    },
                    steps = (max_text_size - min_text_size).toInt() - 1,
                    valueRange = min_text_size..max_text_size,
                )
            }
        }

        PreferenceGroup {
            PreferenceTemplate(
                title = { Text("Color Scheme") },
                description = { Text("Terminal palettes based on Termix built-in schemes") }
            )

            var selectedTheme by remember { mutableStateOf(Settings.terminal_theme) }
            val themeNames = listOf(
                "Default",
                "Dracula",
                "Nord",
                "Solarized Dark",
                "Solarized Light",
                "Gruvbox Dark",
                "Gruvbox Light",
                "One Dark",
                "Tokyo Night",
                "Tokyo Night Light",
                "Catppuccin Mocha",
                "Catppuccin Latte",
                "Monokai",
                "Material Dark",
                "Ayu Dark",
                "Ayu Light"
            )
            val colorsFile = localDir().child("colors.properties")

            fun applyTheme(themeName: String) {
                selectedTheme = themeName
                Settings.terminal_theme = themeName
                val data = andLinuxThemeProperties(themeName)
                if (data.isBlank()) {
                    colorsFile.delete()
                    TerminalColors.COLOR_SCHEME.updateWith(Properties())
                } else {
                    colorsFile.parentFile?.mkdirs()
                    colorsFile.writeText(data)
                    val props = Properties()
                    props.load(data.byteInputStream())
                    TerminalColors.COLOR_SCHEME.updateWith(props)
                }
                val lightTheme = andLinuxThemeIsLight(themeName)
                Settings.blackTextColor = lightTheme
                darkText.value = lightTheme
                terminalView.get()?.apply {
                    mEmulator?.mColors?.reset()
                    onScreenUpdated()
                }
            }

            themeNames.forEach { themeName ->
                PreferenceTemplate(
                    modifier = Modifier.clickable { applyTheme(themeName) },
                    title = { Text(themeName) },
                    description = { Text(if (themeName == "Default") "Built-in terminal colors" else if (andLinuxThemeIsLight(themeName)) "Light terminal color preset" else "Dark terminal color preset") },
                    startWidget = { RadioButton(selected = selectedTheme == themeName, onClick = { applyTheme(themeName) }) }
                )
            }
        }

        fun getFileNameFromUri(context: Context, uri: Uri): String? {
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        return cursor.getString(nameIndex)
                    }
                }
            } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
                return File(uri.path!!).name
            }
            return null
        }

        PreferenceGroup {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                }
                Text(
                    text = stringResource(strings.font_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            val scope = rememberCoroutineScope()
            val font by remember { mutableStateOf<File>(context.filesDir.child("font.ttf")) }
            var fontExists by remember { mutableStateOf(font.exists()) }

            val noFontSelected = stringResource(strings.no_font_selected)
            var fontName by remember { mutableStateOf(if (!fontExists || !font.canRead()){
                noFontSelected
            }else{
                Settings.custom_font_name
            }) }

            val fontLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    scope.launch(Dispatchers.IO){
                        font.createFileIfNot()
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            font.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        val name = getFileNameFromUri(context,uri).toString()
                        Settings.custom_font_name = name
                        fontName = name
                        fontExists = font.exists()
                        setFont(Typeface.createFromFile(font))
                    }
                }

            }

            PreferenceTemplate(
                modifier = Modifier.clickable(onClick = {
                    scope.launch{
                        fontLauncher.launch("font/ttf")
                    }
                }),
                title = {
                    Text(stringResource(strings.custom_font))
                },
                description = {
                    Text(fontName)
                },
                endWidget = {
                    if (fontExists){
                        IconButton(onClick = {
                            scope.launch{
                                font.delete()
                                fontName = noFontSelected
                                Settings.custom_font_name = noFontSelected
                                setFont(Typeface.MONOSPACE)
                                fontExists = font.exists()
                            }
                        }) {
                            Icon(imageVector = Icons.Outlined.Delete,contentDescription = "delete")
                        }
                    }
                }
            )
        }

        PreferenceGroup {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val image by remember { mutableStateOf<File>(context.filesDir.child("background")) }


            var imageExists by remember { mutableStateOf(image.exists()) }



            val noImageSelected = stringResource(strings.no_image_selected)
            var backgroundName by remember { mutableStateOf(if (!imageExists || !image.canRead()){
                noImageSelected
            }else{
                Settings.custom_background_name
            }) }



            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    scope.launch(Dispatchers.IO){
                        image.createFileIfNot()
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            image.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        val name = getFileNameFromUri(context,uri).toString()
                        Settings.custom_background_name = name
                        backgroundName = name


                        withContext(Dispatchers.IO) {
                            val file = context.filesDir.child("background")
                            if (!file.exists()) return@withContext
                            bitmap.value = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                            bitmap.value?.apply {
                                val androidBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val buffer = IntArray(width * height)
                                readPixels(buffer, 0, 0, width, height)
                                androidBitmap.setPixels(buffer, 0, width, 0, 0, width, height)
                                Palette.from(androidBitmap).generate { palette ->
                                    val dominantColor = palette?.getDominantColor(android.graphics.Color.WHITE)
                                    val luminance = androidx.core.graphics.ColorUtils.calculateLuminance(dominantColor ?: android.graphics.Color.WHITE)
                                    val blackText = luminance > 0.5f
                                    Settings.blackTextColor = blackText
                                    darkText.value = blackText
                                }
                            }

                        }
                        imageExists = image.exists()
                    }

                }


            }

            PreferenceTemplate(
                modifier = Modifier.clickable(onClick = {
                    scope.launch{
                        launcher.launch("image/*")
                    }
                }),
                title = {
                    Text(stringResource(strings.custom_background))
                },
                description = {
                    Text(backgroundName)
                },
                endWidget = {
                    val darkMode = isSystemInDarkTheme()
                    if (imageExists){
                        IconButton(onClick = {
                            scope.launch{
                                image.delete()
                                Settings.custom_background_name = noImageSelected
                                backgroundName = noImageSelected
                                darkText.value = !darkMode
                                imageExists = image.exists()
                                bitmap.value = null
                            }
                        }) {
                            Icon(imageVector = Icons.Outlined.Delete,contentDescription = "delete")
                        }
                    }

                }
            )

        }

        PreferenceGroup {
            PreferenceTemplate(title = {
                Text(stringResource(strings.wallpaper_alpha))
            }) { Text(
                DecimalFormat("0.##")
                .apply { roundingMode = RoundingMode.HALF_UP }
                .format(wallAlpha)) }
            PreferenceTemplate(title = {}){
                Slider(
                    value = wallAlpha,
                    onValueChange = {
                        wallAlpha = it
                    },
                    onValueChangeFinished = {
                        Settings.wallTransparency = wallAlpha
                    }
                )
            }
        }


        PreferenceGroup {
            SettingsToggle(label = stringResource(strings.bell), description = stringResource(strings.bell_desc), showSwitch = true, default = Settings.bell, sideEffect = {
                Settings.bell = it
            })

            SettingsToggle(label = stringResource(strings.vibrate), description = stringResource(strings.vibrate_desc), showSwitch = true, default = Settings.vibrate, sideEffect = {
                Settings.vibrate = it
            })
        }

        PreferenceGroup {
            SettingsToggle(
                label = stringResource(strings.statusbar),
                description = stringResource(strings.statusbar_desc),
                showSwitch = true,
                default = Settings.statusBar, sideEffect = {
                    Settings.statusBar = it
                    showStatusBar.value = it
                })

            SettingsToggle(
                label = stringResource(strings.horizontal_statusbar),
                description = stringResource(strings.horizontal_statusbar_desc),
                showSwitch = true,
                default = Settings.horizontal_statusBar, sideEffect = {
                    Settings.horizontal_statusBar = it
                    horizontal_statusBar.value = it
                })


            val attentionTitle = stringResource(strings.attention)
            val toolbarWarning = stringResource(strings.toolbar_warning)
            val cancelStr = stringResource(strings.cancel)
            val sideEffect:(Boolean)-> Unit = {
                if (!it && showToolbar.value){
                    MaterialAlertDialogBuilder(context).apply {
                        setTitle(attentionTitle)
                        setMessage(toolbarWarning)
                        setPositiveButton("OK"){_,_ ->
                            Settings.toolbar = it
                            showToolbar.value = it
                        }
                        setNegativeButton(cancelStr,null)
                        show()
                    }
                }else{
                    Settings.toolbar = it
                    showToolbar.value = it
                }

            }


            PreferenceSwitch(checked = showToolbar.value,
                onCheckedChange = {
                    sideEffect.invoke(it)
                },
                label = stringResource(strings.titlebar),
                modifier = modifier,
                description = stringResource(strings.titlebar_desc),
                onClick = {
                    sideEffect.invoke(!showToolbar.value)
                })

            SettingsToggle(
                isEnabled = showToolbar.value,
                label = stringResource(strings.horizontal_titlebar),
                description = stringResource(strings.horizontal_titlebar_desc),
                showSwitch = true,
                default = Settings.toolbar_in_horizontal, sideEffect = {
                    Settings.toolbar_in_horizontal = it
                    showHorizontalToolbar.value = it
                })
            SettingsToggle(
                label = stringResource(strings.virtual_keys),
                description = stringResource(strings.virtual_keys_desc),
                showSwitch = true,
                default = Settings.virtualKeys, sideEffect = {
                    Settings.virtualKeys = it
                    showVirtualKeys.value = it
                })

            SettingsToggle(
                label = stringResource(strings.hide_soft_keyboard),
                description = stringResource(strings.hide_soft_keyboard_desc),
                showSwitch = true,
                default = Settings.hide_soft_keyboard_if_hwd, sideEffect = {
                    Settings.hide_soft_keyboard_if_hwd = it
                })

        }

        // Keyboard Shortcuts
        PreferenceGroup(heading = stringResource(strings.keyboard_shortcuts)) {
            var shortcutsEnabled by remember { mutableStateOf(Settings.shortcuts_enabled) }
            var showCaptureFor by remember { mutableStateOf<ShortcutAction?>(null) }

            SettingsToggle(
                label = stringResource(strings.keyboard_shortcuts),
                description = stringResource(strings.keyboard_shortcuts_desc),
                showSwitch = true,
                default = Settings.shortcuts_enabled,
                sideEffect = {
                    Settings.shortcuts_enabled = it
                    shortcutsEnabled = it
                })

            for (action in ShortcutAction.entries) {
                val binding = Settings.getShortcutBinding(action)
                val labelRes = when (action) {
                    ShortcutAction.PASTE -> strings.shortcut_paste
                    ShortcutAction.NEW_SESSION -> strings.shortcut_new_session
                    ShortcutAction.CLOSE_SESSION -> strings.shortcut_close_session
                    ShortcutAction.SWITCH_SESSION_PREV -> strings.shortcut_switch_prev
                    ShortcutAction.SWITCH_SESSION_NEXT -> strings.shortcut_switch_next
                }
                val descRes = when (action) {
                    ShortcutAction.PASTE -> strings.shortcut_paste_desc
                    ShortcutAction.NEW_SESSION -> strings.shortcut_new_session_desc
                    ShortcutAction.CLOSE_SESSION -> strings.shortcut_close_session_desc
                    ShortcutAction.SWITCH_SESSION_PREV -> strings.shortcut_switch_prev_desc
                    ShortcutAction.SWITCH_SESSION_NEXT -> strings.shortcut_switch_next_desc
                }
                SettingsToggle(
                    isEnabled = shortcutsEnabled,
                    label = stringResource(labelRes),
                    description = "${stringResource(descRes)} (${binding.toDisplayString()})",
                    showSwitch = false,
                    default = false,
                    sideEffect = { showCaptureFor = action },
                )
            }

            if (showCaptureFor != null) {
                ShortcutCaptureDialog(
                    action = showCaptureFor!!,
                    onDismiss = { showCaptureFor = null },
                    onConfirm = { binding ->
                        Settings.setShortcutBinding(showCaptureFor!!, binding)
                        showCaptureFor = null
                    },
                )
            }
        }


    }


}
