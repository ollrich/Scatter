package app.scatterto

import android.app.Application
import app.scatterto.data.AppContainer

/**
 * Einstiegspunkt der App und Ort der manuellen Dependency-Injection (§12.3 Nr. 2):
 * hält den verschlüsselten Credential-Store und die drei Repositories im [AppContainer].
 */
class ScatterToApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
