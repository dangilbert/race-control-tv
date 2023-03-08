package fr.groggy.racecontrol.tv.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.groggy.racecontrol.tv.R
import fr.groggy.racecontrol.tv.core.credentials.CredentialsService
import fr.groggy.racecontrol.tv.ui.base.RaceControlActivity
import fr.groggy.racecontrol.tv.ui.home.HomeActivity
import fr.groggy.racecontrol.tv.ui.signin.SignInActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : RaceControlActivity(R.layout.activity_main) {
    @Inject internal lateinit var credentialsService: CredentialsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startHomeActivity()
    }

    private fun startHomeActivity() {
        lifecycleScope.launchWhenStarted {
            val intent = if (credentialsService.hasValidF1Credentials()) {
                HomeActivity.intent(this@MainActivity)
            } else {
                SignInActivity.intent(this@MainActivity)
            }
            startActivity(intent)
            finish()
        }
    }
}
