package Multidisplinary.Project.MDP_Group_9;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import Multidisplinary.Project.MDP_Group_9.R;
import Multidisplinary.Project.MDP_Group_9.Settings.BluetoothActivity;
import Multidisplinary.Project.MDP_Group_9.Fragments.CommunicationFragment;
import Multidisplinary.Project.MDP_Group_9.Settings.BluetoothServices;
import Multidisplinary.Project.MDP_Group_9.Ui.GridMap;
import Multidisplinary.Project.MDP_Group_9.Ui.MapInformation;
import Multidisplinary.Project.MDP_Group_9.Fragments.MapFragment;
import Multidisplinary.Project.MDP_Group_9.Fragments.ControlFragment;
import Multidisplinary.Project.MDP_Group_9.Fragments.ReconfigureFragment;
import Multidisplinary.Project.MDP_Group_9.Settings.SectionsPagerAdapter;

import com.google.android.gms.common.util.JsonUtils;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;
    private final HashMap<Integer, int[]> imageMappings = new HashMap<Integer, int[]>();

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView;
    static Button f1, f2,printimagebut;
    Button reconfigure;
    ReconfigureFragment reconfigureFragment = new ReconfigureFragment();

    BluetoothServices mBluetoothConnection;
    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;


    String expstring, obsstring,imagestringstored;
    private static final String TAG = "Main Activity";

    JSONArray obj = null;
    JSONArray tempimagestring = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(9999);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));
        Bundle getstringfrommapfragment = getIntent().getExtras();
        if (getstringfrommapfragment != null)
        {
            System.out.println("Bundle is not null");

        }
        else if (getstringfrommapfragment == null)
        {
            expstring = null;
            obsstring = null;

            //Toast.makeText(this, "Bundle is null", Toast.LENGTH_SHORT).show();
        }


        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.putString("tempimagestring","");
        editor.commit();


        Button printMDFStringButton = findViewById(R.id.printMDFString);
        printMDFStringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String expstring = getIntent().getStringExtra("Explored");
//                String obsstring = getIntent().getStringExtra("Obstacle");
//                if ((expstring.equals(null)&&obsstring.equals(null)))
//                {
//                    String expmessage = "Explored : "+"null";
//                    String obsmessage = "Obstacle : "+"null";
//                    printMessage(expmessage+"\n"+obsmessage);
//                }
//                else {
                    String expmessage = "Explored : " + expstring;
                    String obsmessage = "Obstacle : " + obsstring;
                    printMessage(expmessage + "\n" + obsmessage);

                    //pass to fragment
                    Bundle bundle = new Bundle();
                    bundle.putString("Explored", expstring);
                    bundle.putString("Obstacle", obsstring);
                    // set Fragmentclass Arguments
                    MapFragment fragobj = new MapFragment();
                    fragobj.setArguments(bundle);
                    //String message = "Explored : " + GridMap.getPublicMDFExploration();
                    // String message = "B:stat:Exploration mdf:" + GridMap.getPublicMDFExploration();
//                editor = sharedPreferences.edit();
//                editor.putString("message","Explored : "+ CommunicationFragment.getMessageReceivedTV().getText()  + expstring+"\n");
//                editor.commit();
                    refreshMessageReceived();
//                message = "Obstacle : " + GridMap.getPublicMDFObstacle() + "0";
//                editor.putString("message", CommunicationFragment.getMessageReceivedTV().getText() + "\n" + message);
//                editor.commit();
//                editor.putString("message","Obstacle : "+ CommunicationFragment.getMessageReceivedTV().getText()  + obsstring+ "\n");
//                editor.commit();
//                refreshMessageReceived();
//                }
            }
        });

        // Toolbar
        Button bluetoothButton = findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(popup);
            }
        });
        Button mapInformationButton = findViewById(R.id.mapInfoButton);
        mapInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("mapJsonObject", String.valueOf(gridMap.getCreateJsonObject()));
                editor.commit();
                Intent popup = new Intent(MainActivity.this, MapInformation.class);
                startActivity(popup);
            }
        });


        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatusTextView);

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        f1 = findViewById(R.id.f1ActionButton);
        f2 = findViewById(R.id.f2ActionButton);
        reconfigure = findViewById(R.id.configureButton);
        printimagebut = findViewById(R.id.print_im_button);

        if (sharedPreferences.contains("F1")) {
            f1.setContentDescription(sharedPreferences.getString("F1", ""));
            showLog("setText for f1Btn: " + f1.getContentDescription().toString());
        }
        if (sharedPreferences.contains("F2")) {
            f2.setContentDescription(sharedPreferences.getString("F2", ""));
            showLog("setText for f2Btn: " + f2.getContentDescription().toString());
        }

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f1Btn");
                if (!f1.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f1.getContentDescription().toString());
//                    refreshMessageReceived();
                showLog("f1Btn value: " + f1.getContentDescription().toString());
                showLog("Exiting f1Btn");
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f2Btn");
                if (!f2.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f2.getContentDescription().toString());
                showLog("f2Btn value: " + f2.getContentDescription().toString());
                showLog("Exiting f2Btn");
            }
        });

        reconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked reconfigureBtn");
                reconfigureFragment.show(getFragmentManager(), "Reconfigure Fragment");
                showLog("Exiting reconfigureBtn");
            }
        });
        printimagebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringBuilder imageString = new StringBuilder();
                imageString.append("{ ");
                for(int id: imageMappings.keySet()){
                    String currImage = "("+ id +","+ imageMappings.get(id)[0] +","+ imageMappings.get(id)[1] +")";
                    imageString.append(currImage);
                    imageString.append(",");
//                    gridMap.drawImageNumberCell(imageMappings.get(id)[0], imageMappings.get(id)[1], id);
                }

//                for(int id: imageMappings.keySet()){
//                    if (imageMappings.get(id)[0]>=0&&imageMappings.get(id)[1]>=0)
//                        gridMap.drawImageNumberCell(imageMappings.get(id)[0], imageMappings.get(id)[1], id);
//                }

                imageString.deleteCharAt(imageString.length() - 1);
                imageString.append(" }");
                String images = imageString.toString();

                editor.putString("message","{image ID, x coordinate, y coordinate} =" + images);
                editor.commit();
                refreshMessageReceived();
            }
        });
    }

    public static Button getF1() { return f1; }

    public static Button getF2() { return f2; }

    public static GridMap getGridMap() {
        return gridMap;
    }

    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Send message to bluetooth
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothServices.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothServices.write(bytes);
        }
        showLog(message);
        editor.putString("message", CommunicationFragment.getMessageReceivedTV().getText() + "\n" + message);
        editor.commit();
        refreshMessageReceived();
        showLog("Exiting printMessage");
    }

    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
//            case "starting":
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = name + "[" + x + "," + y + "]";

                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        editor.putString("message", CommunicationFragment.getMessageReceivedTV().getText() + "\n" + message);
        editor.commit();
        if (BluetoothServices.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothServices.write(bytes);
        }
        showLog("Exiting printMessage");
    }

    public static void refreshMessageReceived() {
        CommunicationFragment.getMessageReceivedTV().setText(sharedPreferences.getString("message", ""));
    }


    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        printMessage("Direction is set to " + direction);
    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message", sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "+mDevice.getName(), Toast.LENGTH_LONG).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "+mDevice.getName(), Toast.LENGTH_LONG).show();
//                mBluetoothConnection = new BluetoothServices(MainActivity.this);
//                mBluetoothConnection.startAcceptThread();

                editor.putString("connStatus", "Disconnected");
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
            String[] septext = message.split("\\$");
            String hextext = "";
            String hextext2 = "";
            for(int u=0; u <septext.length; u++) {
                try {
//                    if (septext[u].length() < 20) {
                        switch (septext[u]) {
                            case "G0":
                            case "B:stat:forward":
                                gridMap.moveRobot("forward");
                                break;
                            case "B:stat:forward:1":
                                gridMap.moveRobot("forward");
                                break;
                            case "B:stat:forward:2":
                                for(int w=0;w<2;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:3":
                                for(int w=0;w<3;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:4":
                                for(int w=0;w<4;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:5":
                                for(int w=0;w<5;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:6":
                                for(int w=0;w<6;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:7":
                                for(int w=0;w<7;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                                case "B:stat:forward:8":
                                for(int w=0;w<8;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                                case "B:stat:forward:9":
                                for(int w=0;w<9;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:10":
                                for(int w=0;w<10;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:11":
                                for(int w=0;w<11;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:12":
                                for(int w=0;w<12;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:13":
                                for(int w=0;w<13;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:14":
                                for(int w=0;w<14;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:15":
                                for(int w=0;w<15;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:16":
                                for(int w=0;w<16;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:17":
                                for(int w=0;w<17;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:18":
                                for(int w=0;w<18;w++)
                                {
                                    gridMap.moveRobot("forward");
                                   // Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:19":
                                for(int w=0;w<19;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:forward:20":
                                for(int w=0;w<20;w++)
                                {
                                    gridMap.moveRobot("forward");
                                    //Thread.sleep(400);
                                }
                                break;
                            case "B:stat:right":
                                gridMap.moveRobot("right");
                                break;
                            case "B:stat:right:1":
                                gridMap.moveRobot("right");
                                break;
                            case "B:stat:left":
                                gridMap.moveRobot("left");
                                break;
                            case "B:stat:left:1":
                                gridMap.moveRobot("left");
                                break;
                            case "B:stat:reverse":
                                gridMap.moveRobot("back");
                                break;
                            case "B:stat:reverse:1":
                                gridMap.moveRobot("back");
                                break;
                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (septext[u].startsWith("B:map:absolute")) {
                        String[] seperatedtext = septext[u].split(":");
                        int x = Integer.parseInt(seperatedtext[3]);
                        int y = Integer.parseInt(seperatedtext[4]);
                        int id = Integer.parseInt(seperatedtext[5]);
                        if(x>=0&&y>=0&&id>=0) {
                            int[] coords = {x, y};
                            System.out.println("x: " + x + " y: " + y + "id: " + id);

                            if (!imageMappings.containsKey(id)) {
                                imageMappings.put(id, coords);
                            }
//                            else {
//                                return;
//                            }
                            else {
                                int[] oldCoords = imageMappings.get(id);
                                imageMappings.put(id, coords);
                                gridMap.setCellType(oldCoords[0], oldCoords[1], "unexplored");
                            }

                            sharedPreferences();
                            obj = new JSONArray();
                            obj.put(id);
                            obj.put(x);
                            obj.put(y);
                            tempimagestring.put(obj);


                            SharedPreferences settings = getSharedPreferences("Shared Preferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            // Reading from SharedPreferences
                            imagestringstored = settings.getString("imagestored", "");
                            // Writing data to SharedPreferences
                            editor.putString("imagestored", String.valueOf(tempimagestring));
                            editor.commit();

                            gridMap.drawImageNumberCell(x, y, id);
                        }
                        else
                        {
                            return;
                        }
                    }
                } catch (Exception e) {
                    showLog("Adding Image Failed");
                }

                try {
                    if (septext[u].startsWith("IM")) {
                        String[] seperatedtext = septext[u].split("\\|");
                        int x = Integer.parseInt(seperatedtext[1]);
                        int y = Integer.parseInt(seperatedtext[2]);
                        int id = Integer.parseInt(seperatedtext[3]);
                        System.out.println("x: "+x + " y: "+ y +"id: "+id);
                        gridMap.drawImageNumberCell(x, y, id);
                    }
                } catch (Exception e) {
                    showLog("Adding Image Failed");
                }
                try {
                    if (septext[u].startsWith("B:stat:Exploration mdf")) {
                   // if (septext[u].substring(0, 99).equalsIgnoreCase("MDF")) {
                        System.out.println("septext"+septext[u]);
                        String[] seperatedtextMDF = septext[u].split(":");
                        System.out.println("seperatedtextMDF"+seperatedtextMDF);
                        hextext = seperatedtextMDF[3];
                        System.out.println("hextext"+hextext);
                        gridMap.mapDescriptorExplored(hextext);
                        expstring =hextext;
                        showLog(hextext);

                    }
                } catch (Exception e) {
                    showLog("Fail to update Map");
                }

                try {
                    if (septext[u].startsWith("B:stat:Obstacle mdf:")) {
                        String[] seperatedtextMDF = septext[u].split(":");
                        hextext2 = seperatedtextMDF[3];
                        gridMap.mapDescriptorObstacle(hextext2);
                        obsstring = String.valueOf(hextext2);
                        //Image recognisation
//                        for(int id: imageMappings.keySet()){
//                            if (imageMappings.get(id)[0]>=0&&imageMappings.get(id)[1]>=0)
//                                gridMap.drawImageNumberCell(imageMappings.get(id)[0], imageMappings.get(id)[1], id);
//                        }
                    }
                } catch (Exception e) {
                    showLog("Fail to update Map");
                }
                if
                (!hextext.equals("") &&!hextext2.equals("")){
                    sharedPreferences();
                    String receivedText3 = sharedPreferences.getString("message", "") +  "ExploredMDF: " + hextext + "\n" + "Obstacle: \n" + hextext2 + "\n\n --------------------------------------------------------------------------------\n";
                    editor.putString("message", receivedText3);
                    editor.commit();
                    refreshMessageReceived();
                }
//                else if(!hextext.equals("")&&hextext2.equals("")){
//                    sharedPreferences();
//                    String receivedText = sharedPreferences.getString("message", "") +  "ExploredMDF: " + hextext  + "\n\n --------------------------------------------------------------------------------\n";
//                    editor.putString("message", receivedText);
//                    editor.commit();
//                    refreshMessageReceived();
//                }
//                else if(!hextext2.equals("") &&hextext.equals("")){
//                    sharedPreferences();
//                    String receivedText2 = sharedPreferences.getString("message", "") + "Obstacle: \n" + hextext2 + "\n\n --------------------------------------------------------------------------------\n";
//                    editor.putString("message", receivedText2);
//                    editor.commit();
//                    refreshMessageReceived();
//                }

                try {
                    if (septext[u].startsWith("B:set:map:[")) {
                        // if (septext[u].substring(0, 99).equalsIgnoreCase("MDF")) {
                        System.out.println("septext"+septext[u]);
                        String[] seperatedtextMDF = septext[u].split(",");
                        System.out.println("seperatedtextMDF"+seperatedtextMDF);
                        hextext = seperatedtextMDF[5];
                        System.out.println("hextext"+hextext);
                        gridMap.mapDescriptorExplored(hextext);
                        showLog(hextext);
                    }
                } catch (Exception e) {
                    showLog("Fail to update Map");
                }

                try {
                    if (septext[u].startsWith("B:stat:waypoint:")) {
                        System.out.println("hextext"+septext[u]);
                        String[] seperatedtextMDF = septext[u].split(":");
                        System.out.println("seperatedtextMDF"+seperatedtextMDF);
                        int hextext3 =Integer.parseInt(seperatedtextMDF[3]);
                       int hextext4 = Integer.parseInt(seperatedtextMDF[4]);
                       hextext3++;
                       hextext4++;
                        System.out.println("hextext3"+(hextext3));
                        System.out.println("hextext4"+(hextext4));
                       gridMap.setWaypointCoordwo((hextext3),(hextext4));
                        gridMap.updateMap();
                    }
                } catch (Exception e) {
                    showLog("Fail to update Map");
                }


            }// inside of the array loop[u]

//            try {
//                if (message.length() > 7 && message.startsWith(" mdf", 2)) {
//                    String resultString = "";
//                    String amdString = message.substring(11,message.length()-2);
//                    showLog("amdString: " + amdString);
//                    BigInteger hexBigIntegerExplored = new BigInteger(amdString, 16);
//                    String exploredString = hexBigIntegerExplored.toString(2);
//                    while (exploredString.length() < 300)
//                        exploredString = "0" + exploredString;
//
//                    for (int i=0; i<exploredString.length(); i=i+15) {
//                        int j=0;
//                        String subString = "";
//                        while (j<15) {
//                            subString = subString + exploredString.charAt(j+i);
//                            j++;
//                        }
//                        resultString = subString + resultString;
//                    }
//                    hexBigIntegerExplored = new BigInteger(resultString, 2);
//                    resultString = hexBigIntegerExplored.toString(16);
//
//                    JSONObject amdObject = new JSONObject();
//                    amdObject.put("explored", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//                    amdObject.put("length", amdString.length()*4);
//                    amdObject.put("obstacle", resultString);
////                    amdObject.put("B:stat:Exploration mdf:", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
////                    amdObject.put("length", amdString.length()*4);
////                    amdObject.put("B:stat:obstacle mdf:", resultString);
//                    JSONArray amdArray = new JSONArray();
//                    amdArray.put(amdObject);
//                    JSONObject amdMessage = new JSONObject();
//                    amdMessage.put("map", amdArray);
//                    message = String.valueOf(amdMessage);
//                    showLog("Executed for AMD message, message: " + message);
//
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            try {
                if (message.length() > 8 && message.startsWith("image", 2)) {
                    JSONObject jsonObject = new JSONObject(message);
                    JSONArray jsonArray = jsonObject.getJSONArray("image");
                    gridMap.drawImageNumberCell(jsonArray.getInt(0),jsonArray.getInt(1),jsonArray.getInt(2));
                    showLog("Image Added for index: " + jsonArray.getInt(0) + "," +jsonArray.getInt(1));
                }
            } catch (JSONException e) {
                showLog("Adding Image Failed");
            }

            if (gridMap.getAutoUpdate() || MapFragment.manualUpdateRequest) {
                try {
                    gridMap.setReceivedJsonObject(new JSONObject(message));
                    gridMap.updateMapInformation();
                    MapFragment.manualUpdateRequest = false;
                    showLog("messageReceiver: try decode successful");
                } catch (JSONException e) {
                    showLog("messageReceiver: try decode unsuccessful");
                }
            }
            sharedPreferences();
            String receivedText = sharedPreferences.getString("message", "") + "\n" + message;
            editor.putString("message", receivedText);
            editor.commit();
            refreshMessageReceived();

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}