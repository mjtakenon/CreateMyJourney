package mjtakenon.createmyjourney;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static mjtakenon.createmyjourney.Const.*;

// 元はAppCompatActivityだったけどFragmentActivityに変えた
public class EditJourneyActivity extends AppCompatActivity implements OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        //レイアウト作成ボタン
        Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText viewJourneyName = new EditText(EditJourneyActivity.this);
                //旅の初期名は日付+目的地
                if (getIntent().getExtras().getInt(MODE) == MODE_ADD) {
                    viewJourneyName.setText(getIntent().getExtras().getString(DATE_BEGIN) + " " + getIntent().getExtras().getString(PLACE_DIST));
                } else if (getIntent().getExtras().getInt(MODE) == MODE_LOAD) {
                    viewJourneyName.setText(getIntent().getExtras().getString(JOURNEY_NAME));
                }
                //旅名を入れるダイアログ
                new AlertDialog.Builder(EditJourneyActivity.this).setTitle("この旅行の名前を入力してください").setView(viewJourneyName)
                        .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO 旅名にコンマを入れるとありえんバグる
                                Iterator<Integer> it = mapTimeToPlace.keySet().iterator();
                                try {
                                    FileOutputStream outputStream = openFileOutput(SAVEFILE, Context.MODE_APPEND);
                                    //info,旅行名,旅行日時
                                    outputStream.write((COLUMN_INFO + "," + viewJourneyName.getText() + "," + getIntent().getExtras().getString(DATE_BEGIN) + "\n").getBytes());

                                    while (it.hasNext()) {
                                        Integer key = it.next();
                                        //マーカーの座標を場所の座標に設定
                                        Place place = mapTimeToPlace.get(key);
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
                setPlaces(layoutPlan, mapTimeToPlace);
            }
        });

        Intent intent = this.getIntent();

        //タイムラインは時刻を左、場所画像と場所を右にセットで積んでく
        LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);

        //Intentなしで起動した場合
        if(intent == null) {
            finish();
            return;
        }

        //TODO 読み込み画面からきてたらmapTimeToPlaceを読み込み、setPlacesを実行
        //もし作成画面からきてたら出発地、目的地、到着地を追加
        Bundle bundle = intent.getExtras();
        if(bundle.getInt(MODE) == MODE_ADD) {    //読み込み画面からきたFlagがいるか?
            mapTimeToPlace = new TreeMap<Integer, Place>();
            if (bundle.getString(PLACE_BEGIN) != null) {
                Place placeBegin = new Place(mapTimeToPlace.size(), bundle.getString(PLACE_BEGIN), null, null, bundle.getString(TIME_BEGIN));
                mapTimeToPlace.put(placeBegin.getId(), placeBegin);
            }
            if (bundle.getString(PLACE_DIST) != null) {
                Place placeDist = new Place(mapTimeToPlace.size(), bundle.getString(PLACE_DIST), null, intent.getIntExtra(TIME_DURATION, 0), null);
                mapTimeToPlace.put(placeDist.getId(), placeDist);
            }
            if (bundle.getString(PLACE_END) != null) {
                Place placeEnd = new Place(mapTimeToPlace.size(), bundle.getString(PLACE_END), bundle.getString(TIME_END), null, null);
                mapTimeToPlace.put(placeEnd.getId(), placeEnd);
            }
        } else if (bundle.getInt(MODE) == MODE_LOAD) {
            mapTimeToPlace = new TreeMap<Integer, Place>();
            ArrayList<Integer> listId = bundle.getIntegerArrayList(PLACE_ID);
            ArrayList<String> listName = bundle.getStringArrayList(PLACE_NAME);
            ArrayList<String> listArrivalTime = bundle.getStringArrayList(TIME_ARRIVAL);
            ArrayList<Integer> listDurationMinute = bundle.getIntegerArrayList(TIME_DURATION);
            ArrayList<String> listDepartureTime = bundle.getStringArrayList(TIME_DEPARTURE);
            for(int n = 0; n < listId.size(); n++) {
                Place place = new Place(listId.get(n),listName.get(n),listArrivalTime.get(n),listDurationMinute.get(n),listDepartureTime.get(n));
                mapTimeToPlace.put(listId.get(n),place);
            }
        }

        setPlaces(layoutPlan, mapTimeToPlace);

        SupportMapFragment fragmentMap = new SupportMapFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.fragmentMapRoot, fragmentMap, "fragmentMap");
        ft.commit();
        fragmentMap.getMapAsync(this);

        directionResponceJSON = null;
        mapReady = false;
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
                    //Toast.makeText(context,imageUrl,Toast.LENGTH_LONG);
                } else {
                    //Toast.makeText(context,"ネットワーク接続がありません",Toast.LENGTH_SHORT);
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
    private void setTimeRequired(final Activity activity, final TreeMap<Integer, Place> places) {

        new AsyncTask<Void, Void, ArrayList<Integer>>() {
            String apiUrl;
            ProgressDialog progressDialog;
            String responseJSON;

            @Override
            protected void onPreExecute() {
                ArrayList<Place> placesList = new ArrayList<Place>();

                Iterator<Integer> it = places.keySet().iterator();
                while (it.hasNext()) {
                    Integer key = it.next();
                    placesList.add(places.get(key));
                }

                String encodedBeginPlace;       //最初の場所名
                String encodedEndPlace;         //最後の場所名
                ArrayList<String> encodedDistPlaces = new ArrayList<String>();

                //placesが2以上じゃないと死ぬ
                try {
                    encodedBeginPlace = URLEncoder.encode(placesList.get(0).getName(), "UTF-8");
                    encodedEndPlace = URLEncoder.encode(placesList.get(placesList.size() - 1).getName(), "UTF-8");
                    for (int n = 1; n < placesList.size() - 1; n++) {
                        encodedDistPlaces.add(URLEncoder.encode(placesList.get(n).getName(), "UTF-8"));
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
                    if (n + 1 < encodedDistPlaces.size()) {
                        apiUrl += "|";
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
                    for (int n = 0; n < places.size() - 1; n++) {
                        Integer timeSec = directionResponceJSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(n).getJSONObject("duration").getInt("value");
                        timeSecs.add(timeSec);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return timeSecs;
            }

            //TODO たまに所要時間が0にされるバグがある
            @Override
            protected void onPostExecute(ArrayList<Integer> timeSecs) {

                if(timeSecs == null) {
                    return;
                }

                Iterator<Integer> it = places.keySet().iterator();
                Integer prevKey = null;

                for (int n = 0; n < places.size(); n++) {
                    Integer key = it.next();

                    Date dateBegin = null;
                    TextView textView = (TextView) findViewById(places.get(key).getId());

                    //移動時間を計算
                    try {
                        if (prevKey != null) { //前の場所の出発時刻
                            dateBegin = dfTime.parse(places.get(prevKey).getDepartureTime());
                        } else {               //出発地だった場合
                            dateBegin = dfTime.parse(places.get(key).getDepartureTime());
                        }
                    } catch (ParseException e) {
                        textView.setText("取得失敗");
                        e.printStackTrace();
                        continue;
                    }

                    // 日付計算のためCalendarに変換
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateBegin);

                    // 出発地以外移動時間を加算し到着時刻を計算
                    if (places.get(key).getDepartureTime() == null) {
                        calendar.add(Calendar.SECOND, timeSecs.get(n - 1));
                        //ArrivalTimeを算出
                        String newArrivalTime = dfTime.format(calendar.getTime());
                        places.get(key).setArrivalTime(newArrivalTime);
                    }

                    // 経由地の場合のみ滞在時間を取得
                    if (places.get(key).getDurationMinute() != null) {
                        calendar.add(Calendar.MINUTE, places.get(key).getDurationMinute());
                        String newDepartureTime = dfTime.format(calendar.getTime());
                        places.get(key).setDepartureTime(newDepartureTime);
                        //newDurationMinute = places.get(key).getDurationMinute();
                    }
                    //places.get(key).setDurationMinute(newDurationMinute);

                    // 到着地の場合以外出発時刻を計算
                    if (places.get(key).getArrivalTime() == null) {
                        String newDepartureTime = dfTime.format(calendar.getTime());
                        places.get(key).setDepartureTime(newDepartureTime);
                    }

                    //textViewに表示するテキストを作成
                    String text = "";
                    // 出発地の場合
                    if (places.get(key).getDepartureTime() != null && places.get(key).getDurationMinute() == null) {
                        text = places.get(key).getDepartureTime() + "発";
                    }
                    // 経由地の場合
                    if (places.get(key).getDurationMinute() != null) {
                        text = places.get(key).getArrivalTime() + "着\n" + places.get(key).getDurationMinute() + "分滞在\n" + places.get(key).getDepartureTime() + "発";
                    }
                    // 到着地の場合以外出発時刻を計算
                    if (places.get(key).getArrivalTime() != null && places.get(key).getDurationMinute() == null) {
                        text = places.get(key).getArrivalTime() + "着";
                    }
                    textView.setText(text);

                    prevKey = key;
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
            Toast.makeText(getApplication(), addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            Toast.makeText(getApplication(), "目的地の検索に失敗", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
        return address;
    }

    private void addPlaceRow(LinearLayout layout, Place place) {
        //時間とか表示するビューを作成
        TextView textTime = new TextView(this);

        textTime.setText("検索中...");
        textTime.setId(place.getId());
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
        buttonAddDetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 経由地追加のウィンドウ開いて戻して
                int id = v.getId();
            }
        });

        layout.addView(buttonAddDetour, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    //場所と追加用ボタンを交互に配置
    private void setPlaces(LinearLayout layout, TreeMap<Integer, Place> places) {
        Iterator<Integer> it = places.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            addPlaceRow(layout, places.get(key));
            if (it.hasNext()) {
                addDetourButton(layout,places.get(key).getId()+ADD_BUTTON_ID_BEGIN);
            }
        }
        setTimeRequired(EditJourneyActivity.this, places);
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
        polylineOptions.color(Color.RED);
        googleMap.addPolyline(polylineOptions);

        //マーカーを描画
        Iterator<Integer> it = mapTimeToPlace.keySet().iterator();
        for(int n = 0; n < marker.size(); n++) {
            Integer key = it.next();
            //マーカーの座標を場所の座標に設定
            mapTimeToPlace.get(key).setPosition(marker.get(n));
            googleMap.addMarker(new MarkerOptions().position(marker.get(n)).title(mapTimeToPlace.get(key).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        //カメラの中心は左上と右下の中心
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,25));
    }

    final DateFormat dfTime = new SimpleDateFormat("HH:mm");

    private TreeMap<Integer, Place> mapTimeToPlace = null;

    private JSONObject directionResponceJSON;

    private Boolean mapReady = false;
    private GoogleMap googleMap;

}
