package com.android.hunminjeongeumapp.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import com.android.hunminjeongeumapp.R
import com.android.hunminjeongeumapp.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.res.ResourcesCompat

class WordWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_TOGGLE_VIEW") {
            val currentState = intent.getBooleanExtra("showExample", true)
            val newState = !currentState
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            if (newState) {
                views.setViewVisibility(R.id.tvExample, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.tvWordMeaning, android.view.View.GONE)
            } else {
                views.setViewVisibility(R.id.tvExample, android.view.View.GONE)
                views.setViewVisibility(R.id.tvWordMeaning, android.view.View.VISIBLE)
            }

            val toggleIntent = Intent(context, WordWidget::class.java).apply {
                action = "ACTION_TOGGLE_VIEW"
                putExtra("showExample", newState)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvExample, pendingIntent)
            views.setOnClickPendingIntent(R.id.tvWordMeaning, pendingIntent)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds =
                appWidgetManager.getAppWidgetIds(ComponentName(context, WordWidget::class.java))

            for (appWidgetId in appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            CoroutineScope(Dispatchers.IO).launch {
                val repository = WordRepository(context, "C939B0E78C9F2773A87D4088BA8A777D")
                val wordItem = repository.fetchWordData()

                if (wordItem != null) {
                    val exampleText = wordItem.example ?: "예문 없음"
                    val wordText = wordItem.word ?: "단어 없음"
                    val meaningText = wordItem.senses?.firstOrNull()?.definition ?: "뜻 없음"
                    val exampleImage = createTextBitmap(exampleText, wordText, context, 700)
                    val meaningImage = createTextBitmap(meaningText, wordText, context, 700)
                    //val exampleImage = createTextBitmap(exampleText, wordText, context)
                    //val meaningImage = createTextBitmap("$wordText: $meaningText", wordText, context)

                    views.setImageViewBitmap(R.id.tvExample, exampleImage)
                    views.setImageViewBitmap(R.id.tvWordMeaning, meaningImage)

                    val intent = Intent(context, WordWidget::class.java).apply {
                        action = "ACTION_TOGGLE_VIEW"
                        putExtra("showExample", false)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    views.setOnClickPendingIntent(R.id.tvExample, pendingIntent)
                    views.setOnClickPendingIntent(R.id.tvWordMeaning, pendingIntent)
                    views.setViewVisibility(R.id.tvExample, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.tvWordMeaning, android.view.View.GONE)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        @SuppressLint("ResourceAsColor")
        private fun createTextBitmap(text: String, highlightText: String, context: Context, maxWidth: Int): Bitmap {
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY // 기본 텍스트 색상
                textSize = 30f
                typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.chungjukimsaeng), Typeface.NORMAL)
            }

            // Create a SpannableString and apply styles
            val spannable = SpannableString(text)
            val start = text.indexOf(highlightText)
            if (start != -1) {
                val end = start + highlightText.length
                // Apply color and bold style to the highlighted text
                spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // Create a StaticLayout for the text
            val layout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(1.0f, 1.0f)
                .setIncludePad(true)
                .build()

            // Create a bitmap from the layout
            val bitmap = Bitmap.createBitmap(layout.width, layout.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            layout.draw(canvas)

            return bitmap
        }

    }
}