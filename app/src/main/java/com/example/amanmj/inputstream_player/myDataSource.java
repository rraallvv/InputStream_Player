package com.example.amanmj.inputstream_player;

import android.content.Context;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/* my custom DataSource to read myInputStream (Similar to AssetDataSource) */
public class myDataSource implements DataSource {

	private Context context;
	private myInputStream inputStream;
	private String uriString;
	private long bytesRemaining;
	private boolean isOpen;

	public myDataSource(Context context) {
		this.context = context;
	}

	@Override
	public long open(DataSpec dataspec) throws IOException
	{
		uriString = dataspec.uri.toString();

		File file = new File(context.getCacheDir(),uriString);

		FileInputStream randomAccessFile=new FileInputStream(file);
		inputStream=new myInputStream(randomAccessFile);

		if (inputStream != null) {
			isOpen = true;
			bytesRemaining =  inputStream.available();
		}

		return bytesRemaining;
	}

	@Override
	public int read(byte[] buffer, int offset, int readLength) throws IOException
	{
		if (bytesRemaining == 0)
		{
			return -1;
		}
		else
		{
			int bytesRead = 0;
			bytesRead = inputStream.read(buffer, offset, readLength);

			if (bytesRead > 0) {
				bytesRemaining -= bytesRead;
			}

			return bytesRead;
		}
	}

    @Override
	public void close() throws IOException
	{
		uriString = null;
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				throw new IOException(e);
			}
			finally
			{
				inputStream = null;
				if (isOpen)
				{
					isOpen = false;
				}
			}
		}
	}
}

