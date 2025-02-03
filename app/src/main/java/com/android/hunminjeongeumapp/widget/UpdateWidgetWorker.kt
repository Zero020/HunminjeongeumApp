package com.android.hunminjeongeumapp.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class UpdateWidgetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val thisWidget = ComponentName(applicationContext, WordWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                WordWidget.updateAppWidget(applicationContext, appWidgetManager, widgetId)
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    companion object {
        fun scheduleUpdate(context: Context) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                UpdateWidgetWorker::class.java,
                12, TimeUnit.HOURS, // 반복 주기 설정: 12시간마다 실행
                1, TimeUnit.HOURS // 유연성 간격
            ).build()

            WorkManager.getInstance(context).enqueue(periodicWorkRequest)
        }
    }
}
