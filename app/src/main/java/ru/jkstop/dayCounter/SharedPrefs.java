package ru.jkstop.dayCounter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared Preferences
 */
public class SharedPrefs {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor sharedPreferencesEditor;

    private static final String START_DATE_KEY = "start_date_key_";
    private static final String SELECTED_DESIGN_INDEX = "selected_design_index";
    private static final String SELECTED_COLOR_INDEX = "selected_color_index";
    private static final String TEXT_SIZE = "text_size_";
    private static final String CURRENT_DESIGN = "current_design_";
    private static final String CURRENT_COLOR = "current_color_";
    private static final String NOTIF_PERIOD = "notification_period_";

    private static synchronized SharedPreferences getDefaultPreferences() {
        if (sharedPreferences == null){
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        }
        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    private static synchronized SharedPreferences.Editor getDefaultPreferencesEditor() {
        if (sharedPreferencesEditor == null){
            sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
        }
        return PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
    }

    //дата отсчета
    public static void setWidgetStartDate(long startDate, int widgetId) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        getDefaultPreferencesEditor().putLong(START_DATE_KEY + widgetId, calendar.getTimeInMillis()).apply();
    }

    public static long getWidgetStartDate(int widgetId) {
        if (getDefaultPreferences().getLong(START_DATE_KEY + widgetId, 0) == 0) {
            setWidgetStartDate(System.currentTimeMillis(), widgetId);
        }
        return getDefaultPreferences().getLong(START_DATE_KEY + widgetId, System.currentTimeMillis());
    }

    public static void deleteWidgetStartDate(int widgetId) {
        getDefaultPreferencesEditor().remove(START_DATE_KEY + widgetId).apply();
    }

    //индексы настройки внешнего вида
    public static void setSelectedDesignIndex(int index, int widgetId) {
        getDefaultPreferencesEditor().putInt(SELECTED_DESIGN_INDEX + widgetId, index).apply();
    }

    public static int getSelectedDesignIndex(int widgetId) {
        return getDefaultPreferences().getInt(SELECTED_DESIGN_INDEX + widgetId, 0);
    }

    public static void deleteDesignIndex(int widgetId) {
        getDefaultPreferencesEditor().remove(SELECTED_DESIGN_INDEX + widgetId).apply();
    }

    public static void setSelectedColorIndex(int index, int widgetId) {
        getDefaultPreferencesEditor().putInt(SELECTED_COLOR_INDEX + widgetId, index).apply();
    }

    public static int getSelectedColorIndex(int widgetId) {
        return getDefaultPreferences().getInt(SELECTED_COLOR_INDEX + widgetId, 0);
    }

    public static void deleteColorIndex(int widgetId) {
        getDefaultPreferencesEditor().remove(SELECTED_COLOR_INDEX + widgetId).apply();
    }

    //размер текста
    public static void setWidgetTextSize(int textSize, int widgetId) {
        getDefaultPreferencesEditor().putInt(TEXT_SIZE + widgetId, textSize).apply();
    }

    public static int getWidgetTextSize(int widgetId) {
        return getDefaultPreferences().getInt(TEXT_SIZE + widgetId, 48);
    }

    public static void deleteWidgetTextSize(int widgetId) {
        getDefaultPreferencesEditor().remove(TEXT_SIZE + widgetId).apply();
    }

    //дизайн
    public static void setWidgetDesign(int resId, int widgetId) {
        getDefaultPreferencesEditor().putInt(CURRENT_DESIGN + widgetId, resId).apply();
    }

    public static int getWidgetDesign(int widgetId) {
        return getDefaultPreferences().getInt(CURRENT_DESIGN + widgetId, 0);
    }

    public static void deleteWidgetDesign(int widgetId) {
        getDefaultPreferencesEditor().remove(CURRENT_DESIGN + widgetId).apply();
    }

    //цвет
    public static void setWidgetColor(int resId, int widgetId) {
        getDefaultPreferencesEditor().putInt(CURRENT_COLOR + widgetId, resId).apply();
    }

    public static int getWidgetColor(int widgetId) {
        return getDefaultPreferences().getInt(CURRENT_COLOR + widgetId, 0);
    }

    public static void deleteWidgetColor(int widgetId) {
        getDefaultPreferencesEditor().remove(CURRENT_COLOR + widgetId).apply();
    }

    //периодичность уведомлений
    public static void setNotificationPeriod(Set<String> entries, int widgetId) {
        getDefaultPreferencesEditor().putStringSet(NOTIF_PERIOD + widgetId, entries).apply();
    }

    public static Set<String> getNotificationPeriod(int widgetId) {
        return getDefaultPreferences().getStringSet(NOTIF_PERIOD + widgetId, new HashSet<String>());
    }

    public static void deleteNotificationPeriod(int widgetId) {
        getDefaultPreferencesEditor().remove(NOTIF_PERIOD + widgetId).apply();
    }

    public static void deleteAllWidgetPrefs(int widgetId) {
        deleteWidgetStartDate(widgetId);
        deleteWidgetTextSize(widgetId);
        deleteDesignIndex(widgetId);
        deleteColorIndex(widgetId);
        deleteWidgetDesign(widgetId);
        deleteWidgetColor(widgetId);
        deleteNotificationPeriod(widgetId);
    }

}
