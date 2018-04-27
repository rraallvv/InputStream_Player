/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.amanmj.inputstream_player;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link DataSource} for reading from a local asset.
 */
public final class InputStreamDataSource implements DataSource {

	private final Context context;
	private Uri uri;
	private InputStream inputStream;
	private long bytesRemaining;
	private boolean opened;

	/**
	 * @param context A context.
	 */
	public InputStreamDataSource(Context context) {
		this.context = context;
	}

	@Override
	public long open(DataSpec dataSpec) throws IOException {
		uri = dataSpec.uri;

		File file = new File(context.getCacheDir(), uri.toString());
		inputStream = new FileInputStream(file);

		long skipped = inputStream.skip(dataSpec.position);
		if (skipped < dataSpec.position) {
			// assetManager.open() returns an AssetInputStream, whose skip() implementation only skips
			// fewer bytes than requested if the skip is beyond the end of the asset's data.
			throw new EOFException();
		}
		if (dataSpec.length != C.LENGTH_UNSET) {
			bytesRemaining = dataSpec.length;
		} else {
			bytesRemaining = inputStream.available();
			if (bytesRemaining == Integer.MAX_VALUE) {
				// assetManager.open() returns an AssetInputStream, whose available() implementation
				// returns Integer.MAX_VALUE if the remaining length is greater than (or equal to)
				// Integer.MAX_VALUE. We don't know the true length in this case, so treat as unbounded.
				bytesRemaining = C.LENGTH_UNSET;
			}
		}

		opened = true;

		return bytesRemaining;
	}

	@Override
	public int read(byte[] buffer, int offset, int readLength) throws IOException {
		if (readLength == 0) {
			return 0;
		} else if (bytesRemaining == 0) {
			return C.RESULT_END_OF_INPUT;
		}

		int bytesRead;
		int bytesToRead = bytesRemaining == C.LENGTH_UNSET ? readLength
				: (int) Math.min(bytesRemaining, readLength);
		bytesRead = inputStream.read(buffer, offset, bytesToRead);

		if (bytesRead == -1) {
			if (bytesRemaining != C.LENGTH_UNSET) {
				// End of stream reached having not read sufficient data.
				throw new IOException();
			}
			return C.RESULT_END_OF_INPUT;
		}
		if (bytesRemaining != C.LENGTH_UNSET) {
			bytesRemaining -= bytesRead;
		}

		return bytesRead;
	}

	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public void close() throws IOException {
		uri = null;
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new IOException();
		} finally {
			inputStream = null;
			if (opened) {
				opened = false;
			}
		}
	}
}
