package me.dawars.sandbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] items = new String[]{
                "FingerCurve",
                "BezierCurve",
                "BezierSpline",
                "WearComm",
                "WearCommBack",
                "SpheroTilt",
                "SpheroWearTilt"
        };

        mListView = (ListView) findViewById(R.id.activity_main_listview_id);
        mListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Class clazz = null;
                try {
                    clazz = Class.forName(getPackageName() + "." + items[position] + "Activity");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), clazz);
                startActivity(intent);
            }
        });
    }
}
