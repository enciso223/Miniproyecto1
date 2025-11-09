package com.univalle.equipocinco.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.univalle.equipocinco.R
import com.univalle.equipocinco.data.local.database.InventoryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_SALDO = "com.univalle.equipocinco.ACTION_TOGGLE_SALDO"
        private const val ACTION_OPEN_LOGIN = "com.univalle.equipocinco.ACTION_OPEN_LOGIN"
        private const val PREFS_NAME = "inventory_widget_prefs"
        private const val PREF_SHOW_SALDO = "show_saldo"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, InventoryWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)

        when (intent.action) {
            ACTION_TOGGLE_SALDO -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val current = prefs.getBoolean(PREF_SHOW_SALDO, false)
                prefs.edit().putBoolean(PREF_SHOW_SALDO, !current).apply()
                for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
            }

            ACTION_OPEN_LOGIN -> {
                // Abre la HU2 (login)
                val loginIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                loginIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(loginIntent)
            }
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.inventory_widget)

        // Listener para el icono del ojo
        val toggleIntent = Intent(context, InventoryWidgetProvider::class.java).setAction(ACTION_TOGGLE_SALDO)
        val togglePending = PendingIntent.getBroadcast(
            context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.img_eye, togglePending)

        // Listener para el botón "Gestionar inventario"
        val manageIntent = Intent(context, InventoryWidgetProvider::class.java).setAction(ACTION_OPEN_LOGIN)
        val managePending = PendingIntent.getBroadcast(
            context, 1, manageIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.ll_manage, managePending)
        views.setOnClickPendingIntent(R.id.img_manage, managePending)

        // Leer preferencia (mostrar/ocultar saldo)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val showSaldo = prefs.getBoolean(PREF_SHOW_SALDO, false)

        if (showSaldo) {
            // Mostrar saldo real
            views.setImageViewResource(R.id.img_eye, R.drawable.ic_eye_closed)
            loadSaldoFromDatabase(context) { saldoFormateado ->
                views.setTextViewText(R.id.tv_saldo, saldoFormateado)
                manager.updateAppWidget(widgetId, views)
            }
        } else {
            // Mostrar saldo oculto
            views.setImageViewResource(R.id.img_eye, R.drawable.ic_eye_open)
            views.setTextViewText(R.id.tv_saldo, "$****")
            manager.updateAppWidget(widgetId, views)
        }
    }

    /** Calcula el saldo desde la base de datos Room */
    private fun loadSaldoFromDatabase(context: Context, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = InventoryDatabase.getDatabase(context)
            val total = db.productDao().getTotalInventoryValue() ?: 0.0
            val saldoFormateado = formatCurrency(total)
            callback(saldoFormateado)
        }
    }

    /** Formatea con separadores y dos decimales */
    private fun formatCurrency(value: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "$" + nf.format(value)
    }
}
