package fr.groggy.racecontrol.tv.ui.season.archive

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.groggy.racecontrol.tv.R
import fr.groggy.racecontrol.tv.ui.base.RaceControlActivity

@AndroidEntryPoint
class SeasonArchiveActivity: RaceControlActivity(R.layout.activity_archive_list) {
    companion object {
        fun intent(context: Context) = Intent(context, SeasonArchiveActivity::class.java)
    }
}
