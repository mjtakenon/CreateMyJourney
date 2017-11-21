package mjtakenon.createmyjourney;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static mjtakenon.createmyjourney.Const.*;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
//                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
//                .threadPoolSize(3) // default
//                .threadPriority(Thread.NORM_PRIORITY - 1) // default
//                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // default
                .memoryCacheSize(2 * 1024 * 1024)
//                .imageDownloader(new BaseImageDownloader(this)) // default
//                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .build();
        ImageLoader.getInstance().init(config);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Journeyを新規作成する画面へ
                Intent intent = new Intent(getApplication(), NewActivity.class);
                startActivity(intent);
            }
        });

        ListView listJourney = (ListView) findViewById(R.id.listJourney);

        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        listJourney.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ListView listView = (ListView) parent;

                Bundle bundle = new Bundle();
                bundle.putInt(MODE,MODE_LOAD);

                bundle.putString(JOURNEY_NAME,journeyNames.get(position));
                bundle.putString(DATE_BEGIN,dateBegin.get(position));

                bundle.putSerializable(SERIAL_PLACES, listPlaces.get(position));

                Intent intent = new Intent(getApplication(), EditJourneyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        listJourney.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                journeyNames.remove(position);
                listPlaces.remove(position);
                saveJourneyList();
                loadJourneyList(listView);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView listJourney = (ListView) findViewById(R.id.listJourney);
        loadJourneyList(listJourney);
    }

    //csvから旅リスト読み込み
    Boolean loadJourneyList(ListView listJourney) {
        try {
            InputStream inputStream = openFileInput(SAVEFILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);

            //旅の経由地の配列が複数
            listPlaces = new ArrayList<ArrayList<Place>>();
            //旅の名前の配列
            journeyNames = new ArrayList<String>();
            dateBegin = new ArrayList<String>();
            //1つの旅の経由地の配列
            ArrayList<Place> places = null;
            String line;
            Integer placeId = 0;
            Integer journeyId = 0;
            while ((line = bufferReader.readLine()) != null) {
                String[] string = line.split(",");
                if (string[0].equals(COLUMN_INFO)) {          //旅の情報
                    journeyNames.add(string[1]);         //リストに表示するための名前
                    dateBegin.add(string[2]);            //旅の出発日
                    if(places != null) {
                        for(int n = 0; n < places.size(); n++) {
                            if(n == 0) {
                                places.get(n).setType(Place.TYPE_BEGIN);
                            } else if (n == places.size()-1) {
                                places.get(n).setType(Place.TYPE_END);
                            } else {
                                places.get(n).setType(Place.TYPE_DIST);
                            }
                        }
                        listPlaces.add(places);
                    }
                    places = new ArrayList<Place>();
                    placeId = 0;
                    journeyId++;
                } else if(string[0].equals(COLUMN_PLACE)) {  //地点情報
                    Place place = new Place(placeId,string[1]);
                    if(string.length >= 3) {
                        if (!string[2].isEmpty()) {
                            place.setArrivalTime(string[2]);
                        }
                    }
                    if(string.length >= 4) {
                        if (!string[3].isEmpty()) {
                            place.setDurationMinute(Integer.valueOf(string[3]));
                        }
                    }
                    if(string.length >= 5) {
                        if (!string[4].isEmpty()) {
                            place.setDepartureTime(string[4]);
                        }
                    }
                    places.add(place);
                    placeId++;
                }
            }
            for(int n = 0; n < places.size(); n++) {
                if(n == 0) {
                    places.get(n).setType(Place.TYPE_BEGIN);
                } else if (n == places.size()-1) {
                    places.get(n).setType(Place.TYPE_END);
                } else {
                    places.get(n).setType(Place.TYPE_DIST);
                }
            }
            listPlaces.add(places);
            bufferReader.close();
        } catch (Exception e) {
            Toast.makeText(MenuActivity.this,"ファイルが存在しないか読み込めませんでした",Toast.LENGTH_SHORT).show();
            deleteFile(SAVEFILE);
            e.printStackTrace();
            return false;
        }

        String[] strs = journeyNames.toArray(new String[0]);
        ArrayAdapter<String> aA = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strs);
        listJourney.setAdapter(aA);

        return true;
    }

    //TODO セーブデータをシリアライズ化するのにJourneyクラスを作成する必要がある
    Boolean saveJourneyList() {
        try {
            FileOutputStream outputStream = openFileOutput(SAVEFILE, Context.MODE_PRIVATE);
            //info,旅行名,旅行日時
            for (int n = 0; n < listPlaces.size(); n++) {
                outputStream.write((COLUMN_INFO + "," + journeyNames.get(n) + "," + dateBegin.get(n) + "\n").getBytes());
                for (int m = 0; m < listPlaces.get(n).size(); m++) {
                    String string = COLUMN_PLACE +"," + listPlaces.get(n).get(m).getName();
                    if(!listPlaces.get(n).get(m).getType().equals(Place.TYPE_BEGIN)) {
                        string += "," + listPlaces.get(n).get(m).getArrivalTime();
                    } else {
                        string += ",";
                    }
                    if(listPlaces.get(n).get(m).getType().equals(Place.TYPE_DIST)) {
                        string += "," + listPlaces.get(n).get(m).getDurationMinute();
                    } else {
                        string += ",";
                    }
                    if(!listPlaces.get(n).get(m).getType().equals(Place.TYPE_END)) {
                        string += "," + listPlaces.get(n).get(m).getDepartureTime();
                    } else {
                        string += ",";
                    }
                    string += "\n";
                    outputStream.write(string.getBytes());
                }
            }
        }
        catch (Exception e) {
           Toast.makeText(MenuActivity.this,"ファイルが保存できませんでした",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private ArrayList<ArrayList<Place>> listPlaces = null;
    ArrayList<String> journeyNames = null;
    ArrayList<String> dateBegin = null;
}

