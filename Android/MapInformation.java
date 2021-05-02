package Multidisplinary.Project.MDP_Group_9.Ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import Multidisplinary.Project.MDP_Group_9.Models.GridViewModel;
import Multidisplinary.Project.MDP_Group_9.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MapInformation extends AppCompatActivity {
    private final static String TAG = "MapInformation";

    String mapString;
    String connStatus;
    JSONObject mapJsonObject;
    GridViewModel gridView;
    Button obstacleBtn;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showLog("Entering onCreateView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map_information);

        connStatus = "Disconnected";

        sharedPreferences = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("mapJsonObject")) {
            mapString = sharedPreferences.getString("mapJsonObject", "");
            showLog(mapString);
            try {
                mapJsonObject = new JSONObject(mapString);
                showLog("mapJsonObject try success");
            } catch (JSONException e) {
                e.printStackTrace();
                showLog("mapJsonObject try fail");
            }
            gridView = new GridViewModel(this);
            gridView = findViewById(R.id.mapInformationView);
            GridViewModel.mapJsonObject = mapJsonObject;
        }

        //obstacleBtn = findViewById(R.id.obstacleBtn);

//        obstacleBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (obstacleBtn.getText().equals("Show Explored")) {
//                    gridView.plotObstacle = true;
//                    Toast.makeText(getApplicationContext(), "Showing obstacle cells", Toast.LENGTH_SHORT).show();
//                    gridView.invalidate();
//                }
//                else if (obstacleBtn.getText().equals("Show Obstacle")) {
//                    gridView.plotObstacle = false;
//                    Toast.makeText(getApplicationContext(), "Showing explored cells", Toast.LENGTH_SHORT).show();
//                    gridView.invalidate();
//                }
//            }
//        });

        showLog("Exiting onCreateView");
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }
}