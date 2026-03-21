package com.template.project

import androidx.compose.ui.window.ComposeUIViewController
import com.template.project.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(appModule)
        }
    }
) {
    App()
}