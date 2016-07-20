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
import android.support.v7.app.NotificationCompat;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
        views.setTextViewText(R.id.widget_text, CountWidgetConfigureActivity.calculateDatesDiff(appWidgetId));
        views.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_PX, SharedPrefs.getWidgetTextSize(appWidgetId));

        views.setImageViewResource(R.id.widget_icon, SharedPrefs.getWidgetDesign(appWidgetId));

        int colorId = SharedPrefs.getWidgetColor(appWidgetId);
        if (colorId!=0){
            views.setInt(R.id.widget_icon, "setColorFilter", App.getContext().getResources().getColor(colorId));
        }


        Intent configIntent = new Intent(context, CountWidgetConfigureActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        views.setOnClickPendingIntent(R.id.widget_icon, PendingIntent.getActivity(context, appWidgetId, configIntent, 0));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        needNotification(Integer.valueOf(CountWidgetConfigureActivity.calculateDatesDiff(appWidgetId)), appWidgetId);

    }

    private static void sendNotif(int widgetId, String message){

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(App.getContext())
                .setSmallIcon(R.drawable.cake_variant)
                .setAutoCancel(true)
                .setContentText(message + " с " + DateFormat.getDateInstance().format(new Date(SharedPrefs.getWidgetStartDate(widgetId))))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(App.getContext().getString(R.string.notification_title))
                .setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.getNotification();

        NotificationManager notificationManager = (NotificationManager)App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(widgetId, notification);
    }

    private static void needNotification(int passedDays, int widgetId){
        Set<String> notifSettings = SharedPrefs.getNotificationPeriod(widgetId);
        Long startDate = SharedPrefs.getWidgetStartDate(widgetId);

        Calendar startDateCalendar = new GregorianCalendar();
        startDateCalendar.setTimeInMillis(startDate);

        Calendar nowCalendar = Calendar.getInstance();
        String[] prefix;

        if (passedDays == 0){
            // 0 дней
            sendNotif(widgetId, App.getContext().getResources().getString(R.string.notification_start_count));
        }else if (nowCalendar.get(Calendar.MONTH) == startDateCalendar.get(Calendar.MONTH) &&
                nowCalendar.get(Calendar.DAY_OF_MONTH) == startDateCalendar.get(Calendar.DAY_OF_MONTH) &&
                notifSettings.contains(CountWidgetConfigureActivity.NOTIF_1_Y)){
            //если прошел год
            int diffYear = nowCalendar.get(Calendar.YEAR) - startDateCalendar.get(Calendar.YEAR);
            prefix = persuade(Calendar.YEAR, diffYear);
            sendNotif(widgetId, prefix[0] + diffYear + prefix[1]);

        } else if (passedDays % 100 == 0 && notifSettings.contains(CountWidgetConfigureActivity.NOTIF_100_D)){
            //прошло 100 дней
            prefix = persuade(Calendar.DAY_OF_MONTH, passedDays);
            sendNotif(widgetId, prefix[0] + passedDays + prefix[1]);
        } else if (passedDays % 50 == 0 && notifSettings.contains(CountWidgetConfigureActivity.NOTIF_50_D)){
            //прошло 50 дней
            prefix = persuade(Calendar.DAY_OF_MONTH, passedDays);
            sendNotif(widgetId,prefix[0] + passedDays + prefix[1]);
        }else if (nowCalendar.get(Calendar.DAY_OF_MONTH) == startDateCalendar.get(Calendar.DAY_OF_MONTH) &&
                notifSettings.contains(CountWidgetConfigureActivity.NOTIF_1_M)){
            //прошел месяц
            int diffYear = nowCalendar.get(Calendar.YEAR) - startDateCalendar.get(Calendar.YEAR);
            int monthCount = diffYear * 12 + nowCalendar.get(Calendar.MONTH) - startDateCalendar.get(Calendar.MONTH);
            prefix = persuade(Calendar.MONTH, monthCount);
            sendNotif(widgetId, prefix[0] + monthCount + prefix[1]);
        } else if (passedDays % 10 == 0 && notifSettings.contains(CountWidgetConfigureActivity.NOTIF_10_D)){
            //прошло 10 дней
            prefix = persuade(Calendar.DAY_OF_MONTH, passedDays);
            sendNotif(widgetId,prefix[0] + passedDays + prefix[1]);
        }

    }

    private static String[] persuade(int what, int value){
        String[] out = new String[2];
        switch (what){
            case Calendar.YEAR:
                if (value == 1 || value%10 == 1){
                    out[0] = "Прошёл ";
                    out[1] = " год";
                } else if (value < 5 || (value%10 < 5 && value%10 != 0)){
                    out[0] = "Прошло ";
                    out[1] = " года";
                } else{
                    out[0] = "Прошло ";
                    out[1] = " лет";
                }
                break;
            case Calendar.MONTH:
                if (value == 1 || value%10 == 1){
                    out[0] = "Прошёл ";
                    out[1] = " месяц";
                } else if (value < 5 || (value%10 < 5 && value%10 != 0)){
                    out[0] = "Прошло ";
                    out[1] = " месяца";
                } else{
                    out[0] = "Прошло ";
                    out[1] = " месяцев";
                }
                break;
            case Calendar.DAY_OF_MONTH:
                if (value == 1 || value%10 == 1){
                    out[0] = "Прошёл ";
                    out[1] = " день";
                } else if (value < 5 || (value%10 < 5 && value%10 != 0)){
                    out[0] = "Прошло ";
                    out[1] = " дня";
                } else{
                    out[0] = "Прошло ";
                    out[1] = " дней";
                }
                break;
            default:
                break;
        }
        return out;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

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
            SharedPrefs.deleteAllWidgetPrefs(appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent updateIntent = new Intent(context, CountWidget.class);
        updateIntent.setAction(UPDATE_WIDGET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CountWidgetConfigureActivity.mAppWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        Intent updateIntent = new Intent(context, CountWidget.class);
        updateIntent.setAction(UPDATE_WIDGET);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, CountWidgetConfigureActivity.mAppWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

