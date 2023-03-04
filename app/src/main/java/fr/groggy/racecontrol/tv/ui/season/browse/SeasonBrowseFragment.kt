package fr.groggy.racecontrol.tv.ui.season.browse

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import fr.groggy.racecontrol.tv.R
import fr.groggy.racecontrol.tv.core.settings.SettingsRepository
import fr.groggy.racecontrol.tv.f1tv.Archive
import fr.groggy.racecontrol.tv.ui.event.EventListRowDiffCallback
import fr.groggy.racecontrol.tv.ui.session.SessionCardPresenter
import fr.groggy.racecontrol.tv.ui.session.browse.SessionBrowseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.Year
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class SeasonBrowseFragment : BrowseSupportFragment(), OnItemViewClickedListener {
    @Inject internal lateinit var sessionCardPresenter: SessionCardPresenter
    @Inject lateinit var settingsRepository: SettingsRepository

    private val eventListRowDiffCallback = EventListRowDiffCallback()

    private val eventsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setupUIElements()
        setupEventListeners()

        val viewModel: SeasonBrowseViewModel by viewModels({ requireActivity() })
        val archive = findArchive(requireActivity())
        lifecycleScope.launchWhenStarted {
            viewModel.getSeason(archive).asLiveData().observe(viewLifecycleOwner, ::onUpdatedSeason)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (settingsRepository.getCurrent().displayThumbnailsEnabled) {
            initializeBackground()
        }
    }

    private fun setupUIElements() {
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.fastlane_background)
        adapter = eventsAdapter
    }

    /**
     * Function that will load a [Session.largePictureUrl] as a background image when a card is selected
     */
    private fun initializeBackground() {
        val backgroundManager = BackgroundManager.getInstance(activity).apply {
            attach(activity?.window)
        }

        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
            if (item is Session) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(BACKGROUND_UPDATE_DELAY) // A small delay so that the loading of the image and transition is more smooth

                    Glide.with(requireActivity())
                        .load(item.largePictureUrl)
                        .error(R.drawable.banner)
                        .into<CustomTarget<Drawable>>(object :
                            CustomTarget<Drawable>(metrics.widthPixels, metrics.heightPixels) {

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                backgroundManager.drawable = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })

                }
            }
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = this
    }

    private fun onUpdatedSeason(season: Season) {
        title = season.name
        val existingListRows = eventsAdapter.unmodifiableList<ListRow>()
        val events = season.events
            .filter { it.sessions.isNotEmpty() }
            .map { toListRow(it, existingListRows) }
        if (existingListRows.size != events.size ||
            (0 until existingListRows.size).any { index -> !hasMatchingSessions(existingListRows[index], events[index]) }) {
            eventsAdapter.setItems(events, eventListRowDiffCallback)
        }
    }

    private fun hasMatchingSessions(
        existingListRow: ListRow,
        sessionsListRow: ListRow
    ) = (existingListRow.adapter.size() == sessionsListRow.adapter.size() // Do we have the same number of items
            || (0 until existingListRow.adapter.size()).all { index -> // If so, do the sessions in each match in order?
        existingListRow.adapter[index] as Session == sessionsListRow.adapter[index] as Session
    })

    private fun toListRow(event: Event, existingListRows: List<ListRow>): ListRow {
        val existingListRow = existingListRows.find { it.headerItem.name == event.name }
        val (listRow, sessionsAdapter) = if (existingListRow == null) {
            val sessionsAdapter = ArrayObjectAdapter(sessionCardPresenter)
            val listRow = ListRow(HeaderItem(event.name), sessionsAdapter)
            listRow to sessionsAdapter
        } else {
            val sessionsAdapter = existingListRow.adapter as ArrayObjectAdapter
            existingListRow to sessionsAdapter
        }
        if (existingListRow == null || !hasMatchingSessions(existingListRow, listRow)) {
            sessionsAdapter.setItems(event.sessions, Session.diffCallback)
        }
        return listRow
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder,
        item: Any,
        rowViewHolder: RowPresenter.ViewHolder,
        row: Row
    ) {
        val session = item as Session
        val intent =
            SessionBrowseActivity.intent(requireActivity(), session.id.value, session.contentId)
        startActivity(intent)
    }

    companion object {
        private val TAG = SeasonBrowseFragment::class.simpleName

        private val YEAR = "${SeasonBrowseFragment::class}.YEAR"

        private const val BACKGROUND_UPDATE_DELAY = 300L

        fun putArchive(intent: Intent, archive: Archive) {
            intent.putExtra(YEAR, archive.year)
        }

        fun findArchive(activity: Activity): Archive {
            val year = activity.intent.getIntExtra(
                YEAR, Year.now().value
            )
            return Archive(year)
        }
    }
}
