package com.example.voca;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.example.voca.List.TabActivity;
import com.example.voca.authentication.SignOut;
import com.example.voca.drawerActivity.DarkModeActivity;
import com.example.voca.drawerActivity.EditAccountActivity;
import com.example.voca.drawerActivity.GoalSettingActivity;
import com.example.voca.drawerActivity.NoticeActivity;
import com.example.voca.realtimeDB.GoalData;
import com.firebase.ui.auth.IdpResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private IdpResponse mIdpResponse;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabaseReference; // ????????????????????? ?????? ??????
    private DatabaseReference mDatabase;

    ArrayList<GoalData> goalData; // ??????,??????,??????????????? ?????? ?????????

    BarDataSet dataSet1;
    BarDataSet dataSet;

    CircleProgressBar dailyChart;
    TextView dayRecord;

    Map<String, Object> mapKey;
    Map<String, Object> innerMap;

    int j=0;

    String today;

    BarChart weekChart;
    BarData bardata;
    XAxis xAxis;

    private ActionBarDrawerToggle toggle;//?????? ????????? ?????? ??????

    private SwitchCompat darkModeSwitch;//???????????? ????????? ??????

    String themeColor;

    int goal;
    int achievement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIdpResponse = IdpResponse.fromResultIntent(getIntent());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String uid = user.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance(); // ????????? DB ?????? ?????? ????????????
        mDatabaseReference = firebaseDatabase.getReference("users"); // ?????? ??????????????? ???????????? ????????? ????????? ??????
        mDatabase = mDatabaseReference.child(uid).child("goals"); // DB/users/uid/goals
        Log.d(TAG, "users" + mDatabase);


        //???????????? ??????
        themeColor = DarkModeUtil.modLoad(getApplicationContext());
        DarkModeUtil.applyTheme(themeColor);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //???????????? ?????????
        TextView mainText=findViewById(R.id.text_main);
        mainText.setText((TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName())+"?????? ?????? ?????????");

        /*Button testBtn=findViewById(R.id.btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VocaListActivity.class));
            }
        });*/

        goalData = new ArrayList<>();

        // ????????? ?????? ??? ?????? -> DB/users/uid/goals
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            // ????????? ?????? ????????? ?????? ????????? ?????? ???????????? ?????? ???????????? ??????
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // ?????? ??????
                today = getToday();

                boolean isExist = false;

                Log.d(TAG, "addValueEventListener: getChildren() " + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (today.equals(snapshot.getKey())) {
                        isExist = true;
                        break;
                    }
                }

                Log.d(TAG, "TF: " + isExist);

                if (!isExist) {
                    int goal = 20;
                    String yesterday = String.valueOf(Integer.parseInt(dataSnapshot.getKey()) - 1);
                    Log.d(TAG, "yesterday: " + yesterday);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (yesterday.equals(snapshot.getKey())) {
                            goal = dataSnapshot.child(yesterday).getValue(Integer.class); // ?????? ???????????? ????????? ????????? ????????? ????????? ??????
                            Log.d(TAG, "goal: " + goal);
                        }

                    }

                    mapKey = new HashMap<>();
                    innerMap = new HashMap<>();

                    mapKey.put(today, innerMap);

                    innerMap.put("count", 0);
                    innerMap.put("goal", goal);

                    Log.d(TAG, "innerMap: " + innerMap);

                    mDatabase.child(today).updateChildren(innerMap);
                }

                for (DataSnapshot snapshot_data : dataSnapshot.getChildren()) { // date
                    int i = 0;
                    GoalData data = new GoalData();

                    data.setDate(snapshot_data.getKey());
                    // Log.d(TAG, "onDataChange: data " + );

                    for (DataSnapshot snapshot : snapshot_data.getChildren()) {
                        switch (i) {
                            case 0:
                                data.setCount(snapshot.getValue().toString());
                                break;

                            case 1:
                                data.setGoal(snapshot.getValue().toString());
                                break;
                        }
                        i++;

                    }
                    goalData.add(data);
                    Log.d(TAG, "onDataChange: goalData  "+j+"?????? " + goalData.get(j).getGoal());
                    Log.d(TAG, "onDataChange: goalData  " +j+"?????? " + goalData.get(j).getCount());
                    Log.d(TAG, "onDataChange: goalData  " +j+"?????? " + goalData.get(j).getDate());
                    j++;
                }
                goal=Integer.parseInt(goalData.get(goalData.size()-1).getGoal());
                achievement=Integer.parseInt(goalData.get(goalData.size()-1).getCount());
                initData();
                edit_goal();
            }

            // ????????? ??????
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //?????? ??????
        dailyChart=findViewById(R.id.circle_bar);
        dayRecord=findViewById(R.id.day_record);
        edit_goal();
        //?????? ?????? ????????? ?????? ????????????
        HorizontalScrollView horizonScroll=findViewById(R.id.horizon_scroll);
        //?????????????????? ????????? ?????? ??????
        horizonScroll.post(new Runnable() {
            @Override
            public void run() {
                horizonScroll.fullScroll(horizonScroll.FOCUS_RIGHT);
            }
        });

        //?????? ?????? ??????
       // weekChart=findViewById(R.id.bar_chart);
        weekChart = (BarChart) findViewById(R.id.bar_chart);


        //achivements=new ArrayList<BarEntry>();    //?????? ?????????
        //goals=new ArrayList<>();     //?????? ?????????
        //date=new ArrayList<>(); // ??????

        initData(); // ?????? ??????





        //?????? ???????????? ?????? ????????? ???????????? ???
        Button learningBtn=findViewById(R.id.learning_btn);
        learningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TabActivity.class));
            }
        });


        /*---------------------??????????????? NavigationView code---------------------*/
        // drawer icon ????????? ??????
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawerLayout = findViewById(R.id.main_drawer);// drawerLayout
        NavigationView navigationView = findViewById(R.id.main_drawer_view);//navigationView

        /* to tie together the functionality of DrawerLayout and the framework ActionBar
        * ?????? ?????? ?????????????????? ?????? ???????????? ????????? ???????????? ??????
        * */
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(toggle); // If omitted, the toggle icon is not changed.

        //???????????? ????????? ????????? dialog + ???????????????/
        SignOut signout = new SignOut(this);
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if(dialog==alertDialog && which==DialogInterface.BUTTON_POSITIVE)
                signout.signOut();
            }
        };

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            /*??????????????? ?????? ?????? ?????? ???*/
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id;
                id = item.getItemId();
                switch (id) {
                    case R.id.menu_drawer_editAccountInformation:
                        Intent intent1=new Intent(MainActivity.this, EditAccountActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.menu_drawer_notice:
                        Intent intent2=new Intent(MainActivity.this, NoticeActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.menu_drawer_goalSetting:
                        Intent intent3=new Intent(MainActivity.this, GoalSettingActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.menu_drawer_darkMode:
                        Intent intent4=new Intent(MainActivity.this, DarkModeActivity.class);
                        startActivity(intent4);
                        break;
                    case R.id.menu_drawer_logout:
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setTitle("??????");
                        builder.setMessage("???????????? ???????????????????");
                        builder.setPositiveButton("OK", dialogListener);
                        builder.setNegativeButton("No",null);
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();
                        break;
                }
                return true;
            }
        });

        //???????????? ???????????????
        darkModeSwitch=(SwitchCompat) navigationView.getMenu().findItem(R.id.menu_drawer_darkMode).getActionView().findViewById(R.id.switch_dark_mode);
        if(themeColor.equals(DarkModeUtil.DARK_MODE))
            darkModeSwitch.setChecked(true);
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    showToast("???????????? off");
                    themeColor=DarkModeUtil.LIGHT_MODE;
                    DarkModeUtil.applyTheme(themeColor);
                    DarkModeUtil.modSave(getApplicationContext(),themeColor);
                }
                else {
                    showToast("???????????? on");
                    themeColor=DarkModeUtil.DARK_MODE;
                    DarkModeUtil.applyTheme(themeColor);
                    DarkModeUtil.modSave(getApplicationContext(),themeColor);
                }
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
    // Whenever the Up button is clicked, this method is called
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// Pass the event to ActionBarDrawerToggle,
// if it returns true, then it has handled the Up button touch event.

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
// Handle other action bar items.
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }
    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message,Toast.LENGTH_SHORT);
        toast.show();
    }


    // ?????? ?????? ??????
    private String getToday() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);

        String today = sdf.format(date);
        Log.d(TAG, "today: " + today);

        return today;
    }



    // ?????? ????????? ?????? ???????????? ????????? ?????? ???
    private void initData()
    {
        Log.d(TAG, "goalData.size(): " + goalData.size());
        for(int i=0;i<goalData.size();i++) {
            Log.d(TAG, "goalData " + goalData.get(i).getGoal());
            Log.d(TAG, "goalData " + goalData.get(i).getCount());
            Log.d(TAG, "goalData " + goalData.get(i).getDate());
        }

        List<BarEntry> achivements;
        List<BarEntry> goals;
        List<String> date;

        achivements=new ArrayList<>();    //?????? ?????????
        goals=new ArrayList<>();     //?????? ?????????
        date=new ArrayList<>(); // ??????

        //?????? ?????????
        for(int i=0;i<goalData.size();i++){
            achivements.add(new BarEntry(i,Integer.parseInt(goalData.get(i).getCount())));
            Log.d(TAG, "achivement: " + achivements);
        }
        dataSet1=new BarDataSet(achivements,"?????? ?????????");

        //?????? ?????????
        for(int i=0;i<goalData.size();i++){
            goals.add(new BarEntry(i,Integer.parseInt(goalData.get(i).getGoal())));
            Log.d(TAG, "goals: " + goals);
        }
        dataSet=new BarDataSet(goals,"??????");

        //????????? ????????? ??????

        bardata=new BarData(dataSet,dataSet1);

        for(int i=0;i<goalData.size();i++){
            date.add(goalData.get(i).getDate());
            Log.d(TAG, "date: " + date);
        }


        xAxis=weekChart.getXAxis();
        dataSet1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet1.setColor(ColorTemplate.JOYFUL_COLORS[1]);

        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setColor(R.color.theme1);
        dataSet.setFormLineWidth(10f);

        bardata.setBarWidth(0.4f);//?????? ??????
        bardata.setValueTextSize(15);

        //?????? ????????? ??????(????????? ????????? ??????)
        xAxis.setLabelCount(goalData.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(date));
        weekChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        weekChart.getDescription().setEnabled(false);
        weekChart.getXAxis().setDrawGridLines(false);
        weekChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weekChart.getAxisLeft().setDrawGridLines(false);
        weekChart.getAxisRight().setDrawGridLines(false);
        weekChart.getAxisRight().setEnabled(false);
        weekChart.getAxisLeft().setEnabled(false);
        weekChart.setDrawGridBackground(false);

        xAxis.setAxisLineWidth(10);
        xAxis.setTextSize(15);
        weekChart.setTouchEnabled(false);
        weekChart.setDrawValueAboveBar(false);
        weekChart.setFitBars(true);

        weekChart.setData(bardata);

        weekChart.invalidate();


    }

    // ????????? ?????? ?????? ?????? ??? ?????? ?????? ??????
    private void edit_goal()
    {
        if(goal!=0)
            dailyChart.setProgress((int)((float)achievement/goal*100));
        else dailyChart.setProgress(0);
        dayRecord.setText(achievement+"/"+goal);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}