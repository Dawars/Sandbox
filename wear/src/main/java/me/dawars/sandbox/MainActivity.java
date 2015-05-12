package me.dawars.sandbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;


public class MainActivity extends Activity implements WearableListView.ClickListener {

    private WearableListView mListView;
    private String[] listItems = new String[]{
            "WearComm",
            "WearCommBack",
            "SpheroWearTilt"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainListAdapter adapter = new MainListAdapter(this, listItems);

        mListView = (WearableListView) findViewById(R.id.activity_main_list);
        mListView.setAdapter(adapter);
        mListView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Class clazz = null;
        try {
            clazz = Class.forName(getPackageName() + "." + listItems[viewHolder.getPosition()] + "Activity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), clazz);
        startActivity(intent);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
