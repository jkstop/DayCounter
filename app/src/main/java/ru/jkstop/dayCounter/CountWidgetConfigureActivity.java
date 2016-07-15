package ru.jkstop.dayCounter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * The configuration screen for the {@link CountWidget CountWidget} AppWidget.
 */
public class CountWidgetConfigureActivity extends AppCompatActivity{


    public  static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private CardView cardDate;
    private Context context;
    private ImageView widgetPreviewImage;
    private static TextView widgetPreviewText;
    private static TextView countText;
    private FloatingActionButton fabAddWidget;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = CountWidgetConfigureActivity.this;

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(App.getContext());
            CountWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public CountWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        context = this;

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.count_widget_configure);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        System.out.println("Config for ID " + mAppWidgetId);

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        cardDate = (CardView)findViewById(R.id.card_date);
        cardDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new selectDate().show(getSupportFragmentManager(),"datePicker");
            }
        });


        widgetPreviewImage = (ImageView)findViewById(R.id.appwidget_icon);
        widgetPreviewText = (TextView)findViewById(R.id.appwidget_text);

        countText = (TextView)findViewById(R.id.count_text);

        countText.setText(getFormattedDate(SharedPrefs.getWidgetStartDate(mAppWidgetId)));

        widgetPreviewText.setText(calculateDatesDiff(mAppWidgetId));
        widgetPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, SharedPrefs.getWidgetTextSize(mAppWidgetId));

        ArrayList<Integer> designItems = new ArrayList<>();
        designItems.add(R.drawable.checkbox_blank_circle);
        designItems.add(R.drawable.clipboard);
        designItems.add(R.drawable.heart);
        designItems.add(R.drawable.label);
        designItems.add(R.drawable.message);

        ArrayList<Integer> colorItems = new ArrayList<>();
        colorItems.add(android.R.color.holo_red_dark);
        colorItems.add(android.R.color.holo_green_dark);
        colorItems.add(android.R.color.holo_blue_dark);
        colorItems.add(android.R.color.holo_purple);
        colorItems.add(android.R.color.black);

        RecyclerView recyclerViewDesign = (RecyclerView)findViewById(R.id.design_view);
        recyclerViewDesign.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDesign.setAdapter(new recyclerAdapter(recyclerAdapter.ADAPTER_DESIGN, designItems));

        RecyclerView recyclerViewColor = (RecyclerView)findViewById(R.id.design_color);
        recyclerViewColor.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewColor.setAdapter(new recyclerAdapter(recyclerAdapter.ADAPTER_COLOR, colorItems));

        AppCompatSeekBar appCompatSeekBar = (AppCompatSeekBar)findViewById(R.id.design_text_size);
        appCompatSeekBar.setMax(100);
        appCompatSeekBar.setProgress(SharedPrefs.getWidgetTextSize(mAppWidgetId));
        appCompatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                widgetPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, i);
                SharedPrefs.setWidgetTextSize(i, mAppWidgetId);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        fabAddWidget = (FloatingActionButton)findViewById(R.id.fab_add_widget);
        fabAddWidget.setOnClickListener(mOnClickListener);

    }

    public static String calculateDatesDiff(int widgetId){

        long diffMillis = Math.abs(System.currentTimeMillis() - SharedPrefs.getWidgetStartDate(widgetId));

        System.out.println("calculating... " + TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS));

        return String.valueOf(TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS));
    }


    public static class selectDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        Calendar calendar = Calendar.getInstance();

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            calendar.setTimeInMillis(SharedPrefs.getWidgetStartDate(mAppWidgetId));
            DatePickerDialog picker = new DatePickerDialog(getActivity(),this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            return picker;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
            SharedPrefs.setWidgetStartDate(new GregorianCalendar(y,m,d).getTimeInMillis(), mAppWidgetId);

            countText.setText(getFormattedDate(calendar.getTimeInMillis()));
            widgetPreviewText.setText(calculateDatesDiff(mAppWidgetId));

        }
    }

    public static String getFormattedDate(long timeInMillis){
        return DateFormat.getDateInstance().format(new Date(timeInMillis));
    }

    public static class SharedPrefs{

        private static final String START_DATE_KEY = "start_date_key_";
        private static final String SELECTED_DESIGN_INDEX = "selected_design_index";
        private static final String SELECTED_COLOR_INDEX = "selected_color_index";
        private static final String TEXT_SIZE = "text_size_";
        private static final String CURRENT_DESIGN = "current_design_";
        private static final String CURRENT_COLOR = "current_color_";

        private static SharedPreferences getDefaultPreferences(){
            return PreferenceManager.getDefaultSharedPreferences(App.getContext());
        }

        private static SharedPreferences.Editor getDefaultPreferencesEditor(){
            return PreferenceManager.getDefaultSharedPreferences(App.getContext()).edit();
        }

        //дата отсчета
        public static void setWidgetStartDate(long startDate, int widgetId){
            System.out.println("set widget start date " + widgetId);
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(startDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            getDefaultPreferencesEditor().putLong(START_DATE_KEY + widgetId, calendar.getTimeInMillis()).apply();
        }

        public static long getWidgetStartDate(int widgetId){
            if (getDefaultPreferences().getLong(START_DATE_KEY + widgetId, 0) == 0){
                setWidgetStartDate(System.currentTimeMillis(), widgetId);
            }
            System.out.println("get widget start date " + widgetId + " is " + new Date(getDefaultPreferences().getLong(START_DATE_KEY + widgetId, System.currentTimeMillis())));
            return getDefaultPreferences().getLong(START_DATE_KEY + widgetId, System.currentTimeMillis());
        }

        public static void deleteWidgetStartDate(int widgetId){
            System.out.println("delete widget start date " + widgetId);
            getDefaultPreferencesEditor().remove(START_DATE_KEY + widgetId).apply();
        }

        //индексы настройки внешнего вида
        public static void setSelectedDesignIndex (int index, int widgetId){
            System.out.println("set selected design index " + widgetId);
            getDefaultPreferencesEditor().putInt(SELECTED_DESIGN_INDEX + widgetId, index).apply();
        }

        public static int getSelectedDesignIndex (int widgetId){
            System.out.println("get selected design index " + widgetId);
            return getDefaultPreferences().getInt(SELECTED_DESIGN_INDEX + widgetId, 0);
        }

        public static void deleteDesignIndex(int widgetId){
            System.out.println("delete design index " + widgetId);
            getDefaultPreferencesEditor().remove(SELECTED_DESIGN_INDEX + widgetId).apply();
        }

        public static void setSelectedColorIndex (int index, int widgetId){
            System.out.println("set selected color index " + widgetId);
            getDefaultPreferencesEditor().putInt(SELECTED_COLOR_INDEX + widgetId, index).apply();
        }

        public static int getSelectedColorIndex (int widgetId){
            System.out.println("" + widgetId);
            return getDefaultPreferences().getInt(SELECTED_COLOR_INDEX + widgetId, 0);
        }

        public static void deleteColorIndex(int widgetId){
            System.out.println("delete color index " + widgetId);
            getDefaultPreferencesEditor().remove(SELECTED_COLOR_INDEX + widgetId).apply();
        }

        //размер текста
        public static void setWidgetTextSize(int textSize, int widgetId){
            System.out.println("set widget text size " + widgetId);
            getDefaultPreferencesEditor().putInt(TEXT_SIZE + widgetId, textSize).apply();
        }

        public static int getWidgetTextSize(int widgetId){
            System.out.println("get widget text size " + widgetId);
            return getDefaultPreferences().getInt(TEXT_SIZE + widgetId, 48);
        }

        public static void deleteWidgetTextSize(int widgetId){
            System.out.println("delete widget text size " + widgetId);
            getDefaultPreferencesEditor().remove(TEXT_SIZE + widgetId).apply();
        }

        //дизайн
        public static void setWidgetDesign(int resId, int widgetId){
            System.out.println("set widget design " + widgetId);
            getDefaultPreferencesEditor().putInt(CURRENT_DESIGN + widgetId, resId).apply();
        }

        public static int getWidgetDesign(int widgetId){
            System.out.println("get widget design " + widgetId);
            return getDefaultPreferences().getInt(CURRENT_DESIGN + widgetId, 0);
        }

        public static void deleteWidgetDesign(int widgetId){
            System.out.println("delete widget design " + widgetId);
            getDefaultPreferencesEditor().remove(CURRENT_DESIGN + widgetId).apply();
        }

        //цвет
        public static void setWidgetColor(int resId, int widgetId){
            System.out.println("set widget color " + widgetId);
            getDefaultPreferencesEditor().putInt(CURRENT_COLOR + widgetId, resId).apply();
        }

        public static int getWidgetColor(int widgetId){
            System.out.println("get widget color " + widgetId);
            return getDefaultPreferences().getInt(CURRENT_COLOR + widgetId, 0);
        }

        public static void deleteWidgetColor(int widgetId){
            System.out.println("delete widget color " + widgetId);
            getDefaultPreferencesEditor().remove(CURRENT_COLOR + widgetId).apply();
        }

        public static void deleteAllWidgetPrefs(int widgetId){
            System.out.println("clear preference for widget " + widgetId);
            deleteWidgetStartDate(widgetId);
            deleteWidgetTextSize(widgetId);
            deleteDesignIndex(widgetId);
            deleteColorIndex(widgetId);
            deleteWidgetDesign(widgetId);
            deleteWidgetColor(widgetId);
        }
    }

    private class recyclerAdapter extends RecyclerView.Adapter{

        private static final int ADAPTER_DESIGN = 1;
        private static final int ADAPTER_COLOR = 2;
        private int adapterType;

        private ArrayList<Integer> items;
        private SparseBooleanArray selectedItems = new SparseBooleanArray();

        public recyclerAdapter(int adapterType, ArrayList<Integer> items){
            this.adapterType = adapterType;
            this.items = items;

            switch (adapterType){
                case ADAPTER_DESIGN:
                    selectedItems.put(SharedPrefs.getSelectedDesignIndex(mAppWidgetId), true);
                    widgetPreviewImage.setImageResource(items.get(SharedPrefs.getSelectedDesignIndex(mAppWidgetId)));
                    break;
                case ADAPTER_COLOR:
                    selectedItems.put(SharedPrefs.getSelectedColorIndex(mAppWidgetId), true);
                    widgetPreviewImage.setColorFilter(getResources().getColor(items.get(SharedPrefs.getSelectedColorIndex(mAppWidgetId))), PorterDuff.Mode.SRC_IN);
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        public class DesignHolder extends RecyclerView.ViewHolder{

            public ImageView appCompatImageView;

            public DesignHolder(View itemView) {
                super(itemView);
                appCompatImageView = (ImageView)itemView.findViewById(R.id.image_design_view);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View itemView = View.inflate(context, R.layout.view_design_item, null);
            final DesignHolder designHolder = new DesignHolder(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selectedItems.get(designHolder.getLayoutPosition())){
                        selectedItems.clear();
                        selectedItems.put(designHolder.getLayoutPosition(), true);

                        switch (adapterType){
                            case ADAPTER_DESIGN:
                                widgetPreviewImage.setImageResource(items.get(designHolder.getLayoutPosition()));
                                SharedPrefs.setSelectedDesignIndex(designHolder.getLayoutPosition(), mAppWidgetId);
                                //SharedPrefs.setWidgetDesign(items.get(designHolder.getLayoutPosition()), mAppWidgetId);
                                break;
                            case ADAPTER_COLOR:
                                widgetPreviewImage.setColorFilter(getResources().getColor(items.get(designHolder.getLayoutPosition())), PorterDuff.Mode.SRC_IN);
                                SharedPrefs.setSelectedColorIndex(designHolder.getLayoutPosition(), mAppWidgetId);
                               // SharedPrefs.setWidgetColor(items.get(designHolder.getLayoutPosition()), mAppWidgetId);
                                break;
                            default:
                                break;
                        }

                        notifyDataSetChanged();
                    }
                }
            });
            return designHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            switch (adapterType){
                case ADAPTER_DESIGN:
                    ((DesignHolder)holder).appCompatImageView.setImageResource(items.get(position));
                    if (selectedItems.get(position, false)) SharedPrefs.setWidgetDesign(items.get(position), mAppWidgetId);
                    break;
                case ADAPTER_COLOR:
                    ((DesignHolder)holder).appCompatImageView.setImageResource(R.drawable.checkbox_blank_circle);
                    ((DesignHolder)holder).appCompatImageView.setColorFilter(getResources().getColor(items.get(position)), PorterDuff.Mode.SRC_IN);
                    if (selectedItems.get(position, false)) SharedPrefs.setWidgetColor(items.get(position), mAppWidgetId);
                    break;
                default:
                    break;
            }

            ((DesignHolder)holder).itemView.setSelected(selectedItems.get(position, false));

        }

        @Override
        public long getItemId(int i) {
            return items.indexOf(items.get(i));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }
}

