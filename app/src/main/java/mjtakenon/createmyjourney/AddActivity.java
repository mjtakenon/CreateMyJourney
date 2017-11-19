package mjtakenon.createmyjourney;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        EditText textDateBegin = (EditText) findViewById(R.id.textDateBegin);
        //TODO そもそも日帰りしか対応してない
//        EditText textDateEnd = (EditText) findViewById(R.id.textDateEnd);
        EditText textTimeBegin = (EditText) findViewById(R.id.textTimeBegin);
        EditText textTimeEnd = (EditText) findViewById(R.id.textTimeEnd);
        EditText textPlaceBegin = (EditText) findViewById(R.id.textPlaceBegin);
        EditText textPlaceEnd = (EditText) findViewById(R.id.textPlaceEnd);
        EditText textPlaceDist = (EditText) findViewById(R.id.textPlaceDist);
        EditText textDurationDist = (EditText) findViewById(R.id.textDurationDist);
        ImageButton buttonDateBegin = (ImageButton) findViewById(R.id.buttonDateBegin);
        ImageButton buttonDateEnd = (ImageButton) findViewById(R.id.buttonDateEnd);
        ImageButton buttonTimeBegin = (ImageButton) findViewById(R.id.buttonTimeBegin);
        ImageButton buttonTimeEnd = (ImageButton) findViewById(R.id.buttonTimeEnd);
        Button buttonSearch = (Button) findViewById(R.id.buttonSearch);

        textDateBegin.setText(dfDate.format(dateTime));
//        textDateEnd.setText(dfDate.format(dateTime));

        textTimeBegin.setText("09:00");
        textTimeEnd.setText("19:00");

        buttonDateBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) findViewById(R.id.textDateBegin);
                setDateByCalendar(text);
            }
        });

        buttonDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) findViewById(R.id.textDateEnd);
                setDateByCalendar(text);
            }
        });

        buttonTimeBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) findViewById(R.id.textTimeBegin);
                setTimeByClock(text);
            }
        });

        buttonTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) findViewById(R.id.textTimeEnd);
                setTimeByClock(text);
            }
        });

        //TODO 決定押した後の硬直長すぎィ
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText textDateBegin = (EditText) findViewById(R.id.textDateBegin);
//                EditText textDateEnd = (EditText) findViewById(R.id.textDateEnd);
                EditText textTimeBegin = (EditText) findViewById(R.id.textTimeBegin);
                EditText textTimeEnd = (EditText) findViewById(R.id.textTimeEnd);
                EditText textPlaceBegin = (EditText) findViewById(R.id.textPlaceBegin);
                EditText textPlaceEnd = (EditText) findViewById(R.id.textPlaceEnd);
                EditText textPlaceDist = (EditText) findViewById(R.id.textPlaceDist);
                EditText textDurationDist = (EditText) findViewById(R.id.textDurationDist);

                //入力終了、旅画面への移行
                Bundle bundle = new Bundle();
                bundle.putString("mode", "add");
                bundle.putString("textDateBegin",textDateBegin.getText().toString());
//                intent.putExtra("textDateEnd",textDateEnd.getText().toString());
                bundle.putString("textTimeBegin",textTimeBegin.getText().toString());
                bundle.putString("textTimeEnd",textTimeEnd.getText().toString());
                bundle.putString("textPlaceBegin",textPlaceBegin.getText().toString());
                bundle.putString("textPlaceEnd",textPlaceEnd.getText().toString());
                bundle.putString("textPlaceDist",textPlaceDist.getText().toString());

                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dfTime.parse(textDurationDist.getText().toString()));
                    bundle.putInt("intDurationDist",calendar.get(Calendar.MINUTE) + calendar.get(Calendar.HOUR)*60);
                } catch (ParseException e) {
                    bundle.putInt("intDurationDist",0);
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplication(), EditJourneyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                //?
                finish();
            }
        });


        //TODO GooglePlaceAPIのオートコンプリートを使いてえな
        //https://developers.google.com/places/android-api/?hl=ja
    }


    private void setDateByCalendar(EditText text) {
        DatePickerDialogFragment datePick = new DatePickerDialogFragment();
        datePick.setEditText(text);
        datePick.show(getSupportFragmentManager(), "datePicker");
    }

    private void setTimeByClock(EditText text) {
        TimePickerDialogFragment timePick = new TimePickerDialogFragment();
        timePick.setEditText(text);
        timePick.show(getSupportFragmentManager(), "datePicker");
    }

    final DateFormat dfDate = new SimpleDateFormat("yyyy/MM/dd");
    final DateFormat dfTime = new SimpleDateFormat("HH:mm");
    final Date dateTime = new Date(System.currentTimeMillis());
}

