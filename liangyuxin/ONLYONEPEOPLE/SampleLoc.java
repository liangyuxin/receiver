package com.example.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
//import android.graphics.Camera;
import android.hardware.Camera;
import android.location.*;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
//import android.widget.Toast;
//import android.widget.TextView;



@SuppressLint("SimpleDateFormat") public class SampleLoc extends Activity implements LocationListener {
	//GPSの閾値設定
	public static final int GPS_Range = 1;
	public static final int GPS_Time = 123;
	//GPS取得関係
	private LocationManager lm;
	//アンドロイド間通信
	private PendingIntent pi;
	// カメラインスタンス
    private Camera mCam = null;
    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;
    // 開始ボタンの2度押し禁止用フラグ
    private boolean mIsTake = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_loc);
        
        // ロケーションマネージャのインスタンスを取得する
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                   
        // カメラインスタンスの取得
        try {
            mCam = Camera.open();
        } catch (Exception e) {
            // エラー
            this.finish();
        }
        // FrameLayout に CameraPreview クラスを設定
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);

        //UI設定
        Button btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ローケーション取得条件の設定
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setSpeedRequired(false);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
                // 位置情報の更新を受け取るように設定
                lm.requestLocationUpdates(
                		LocationManager.GPS_PROVIDER, // プロバイダ
                GPS_Time, // 通知のための最小時間間隔
                GPS_Range, // 通知のための最小距離間隔
                new LocationListener(){// 位置情報リスナー,thisはバグが多いhttp://stackoverflow.com/questions/17119968/android-locationmanager-requestlocationupdates-doesnt-work
                	@Override
                	public void onStatusChanged(String provider, int status, Bundle extras) {
                        if (!mIsTake) {
                        	// 撮影中の2度押し禁止用フラグ
                        	mIsTake = true;
                        	// 画像取得
                        	mCam.takePicture(null, null, mPicJpgListener);
                        }
                	}
            	    @Override
            	    public void onProviderEnabled(String provider) {
                        if (!mIsTake) {
                        	// 撮影中の2度押し禁止用フラグ
                        	mIsTake = true;
                        	// 画像取得
                        	mCam.takePicture(null, null, mPicJpgListener);
                        }
            	    }
            	    @Override
            	    public void onProviderDisabled(String provider) {
                        if (!mIsTake) {
                        	// 撮影中の2度押し禁止用フラグ
                        	mIsTake = true;
                        	// 画像取得
                        	mCam.takePicture(null, null, mPicJpgListener);
                        }
            	    }
            	    @Override
            	    public void onLocationChanged(final Location location) {
                        if (!mIsTake) {
                        	// 撮影中の2度押し禁止用フラグ
                        	mIsTake = true;
                        	// 画像取得
                        	mCam.takePicture(null, null, mPicJpgListener);
                        }
            	    }
                }); 
                                
                /*送信設定
                //PendingIntentの生成
                Intent nextIntent = new Intent(this, ReceiveLocation.class);
                pi = PendingIntent.getBroadcast(this, 0x432f, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
         
                lm.requestLocationUpdates(1000, 1, criteria, pi);
            	*/
            	
            	//レシーバー側設定
                /*
                if (!mIsTake) {
                	// 撮影中の2度押し禁止用フラグ
                	mIsTake = true;
                	// 画像取得
                	mCam.takePicture(null, null, mPicJpgListener);
                }
                */   
            }
        });
        
        Button btnStop = (Button)findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // ここに処理を記述
            	onStop();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.sample_loc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //lm.removeUpdates(pi);
        lm.removeUpdates(this);
        // カメラ破棄インスタンスを解放
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
    }
    public void onStop() {
    	super.onStop();
    	 
    	// 位置情報の更新を止める
    	lm.removeUpdates(this);
    	 
    	}

    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";

            // SD カードフォルダを取得
            File file = new File(saveDir);

            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }

            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            // ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                registAndroidDB(imgPath);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }

            fos = null;

            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCam.startPreview();

            mIsTake = false;
        }
    };

    /**
     * アンドロイドのデータベースへ画像のパスを登録
     * @param path 登録するパス
     */
    private void registAndroidDB(String path) {
        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = SampleLoc.this.getContentResolver();
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    
    

	
    
    public void onLocationChanged(Location location) {
    	// 例としてラベルに取得した位置を表示
    	//TextView latitudeLabel = new TextView(null);
    	//TextView longitudeLabel = new TextView(null);
		//latitudeLabel.setText(Double.toString(location.getLatitude()));
    	//longitudeLabel.setText(Double.toString(location.getLongitude()));
    	 
    }
    	 
    public void onProviderEnabled(String provider) {
     
    }
    	 
    public void onProviderDisabled(String provider) {
     
    }
    	 
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	 
    }    
    
    
}



