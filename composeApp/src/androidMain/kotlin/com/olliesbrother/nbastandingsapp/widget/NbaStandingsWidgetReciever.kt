package com.olliesbrother.nbastandingsapp.widget

import androidx.glance.appwidget.GlanceAppWidget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NbaStandingsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NbaStandingsWidget()
}