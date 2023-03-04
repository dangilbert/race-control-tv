package fr.groggy.racecontrol.tv.ui.season.browse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.groggy.racecontrol.tv.R
import fr.groggy.racecontrol.tv.core.season.SeasonService
import fr.groggy.racecontrol.tv.f1tv.Archive
import fr.groggy.racecontrol.tv.utils.coroutines.schedule
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Year
import javax.inject.Inject

@AndroidEntryPoint
class SeasonBrowseActivity : FragmentActivity(R.layout.activity_season_browse) {

    companion object {
        private val TAG = SeasonBrowseActivity::class.simpleName

        fun intent(context: Context): Intent =
            Intent(context, SeasonBrowseActivity::class.java)

        fun intent(context: Context, archive: Archive): Intent {
            val intent = intent(context)
            SeasonBrowseFragment.putArchive(
                intent,
                archive
            )
            return intent
        }
    }

    @Inject internal lateinit var seasonService: SeasonService

    private var syncJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: SeasonBrowseViewModel by viewModels()
        lifecycleScope.launchWhenCreated {
            val archive = SeasonBrowseFragment.findArchive(this@SeasonBrowseActivity)
            viewModel.archiveLoaded(archive)
        }
    }

    override fun onStart() {
        super.onStart()
        syncJob = lifecycleScope.launch {
            val archive = SeasonBrowseFragment.findArchive(this@SeasonBrowseActivity)
            // Only refresh the season data if it is the current year, as older content should not be changing
            if (archive.year != Year.now().value) {
                loadSeasonContent()
            } else {
                schedule(Duration.ofMinutes(1)) {
                    loadSeasonContent()
                }
            }
        }
    }

    override fun onStop() {
        syncJob?.cancel()
        syncJob = null
        super.onStop()
    }

    private suspend fun loadSeasonContent() {
        Log.d("Fetching new data", "Lifecycle state is ${lifecycle.currentState}")
        val archive = SeasonBrowseFragment.findArchive(this@SeasonBrowseActivity)
        seasonService.loadSeason(archive)

        if (supportFragmentManager.findFragmentByTag(TAG) !is SeasonBrowseFragment) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, SeasonBrowseFragment(), TAG)
            }
        }
    }

}
