package com.lhg1304.onimani.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 온라인 상의 이미지 리소스를 다운로드하기 위한 모듈
 * 메모리 캐쉬 및 파일 캐쉬를 지원함.
 * @author dckim
 */
public class ImageDownloader {

    private static final String LOG_TAG = "[ImageDownloader]";

    /** 파일 캐쉬 모듈 */
    private FileCache mFileCache = null;

    /** 비트맵 이미지 변환 시 크기의 변환 여부*/
    private boolean mUseBitmapScale = false;
    private Map<ImageView, String> mImageViewsMap = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    /** 다운로드 목록을 관리할 큐 기능 */
    private PhotosLoader mPhotoLoaderThread = null;
    private Object mSyncObj = new Object();

    private Context mContext = null;

    /** 다운로드 완료 리스너 */
    private DownloadCompleteListener mDownloadCompleteListener = null;

    /** 다운로드 완료 리스너(인터페이스)
     * 로컬에서 사용할 이미지가 준비되면 UI에 반영하기 위해 사용함
     */
    public interface DownloadCompleteListener {
        public abstract void onDownloadComplete(ImageView imageView, Bitmap bitmap);
    }

    /**
     * 다운로드 완료 리스너 설정
     * @param listener 등록할 리스너(보통 UI쪽에서 만든 리스너를 등록하여 사용함)
     */
    public void setOnDownloadComplete(DownloadCompleteListener listener) {
        mDownloadCompleteListener = listener;
    }

    /**
     * ImageDownloader Constructor 
     * @param context Context
     */
    public ImageDownloader(Context context) {

    	mContext = context;
        mFileCache = new FileCache(context);

        mPhotoLoaderThread = new PhotosLoader();
        mPhotoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
        mPhotoLoaderThread.start();
    }

    /**
     * 이미지 다운로드를 중지함.
     */
    public void stopThread() {

    	if (mPhotoLoaderThread != null) {
            mPhotoLoaderThread.interrupt();
            mPhotoLoaderThread = null;
        }
    }

    /**
     * 이미지 다운로드를 시작함.
     */
    public void startThread() {

        if (mPhotoLoaderThread == null) {
            mPhotoLoaderThread = new PhotosLoader();
            mPhotoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
            mPhotoLoaderThread.start();
        } else {

            if (mPhotoLoaderThread.getState() == Thread.State.NEW) {
                mPhotoLoaderThread.start();
            }
        }
    }

    /**
     * Thread를 통하여 다운로드 후 Callback으로 결과 전달
     * @param url 다운로드 할 이미지 위치
     * @param imageView 이미지를 설정할 뷰
     */
    public void download(String url, ImageView imageView) {

        mImageViewsMap.put(imageView, url);
        queuePhoto(url, imageView);
    }

    /**
     * 요청된 이미지 다운로드를 취소함.
     * @param key 설정하려고 등록했던 이미지 뷰
     */
    public void removeItem(ImageView key) {
        if (mPhotosQueue.getSize() > 0) {
            mPhotosQueue.Clean(key);
        }
    }

    /**
     * 이미지 다운로드를 위한 대기열 등록
     * @param url 이미지 위치
     * @param imageView 이미지를 설정할 뷰
     */
    private void queuePhoto(String url, ImageView imageView) {

        if (mPhotosQueue.getSize() > 0)
            mPhotosQueue.Clean(imageView);

        PhotoToLoad p = new PhotoToLoad(url, imageView);
        synchronized (mPhotosQueue.mPhotosToLoad) {
            mPhotosQueue.mPhotosToLoad.add(p);
            mPhotosQueue.mPhotosToLoad.notifyAll();
        }

        // Thread Start
        startThread();
    }

    /**
     * 서버로 부터 Thumbnail Image를 가져온다.
     * @param url : Thumbnail URL
     * @return
     */
    private Bitmap getBitmap(String url) {
        Bitmap bitmap= getBitmapFromCache(url);
        if (bitmap != null) {
        	Log.i(LOG_TAG, "Cached Bitmap: "+url);
            return bitmap;
        }
        
        if (!isAvailableNetwork(mContext, false)) {
        	Log.i(LOG_TAG, "Network Off");
        	return null;
        }

        HttpURLConnection conn = null;
        // URL 다운로드
        try {
            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 비트맵 다운로드
            int imageSize = conn.getContentLength();
            InputStream inputStream = conn.getInputStream();

            // 이미지 크기 변환
            if (mUseBitmapScale) {
                Bitmap scaledBmp = getScaledImage(inputStream, imageSize);
                if (scaledBmp != null) {
                    bitmap = scaledBmp;
                }
            } else {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }

            // 파일로 저장
            if (mFileCache != null && bitmap != null) {
            	Log.i(LOG_TAG, "Cache addBitmap(): "+url);
                mFileCache.addBitmap(url, bitmap);
            }

            return bitmap;
        } catch (IOException ex) {
        	ex.printStackTrace();
            Log.i(LOG_TAG, "IOException:"+ex);
            return null;
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    // Task for the queue
    private static class PhotoToLoad {
		public String mImageUrl;
		public ImageView mImageView;

		public PhotoToLoad(String url, ImageView imageView) {
			mImageUrl = url;
			mImageView = imageView;
		}
   }

    PhotosQueue mPhotosQueue = new PhotosQueue();

    /**
     *  stores list of photos to download
     */
    private static class PhotosQueue {
        private Queue<PhotoToLoad> mPhotosToLoad = new LinkedList<PhotoToLoad>();

        // removes all instances of this ImageView
        public void Clean(ImageView image) {

            synchronized (mPhotosToLoad) {

                if (!mPhotosToLoad.isEmpty()) {

                    Iterator<PhotoToLoad> it = mPhotosToLoad.iterator();
                    while (!mPhotosToLoad.isEmpty() && it.hasNext()) {
                        PhotoToLoad iteratorValue = (PhotoToLoad) it.next();

                        if (iteratorValue.mImageView == image) {
                            it.remove();
                            return;
                        }

                        if (mPhotosToLoad.isEmpty()) {
                            return;
                        }
                    }
                }
            }
        }

        public int getSize() {
            return mPhotosToLoad.size();
        }
    }

    private Handler mThreadStopHandler = new Handler() {
        public void handleMessage(Message msg) {
            stopThread();
        }
    };

    private class PhotosLoader extends Thread {
        public PhotosLoader() {
        }

        public void run() {
            try {
                while (true) {
                    if (mPhotosQueue.mPhotosToLoad.size() == 0) {
                        synchronized (mPhotosQueue.mPhotosToLoad) {
                            mThreadStopHandler.sendEmptyMessageDelayed(0, 3000);
                            mPhotosQueue.mPhotosToLoad.wait();
                        }
                    }

                    mThreadStopHandler.removeMessages(0);

                    if (mPhotosQueue.mPhotosToLoad.size() != 0) {
                        PhotoToLoad photoToLoad;
                        synchronized (mPhotosQueue.mPhotosToLoad) {
                            photoToLoad = mPhotosQueue.mPhotosToLoad.poll();
                        }

                        if (photoToLoad != null) {
                            // 이미지 로딩 확인
                        	Log.i(LOG_TAG, "Request Image: "+photoToLoad.mImageUrl);

                            Bitmap bitmap = getBitmap(photoToLoad.mImageUrl);
                            String imgUrl = mImageViewsMap.get(photoToLoad.mImageView);
                            if (null == imgUrl)
                            	Log.e(LOG_TAG, "imgUrl:"+imgUrl);

                            if (imgUrl != null && imgUrl.equals(photoToLoad.mImageUrl)) {
                            	if (mContext != null) {
                                    Activity a = (Activity) mContext;
                                    a.runOnUiThread(new BitmapDisplayer(bitmap, photoToLoad.mImageView));
                                }
                            }
                        }
                    }

                    if (Thread.interrupted()) {
                        break;
                    }

                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                    	e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                //Log.e(LOG_TAG, "[DownloadThread-InterruptedException] Thread Stop!!!");
            }
        }
    }

    /**
     * 사용에 적절한 크기의 비트맵으로 생성함
     * 1MB 이상일 경우 1/4로 축소, 300KB 이상일 경우 1/2로 축소
     * @param inputStream 원본 이미지 스트림
     * @param imageSize 원본 이미지 크기(용량)
     * @return Bitmap 생성된 비트맵 이미지를 반환함.
     */
    private Bitmap getScaledImage(InputStream inputStream, int imageSize) {

        try {
            Bitmap bitmap = null;

            if (imageSize > 1000000) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);
            } else if (imageSize > 300000) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);
            } else {
                bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
            }
            
            return bitmap;
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return null;
    }

    /** Used to display bitmap in the UI thread */
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

        public void run() {

            synchronized (mSyncObj) {
				if (bitmap != null) {
					//imageView.setScaleType(ScaleType.FIT_XY);
					imageView.setImageBitmap(bitmap);
				} else {
					imageView.setImageDrawable(null);
				}
            }

            if (mDownloadCompleteListener != null) {
                mDownloadCompleteListener.onDownloadComplete(imageView, bitmap);
            }
        }
    }

    /**
     * 해당 url에 있는 이미지가 파일 캐쉬에 존재하는지 조사하여 이미지를 얻어 온다.
     * @param url 원격지의 리소스 위치
     * @return Bitmap 파일 캐쉬에 이미지가 존재하면 비트맵을 반환하고, 그렇지 않으면 null을 반환함. 
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (mHardBitmapCache) {
            final Bitmap bitmap = mHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                mHardBitmapCache.remove(url);
                mHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                mSoftBitmapCache.remove(url);
            }
        }

        // File Cache 체크
        if (mFileCache != null) {
            return mFileCache.getBitmap(url);
        }

        return null;
    }

    /**
     * 파일 캐쉬에 원하는 이미지가 캐쉬되어 있는지 조사함/
     * @param url 다운로드할 이미지
     * @return true이면 캐쉬에 이미지 존재함을 의미
     */
    public boolean isFileCachedImage(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }

        // File Cache 체크
        boolean isCache = false;
        if (mFileCache != null) {
            isCache = mFileCache.isCachedBitmap(url);
        }

        return isCache;
    }

    /**
     * 메모리상의 캐쉬를 모두 제거함.
     */
    public void clearMemoryCache() {
        mHardBitmapCache.clear();
        mSoftBitmapCache.clear();
    }

    /**
     * 메모리상의 캐쉬와 파일 캐쉬를 모두 제거함.
     */
    public void clearCache() {
        clearMemoryCache();
        if (mFileCache != null) {
            mFileCache.clear();
        }
    }

    /** 파일 캐쉬 클래스 */
    private static class FileCache {

        private File mCacheDir = null;

        public FileCache(Context context) {

            //Find the dir to save cached images
        	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String storagePath = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    storagePath = Environment.getExternalStorageDirectory().getPath();
                }

        		mCacheDir = new File(storagePath+"/OniMani/thumbnail/");
        		if (!mCacheDir.exists()) {
        			if(!mCacheDir.mkdirs()) {
        				Log.e(LOG_TAG, "mkdir Failed : " + mCacheDir.getAbsolutePath());
        			}
        		}
        		
        		return;
        	}
        	
        	Log.e(LOG_TAG, "ExternalStorageState");
        }

        /**
         * 파일 캐쉬에 비트맵 파일이 있는지 조사함.
         * @param url 이미지 위치(식별자로 사용)
         * @return 파일이 존재하면 비트맵 이미지로 변환하여 반환함.
         */
        private Bitmap getBitmap(String url) {
        	if (null == mCacheDir)
        		return null;

            FileInputStream is = null;
            Bitmap bitmap = null;
            try {
                String filename = String.valueOf(url.hashCode());
                File f = new File(mCacheDir, filename);
                if (f.exists()) {
	                is = new FileInputStream(f);
	                bitmap = BitmapFactory.decodeStream(is);
                }
            } catch (FileNotFoundException e) {
            	e.printStackTrace();
            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    	e.printStackTrace();
                    }
                }
            }

            return bitmap;
        }

        /**
         * 파일로 저장된 이미지가 있는지 조사함/
         * @param url 다운로드할 이미지
         * @return true이면 캐쉬에 사용될 파일이 저장되어 있음을 의미
         */
        private boolean isCachedBitmap(String url) {

            if (url == null || url.length() == 0 || null == mCacheDir) {
                return false;
            }

            try {
                String filename = String.valueOf(url.hashCode());
                File f = new File(mCacheDir, filename);
                return f.exists();
            } catch (Exception e) {
            	e.printStackTrace();
            }

            return false;
        }

        /**
         * 파일 캐쉬 정보를 추가함
         * @param url 이미지 url 식별자로 사용됨
         * @param bitmap 비트맵 이미지
         */
        private void addBitmap(String url, Bitmap bitmap) {
        	if (null == mCacheDir)
        		return;

            try {
                String filename = String.valueOf(url.hashCode());
                File f = new File(mCacheDir, filename);

                OutputStream os = new FileOutputStream(f);
                bitmap.compress(CompressFormat.JPEG, 100, os);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /** 파일 캐쉬용 정보를 제거함 */
        private void clear() {
        	if (null == mCacheDir)
        		return;

            File[] files = mCacheDir.listFiles();
            for (File f : files) {
                if(!f.delete()) {
                    Log.e(LOG_TAG, "clear delete Failed : " + f.getAbsolutePath());
                }
            }
        }
    }

    private static final int HARD_CACHE_CAPACITY = 10;
    private final HashMap<String, Bitmap> mHardBitmapCache = new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {

        private static final long serialVersionUID = 7056069285521744709L;

        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                mSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else {
                return false;
            }
        }
    };

    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;
                    } else {
                        bytesSkipped = 1;
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }





    /**
     * Download 가능 Network 확인(데이터 네트워크 사용 가능 여부 포함)
     * @param ctx Context
     * @param checkDataUpDown 데이터 네트워크 상에서 업로드/다운로드 가능 여부 포함
     * @return boolean true이면 네트워크 사용 가능
     */
    public static boolean isAvailableNetwork(Context ctx, boolean checkDataUpDown) {

        if (null == ctx) {
            return false;
        }

        NetworkInfo ni = ((ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (ni == null) {
            // 활성 네트워크가 없을 경우
            return false;
        }

        int activeNetType = ni.getType();
        if (activeNetType == ConnectivityManager.TYPE_MOBILE) {

            // WCDMA 연결 체크
            if (ni.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }

        } else if (activeNetType == ConnectivityManager.TYPE_WIFI || activeNetType == ConnectivityManager.TYPE_ETHERNET) {
            if (ni.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }
}
