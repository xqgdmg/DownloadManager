package dmg.xqg.com.downloadmanager;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class DownloadActivity extends Activity implements View.OnClickListener {
    private TextView down;
    private TextView progress;
    private TextView file_name;
    private ProgressBar pb_update;
    private DownloadManager downloadManager;
    private DownloadManager.Request request;
    public static String downloadUrl = "http://opttkka76.bkt.clouddn.com/pwallet_debug%281.9.8%29%2894%29.apk";
    Timer timer;
    long id;
    TimerTask task;
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int pro = bundle.getInt("pro");
            String name  = bundle.getString("name");
            pb_update.setProgress(pro);
            progress.setText(String.valueOf(pro)+"%");
            file_name.setText(name);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        down = (TextView) findViewById(R.id.down);
        progress = (TextView) findViewById(R.id.progress);
        file_name = (TextView) findViewById(R.id.file_name);
        pb_update = (ProgressBar) findViewById(R.id.pb_update);
        down.setOnClickListener(this);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        request = new DownloadManager.Request(Uri.parse(downloadUrl));

        request.setTitle("Discovery");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        request.setMimeType("application/vnd.android.package-archive");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;

        //设置文件存放路径
        request.setDestinationInExternalPublicDir(  Environment.DIRECTORY_DOWNLOADS  , "abcd.apk" ) ;
        pb_update.setMax(100);
        final  DownloadManager.Query query = new DownloadManager.Query();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = downloadManager.query(query.setFilterById(id));
                if (cursor != null && cursor.moveToFirst()) {// 不为空，有第一个
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        pb_update.setProgress(100);
                        install(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/abcd.apk" );
                        task.cancel();
                    }
                    String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    String address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    // 注意 int 的取值范围是 2147483647，按byte算的话，太大的文件这个值可能装不下，导致进度出现负数
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    Log.e("chris","已下载字节数==" + bytes_downloaded);
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    Log.e("chris","总字节数==" + bytes_total);
                    double i = bytes_downloaded*1.000d / bytes_total;// 注意两个int相除直接用double接收会直接舍掉小数位，所以要*1.000d
                    Log.e("chris","i==" + i);
                    int pro = (int) (i* 100);
                    Log.e("chris","当前进度==" + pro);

                    Message msg =Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("pro",pro);
                    bundle.putString("name",title);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
                cursor.close();
            }
        };
        timer.schedule(task, 0,1000);
    }

    @Override
    public void onClick(View v) {
        id = downloadManager.enqueue(request);
        task.run();
        down.setClickable(false);
        down.setBackgroundColor(Color.parseColor("#5500ff00"));

    }
    private void install(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面
        startActivity(intent);
    }
}
