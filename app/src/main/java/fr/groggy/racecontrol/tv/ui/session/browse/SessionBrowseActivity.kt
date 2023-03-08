package fr.groggy.racecontrol.tv.ui.session.browse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.groggy.racecontrol.tv.R
import fr.groggy.racecontrol.tv.ui.base.RaceControlActivity
import fr.groggy.racecontrol.tv.ui.channel.playback.ChannelPlaybackActivity

@AndroidEntryPoint
class SessionBrowseActivity : RaceControlActivity(R.layout.activity_session_browse) {
    companion object {
        fun intent(
            context: Context,
            sessionId: String,
            contentId: String
        ): Intent {
            val intent = Intent(context, SessionBrowseActivity::class.java)
            SessionGridFragment.putContentId(intent, contentId)
            SessionGridFragment.putSessionId(intent, sessionId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentId = SessionGridFragment.findContentId(this)
            ?: return finish()
        val sessionId = SessionGridFragment.findSessionId(this)
            ?: return finish()
        val viewModel: SessionBrowseViewModel by viewModels()

        lifecycleScope.launchWhenStarted {
            when (val session = viewModel.sessionLoaded(sessionId, contentId)) {
                is SingleChannelSession -> {
                    val intent = ChannelPlaybackActivity.intent(
                        this@SessionBrowseActivity,
                        sessionId,
                        session.channel?.value,
                        session.contentId
                    )
                    startActivity(intent)
                    finish()
                }
                is MultiChannelsSession -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SessionGridFragment::class.java, null)
                        .commit()
                }
            }
        }
    }

}
