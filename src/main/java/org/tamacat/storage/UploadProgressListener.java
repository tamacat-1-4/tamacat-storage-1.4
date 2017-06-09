/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.PrintWriter;

import org.apache.commons.fileupload.ProgressListener;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;

public class UploadProgressListener implements ProgressListener {

	static final Log LOG = LogFactory.getLog(UploadProgressListener.class);
	
	private long num100Ks = 0;

	private long theBytesRead = 0;
	private long theContentLength = -1;
	private int whichItem = 0;
	private int percentDone = 0;
	private boolean contentLengthKnown = false;
	private PrintWriter writer;
	
	public UploadProgressListener(PrintWriter writer) {
		this.writer = writer;
	}
	
	@Override
	public void update(long pBytesRead, long pContentLength, int pItems) {
		if (pContentLength > -1) {
			contentLengthKnown = true;
		}
		theBytesRead = pBytesRead;
		theContentLength = pContentLength;
		whichItem = pItems;

		long nowNum100Ks = pBytesRead / 100000;
		// Only run this code once every 100K
		if (nowNum100Ks > num100Ks) {
			num100Ks = nowNum100Ks;
			if (contentLengthKnown) {
				int now = (int) Math.round(100.00 * pBytesRead / pContentLength);
				if (percentDone < now) {
					percentDone = now; 
					String message = getMessage();
					writer.print(getMessage());
					writer.flush();
					LOG.debug(message);
				}
			}
		}
	}

	public String getMessage() {
		if (theContentLength == -1) {
			return "" + theBytesRead + " of Unknown-Total bytes have been read.";
		} else {
			return "" + theBytesRead + " of " + theContentLength + " bytes have been read (" + percentDone + "% done).";
		}
	}

	public long getNum100Ks() {
		return num100Ks;
	}

	public void setNum100Ks(long num100Ks) {
		this.num100Ks = num100Ks;
	}

	public long getTheBytesRead() {
		return theBytesRead;
	}

	public void setTheBytesRead(long theBytesRead) {
		this.theBytesRead = theBytesRead;
	}

	public long getTheContentLength() {
		return theContentLength;
	}

	public void setTheContentLength(long theContentLength) {
		this.theContentLength = theContentLength;
	}

	public int getWhichItem() {
		return whichItem;
	}

	public void setWhichItem(int whichItem) {
		this.whichItem = whichItem;
	}

	public int getPercentDone() {
		return percentDone;
	}

	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}

	public boolean isContentLengthKnown() {
		return contentLengthKnown;
	}

	public void setContentLengthKnown(boolean contentLengthKnown) {
		this.contentLengthKnown = contentLengthKnown;
	}
}
