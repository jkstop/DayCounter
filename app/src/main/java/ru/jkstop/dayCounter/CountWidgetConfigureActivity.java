package ru.jkstop.dayCounter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The configuration screen for the {@link CountWidget CountWidget} AppWidget.
 */
public class CountWidgetConfigureActivity extends AppCompatActivity{


    public static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static final String NOTIF_10_D = App.getContext().getResources().getString(R.string.notification_10_days);
    public static final String NOTIF_1_M = App.getContext().getResources().getString(R.string.notification_mounth);
    public static final String NOTIF_50_D = App.getContext().getResources().getString(R.string.notification_50_days);
    public static final String NOTIF_100_D = App.getContext().getResources().getString(R.string.notification_100_days);
    public static final String NOTIF_1_Y = App.getContext().getResources().getString(R.string.notification_year);

    private AdapterWidgetDesign adapterWidgetDesignStyle, adapterWidgetDesignColor;
    private TypedArray widgetDesignItems, widgetColorItems;

    private Context context;
    private ImageView widgetPreviewImage;
    private static TextView widgetPreviewText;
    private static TextView countText;
    private FloatingActionButton fabAddWidget;
    private ListView listNotifPeriod;
    private Set<String> checkedNotif = new HashSet<>();
    private ArrayList<String> notificationItems = new ArrayList<>();

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = CountWidgetConfigureActivity.this;

            SharedPrefs.setNotificationPeriod(checkedNotif, mAppWidgetId);

            SharedPrefs.setWidgetDesign(adapterWidgetDesignStyle.items.getResourceId(adapterWidgetDesignStyle.selectedItem.get(true), 0), mAppWidgetId);
            SharedPrefs.setSelectedDesignIndex(adapterWidgetDesignStyle.selectedItem.get(true), mAppWidgetId);

            SharedPrefs.setWidgetColor(adapterWidgetDesignColor.items.getResourceId(adapterWidgetDesignColor.selectedItem.get(true), 0), mAppWidgetId);
            SharedPrefs.setSelectedColorIndex(adapterWidgetDesignColor.selectedItem.get(true), mAppWidgetId);

            widgetDesignItems.recycle();
            widgetColorItems.recycle();

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(App.getContext());
            CountWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();

            System.out.println("widget must be created");
        }
    };

    AppCompatCheckBox.OnCheckedChangeListener onCheckListener = new AppCompatCheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b){
                checkedNotif.add(compoundButton.getText().toString());
            } else {
                checkedNotif.remove(compoundButton.getText().toString());
            }


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

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        findViewById(R.id.card_start_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new selectDate().show(getSupportFragmentManager(),"datePicker");
            }
        });

        countText = (TextView)findViewById(R.id.card_start_date_count);
        countText.setText(getFormattedDate(SharedPrefs.getWidgetStartDate(mAppWidgetId)));

        widgetPreviewImage = (ImageView)findViewById(R.id.widget_icon);
        widgetPreviewText = (TextView)findViewById(R.id.widget_text);
        widgetPreviewText.setText(calculateDatesDiff(mAppWidgetId));
        widgetPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, SharedPrefs.getWidgetTextSize(mAppWidgetId));

        widgetDesignItems = getResources().obtainTypedArray(R.array.widget_styles);
        widgetColorItems = getResources().obtainTypedArray(R.array.widget_colors);

        adapterWidgetDesignStyle = new AdapterWidgetDesign(AdapterWidgetDesign.ADAPTER_DESIGN, widgetDesignItems);
        RecyclerView recyclerViewDesign = (RecyclerView)findViewById(R.id.card_widget_design_style_recycler);
        recyclerViewDesign.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDesign.setAdapter(adapterWidgetDesignStyle);

        adapterWidgetDesignColor = new AdapterWidgetDesign(AdapterWidgetDesign.ADAPTER_COLOR, widgetColorItems);
        RecyclerView recyclerViewColor = (RecyclerView)findViewById(R.id.card_widget_design_color_recycler);
        recyclerViewColor.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewColor.setAdapter(adapterWidgetDesignColor);

        AppCompatSeekBar appCompatSeekBar = (AppCompatSeekBar)findViewById(R.id.card_widget_design_text_size_seekbar);
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

        fabAddWidget = (FloatingActionButton)findViewById(R.id.add_widget_fab);
        fabAddWidget.setOnClickListener(mOnClickListener);

        checkedNotif = SharedPrefs.getNotificationPeriod(mAppWidgetId);

        notificationItems.add(NOTIF_10_D);
        notificationItems.add(NOTIF_1_M);
        notificationItems.add(NOTIF_50_D);
        notificationItems.add(NOTIF_100_D);
        notificationItems.add(NOTIF_1_Y);

        listNotifPeriod = (ListView)findViewById(R.id.card_notification_settings_list);
        listNotifPeriod.setAdapter(new adapterListNotifPeriod(notificationItems));

    }

    public static String calculateDatesDiff(int widgetId){
        long diffMillis = Math.abs(System.currentTimeMillis() - SharedPrefs.getWidgetStartDate(widgetId));
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

            countText.setText(getFormattedDate(SharedPrefs.getWidgetStartDate(mAppWidgetId)));
            widgetPreviewText.setText(calculateDatesDiff(mAppWidgetId));
        }
    }

    public static String getFormattedDate(long timeInMillis){
        return DateFormat.getDateInstance().format(new Date(timeInMillis));
    }

    private class AdapterWidgetDesign extends RecyclerView.Adapter{

        private static final int ADAPTER_DESIGN = 1;
        private static final int ADAPTER_COLOR = 2;
        private int adapterType;

        private TypedArray items;
        private HashMap<Boolean, Integer> selectedItem = new HashMap<>();

        public AdapterWidgetDesign(int adapterType, TypedArray items){
            this.adapterType = adapterType;
            this.items = items;

            switch (adapterType){
                case ADAPTER_DESIGN:
                    selectedItem.put(true, SharedPrefs.getSelectedDesignIndex(mAppWidgetId));
                    widgetPreviewImage.setImageDrawable(items.getDrawable(SharedPrefs.getSelectedDesignIndex(mAppWidgetId)));
                    break;
                case ADAPTER_COLOR:
                    selectedItem.put(true, SharedPrefs.getSelectedColorIndex(mAppWidgetId));
                    widgetPreviewImage.setColorFilter(items.getColor(SharedPrefs.getSelectedColorIndex(mAppWidgetId), 0), PorterDuff.Mode.SRC_IN);
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
                appCompatImageView = (ImageView)itemView.findViewById(R.id.card_widget_design_selectable_item);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View itemView = View.inflate(context, R.layout.view_design_item, null);
            final DesignHolder designHolder = new DesignHolder(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItem.clear();
                    selectedItem.put(true, designHolder.getLayoutPosition());

                    switch (adapterType){
                        case ADAPTER_DESIGN:
                            widgetPreviewImage.setImageDrawable(items.getDrawable(designHolder.getLayoutPosition()));
                            break;
                        case ADAPTER_COLOR:
                            widgetPreviewImage.setColorFilter(items.getColor(designHolder.getLayoutPosition(), 0), PorterDuff.Mode.SRC_IN);
                            break;
                        default:
                            break;
                    }

                    notifyDataSetChanged();
                }
            });
            return designHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            switch (adapterType){
                case ADAPTER_DESIGN:
                    ((DesignHolder)holder).appCompatImageView.setImageDrawable(items.getDrawable(position));
                    break;
                case ADAPTER_COLOR:
                    ((DesignHolder)holder).appCompatImageView.setImageResource(R.drawable.checkbox_blank_circle);
                    ((DesignHolder)holder).appCompatImageView.setColorFilter(items.getColor(position, 0), PorterDuff.Mode.SRC_IN);
                    break;
                default:
                    break;
            }

            if (selectedItem.get(true) == position){
                ((DesignHolder)holder).itemView.setSelected(true);
            } else {
                ((DesignHolder)holder).itemView.setSelected(false);
            }
        }

        @Override
        public long getItemId(int i) {
            return items.getIndex(i);
        }

        @Override
        public int getItemCount() {
            return items.length();
        }

    }

    private class adapterListNotifPeriod extends BaseAdapter{

        private List<String> items;

        public adapterListNotifPeriod(List<String> items){
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.indexOf(items.get(i));
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(items.get(i));
            checkBox.setOnCheckedChangeListener(onCheckListener);

            if (checkedNotif.contains(items.get(i))){
                checkBox.setChecked(true);
            }
            return checkBox;
        }
    }

}

