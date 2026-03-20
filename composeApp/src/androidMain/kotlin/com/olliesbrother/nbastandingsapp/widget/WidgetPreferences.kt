package com.olliesbrother.nbastandingsapp.widget

import android.content.Context
import com.olliesbrother.nbastandingsapp.model.Conference

class WidgetPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("nba_widget_prefs", Context.MODE_PRIVATE)

    fun getSelectedConference(): Conference {
        val value = prefs.getString("selected_conference", Conference.EAST.name)
            ?: Conference.EAST.name
        return Conference.valueOf(value)
    }

    fun setSelectedConference(conference: Conference) {
        prefs.edit()
            .putString("selected_conference", conference.name)
            .apply()
    }
}