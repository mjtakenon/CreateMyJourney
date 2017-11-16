package mjtakenon.createmyjourney;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class EditJourneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        //レイアウト作成ボタン
        Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //レイアウトを保存し元の画面に戻る?
                finish();
            }
        });

        //更新ボタン
        Button buttonReflesh = (Button) findViewById(R.id.buttonReflesh);
        buttonReflesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);
                layoutPlan.removeAllViews();

                Iterator<Integer> it = mapTimeToPlace.keySet().iterator();
                while (it.hasNext()) {
                    Integer key = it.next();
                    addPlaceRow(layoutPlan, mapTimeToPlace.get(key));
                }

                setTimeRequired(getApplicationContext(), mapTimeToPlace);
            }
        });

        Intent intent = this.getIntent();

        //タイムラインは時刻を左、場所画像と場所を右にセットで積んでく
        LinearLayout layoutPlan = (LinearLayout) findViewById(R.id.layoutPlan);

        //出発地、目的地、到着地を追加
        if(intent.getStringExtra("textPlaceBegin") != null){
            Place placeBegin = new Place(mapTimeToPlace.size(),intent.getStringExtra("textPlaceBegin"),intent.getStringExtra("textTimeBegin"),0);
            mapTimeToPlace.put(placeBegin.getId(),placeBegin);
        }
        if(intent.getStringExtra("textPlaceDist") != null) {
            //TODO 開始場所からの所要時刻で計算したい
            Place placeDist = new Place(mapTimeToPlace.size(),intent.getStringExtra("textPlaceDist"),"null",0);
            //getTimeRequired(this,intent.getStringExtra("textPlaceBegin"),intent.getStringExtra("textPlaceDist"),intent.getStringExtra("textTimeBegin"));
            mapTimeToPlace.put(placeDist.getId(),placeDist);
        }
        if(intent.getStringExtra("textPlaceEnd") != null) {
            Place placeEnd = new Place(mapTimeToPlace.size(),intent.getStringExtra("textPlaceEnd"),intent.getStringExtra("textTimeEnd"),0);
            mapTimeToPlace.put(placeEnd.getId(),placeEnd);
        }

        Iterator<Integer> it = mapTimeToPlace.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            addPlaceRow(layoutPlan, mapTimeToPlace.get(key));
        }
        setTimeRequired(getApplicationContext(), mapTimeToPlace);
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
                    encodedString = URLEncoder.encode(word,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    encodedString = " ";
                    e.printStackTrace();
                    return;
                }
                apiUrl = "https://api.photozou.jp/rest/search_public.json?type=photo&keyword="+encodedString+"&limit=1";
            }

            @Override
            protected String doInBackground(String... params) {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder().url(apiUrl).get().build();
                Response response = null;
                try {
                    Call c= client.newCall(request);
                    response = c.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(response == null) {
                    return null;
                }

                String imageUrl;
                try {
                    JSONObject jo = new JSONObject(response.body().string());
                    if(!jo.getString("stat").equals("ok")) {
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
                if(imageUrl != null) {
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
        new AsyncTask<String, Void, String>() {

            String encodedString;
            String apiUrl;

            @Override
            protected void onPreExecute() {
                GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(imageview);
                Glide.with(context).load(R.raw.loader).into(target);
                imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

                try {
                    encodedString = URLEncoder.encode(word,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    encodedString = " ";
                    e.printStackTrace();
                    return;
                }

                String flickrApikey = "54943877e5144fdb63a83366c3549bc5";

                apiUrl = "https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                        "&api_key=" + flickrApikey +
                        "&text=" + encodedString +
                        "&format=json&nojsoncallback=1" +
                        "&per_page=1" +
                        "&content_type=1" +
                        "&sort=interestingness-desc" +
                        "&extras=url_s";
            }

            @Override
            protected String doInBackground(String... params) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(apiUrl).get().build();
                Response response = null;

                try {
                    Call c= client.newCall(request);
                    response = c.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(response == null) {
                    return null;
                }

                String imageUrl;

                try {
                    JSONObject jo = new JSONObject(response.body().string());
                    if(!jo.getString("stat").equals("ok")) {
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
                if(imageUrl != null) {
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

    //所要時間を取得、placesのtextにセット
    private void setTimeRequired(final Context context, final TreeMap<Integer,Place> places) {
        new AsyncTask<Void, Void, ArrayList<Integer>>() {
            String apiUrl;
            ProgressDialog progressDialog;

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

                //placesが2以上じゃないとどうなるかわからん
                try {
                    encodedBeginPlace = URLEncoder.encode(placesList.get(0).getName(), "UTF-8");
                    encodedEndPlace = URLEncoder.encode(placesList.get(placesList.size()-1).getName(), "UTF-8");
                    for(int n = 1; n < placesList.size()-1; n++) {
                        encodedDistPlaces.add(URLEncoder.encode(placesList.get(n).getName(), "UTF-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    encodedBeginPlace = " ";
                    encodedEndPlace = " ";
                    e.printStackTrace();
                }

                //apiUrl作成
                String googleApikey = "AIzaSyDIoGExI7NPDFq6IJwVTDfyeDgca9q2OQQ";
                apiUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + encodedBeginPlace + "&destination="
                        + encodedEndPlace + "&key="
                        + googleApikey + "&mode=driving";

                //立ち寄りポイントの追加
                if(encodedDistPlaces.size() >= 1) {
                    apiUrl += "&waypoints=";
                }
                for(int n = 0; n < encodedDistPlaces.size(); n++) {
                    apiUrl += encodedDistPlaces.get(n);
                    if(n + 1 < encodedDistPlaces.size()) {
                        apiUrl += "|";
                    }
                }
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
                    JSONObject jo = new JSONObject(response.body().string());
                    if (!jo.getString("status").equals("OK")) {
                        return null;
                    }
                    //総移動時間を取得
                    for(int n = 0; n < places.size() -1; n++) {
                        Integer timeSec = jo.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(n).getJSONObject("duration").getInt("value");
                        timeSecs.add(timeSec);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return timeSecs;
            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }

            @Override
            protected void onPostExecute(ArrayList<Integer> timeSecs) {

                Iterator<Integer> it = places.keySet().iterator();
                Integer prevKey = null;

                for(int n = 0; n < places.size(); n++) {
                    Integer key = it.next();

                    //出発地の時刻の設定はない
                    if(prevKey == null) {
                        prevKey = key;
                        continue;
                    }

                    Date dateBegin = null;
                    TextView textView = (TextView)findViewById(places.get(key).getId());

                    try {
                        //出発時刻
                        dateBegin = dfTime.parse(places.get(prevKey).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(dateBegin == null) {
                        textView.setText("failed");
                    }

                    // Date型の日時をCalendar型に変換
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateBegin);

                    // 日時を加算する
                    calendar.add(Calendar.SECOND, timeSecs.get(n-1));

                    // Calendar型の日時をDate型に戻す
                    Date newDate = calendar.getTime();

                    String newTime = dfTime.format(newDate);
                    textView.setText(newTime);
                    places.get(key).setTime(newTime);

                    prevKey = key;
                }
                return;
            }
        }.execute();
        return;
    }

    @Nullable
    private String getAddressByPlace(Place place) {

        if(!Geocoder.isPresent()) {
            Toast.makeText(getApplication(), "位置情報サービスが無効", Toast.LENGTH_SHORT).show();
            return null;
        }

        Geocoder coder = new Geocoder(getApplicationContext());

        List<Address> addresses;
        String address;

        try {
            addresses = coder.getFromLocationName(place.getName(),1);
            Toast.makeText(getApplication(), addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            Toast.makeText(getApplication(), "目的地の検索に失敗",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
        return address;
    }

    private void addPlaceRow(LinearLayout layout,Place place)
    {
        //時間表示
        TextView textTime = new TextView(this);
        //intentから時間を取得(最初以外はnull)
        textTime.setText(place.getTime());
        textTime.setId(place.getId());
        textTime.setPadding(0,0,20,0);

        //レイアウトを取得
        LinearLayout layoutTime = new LinearLayout(this);
        layoutTime.setOrientation(LinearLayout.HORIZONTAL);

        layoutTime.addView(textTime, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layoutTime.setPadding(20,20,50,20);

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

        layout.addView(layoutTime, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    final DateFormat dfTime = new SimpleDateFormat("HH:mm");

    private TreeMap<Integer,Place> mapTimeToPlace = new TreeMap<Integer,Place>();
}
