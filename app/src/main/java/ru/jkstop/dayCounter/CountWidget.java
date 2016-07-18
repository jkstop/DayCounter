package ru.jkstop.dayCounter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.NotificationCompat;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CountWidgetConfigureActivity CountWidgetConfigureActivity}
 */
public class CountWidget extends AppWidgetProvider {

    private static final String UPDATE_WIDGET = "update_widget";

    NotificationManager notificationManager;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.count_widget);
        views.setTextViewText(R.id.appwidget_text, CountWidgetConfigureActivity.calculateDatesDiff(appWidgetId));
        views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_PX, CountWidgetConfigureActivity.SharedPrefs.getWidgetTextSize(appWidgetId));

        views.setImageViewResource(R.id.appwidget_icon, CountWidgetConfigureActivity.SharedPrefs.getWidgetDesign(appWidgetId));

        int colorId = CountWidgetConfigureActivity.SharedPrefs.getWidgetColor(appWidgetId);
        if (colorId!=0){
            views.setInt(R.id.appwidget_icon, "setColorFilter", App.getContext().getResources().getColor(colorId));
        }


        Intent configIntent = new Intent(context, CountWidgetConfigureActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        views.setOnClickPendingIntent(R.id.appwidget_icon, PendingIntent.getActivity(context, appWidgetId, configIntent, 0));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //if (needNotification(Integer.valueOf(CountWidgetConfigureActivity.calculateDatesDiff(appWidgetId)), appWidgetId)){
        //    sendNotif(appWidgetId, "message widget");
       // }

        needNotification(Integer.valueOf(CountWidgetConfigureActivity.calculateDatesDiff(appWidgetId)), appWidgetId);

       // System.out.println("update app widget " + appWidgetId);
    }

    private static void sendNotif(int widgetId, String message){
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(App.getContext())
                .setSmallIcon(R.drawable.bell_outline)
                .setAutoCancel(true)
                .setTicker("Ticker")
                .setContentText(message + " " + widgetId)
                .setContentIntent(null)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Content title")
                .setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.getNotification();

        NotificationManager notificationManager = (NotificationManager)App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(widgetId, notification);
    }

    private static void needNotification(int passedDays, int widgetId){
        Set<String> notifSettings = CountWidgetConfigureActivity.SharedPrefs.getNotificationPeriod(widgetId);
        Resources resources = App.getContext().getResources();
        if (passedDays % 10 == 0 && notifSettings.contains(resources.getString(R.string.notification_10_days))){
            sendNotif(widgetId, "10 days!!!");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        System.out.println("ON RECEIVE");

        ComponentName thisWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int [] ids = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int i : ids){
            updateAppWidget(context, appWidgetManager, i);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CountWidgetConfigureActivity.SharedPrefs.deleteAllWidgetPrefs(appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        System.out.println("widget enabled id " + CountWidgetConfigureActivity.mAppWidgetId);

        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        System.out.println("calednar time " + new Date(calendar.getTimeInMillis()));

        Intent updateIntent = new Intent(context, CountWidget.class);
        updateIntent.setAction(UPDATE_WIDGET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CountWidgetConfigureActivity.mAppWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        System.out.println("repeating start pending " + pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        System.out.println("widget disabled");

        Intent updateIntent = new Intent(context, CountWidget.class);
        updateIntent.setAction(UPDATE_WIDGET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CountWidgetConfigureActivity.mAppWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        System.out.println("repeating end");
    }
}

