package mjtakenon.createmyjourney;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static mjtakenon.createmyjourney.Const.*;

public class AddPlaceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addplace);

        // 決定ボタン
        Button buttonDecide = (Button) findViewById(R.id.buttonDecide);
        buttonDecide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
            }
        });

        // キャンセルボタン
        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = intent.getExtras();
                intent.putExtras(bundle);
                setResult(RESULT_CANCEL,intent);
                finish();
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null && bundle.getInt(MODE) == MODE_ADD) {
            placeBegin = (Place)bundle.getSerializable(PLACE_BEGIN);
            placeEnd = (Place)bundle.getSerializable(PLACE_END);
        } else {
            placeBegin = null;
            placeEnd = null;
        }

        LinearLayout layoutDestinations = (LinearLayout)findViewById(R.id.layoutDestinations);

        loadDistnations("opendata");

        setDestinations(layoutDestinations);

        //finish(RESULT_OK);

    }

    // 画面に戻ってきたとき
    @Override
    public void onResume() {
        super.onResume();
    }

    //csvから目的地読み込み
    Boolean loadDistnations(String filename) {
        try {

            String filelist[] = getAssets().list("opendata");
            Integer id = 0;

            for(int n = 0; n < filelist.length; n++) {

                if (!filelist[n].contains(".csv")) {
                    continue;
                }

                AssetManager assetManager = getResources().getAssets();
                InputStream inputStream = assetManager.open("opendata" + "/" + filelist[n]);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);

                //最初の一行読み飛ばす
                String line = bufferReader.readLine();

                while ((line = bufferReader.readLine()) != null) {
                    String[] string = line.split(",");

                    if(!mapDistnations.containsKey(string[0])) {
                        mapDistnations.put(string[0], new ArrayList<OpenData>());
                    }

                    if(string.length >= 4) {    //緯度経度を含む
                        mapDistnations.get(string[0]).add(new OpenData(id, string[0],string[1],string[2],Double.parseDouble(string[3]),Double.parseDouble(string[4])));
                    } else {                    //緯度経度を含まない
                        mapDistnations.get(string[0]).add(new OpenData(id, string[0],string[1],string[2]));
                    }
                    id++;
                }
                bufferReader.close();
            }
        } catch(Exception e) {
            Toast.makeText(AddPlaceActivity.this, "ファイルが存在しないか読み込めませんでした", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //目的地をオープンデータから読み込み、表示
    private void setDestinations(LinearLayout layoutDestinations) {
        //種類ごとに分類して表示
        Iterator<String> itr = mapDistnations.keySet().iterator();
        while(itr.hasNext()) {
        //for(int n = 0; n < 2; n++) {
            String key = itr.next();

            //目的地の分類の名前
            TextView textGroup = new TextView(this);
            textGroup.setText(key);

            //目的地を横並びで表示するスクロール
            HorizontalScrollView scrollView = new HorizontalScrollView(this);

            //目的地を横並びさせるLayout
            LinearLayout layoutDistGroup = new LinearLayout(this);
            layoutDistGroup.setOrientation(LinearLayout.HORIZONTAL);

            ArrayList<OpenData> openDatas = mapDistnations.get(key);

            for(int n = 0; n < openDatas.size(); n++) {
                //目的地の画像と名前を縦並びセットで格納するLayout
                LinearLayout layoutImageName = new LinearLayout(this);
                layoutImageName.setOrientation(LinearLayout.VERTICAL);
                layoutImageName.setId(openDatas.get(n).getId());

                String name = openDatas.get(n).getName();
                ImageView imageView = new ImageView(this);
                setFlickrImageByWord(this,imageView,name);

                TextView textView = new TextView(this);
                textView.setText(name);

                //ImageとTextのペアを作成
                layoutImageName.addView(imageView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutImageName.addView(textView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutImageName.setPadding(8,8,8,8);

                layoutImageName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        // 追加するView
                        bundle.putInt(NEW_ID,getIntent().getExtras().getInt(NEW_ID));
                        // クリックされた目的地の名前
                        OpenData openData = getOpenDataById(v.getId());
                        if(openData != null) {
                            bundle.putSerializable(NEW_DIST,openData);
                        }
                        intent.putExtras(bundle);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });

                //ペアを横並びに並べる
                layoutDistGroup.addView(layoutImageName, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            }

            //横並びをスクロールに格納
            scrollView.addView(layoutDistGroup, ScrollView.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

            //分類名とスクロールを縦並びに格納
            layoutDestinations.addView(textGroup, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDestinations.addView(scrollView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    OpenData getOpenDataById(Integer id) {
        Iterator<String> it = mapDistnations.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            for(int m = 0; m < mapDistnations.get(key).size(); m++) {
                if(mapDistnations.get(key).get(m).getId().equals(id)) {
                    return mapDistnations.get(key).get(m);
                }
            }
        }
        return null;
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
            Toast.makeText(getApplication(), "目的地の検索に失敗", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
        return address;
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

    private Place placeBegin;
    private Place placeEnd;

    private TreeMap<String,ArrayList<OpenData>> mapDistnations = new TreeMap<String,ArrayList<OpenData>>();
}
