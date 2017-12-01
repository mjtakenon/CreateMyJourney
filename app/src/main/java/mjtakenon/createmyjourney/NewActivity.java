package mjtakenon.createmyjourney;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static mjtakenon.createmyjourney.Const.*;

public class NewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        EditText textDateBegin = (EditText) findViewById(R.id.textDateBegin);
        //TODO 日帰りしか対応してない
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

        textDateBegin.setText(FORMAT_DATE.format(new Date(System.currentTimeMillis())));
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
        //TODO 各フォームのバリデーション
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
                bundle.putInt(MODE, MODE_NEW);
                bundle.putString(DATE_BEGIN,textDateBegin.getText().toString());

                Integer durationTimeMinute = 0;
                //目的地にいる時間の計算
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(FORMAT_TIME.parse(textDurationDist.getText().toString()));
                    durationTimeMinute = calendar.get(Calendar.MINUTE) + calendar.get(Calendar.HOUR)*60;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return;
                }

                ArrayList<Place> places = new ArrayList<Place>();
                places.add(new Place(0,textPlaceBegin.getText().toString(),Place.TYPE_BEGIN,null,null,textTimeBegin.getText().toString()));
                places.add(new Place(1,textPlaceDist.getText().toString(),Place.TYPE_DIST,null,durationTimeMinute,null));
                places.add(new Place(2,textPlaceEnd.getText().toString(),Place.TYPE_END,textTimeEnd.getText().toString(),null,null));

                bundle.putSerializable(SERIAL_PLACES,places);

                // 保存のデフォルト名用にDISTも渡しとく
                bundle.putString(PLACE_DIST,textPlaceDist.getText().toString());

                Intent intent = new Intent(getApplication(), EditJourneyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                //TODO キャンセルしたときにこの画面に戻ってくる必要があるか?
                finish();
            }
        });
        //TODO GooglePlaceAPIのオートコンプリートを使いてえな
        //https://developers.google.com/places/android-api/?hl=ja


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_CANCEL,intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}

