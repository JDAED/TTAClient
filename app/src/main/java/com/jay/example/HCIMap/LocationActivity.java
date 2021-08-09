package com.jay.example.HCIMap;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.media.MediaPlayer;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

import android.graphics.Point;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


public class LocationActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private Context currentContext;
    private Menu appMenu;

    private ImageView imageHciMap;
    private int mapDirection = 0;
    private ImageButton buildingButton;
    private TextView buildingName;
    private boolean buildingShown = false;
    private ImageButton peopleButton;
    private TextView buildingName2;

    private int screenWidth;
    private int screenHeight;

    private int mapViewWidth;
    private int mapViewHeight;

    private final float mapLeftDownX = (float) 103.57939;
    private final float mapLeftDownY = (float) 1.21837;
    private final float mapRightUpX = (float) 104.05936;
    private final float mapRightUpY = (float) 1.47099;

    private final float mapCenterX = (float) ((mapLeftDownX + mapRightUpX) / 2.0);
    private final float mapCenterY = (float) ((mapLeftDownY + mapRightUpY) / 2.0);

    private final float mapPhysicalWidth = mapRightUpX - mapLeftDownX;
    private final float mapPhysicalHeight = mapRightUpY - mapLeftDownY;

    private final int mapImageWidth = 1280;
    private final int mapImageHeight = 672;
    private float mapScaleFactor = 1.0f;

    private int mapShownWidth;
    private int mapShownHeight;

    private int mapShownLeftX;
    private int mapShownLeftY;

    Date previousDate = new Date();

    Canvas canvas = new Canvas();
    private Location buildingCenterLocation = new Location("");

    private class Pointf {
        private float x;
        private float y;

        private Pointf() {
            x = 0;
            y = 0;
        }

        private Pointf(float i_x, float i_y) {
            x = i_x;
            y = i_y;
        }
    }

    private int buildingDataCounter;
    private int buildingIndexSelected = 0;
    private int buildingPeopleReached = -1;
    private int buildingReminding = -1;

    private String[] buildingNameArray;
    private Pointf[] locationArray = new Pointf[250];
    private int totalLocation = 0;
    private int[] buildingIndex = new int[25];
    private Pointf[] buildingCornerA = new Pointf[25];
    private Pointf[] buildingCornerB = new Pointf[25];
    private Pointf[] buildingCornerC = new Pointf[25];
    private Pointf[] buildingCornerD = new Pointf[25];

    private Pointf[] buildingCenter = new Pointf[25];

    private int buildingButtonHalfWidth;
    private int buildingButtonHeight;
    private Animation buildingButtonAnimation = new AlphaAnimation(0.0f, 1.0f);
    private Animation buildingNameAnimation = new AlphaAnimation(0.0f, 1.0f);

    private int peopleButtonHalfWidth;
    private int peopleButtonHeight;
    private Animation peopleButtonAnimation = new AlphaAnimation(0.0f, 1.0f);

    private TextToSpeech textToSpeech;
    private boolean textToSpeechSupport = false;
    private int textToSpeechStatus = 0;
    private int textToSpeechInitDone = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hci_location);

        if (getResources().getDisplayMetrics().widthPixels < getResources().getDisplayMetrics().heightPixels) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        currentContext = this;

        buildingNameArray = getResources().getStringArray(R.array.building_name_list);
        String[] builingCenterArray = getResources().getStringArray(R.array.building_center_list);
        for (int i = 0; i < buildingNameArray.length; i++) {
            String[] parts = builingCenterArray[i].split(";");

            buildingCenter[i] = new Pointf();

            buildingCenter[i].y = Float.parseFloat(parts[1].trim());
            buildingCenter[i].x = Float.parseFloat(parts[2].trim());
        }

        String[] builingDataArray = getResources().getStringArray(R.array.building_data_list);
        buildingDataCounter = builingDataArray.length;
        for (int i = 0; i < buildingDataCounter; i++) {
            String[] parts = builingDataArray[i].split(";");
            buildingIndex[i] = Integer.parseInt(parts[0].trim());

            buildingCornerA[i] = new Pointf();
            buildingCornerB[i] = new Pointf();
            buildingCornerC[i] = new Pointf();
            buildingCornerD[i] = new Pointf();

            buildingCornerA[i].y = Float.parseFloat(parts[1].trim());
            buildingCornerA[i].x = Float.parseFloat(parts[2].trim());
            buildingCornerB[i].y = Float.parseFloat(parts[3].trim());
            buildingCornerB[i].x = Float.parseFloat(parts[4].trim());
            buildingCornerC[i].y = Float.parseFloat(parts[5].trim());
            buildingCornerC[i].x = Float.parseFloat(parts[6].trim());
            buildingCornerD[i].y = Float.parseFloat(parts[7].trim());
            buildingCornerD[i].x = Float.parseFloat(parts[8].trim());
        }

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        buildingCenterLocation.setLongitude((double) mapCenterX);
        buildingCenterLocation.setLatitude((double) mapCenterY);

        imageHciMap = findViewById(R.id.imageHciMap);
        imageHciMap.getViewTreeObserver().addOnGlobalLayoutListener(new MyGlobalListenerClass());

        buildingButtonAnimation.setDuration(500);
        buildingButtonAnimation.setStartOffset(20);
        buildingButtonAnimation.setRepeatMode(Animation.REVERSE);
        buildingButtonAnimation.setRepeatCount(Animation.INFINITE);

        buildingNameAnimation.setDuration(500);
        buildingNameAnimation.setStartOffset(20);
        buildingNameAnimation.setRepeatMode(Animation.REVERSE);
        buildingNameAnimation.setRepeatCount(Animation.INFINITE);

        peopleButtonAnimation.setDuration(500);
        peopleButtonAnimation.setStartOffset(20);
        peopleButtonAnimation.setRepeatMode(Animation.REVERSE);
        peopleButtonAnimation.setRepeatCount(Animation.INFINITE);

        buildingButton = findViewById(R.id.buildingButton);
        buildingButton.setVisibility(android.widget.ImageButton.INVISIBLE);
        buildingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingButtonAnimation.cancel();
                buildingNameAnimation.cancel();
                buildingShown = false;
            }
        });

        buildingName = findViewById(R.id.buildingName);
        buildingName.setText("");
        buildingName.setVisibility(android.widget.ImageButton.INVISIBLE);
        buildingName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingButtonAnimation.cancel();
                buildingNameAnimation.cancel();
                buildingShown = false;
            }
        });

        peopleButton = findViewById(R.id.peopleButton);
        peopleButton.setVisibility(android.widget.ImageButton.INVISIBLE);

        buildingName2 = findViewById(R.id.buildingName2);
        buildingName2.setText("");
        buildingName2.setVisibility(android.widget.ImageButton.INVISIBLE);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechInitDone = 1;
                    int ttsLang = textToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeechSupport = false;
                    } else {
                        textToSpeechSupport = true;
                    }
                } else {
                    textToSpeechInitDone = -1;
                    textToSpeechSupport = false;
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(LocationActivity.this, "Please open GPS!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
        }

        try {
            showPeoplePosition(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListenerGPS);

        } catch (
                Exception ex) {
            enableLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(locationListenerGPS);
            } catch (Exception ex) {
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        appMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon_rotation_180:
                switch (mapDirection) {
                    case 0:
                        mapDirection = 180;
                        imageHciMap.setImageResource(R.drawable.hcimap180);
                        if (buildingShown)
                            showBuilding(buildingIndexSelected);
                        showPeoplePosition(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        break;
                    case 180:
                        mapDirection = 0;
                        imageHciMap.setImageResource(R.drawable.hcimap);
                        if (buildingShown)
                            showBuilding(buildingIndexSelected);
                        showPeoplePosition(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        break;
                    default:
                        break;
                }
                break;
            case R.id.icon_search:
            {
                showPeopleRoute();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.icon_search).setEnabled(false);

        return super.onPrepareOptionsMenu(menu);
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showPeoplePosition(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            // 当GPS LocationProvider可用时，更新定位
            showPeoplePosition(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
            showPeoplePosition(null);
        }
    };

    class MyGlobalListenerClass implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {

            buildingButtonHalfWidth = (int) (buildingButton.getWidth() / 2);
            buildingButtonHeight = (int) (buildingButton.getHeight());

            peopleButtonHalfWidth = (int) (peopleButton.getWidth() / 2);
            peopleButtonHeight = (int) (peopleButton.getHeight());

            mapViewWidth = imageHciMap.getWidth();
            mapViewHeight = imageHciMap.getHeight();

            if ((mapViewWidth * 1.0 / mapViewHeight) > (mapImageWidth * 1.0 / mapImageHeight)) {
                mapShownWidth = mapImageWidth * mapViewHeight / mapImageHeight;
                mapShownHeight = mapViewHeight;

                mapShownLeftX = (mapViewWidth - mapShownWidth) / 2;
                mapShownLeftY = 0;
            } else {
                if ((mapViewWidth * 1.0 / mapViewHeight) < (mapImageWidth * 1.0 / mapImageHeight)) {
                    mapShownWidth = mapViewWidth;
                    mapShownHeight = mapImageHeight * mapViewWidth / mapImageWidth;

                    mapShownLeftX = 0;
                    mapShownLeftY = (mapViewHeight - mapShownHeight) / 2;
                } else {
                    mapShownWidth = mapViewWidth;
                    mapShownHeight = mapViewHeight;

                    mapShownLeftX = 0;
                    mapShownLeftY = 0;
                }
            }
        }
    }


    private void showPeoplePosition(Location location) {
        if (location != null && mapViewWidth > 0 && mapViewHeight > 0) {
            Pointf currentPointf = new Pointf((float) location.getLongitude(), (float) location.getLatitude());

            if (currentPointf.x < mapLeftDownX || currentPointf.x > mapRightUpX || currentPointf.y < mapLeftDownY || currentPointf.y > mapRightUpY) {
                float currentMapScaleFactor = (float) (Math.min(mapPhysicalWidth / Math.abs(currentPointf.x - mapCenterX), mapPhysicalHeight / Math.abs(currentPointf.y - mapCenterY)) / 2.0);
                currentMapScaleFactor = Math.max(currentMapScaleFactor, (float) 0.05);

                if (currentMapScaleFactor != mapScaleFactor) {
                    if (mapScaleFactor == (float) 1.0) {
                        mapScaleFactor = currentMapScaleFactor;
                        invalidateOptionsMenu();
                    } else
                        mapScaleFactor = currentMapScaleFactor;

                    imageHciMap.setScaleX(mapScaleFactor);
                    imageHciMap.setScaleY(mapScaleFactor);
                }
                Point screenPoint = locationToScreen(currentPointf);

                Date currentDate = new Date();
                long different = currentDate.getTime() - previousDate.getTime();

                if (different / 1000 >= 20) {
                    locationArray[totalLocation] = currentPointf;
                    previousDate = currentDate;
                    totalLocation += 1;
                }

                screenPoint.x = screenPoint.x - peopleButtonHalfWidth;
                screenPoint.y = screenPoint.y - peopleButtonHeight;

                peopleButton.setX(screenPoint.x);
                peopleButton.setY(screenPoint.y);
                peopleButton.startAnimation(peopleButtonAnimation);
            } else {
                if (mapScaleFactor != (float) 1.0) {
                    mapScaleFactor = (float) 1.0;
                    invalidateOptionsMenu();

                    imageHciMap.setScaleX(mapScaleFactor);
                    imageHciMap.setScaleY(mapScaleFactor);
                }

                Point screenPoint = locationToScreen(currentPointf);
                Date currentDate = new Date();
                long different = currentDate.getTime() - previousDate.getTime();

                if (different / 1000 >= 20) {
                    locationArray[totalLocation] = currentPointf;
                    previousDate = currentDate;
                    totalLocation += 1;
                }
                screenPoint.x = screenPoint.x - peopleButtonHalfWidth;
                screenPoint.y = screenPoint.y - peopleButtonHeight;

                peopleButton.setX(screenPoint.x);
                peopleButton.setY(screenPoint.y);
                peopleButton.startAnimation(peopleButtonAnimation);
            }
        } else {
             peopleButtonAnimation.cancel();
            peopleButton.setVisibility(android.widget.ImageButton.INVISIBLE);
        }
    }

    private void showPeopleRoute() {
        Paint paint = new Paint();

        int i;
        paint.setColor(Color.BLACK);
        Point firstPoint = locationToScreen(locationArray[0]);
        Point secondPoint;
       for (i = 1; i < totalLocation; i++) {
           secondPoint = locationToScreen(locationArray[i]);
           canvas.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, paint);
           firstPoint = secondPoint;
        }
    }

    private void showBuilding(int i_buildingIndex) {
        if (i_buildingIndex >= 0 && mapScaleFactor == (float) 1.0) {
            Point screenPoint = locationToScreen(buildingCenter[i_buildingIndex]);

            screenPoint.x = screenPoint.x - buildingButtonHalfWidth;
            screenPoint.y = screenPoint.y - buildingButtonHeight;

            buildingButton.setX(screenPoint.x);
            buildingButton.setY(screenPoint.y);
            buildingName.setX(screenPoint.x);
            buildingName.setY(screenPoint.y + buildingButtonHeight);
            buildingName.setText(buildingNameArray[i_buildingIndex]);

            buildingName.startAnimation(buildingNameAnimation);
            buildingButton.startAnimation(buildingButtonAnimation);
            buildingShown = true;
            if (i_buildingIndex == buildingPeopleReached && i_buildingIndex != buildingReminding) {
                reminder(i_buildingIndex);
            } else if (i_buildingIndex != buildingPeopleReached) {
                buildingReminding = -1;
            }
        } else {
            buildingButtonAnimation.cancel();
            buildingNameAnimation.cancel();
            buildingShown = false;
            buildingReminding = -1;
        }
    }

    private void reminder(int i_buildingIndex) {
        if (textToSpeechInitDone == 0)
            return;

        buildingReminding = i_buildingIndex;

        final MediaPlayer reminderVoice = MediaPlayer.create(this, R.raw.reminder);

        if (textToSpeechSupport)
            textToSpeechStatus = textToSpeech.speak("You have reached your destination: " + buildingNameArray[i_buildingIndex], TextToSpeech.QUEUE_FLUSH, null, null);

        if (!textToSpeechSupport || textToSpeechStatus == TextToSpeech.ERROR)
            reminderVoice.start();

        new AlertDialog.Builder(LocationActivity.this)
                .setTitle("Reminder")
                .setMessage("You have reached your destination: " + buildingNameArray[i_buildingIndex])
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!textToSpeechSupport || textToSpeechStatus == TextToSpeech.ERROR) {
                            reminderVoice.stop();
                            reminderVoice.reset();
                        }
                    }
                }).show();
    }

    private Point locationToScreen(Pointf i_location) {
        Point o_point = new Point();

        int toLeftDownDistanceX = (int) Math.floor((i_location.x - mapLeftDownX) / mapPhysicalWidth * mapShownWidth);
        int toLeftDownDistanceY = (int) Math.floor((i_location.y - mapLeftDownY) / mapPhysicalHeight * mapShownHeight);

        int imageLeftDownX = toLeftDownDistanceX;
        int imageLeftDownY = toLeftDownDistanceY;

        switch (mapDirection) {
            case 0:
                break;
            case 180:
                imageLeftDownX = mapShownWidth - imageLeftDownX;
                imageLeftDownY = mapShownHeight - imageLeftDownY;
                break;
            default:
                break;
        }

        imageLeftDownX = imageLeftDownX + mapShownLeftX;
        imageLeftDownY = imageLeftDownY + mapShownLeftY;

        o_point.x = Math.max(Math.min(imageLeftDownX, mapViewWidth), 0);
        o_point.y = Math.max(Math.min(mapViewHeight - imageLeftDownY, mapViewHeight), 0);
        return o_point;
    }

    private Pointf vector2Points(Pointf i_firstPoint, Pointf i_secondPoint) {
        Pointf O_pointf = new Pointf();
        O_pointf.x = (i_secondPoint.x - i_firstPoint.x);
        O_pointf.y = -1 * (i_secondPoint.y - i_firstPoint.y);
        return O_pointf;
    }

    private boolean pointInRectangle(Pointf i_PointfA, Pointf i_PointfB, Pointf
            i_PointfC, Pointf i_PointfD, Pointf i_PointfM) {
        Pointf AB = vector2Points(i_PointfA, i_PointfB);
        float C1 = -1 * (AB.y * i_PointfA.x + AB.x * i_PointfA.y);
        float D1 = (AB.y * i_PointfM.x + AB.x * i_PointfM.y) + C1;

        Pointf AD = vector2Points(i_PointfA, i_PointfD);
        float C2 = -1 * (AD.y * i_PointfA.x + AD.x * i_PointfA.y);
        float D2 = (AD.y * i_PointfM.x + AD.x * i_PointfM.y) + C2;

        Pointf BC = vector2Points(i_PointfB, i_PointfC);
        float C3 = -1 * (BC.y * i_PointfB.x + BC.x * i_PointfB.y);
        float D3 = (BC.y * i_PointfM.x + BC.x * i_PointfM.y) + C3;

        Pointf CD = vector2Points(i_PointfC, i_PointfD);
        float C4 = -1 * (CD.y * i_PointfC.x + CD.x * i_PointfC.y);
        float D4 = (CD.y * i_PointfM.x + CD.x * i_PointfM.y) + C4;

        return 0 >= D1 && 0 >= D4 && 0 <= D2 && 0 >= D3;
    }

    private void enableLocation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(currentContext);
        alertDialog.setTitle("Enable Location");
        alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
        alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
