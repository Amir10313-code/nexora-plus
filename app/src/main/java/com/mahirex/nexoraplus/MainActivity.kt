package com.mahirex.nexoraplus

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.ui.nav.NexoraApp
import com.mahirex.nexoraplus.ui.theme.NexoraTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val settingsState = mutableStateOf(AppSettings())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                container().settings.settings.collectLatest { s ->
                    settingsState.value = s
                    applyLocale(s.languageTag)
                }
            }
        }

        setContent {
            val settings by settingsState
            NexoraTheme(themeMode = settings.themeMode) {
                val direction = layoutDirectionFor(settings.languageTag)
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    NexoraApp(
                        settings = settings,
                        container = container()
                    )
                }
            }
        }
    }

    private fun container(): AppContainer = (application as NexoraPlusApp).container

    private fun applyLocale(languageTag: String) {
        val target = if (languageTag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(languageTag)
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != target.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(target)
        }
    }

    companion object {
        private val RTL_LANGS = setOf("ar", "fa", "ur", "ku", "he", "iw")

        /** Decides the layout direction: explicit language wins, otherwise the device locale. */
        fun layoutDirectionFor(languageTag: String): LayoutDirection {
            val lang = if (languageTag.isEmpty()) {
                ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)?.language
                    ?: (Resources.getSystem().configuration.let { c ->
                        if (c.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) "ar" else "en"
                    })
            } else languageTag.substringBefore('-').lowercase()
            return if (lang in RTL_LANGS) LayoutDirection.Rtl else LayoutDirection.Ltr
        }
    }
}
