package com.example.amanmj.inputstream_player;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer exoPlayer;
    private DataSource dataSource;
    private TrackRenderer audio;
    private ExtractorSampleSource extractorSampleSource;
    private int rendererCount;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rendererCount=1;

        exoPlayer= ExoPlayer.Factory.newInstance(rendererCount);

        /*check if file is present or not*/

        file=new File(getCacheDir(),"sample.mp3"); // location of file in the root directory of SD Card named "sample.mp3"

        if (!file.exists()) {
            try {
                InputStream inputStream = getAssets().open("sample.mp3");
                OutputStream outputStream = new FileOutputStream(file);

                byte buffer[] = new byte[1024];
                int read = 0;

                while((read=inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer,0,read);
                }

                outputStream.close();
                inputStream.close();

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "sample.mp3 could not be copied to external storage", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                finish();
            }
        }

        /*instantiate myDataSource*/
        dataSource=new myDataSource(this);

        extractorSampleSource=new ExtractorSampleSource(Uri.parse("sample.mp3"),dataSource,new DefaultAllocator(64*1024),64*1024*256);
        audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);

        /*prepare ExoPlayer*/
        exoPlayer.prepare(audio);
        exoPlayer.setPlayWhenReady(true);
    }
}
