package mjtakenon.createmyjourney;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static mjtakenon.createmyjourney.Const.*;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(2 * 1024 * 1024)
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

                bundle.putSerializable(JOURNEY, journeys.get(position));

                Intent intent = new Intent(getApplication(), EditJourneyActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        listJourney.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                final ListView listView = (ListView) parent;
                //TODO 削除確認のダイヤログ
                new AlertDialog.Builder(MenuActivity.this)
                        .setTitle("確認")
                        .setMessage("削除しますか?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                journeys.remove(position);
                                saveJourneyList();
                                loadJourneyList(listView);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
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

    //旅リスト読み込み
    //TODO saveとloadをモジュール化して分割
    Boolean loadJourneyList(ListView listJourney) {
        try {
            InputStream inputStream = openFileInput(SAVEFILE);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            journeys = (ArrayList<Journey>) objectInputStream.readObject();

            objectInputStream.close();
            inputStream.close();

            if(journeys == null) {
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(MenuActivity.this,"ファイルが存在しないか読み込めませんでした",Toast.LENGTH_SHORT).show();
            deleteFile(SAVEFILE);
            e.printStackTrace();
            return false;
        }

        ArrayList<String> names = new ArrayList<String>();
        for(Journey journey : journeys) {
            names.add(journey.getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names.toArray(new String[0]));
        listJourney.setAdapter(arrayAdapter);

        return true;
    }

    Boolean saveJourneyList() {
        try {
            FileOutputStream fileOutputStream = openFileOutput(SAVEFILE, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(journeys);
            objectOutputStream.close();
            fileOutputStream.close();
        }
        catch (Exception e) {
           Toast.makeText(MenuActivity.this,"ファイルが保存できませんでした",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private ArrayList<Journey> journeys;
}

