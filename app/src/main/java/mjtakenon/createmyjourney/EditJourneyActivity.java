package mjtakenon.createmyjourney;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.nostra13.universalimageloader.core.ImageLoader;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static mjtakenon.createmyjourney.Const.*;

public class EditJourneyActivity extends AppCompatActivity implements OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        //レイアウト作成ボタン
        Button buttonCreate = (Button) findViewById(R.id.buttonCreate);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final EditText viewJourneyName = new EditText(EditJourneyActivity.this);
                //旅の初期名は日付+目的地
                if (getIntent().getExtras().getInt(MODE) == MODE_NEW) {
                    viewJourneyName.setText(getIntent().getExtras().getString(DATE_BEGIN) + " " + getIntent().getExtras().getString(PLACE_DIST));
                } else if (getIntent().getExtras().getInt(MODE) == MODE_LOAD) {
                    viewJourneyName.setText(getIntent().getExtras().getString(JOURNEY_NAME));
                }
                //旅名を入れるダイアログ
                new AlertDialog.Builder(EditJourneyActivity.this).setTitle("この旅行の名前を入力してください").setView(viewJourneyName)
                        .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(!saveJourney(viewJourneyName.getText().toString())) {
                                    Toast.makeText(EditJourneyActivity.this,"保存ができませんでした(旅行名にコンマは入力できません)",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                finish();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });

        //更新ボタン
        Button buttonReflesh = (Button) findViewById(R.id.buttonReflesh);
        buttonReflesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);
                layoutPlan.removeAllViews();
                setPlaces(layoutPlan);
            }
        });

        //タイムラインは時刻を左、場所画像と場所を右にセットで積んでく
        LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);

        SupportMapFragment fragmentMap = new SupportMapFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.fragmentMapRoot, fragmentMap, "fragmentMap");
        ft.commit();
        fragmentMap.getMapAsync(this);

        directionResponceJSON = null;
        mapReady = false;

        loadPlaces();
        setPlaces(layoutPlan);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);       // default
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mapReady = true;
        //Directionが取得できているなら経路を表示
        DrawDirections();
    }

    // 画面に戻ってきたとき
    @Override
    public void onResume() {
        super.onResume();

        LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);
        layoutPlan.removeAllViews();
        setPlaces(layoutPlan);
    }

    // Intentから読み込み(=初回のみ使える)
    private void loadPlaces() {
        //もし作成画面からきてたら出発地、目的地、到着地を追加
        this.listPlaces.clear();
        Bundle bundle = getIntent().getExtras();
        if(bundle.getInt(MODE) == MODE_NEW) {
            if (bundle.getString(PLACE_BEGIN) != null) {
                Place placeBegin = new Place(listPlaces.size(), bundle.getString(PLACE_BEGIN), Place.TYPE_BEGIN, null, null, bundle.getString(TIME_BEGIN));
                listPlaces.add(placeBegin);
            }
            if (bundle.getString(PLACE_DIST) != null) {
                Place placeDist = new Place(listPlaces.size(), bundle.getString(PLACE_DIST), Place.TYPE_DIST, null, bundle.getInt(TIME_DURATION, 0), null);
                listPlaces.add(placeDist);
            }
            if (bundle.getString(PLACE_END) != null) {
                Place placeEnd = new Place(listPlaces.size(), bundle.getString(PLACE_END), Place.TYPE_END, bundle.getString(TIME_END), null, null);
                listPlaces.add(placeEnd);
            }
        } else if (bundle.getInt(MODE) == MODE_LOAD) {
            // Bundleから受け取り
            ArrayList<Place> places = (ArrayList<Place>)bundle.getSerializable(SERIAL_PLACES);
            listPlaces = places;

        }
    }

    //場所と追加用ボタンを交互に配置
    private void setPlaces(LinearLayout layout) {
        for(int n = 0; n < listPlaces.size(); n++) {
            addPlaceRow(layout, listPlaces.get(n));
            if (n + 1 < listPlaces.size()) {
                addDetourButton(layout,listPlaces.get(n).getAddButtonId());
            }
        }
        setTimeRequired();
    }

    private void setPhotozouImageByWord(final Context context, final ImageView imageview, final String word) {
        new AsyncTask<String, Void, String>() {

            String encodedString;
            String apiUrl;

            @Override
            protected void onPreExecute() {
                GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(imageview);
                Glide.with(context).load(R.raw.loader).into(target);
                imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

                try {
                    encodedString = URLEncoder.encode(word, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    encodedString = " ";
                    e.printStackTrace();
                    return;
                }
                apiUrl = "https://api.photozou.jp/rest/search_public.json?type=photo&keyword=" + encodedString + "&limit=1";
            }

            @Override
            protected String doInBackground(String... params) {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder().url(apiUrl).get().build();
                Response response = null;
                try {
                    Call c = client.newCall(request);
                    response = c.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    return null;
                }

                String imageUrl;
                try {
                    JSONObject jo = new JSONObject(response.body().string());
                    if (!jo.getString("stat").equals("ok")) {
                        return null;
                    }
                    imageUrl = jo.getJSONObject("info").getJSONArray("photo").getJSONObject(0).getString("image_url");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return imageUrl;
            }

            @Override
            protected void onProgressUpdate(Void... params) {

            }

            @Override
            protected void onPostExecute(String imageUrl) {
                if (imageUrl != null) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(imageUrl, imageview);
                    imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    Toast.makeText(context,"ネットワーク接続がありません",Toast.LENGTH_SHORT).show();
                    imageview.setImageResource(R.drawable.error_small);
                    imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }.execute(word);
    }

    private void setFlickrImageByWord(final Context context, final ImageView imageview, final String word) {
        new AsyncTask<Void, Void, String>() {

            String encodedString;
            String apiUrl;

            @Override
            protected void onPreExecute() {
                GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(imageview);
                Glide.with(context).load(R.raw.loader).into(target);
                imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

                try {
                    encodedString = URLEncoder.encode(word, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    encodedString = " ";
                    e.printStackTrace();
                    return;
                }

                apiUrl = "https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                        "&api_key=" + KEY_FLICKR_API +
                        "&text=" + encodedString +
                        "&format=json&nojsoncallback=1" +
                        "&per_page=1" +
                        "&content_type=1" +
                        "&sort=interestingness-desc" +
                        "&extras=url_s";
            }

            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(apiUrl).get().build();
                Response response = null;

                try {
                    Call c = client.newCall(request);
                    response = c.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    return null;
                }

                String imageUrl;

                try {
                    JSONObject jo = new JSONObject(response.body().string());
                    if (!jo.getString("stat").equals("ok")) {
                        return null;
                    }
                    JSONObject obj = jo.getJSONObject("photos").getJSONArray("photo").getJSONObject(0);

                    String farm = obj.getString("farm");
                    String server = obj.getString("server");
                    String id = obj.getString("id");
                    String secret = obj.getString("secret");
                    imageUrl = "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + ".jpg";
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return imageUrl;
            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }

            @Override
            protected void onPostExecute(String imageUrl) {
                if (imageUrl != null) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(imageUrl, imageview);
                    imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageview.setImageResource(R.drawable.error_small);
                    imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }.execute();
    }

    //Google Directionsを使って所要時間を取得、placesのtextにセット
    private void setTimeRequired() {

        new AsyncTask<Void, Void, ArrayList<Integer>>() {
            String apiUrl;
            ProgressDialog progressDialog;
            String responseJSON;

            @Override
            protected void onPreExecute() {

                String encodedBeginPlace;       //最初の場所名
                String encodedEndPlace;         //最後の場所名
                ArrayList<String> encodedDistPlaces = new ArrayList<String>();

                //placesが2以上じゃないと算出不可
                if(listPlaces.size() < 2) {
                    return;
                }

                try {
                    encodedBeginPlace = URLEncoder.encode(listPlaces.get(0).getName(), "UTF-8");
                    encodedEndPlace = URLEncoder.encode(listPlaces.get(listPlaces.size() - 1).getName(), "UTF-8");
                    for (int n = 1; n < listPlaces.size() - 1; n++) {
                        encodedDistPlaces.add(URLEncoder.encode(listPlaces.get(n).getName(), "UTF-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    encodedBeginPlace = " ";
                    encodedEndPlace = " ";
                    e.printStackTrace();
                }

                //apiUrl作成
                apiUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + encodedBeginPlace + "&destination="
                        + encodedEndPlace + "&key="
                        + KEY_GOOGLE_API + "&mode=driving";

                //立ち寄りポイントの追加
                if (encodedDistPlaces.size() >= 1) {
                    apiUrl += "&waypoints=";
                }

                for (int n = 0; n < encodedDistPlaces.size(); n++) {
                    apiUrl += encodedDistPlaces.get(n);
                    if (n < encodedDistPlaces.size()-1) {
                        try {
                            apiUrl += (URLEncoder.encode("|", "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //TODO プログレスダイヤログが表示中にpauseすると落ちるらしい
                progressDialog = new ProgressDialog(EditJourneyActivity.this);
                progressDialog.setMessage("検索中...");
                progressDialog.show();
            }

            @Override
            protected ArrayList<Integer> doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(apiUrl).get().build();
                Response response = null;

                try {
                    Call c = client.newCall(request);
                    response = c.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    return null;
                }

                ArrayList<Integer> timeSecs = new ArrayList<Integer>();

                try {
                    responseJSON = response.body().string();
                    directionResponceJSON = new JSONObject(responseJSON);
                    if (!directionResponceJSON.getString("status").equals("OK")) {
                        directionResponceJSON = null;
                        return null;
                    }
                    //JSONから各移動時間を取得
                    for (int n = 0; n < listPlaces.size() - 1; n++) {
                        Integer timeSec = directionResponceJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(n).getJSONObject("duration").getInt("value");
                        timeSecs.add(timeSec);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return timeSecs;
            }

            @Override
            protected void onPostExecute(ArrayList<Integer> timeSecs) {

                if(timeSecs == null) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    for (int n = 0; n < listPlaces.size(); n++) {
                        TextView textView = (TextView) findViewById(listPlaces.get(n).getTextViewId());
                        textView.setText("取得失敗");
                    }
                    return;
                }

                for (int n = 0; n < listPlaces.size(); n++) {
                    Date dateBegin;
                    TextView textView = (TextView) findViewById(listPlaces.get(n).getTextViewId());

                    //移動時間を計算
                    try {
                        if (listPlaces.get(n).getType().equals(Place.TYPE_BEGIN)) { //出発地だった場合
                            dateBegin = FORMAT_TIME.parse(listPlaces.get(n).getDepartureTime());
                        } else {     //それ以外は前の場所の出発時刻を取得
                            dateBegin = FORMAT_TIME.parse(listPlaces.get(n-1).getDepartureTime());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        textView.setText("取得失敗");
                        continue;
                    }

                    // 日付計算のためCalendarに変換
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateBegin);

                    String text = "";
                    // 各Placeの時間を計算
                    // 同時にtextViewに表示するテキストを作成
                    if(listPlaces.get(n).getType().equals(Place.TYPE_BEGIN)) {
                        listPlaces.get(n).setDepartureTime(FORMAT_TIME.format(calendar.getTime()));
                        text = listPlaces.get(n).getDepartureTime() + "発";
                    } else if (listPlaces.get(n).getType().equals(Place.TYPE_DIST)) {
                        calendar.add(Calendar.SECOND, timeSecs.get(n - 1));
                        listPlaces.get(n).setArrivalTime(FORMAT_TIME.format(calendar.getTime()));
                        calendar.add(Calendar.MINUTE, listPlaces.get(n).getDurationMinute());
                        listPlaces.get(n).setDepartureTime(FORMAT_TIME.format(calendar.getTime()));
                        text = listPlaces.get(n).getArrivalTime() + "着\n" + listPlaces.get(n).getDurationMinute() + "分滞在\n" + listPlaces.get(n).getDepartureTime() + "発";
                    } else if (listPlaces.get(n).getType().equals(Place.TYPE_END)) {
                        calendar.add(Calendar.SECOND, timeSecs.get(n - 1));
                        listPlaces.get(n).setArrivalTime(FORMAT_TIME.format(calendar.getTime()));
                        text = listPlaces.get(n).getArrivalTime() + "着";
                    }
                    textView.setText(text);
                }

                //Mapが取得できていれば経路を表示
                DrawDirections();

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                return;
            }
        }.execute();
        return;
    }

    @Nullable
    private String getAddressByPlace(Place place) {

        if (!Geocoder.isPresent()) {
            Toast.makeText(getApplication(), "位置情報サービスが無効", Toast.LENGTH_SHORT).show();
            return null;
        }

        Geocoder coder = new Geocoder(getApplicationContext());

        List<Address> addresses;
        String address;

        try {
            addresses = coder.getFromLocationName(place.getName(), 1);
            //Toast.makeText(EditJourneyActivity.this, addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            Toast.makeText(EditJourneyActivity.this, "目的地の検索に失敗", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
        return address;
    }

    private void addPlaceRow(LinearLayout layout, Place place) {
        //時間とか表示するビューを作成
        TextView textTime = new TextView(this);

        textTime.setText("検索中...");
        textTime.setId(place.getTextViewId());
        textTime.setPadding(0, 0, 20, 0);

        //レイアウトを取得
        LinearLayout layoutTime = new LinearLayout(this);
        layoutTime.setOrientation(LinearLayout.HORIZONTAL);

        layoutTime.addView(textTime, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutTime.setPadding(20, 20, 30, 20);

        //建物写真と建物名称を右に
        LinearLayout layoutPlace = new LinearLayout(this);
        layoutPlace.setOrientation(LinearLayout.VERTICAL);

        TextView textPlace = new TextView(this);
        textPlace.setText(place.getName());

        ImageView imagePlace = new ImageView(this);
        setFlickrImageByWord(this, imagePlace, place.getName());

        layoutPlace.addView(imagePlace, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutPlace.addView(textPlace, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutTime.addView(layoutPlace, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        imagePlace.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 滞在時間編集、場所削除のためのDialogを開けるように?
                // 画像クリックでなく編集(削除)ボタンを別で追加した方がいいかも?
                // 出発地と到着地は削除不可にするとか?
            }
        });
        layout.addView(layoutTime, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addDetourButton(LinearLayout layout, int id) {
        ImageButton buttonAddDetour = new ImageButton(this);
        buttonAddDetour.setImageResource(R.drawable.plus_black_small);
        buttonAddDetour.setId(id);
        buttonAddDetour.setOnClickListener(new AddButtonOnClickListener());
        layout.addView(buttonAddDetour, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }


    //JSONからMapにDirectionを表示
    private void DrawDirections() {
        if(directionResponceJSON == null || !mapReady) {
            return;
        }

        //JSONObjectから経路情報を分解する
        //目的地と経由地のマーカー
        ArrayList<LatLng> marker = new ArrayList<LatLng>();
        //経路の直線群
        PolylineOptions polylineOptions = new PolylineOptions();

        LatLngBounds latLngBounds;

        try {
            //最初のルートを選択
            JSONObject routeObject = directionResponceJSON.getJSONArray("routes").getJSONObject(0);
            LatLng northeast = new LatLng(routeObject.getJSONObject("bounds").getJSONObject("northeast").getDouble("lat") , routeObject.getJSONObject("bounds").getJSONObject("northeast").getDouble("lng"));
            LatLng southwest = new LatLng(routeObject.getJSONObject("bounds").getJSONObject("southwest").getDouble("lat") , routeObject.getJSONObject("bounds").getJSONObject("southwest").getDouble("lng"));
            latLngBounds = new LatLngBounds(southwest,northeast);
            //各目的地間のルート
            for(int n = 0; n < routeObject.getJSONArray("legs").length(); n++) {
                JSONObject placeDirectionObject = routeObject.getJSONArray("legs").getJSONObject(n);
                LatLng latLngStart = new LatLng(
                        placeDirectionObject.getJSONObject("start_location").getDouble("lat"),
                        placeDirectionObject.getJSONObject("start_location").getDouble("lng"));
                polylineOptions.add(latLngStart);
                if(n == 0) {
                    marker.add(latLngStart);
                }
                for(int m = 0; m < placeDirectionObject.getJSONArray("steps").length(); m++) {
                    JSONObject passDirectionObject = placeDirectionObject.getJSONArray("steps").getJSONObject(m);
                    polylineOptions.add(new LatLng(
                            passDirectionObject.getJSONObject("start_location").getDouble("lat"),
                            passDirectionObject.getJSONObject("start_location").getDouble("lng")));
                    polylineOptions.add(new LatLng(
                            passDirectionObject.getJSONObject("end_location").getDouble("lat"),
                            passDirectionObject.getJSONObject("end_location").getDouble("lng")));
                }
                LatLng latLngEnd = new LatLng(
                        placeDirectionObject.getJSONObject("end_location").getDouble("lat"),
                        placeDirectionObject.getJSONObject("end_location").getDouble("lng"));
                marker.add(latLngEnd);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        //線分集合を描画
        googleMap.clear();
        polylineOptions.color(Color.RED);
        googleMap.addPolyline(polylineOptions);

        //マーカーを描画
        for(int n = 0; n < listPlaces.size(); n++) {
            //マーカーの座標を場所の座標に設定
            listPlaces.get(n).setLatLng(marker.get(n));
            googleMap.addMarker(new MarkerOptions().position(marker.get(n)).title(listPlaces.get(n).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        //カメラの中心は左上と右下の中心、左右に25ピクセルのマージンつけて
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,25));
    }

    private Boolean saveJourney(String journeyName) {
        if(journeyName.contains(",")) {
            return false;
        }
        try {
            FileOutputStream outputStream = openFileOutput(SAVEFILE, Context.MODE_APPEND);
            //info,旅行名,旅行日時
            outputStream.write((COLUMN_INFO + "," + journeyName + "," + getIntent().getExtras().getString(DATE_BEGIN) + "\n").getBytes());

            for(int n = 0; n < listPlaces.size(); n++) {
                //マーカーの座標を場所の座標に設定
                Place place = listPlaces.get(n);
                String string = COLUMN_PLACE + "," + place.getName();
                if(place.getArrivalTime() != null) {
                    string += "," + place.getArrivalTime();
                } else {
                    string += ",";
                }
                if(place.getDurationMinute() != null) {
                    string += "," + place.getDurationMinute();
                } else {
                    string += ",";
                }
                if(place.getDepartureTime() != null) {
                    string += "," + place.getDepartureTime();
                } else {
                    string += ",";
                }
                string += "\n";
                outputStream.write(string.getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private class AddButtonOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            //TODO 経由地追加のウィンドウ開いて戻して
            int newId = v.getId()+1-ADDBUTTON_ID_BEGIN;
            //Idは挿入する位置
            //TODO AddPlaceActivity起動

            Place place = new Place(newId,"はままつフラワーパーク",Place.TYPE_DIST,null,60,null);
            insertPlace(place);
        }
    }

    private void insertPlace(Place place) {
        int insertPosition = place.getId();
        for(int n = insertPosition; n < listPlaces.size(); n++) {
            listPlaces.get(n).setId(n+1);
        }
        listPlaces.add(insertPosition,place);

        LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);
        layoutPlan.removeAllViews();
        setPlaces(layoutPlan);
    }


    private ArrayList<Place> listPlaces = new ArrayList<Place>();

    private JSONObject directionResponceJSON;

    private Boolean mapReady = false;
    private GoogleMap googleMap;

}
