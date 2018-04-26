package com.example.amanmj.inputstream_player;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
	private SimpleExoPlayer player;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		file=new File(getCacheDir(),"sample.mp3");

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

		//DefaultRenderersFactory renderersFactorySound = new DefaultRenderersFactory(this,null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
		//player = ExoPlayerFactory.newSimpleInstance(renderersFactorySound, new DefaultTrackSelector(), new DefaultLoadControl());
		player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());

		final DataSource dataSource = new InputStreamDataSource(this);
		final Uri uri = Uri.parse("sample.mp3");
		DataSpec dataSpec = new DataSpec(uri);
		try {
			dataSource.open(dataSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataSource.Factory factoryMusic = new DataSource.Factory() {
			@Override
			public DataSource createDataSource() {
				return dataSource;
			}
		};
		MediaSource audioSource = new ExtractorMediaSource.Factory(factoryMusic).createMediaSource(uri, null, null);

		player.prepare(audioSource);
		player.setPlayWhenReady(true);
	}
}
