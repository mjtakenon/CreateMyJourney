package mjtakenon.createmyjourney;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // default
                .memoryCacheSize(2 * 1024 * 1024)
                .imageDownloader(new BaseImageDownloader(this)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .build();
        ImageLoader.getInstance().init(config);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //サブアクティビティに移動
                Intent intent = new Intent(getApplication(), AddActivity.class);
                startActivity(intent);
            }
        });

        ListView listJourney = (ListView) findViewById(R.id.listJourney);
        /*if(!loadJourneyList(listJourney)) {
            Toast.makeText(this,"ファイルを読み込めませんでした",Toast.LENGTH_LONG);
        }*/

        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        listJourney.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                ArrayList<Place> listSelected = listPlaces.get(position);
                ArrayList<Integer> listId = new ArrayList<Integer>();
                ArrayList<String> listName = new ArrayList<String>();
                ArrayList<String> listArrivalTime = new ArrayList<String>();
                ArrayList<Integer> listDurationMinute = new ArrayList<Integer>();
                ArrayList<String> listDepartureTime = new ArrayList<String>();
                for(int n = 0; n < listSelected.size(); n++) {
                    listId.add(listSelected.get(n).getId());
                    listName.add(listSelected.get(n).getName());
                    listArrivalTime.add(listSelected.get(n).getArrivalTime());
                    listDurationMinute.add(listSelected.get(n).getDurationMinute());
                    listDepartureTime.add(listSelected.get(n).getDepartureTime());
                }
                Bundle bundle = new Bundle();
                bundle.putString("mode","load");
                bundle.putIntegerArrayList("id",listId);
                bundle.putStringArrayList("name",listName);
                bundle.putStringArrayList("arrivalTime",listArrivalTime);
                bundle.putIntegerArrayList("durationMinute",listDurationMinute);
                bundle.putStringArrayList("departureTime",listDepartureTime);

                Intent intent = new Intent(getApplication(), EditJourneyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView listJourney = (ListView) findViewById(R.id.listJourney);
        if(!loadJourneyList(listJourney)) {
            Toast.makeText(MainActivity.this,"ファイルを読み込めませんでした",Toast.LENGTH_LONG);
        }
    }

    //TODO Journey名が保存されねえな
    //csvから旅リスト読み込み
    Boolean loadJourneyList(ListView listJourney) {
        ArrayList<String> journeyNames = new ArrayList<String>();

        //listJourney.removeAllViews();
        try {
            InputStream inputStream = openFileInput("savedJourney.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);

            //旅の経由地の配列が複数
            listPlaces = new ArrayList<ArrayList<Place>>();
            //1つの旅の経由地の配列
            ArrayList<Place> places = null;
            String line;
            Integer id = 0;
            //TODO クソ汚いソースなんとかして
            while ((line = bufferReader.readLine()) != null) {
                String[] string = line.split(",");
                if (string.length == 1) {           //最初の行
                    journeyNames.add(string[0]);
                    id = 0;
                    if(places != null) {
                        listPlaces.add(places);
                    }
                    places = new ArrayList<Place>();
                } else {                            //それ以外は地点情報だと仮定して
                    Place place = new Place(id,string[0]);
                    if(string.length >= 2) {
                        if (!string[1].isEmpty()) {
                            place.setArrivalTime(string[1]);
                        }
                    }
                    if(string.length >= 3) {
                        if (!string[2].isEmpty()) {
                            place.setDurationMinute(Integer.valueOf(string[2]));
                        }
                    }
                    if(string.length >= 4) {
                        if (!string[3].isEmpty()) {
                            place.setDepartureTime(string[3]);
                        }
                    }
                    places.add(place);
                    id++;
                }

            }
            listPlaces.add(places);
            bufferReader.close();
        } catch (Exception e) {
            deleteFile("savedJourney.csv");
            e.printStackTrace();
            return false;
        }

        if (journeyNames != null) {
            String[] strs = journeyNames.toArray(new String[0]);
            ArrayAdapter<String> aA = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strs);
            listJourney.setAdapter(aA);
        }
        return true;
    }

    private ArrayList<ArrayList<Place>> listPlaces = null;

//    public native String stringFromJNI();
//
//    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }
}

