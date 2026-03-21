package com.template.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.template.project.di.appModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Template KMP",
        ) {
            App()
        }
    }
}
