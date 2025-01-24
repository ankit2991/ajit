package macro.hd.wallpapers;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WallpapersService extends android.service.wallpaper.WallpaperService {

	private static final String TAG = "JesusWallpaperService";
	public static boolean isServiceRunning;

	@Override
	public Engine onCreateEngine() {
		// TODO Auto-generated method stub
		Logger.e(TAG,"onCreateEngine:");
		return new LoveEngine();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	private class LoveEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, IEventListener {
		List<Bitmap> imgList = new ArrayList<Bitmap>();
		private int counter = 0;
		boolean isWallpaperChange;
		private int delayTime=600000;
		int height, width;
		SettingStore setting;
		int frameDuration = 1000;
		int imageId = 0;
		private GestureDetector gestureDetector;
//		int alphaCounter = 255;
		// private GlowDrawable mDrawable;
		private boolean mVisible = false;
		private final Handler mHandler = new Handler();
		private final Runnable mUpdateDisplay = new Runnable() {
			public void run() {
				Logger.e(TAG,"mUpdateDisplay:"+counter);
				counter++;
				draw();
			}
		};

		@Override
		public void onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			super.onTouchEvent(event);
			gestureDetector.onTouchEvent(event);
		}


		public LoveEngine() {
			super();

			DisplayMetrics metrics = getApplicationContext().getResources()
					.getDisplayMetrics();
			width = metrics.widthPixels;
			height = metrics.heightPixels + CommonFunctions.getNavigationBarHeight(getApplicationContext());

			Logger.e(TAG,"LoveEngine:"+height);

			setting = SettingStore.getInstance(getApplicationContext());
			try {
				frameDuration = AppConstant.time_frame_duration[setting
						.getTimeDurationIndex()];
			} catch (Exception e) {
				e.printStackTrace();
				frameDuration=AppConstant.time_frame_duration[AppConstant.time_frame_duration.length-1];
			}

			try {
				delayTime=frameDuration/10;
			} catch (Exception e) {
				e.printStackTrace();
			}
			Logger.e(TAG,"delayTime:"+delayTime);

			gestureDetector = new GestureDetector(this);
			gestureDetector.setOnDoubleTapListener(this);

//			new Thread(){
//				@Override
//				public void run() {
//					super.run();
					baseImage();
//				}
//			}.start();

		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			if(!isPreview())
				isServiceRunning=true;
			registerFourKWallUpdateListener();
		}

		private void baseImage() {

			if(imgList!=null)
				imgList.clear();

//			File path = new File(Common.getSavedFilePath());
//			path.mkdirs();
//
//			File files[] = path.listFiles();

//			setting = SettingStore.getInstance(JesusWallpaperService.this);

			String lastResponse=setting.getImages();
			final String[] files=lastResponse.split("#");

//			Bitmap temp;
			setting.setLastAutoChangedTime();

			new Thread(){
				@Override
				public void run() {
					super.run();
					for (int i = 0; i < files.length; i++) {
						File file=new File(files[i]);
						if (file.exists()) {
							try {

								final RequestOptions requestOptions=new RequestOptions();
								requestOptions.centerCrop();


								Bitmap bitmap = Glide
										.with(getApplicationContext())
										.asBitmap()
										.load(Uri.fromFile(file)).apply(requestOptions).override(width,height)
										.submit()
										.get();

								if(imgList!=null)
									imgList.add(bitmap);

								try {
									if(files.length>=imgList.size()) {
										draw();
									}
								} catch (Exception e) {
									e.printStackTrace();
									draw();
								}


//						Glide.with(getApplicationContext())
//								.asBitmap()
//								.load(Uri.fromFile(file)).apply(requestOptions)
//								.into(new SimpleTarget<Bitmap>(width,height) {
//									@Override
//									public void onResourceReady(Bitmap bitmap,
//																Transition<? super Bitmap> transition) {
//
//										if(imgList!=null)
//											imgList.add(bitmap);
//
//										try {
//											if(files.length>=imgList.size()) {
//												draw();
//											}
//										} catch (Exception e) {
//											e.printStackTrace();
//											draw();
//										}
//									}
//								});


							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}.start();


		}




		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			if (WallpaperManager.COMMAND_TAP.equals(action)) {

//				handler.postDelayed(new Runnable() {
//					public void run() {
//						if (that.isVisible()) {
//							// valid tap command
//							// DO STUFF
//							// Toast.makeText(GlowService.this, "touch",
//							// Toast.LENGTH_SHORT).show();
//						} else {
//							// Invalid tap command, throw away
//							// Toast.makeText(GlowService.this, "invalid touch",
//							// Toast.LENGTH_SHORT).show();
//						}
//					}
//				}, 100);
			}
			return extras;
		}

		Canvas c;

		private void draw() {

			if(imgList!=null && imgList.size()>0)
			{

				SurfaceHolder holder = getSurfaceHolder();
				c = null;
				try {
					c = holder.lockCanvas();
					c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
					Bitmap imgBit;

					Logger.e(TAG,"draw: frameDuration:"+frameDuration);

					long currentTime = Calendar.getInstance().getTimeInMillis();
					long diff=currentTime - getLast_auto_wallpaper_change_time();
					Logger.e(TAG,"draw: diff:"+diff);
					if (diff>= frameDuration) {
						isWallpaperChange = true;
					} else {
						isWallpaperChange = false;
					}
					Logger.e(TAG,"draw:"+isWallpaperChange);
					if ((counter >= frameDuration / delayTime) || isWallpaperChange) {
						// Log.e("Counter", counter + "");

						imageId++;
						counter = 0;
//						alphaCounter = 255;
						if (imageId >= imgList.size())
							imageId = 0;

						if (isPreview()) {
							setting.setLastAutoChangedTimeTemp();
						} else {
							setting.setLastAutoChangedTime();
						}
						last_auto_wallpaper_change_time = 0;
					}
					Paint p = new Paint();
					p.setColor(Color.BLACK);

					if((((frameDuration / delayTime) - counter) < 10) || isWallpaperChange){
						isWallpaperChange = false;
						if(imageId >= imgList.size() - 1)
							imgBit = imgList.get(0);
						else
							imgBit = imgList.get(imageId + 1);

//						Bitmap resize=getResizedBitmap(imgBit,height,width);
						Bitmap resize=resize(imgBit);
//						Logger.e("size",""+resize.getHeight());
						if(resize.getHeight()<(height-20))
							c.drawBitmap(resize, width/2-resize.getWidth()/2, height/2-resize.getHeight()/2, null);
						else
							c.drawBitmap(resize, width/2-resize.getWidth()/2, 0, null);

//						alphaCounter -= 25.5;
//						p.setAlpha(alphaCounter);
						imgBit = imgList.get(imageId);
//						Bitmap resize1=getResizedBitmap(imgBit,height,width);
						Bitmap resize1=resize(imgBit);
						if(resize1.getHeight()<height-20)
							c.drawBitmap(resize1, width/2-resize1.getWidth()/2, height/2-resize1.getHeight()/2, p);
						else
							c.drawBitmap(resize1, width/2-resize1.getWidth()/2, 0, p);

					}else{
//						p.setAlpha(255);
						imgBit = imgList.get(imageId);
//						Bitmap resize1=getResizedBitmap(imgBit,height,width);
						Bitmap resize1=resize(imgBit);
						if(resize1.getHeight()<height-20)
							c.drawBitmap(resize1, width/2-resize1.getWidth()/2, height/2-resize1.getHeight()/2, p);
						else
							c.drawBitmap(resize1, width/2-resize1.getWidth()/2, 0, p);
					}

				}catch (Exception e){
					e.printStackTrace();
				}catch (Error e){
					e.printStackTrace();
				}finally {
					try {
						if (c != null)
							holder.unlockCanvasAndPost(c);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			mHandler.removeCallbacks(mUpdateDisplay);
			if (mVisible) {
				mHandler.postDelayed(mUpdateDisplay, delayTime);
			}
		}

		@Override
		public int eventNotify(int eventType, Object eventObject) {
			switch (eventType) {
				case EventTypes.EVENT_UPDATE_WALL:
					if(eventObject!=null)
						last_auto_wallpaper_change_time=0;
					Logger.e(TAG,"last_auto_wallpaper_change_time:"+last_auto_wallpaper_change_time);
					baseImage();
					break;
			}
			return 0;
		}

		private Bitmap resize(Bitmap image) {
			return image;
//			int  maxWidth=width;
//			int maxHeight=height;
//			if (maxHeight > 0 && maxWidth > 0) {
//				int width = image.getWidth();
//				int height = image.getHeight();
//				float ratioBitmap = (float) width / (float) height;
//				float ratioMax = (float) maxWidth / (float) maxHeight;
//
//				int finalWidth = maxWidth;
//				int finalHeight = maxHeight;
//				if (ratioMax > ratioBitmap) {
//					finalWidth = (int) ((float)maxHeight * ratioBitmap);
//				} else {
//					finalHeight = (int) ((float)maxWidth / ratioBitmap);
//				}
//
////				image = Glide.
////						with(JesusWallpaperService.this).
////						load("http://....").
////						asBitmap().
////						into(finalWidth, finalHeight). // Width and height
////						get();
//
//				image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
//				return image;
//			} else {
//				return image;
//			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			Logger.e(TAG,"onVisibilityChanged:"+visible);
			if(!isPreview())
				isServiceRunning=true;
			if (visible) {
//				counter = 0;
//				imageId = 0;
//				alphaCounter = 255;
				try {
					frameDuration = AppConstant.time_frame_duration[setting
							.getTimeDurationIndex()];
					delayTime=frameDuration/10;
				} catch (Exception e) {
					e.printStackTrace();
				}

				Logger.e("AutoWallpaperService", (isPreview() ? "Preview : " : "") + "onVisibilityChanged:" + visible + " isWallpaperChange:" + isWallpaperChange);
//				if (isWallpaperChange) {
					draw();
//				}

			} else {
				mHandler.removeCallbacks(mUpdateDisplay);
			}
		}
		public long last_auto_wallpaper_change_time = 0;
		public long getLast_auto_wallpaper_change_time() {
			if (last_auto_wallpaper_change_time == 0) {
				if (isPreview()) {
					last_auto_wallpaper_change_time = SettingStore.getInstance(getApplicationContext()).getLastAutoChangedTimeTemp();
				} else {
					last_auto_wallpaper_change_time = SettingStore.getInstance(getApplicationContext()).getLastAutoChangedTime();
				}
			}
			return last_auto_wallpaper_change_time;
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			draw();
			Logger.e(TAG,"onSurfaceChanged:");
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
			Logger.e(TAG,"onSurfaceDestroyed:");
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if(!isPreview())
				isServiceRunning=false;
			unregisterFourKWallUpdateListener();
			Logger.e(TAG,"onDestroy");
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
			if(imgList!=null) {
				imgList.clear();
				imgList=null;
				CommonFunctions.garbageCollector(true);
			}
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent motionEvent) {
//			return false;
			if (mVisible) {
				if(setting.getIsDoubleTap()) {
					long currentTime = Calendar.getInstance().getTimeInMillis();
					last_auto_wallpaper_change_time = currentTime - frameDuration;
					draw();
				}
			}
			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent motionEvent) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent motionEvent) {

		}

		@Override
		public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}

		private void registerFourKWallUpdateListener() {
			if (isPreview())
				return;
			EventNotifier notifier =
					NotifierFactory.getInstance().getNotifier(
							NotifierFactory.EVENT_NOTIFIER_UPDATE_WALL);
			notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);
		}

		private void unregisterFourKWallUpdateListener() {
			if (isPreview())
				return;
			EventNotifier notifier =
					NotifierFactory.getInstance().getNotifier(
							NotifierFactory.EVENT_NOTIFIER_UPDATE_WALL);
			notifier.unRegisterListener(this);
		}
	}
}
