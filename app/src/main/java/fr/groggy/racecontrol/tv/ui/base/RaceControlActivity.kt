package fr.groggy.racecontrol.tv.ui.base

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper

abstract class RaceControlActivity(@LayoutRes layout: Int) : FragmentActivity(layout) {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

}