package com.example.amanmj.inputstream_player;

import android.content.Context;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/* my custom DataSource to read myInputStream (Similar to AssetDataSource) */
public class myDataSource implements DataSource {

	private Context context;
	private String uriString;
	private myInputStream inputStream;
	private long bytesRemaining;
	private boolean opened;

	public myDataSource(Context context) {
		this.context = context;
	}

	@Override
	public long open(DataSpec dataspec)
	{
		try
		{
			uriString = dataspec.uri.toString();

			File file = new File(context.getCacheDir(),uriString);

			FileInputStream randomAccessFile=new FileInputStream(file);
			inputStream=new myInputStream(randomAccessFile);


			long skipped = inputStream.skip(dataspec.position);
			if (skipped < dataspec.position) {
				// assetManager.open() returns an AssetInputStream, whose skip() implementation only skips
				// fewer bytes than requested if the skip is beyond the end of the asset's data.
				throw new EOFException();
			}
			if (dataspec.length != C.LENGTH_UNBOUNDED) {
				bytesRemaining = dataspec.length - skipped;
			}
			else {
				/*if dataspec.length==-1 then number of bytesRemaining=number of bytes file contains*/
				bytesRemaining=file.length();

				if (bytesRemaining == Integer.MAX_VALUE) {
					// assetManager.open() returns an AssetInputStream, whose available() implementation
					// returns Integer.MAX_VALUE if the remaining length is greater than (or equal to)
					// Integer.MAX_VALUE. We don't know the true length in this case, so treat as unbounded.
					bytesRemaining = C.LENGTH_UNBOUNDED;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		opened = true;
		return bytesRemaining;
	}

	@Override
	public int read(byte[] buffer, int offset, int readLength)
	{
		if (bytesRemaining == 0) {
			return -1;
		}
		else {
			int bytesRead = 0;
			try {
				int bytesToRead = bytesRemaining == C.LENGTH_UNBOUNDED ? readLength
						: (int) Math.min(bytesRemaining, readLength);
				bytesRead = inputStream.read(buffer, offset, bytesToRead);
			}
			catch (IOException e)
			{

			}

			if (bytesRead > 0) {
				if (bytesRemaining != C.LENGTH_UNBOUNDED) {
					bytesRemaining -= bytesRead;
				}

			}
			return bytesRead;
		}
	}

    @Override
	public void close()
	{
		uriString = null;
		if (inputStream != null) {
			try {
				inputStream.close();
			}
			catch (IOException e)
			{

			}
			finally
			{
				inputStream = null;
				if (opened) {
					opened = false;

				}
			}
		}
	}
}

