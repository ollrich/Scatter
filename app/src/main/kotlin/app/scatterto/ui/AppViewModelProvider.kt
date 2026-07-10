package app.scatterto.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.scatterto.ScatterToApplication
import app.scatterto.ui.display.DisplayViewModel
import app.scatterto.ui.log.LogViewModel
import app.scatterto.ui.main.MainViewModel
import app.scatterto.ui.settings.SettingsViewModel

/**
 * ViewModel-Factory für die manuelle DI (§12.3 Nr. 2): zieht den [app.scatterto.data.AppContainer]
 * aus der Application, statt ein DI-Framework zu nutzen.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { SettingsViewModel(app().container) }
        initializer { MainViewModel(app().container, createSavedStateHandle()) }
        initializer { LogViewModel(app().container) }
        initializer { DisplayViewModel(app().container) }
    }
}

private fun CreationExtras.app(): ScatterToApplication =
    this[APPLICATION_KEY] as ScatterToApplication
