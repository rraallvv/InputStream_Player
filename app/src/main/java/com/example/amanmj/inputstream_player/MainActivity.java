package com.example.amanmj.inputstream_player;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
	private SimpleExoPlayer player;
	private String filename = "sample.mp4";
	private TextureView videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		videoView = findViewById(R.id.videoView);

		File file=new File(getCacheDir(),filename);

		if (!file.exists()) {
			try {
				InputStream inputStream = getAssets().open(filename);
				OutputStream outputStream = new FileOutputStream(file);

				byte buffer[] = new byte[1024];
				int read = 0;

				while((read=inputStream.read(buffer)) > 0) {
					outputStream.write(buffer,0,read);
				}

				outputStream.close();
				inputStream.close();

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), filename + " could not be copied to external storage", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				finish();
			}
		}

		//DefaultRenderersFactory renderersFactorySound = new DefaultRenderersFactory(this,null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
		//player = ExoPlayerFactory.newSimpleInstance(renderersFactorySound, new DefaultTrackSelector(), new DefaultLoadControl());
		player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());

		//final DataSource dataSource = new InputStreamDataSource(this);
		final DataSource dataSource = new AssetDataSource(this);
		final Uri uri = Uri.parse(filename);
		DataSpec dataSpec = new DataSpec(uri);
		try {
			dataSource.open(dataSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataSource.Factory dataSourcefactory = new DataSource.Factory() {
			@Override
			public DataSource createDataSource() {
				return dataSource;
			}
		};
		MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourcefactory).createMediaSource(uri, null, null);

		player.setVideoTextureView(videoView);

		player.prepare(mediaSource);
		player.setPlayWhenReady(true);
	}
}
