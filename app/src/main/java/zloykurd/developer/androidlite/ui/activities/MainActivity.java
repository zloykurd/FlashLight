package zloykurd.developer.androidlite.ui.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.util.List;

import zloykurd.developer.androidlite.R;

public class MainActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener, CompoundButton.OnCheckedChangeListener {

    private int sound;
    private SoundPool soundPool;
    private Camera camera;
    Parameters parameters;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createSoundPoolWithBuilder();
        } else {
            createSoundPoolWithConstructor();
        }

        soundPool.setOnLoadCompleteListener(this);
        sound = soundPool.load(this, R.raw.click, 1);
        mySwitch = (Switch) findViewById(R.id.switchBtn);
        mySwitch.setChecked(false);
        mySwitch.setOnCheckedChangeListener(this);

        boolean isCameraFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isCameraFlash) {
            showCameraAlert();
        } else {
            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showCameraAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_text)
                .setPositiveButton(R.string.exit_message, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createSoundPoolWithBuilder() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
    }

    @SuppressWarnings("deprecation")
    protected void createSoundPoolWithConstructor() {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    }

    private void setFlashLigthOn() {
        soundPool.play(sound, 1, 1, 0, 0, 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    parameters = camera.getParameters();

                    if (parameters != null) {
                        List supportedFlashModes = parameters.getSupportedFlashModes();

                        try {
                            if (supportedFlashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                                camera.setParameters(parameters);
                            } else if (supportedFlashModes.contains(Parameters.FLASH_MODE_ON)) {
                                parameters.setFlashMode(Parameters.FLASH_MODE_ON);
                                camera.setParameters(parameters);
                            } else camera = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (camera != null) {
                            try {
                                camera.setParameters(parameters);
                                camera.startPreview();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                camera.setPreviewTexture(new SurfaceTexture(0));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void setFlashLightOff() {
        soundPool.play(sound, 1, 1, 0, 0, 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    camera.stopPreview();
                }
            }
        }).start();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* mySwitch.setChecked(true);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
       /* releaseCamera();
        mySwitch.setChecked(false);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mySwitch.setChecked(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);
        } else{
            if (camera == null) {
                camera = Camera.open();
                camera.getParameters();
            } else{
                setFlashLigthOn();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        finish();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            setFlashLigthOn();
        } else {
            setFlashLightOff();
        }
    }
}