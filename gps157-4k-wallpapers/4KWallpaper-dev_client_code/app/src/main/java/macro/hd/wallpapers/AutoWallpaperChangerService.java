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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AutoWallpaperChangerService extends WallpaperService {

	public static boolean isServiceRunning;
	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}

	private boolean isImageLoading = false;
	public long last_auto_wallpaper_change_time = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	private class WallpaperEngine extends Engine implements IEventListener {
		int frameDuration = 1000;
		int wallId = 0;
		int alphaCounter = 255;
		List<Bitmap> wallList = new ArrayList<Bitmap>();
		boolean isWallpaperChange;
		private int counter = 0;
		Canvas c;
		int height, width;
		SettingStore setting;
		private boolean mVisible = false;
		private final Handler mHandler = new Handler();

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


		private final Runnable mUpdateDisplay = new Runnable() {
			public void run() {
				counter++;
				draw();
			}
		};

		public Bundle onCommand(String action, int x, int y, int z,
								Bundle extras, boolean resultRequested) {
			if (WallpaperManager.COMMAND_TAP.equals(action)) {
			}
			return extras;
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			Logger.i("WallList", "AutoWallpaperChanger onCreate : ");
			if(!isPreview())
				isServiceRunning=true;
			if (isPreview()) {
				try {
					frameDuration = AppConstant.time_frame_duration[setting.getTimeDurationIndexTemp()];
				} catch (Exception e) {
					e.printStackTrace();
					frameDuration = AppConstant.time_frame_duration[AppConstant.time_frame_duration.length - 1];
				}
			} else {
				try {
					frameDuration = AppConstant.time_frame_duration[setting.getTimeDurationIndex()];
				} catch (Exception e) {
					e.printStackTrace();
					frameDuration = AppConstant.time_frame_duration[AppConstant.time_frame_duration.length - 1];
				}
			}
			registerUpdateListener();
			baseImage(false);

		}

		public WallpaperEngine() {
			super();

			DisplayMetrics metrics = getApplicationContext().getResources()
					.getDisplayMetrics();
			width = metrics.widthPixels;
			//Logger.i("AutoWallpaperChanger", "AutoWallpaperChanger :: height : " + height);
			height = CommonFunctions.getDpi(getApplicationContext());
			if (height <= 0) {
				height = metrics.heightPixels + CommonFunctions.getNavigationBarHeight(getApplicationContext());
			}
			//Logger.i("AutoWallpaperChanger", "AutoWallpaperChanger :: height1 : " + totalHeight);
			setting = SettingStore.getInstance(getApplicationContext());
		}



//		public WallpaperEngine() {
//			super();
//
//			DisplayMetrics metrics = getApplicationContext().getResources()
//					.getDisplayMetrics();
//			width = metrics.widthPixels;
//			height = metrics.heightPixels + Common.getNavigationBarHeight(getApplicationContext());
//
//			setting = SettingStore.getInstance(getApplicationContext());
//
//		}

		private void draw() {

//			Logger.e("draw","draw");
			if (wallList != null && wallList.size() > 0) {

				SurfaceHolder surfaceHolder = getSurfaceHolder();
				c = null;
				try {
					c = surfaceHolder.lockCanvas();
					c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
					Bitmap imgBit;

					if (counter >= frameDuration / 100) {
						// Log.e("Counter", counter + "");
						wallId++;
						counter = 0;
						alphaCounter = 255;
						if (wallId >= wallList.size())
							wallId = 0;
					}
					Paint p = new Paint();
					p.setColor(Color.BLACK);

					if (((frameDuration / 100) - counter) < 10) {

						if (wallId >= wallList.size() - 1)
							imgBit = wallList.get(0);
						else
							imgBit = wallList.get(wallId + 1);
						isWallpaperChange = true;
//						Bitmap resize=getResizedBitmap(imgBit,height,width);
						Bitmap resize = resize(imgBit);
//						Logger.e("size",""+resize.getHeight());
						if (resize.getHeight() < (height - 20))
							c.drawBitmap(resize, width / 2 - resize.getWidth() / 2, height / 2 - resize.getHeight() / 2, null);
						else
							c.drawBitmap(resize, width / 2 - resize.getWidth() / 2, 0, null);

						alphaCounter -= 25.5;
						p.setAlpha(alphaCounter);
						imgBit = wallList.get(wallId);
//						Bitmap resize1=getResizedBitmap(imgBit,height,width);
						Bitmap resize1 = resize(imgBit);
						if (resize1.getHeight() < height - 20)
							c.drawBitmap(resize1, width / 2 - resize1.getWidth() / 2, height / 2 - resize1.getHeight() / 2, p);
						else
							c.drawBitmap(resize1, width / 2 - resize1.getWidth() / 2, 0, p);
					} else {
						p.setAlpha(255);
						imgBit = wallList.get(wallId);
//						Bitmap resize1=getResizedBitmap(imgBit,height,width);
						Bitmap resize1 = resize(imgBit);
						if (resize1.getHeight() < height - 20)
							c.drawBitmap(resize1, width / 2 - resize1.getWidth() / 2, height / 2 - resize1.getHeight() / 2, p);
						else
							c.drawBitmap(resize1, width / 2 - resize1.getWidth() / 2, 0, p);
					}

				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					e.printStackTrace();
				} finally {
					try {
						if (c != null)
							surfaceHolder.unlockCanvasAndPost(c);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			mHandler.removeCallbacks(mUpdateDisplay);
			if (mVisible) {
				mHandler.postDelayed(mUpdateDisplay, 100);
			}
		}

		private void baseImage(final boolean needToRefresh) {

			if (wallList != null)
				wallList.clear();

			String wallImage;
			if (isPreview()) {
				wallImage = setting.getAutoWallImageTemp();
			} else {
				wallImage = setting.getAutoWallImage();
			}
			String[] files = wallImage.split("#");

			File file;
			for (int i = 0; i < files.length; i++) {
				file = new File(files[i]);
				if (file.exists()) {
					try {
						RequestOptions requestOptions = new RequestOptions();
						requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
								.skipMemoryCache(true);
						requestOptions.centerCrop();
						Glide.with(getApplicationContext().getApplicationContext())
								.asBitmap()
								.load(Uri.fromFile(file)).apply(requestOptions)
								.into(new SimpleTarget<Bitmap>(width, height) {
									@Override
									public void onResourceReady(Bitmap bitmap,
																Transition<? super Bitmap> transition) {

										if(wallList == null)
											wallList=new ArrayList<>();

										if (wallList != null) {
											wallList.clear();
											wallList.add(bitmap);
											if (needToRefresh) {
												last_auto_wallpaper_change_time = 0;
												setting.setLastAutoChangedTime();
												draw();
											}
										}
									}
								});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		private Bitmap resize(Bitmap image) {
			return image;
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			Logger.e("AutoWallpaperService", "onVisibilityChanged:" + visible);
			if(!isPreview())
				isServiceRunning=true;
			mVisible = visible;
			if (visible) {
				if (isPreview()) {
					try {
						frameDuration = AppConstant.time_frame_duration[setting
								.getTimeDurationIndexTemp()];
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						frameDuration = AppConstant.time_frame_duration[setting
								.getTimeDurationIndex()];
					} catch (Exception e) {
						e.printStackTrace();
					}
					Logger.i("WallList", "AutoWallpaperChanger WallList : " + wallList.size());
				}
				draw();
				//isWallpaperChange = true;
				long currentTime = Calendar.getInstance().getTimeInMillis();
				if (currentTime - getLast_auto_wallpaper_change_time() > frameDuration) {
					isWallpaperChange = true;
				} else {
					isWallpaperChange = false;
				}
				Logger.e("AutoWallpaperService", (isPreview() ? "Preview : " : "") + "onVisibilityChanged:" + visible + " isWallpaperChange:" + isWallpaperChange);
				if (isWallpaperChange) {
					saveToStore();
				}
			} else {
				mHandler.removeCallbacks(mUpdateDisplay);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
									 int width, int height) {
			Logger.e("AutoWallpaperService", "onSurfaceChanged");
			draw();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			Logger.e("AutoWallpaperService", "onSurfaceDestroyed");
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if(!isPreview())
				isServiceRunning=false;
			Logger.e("AutoWallpaperService", "onDestroy");
			mVisible = false;
			unregisterUpdateListener();
			mHandler.removeCallbacks(mUpdateDisplay);
			if (wallList != null) {
				wallList.clear();
				wallList = null;
				CommonFunctions.garbageCollector(true);
			}
		}

		private void saveToStore() {
			if (CommonFunctions.isNetworkAvailable(AutoWallpaperChangerService.this)) {
				setting = SettingStore.getInstance(getApplicationContext());
				String store;
				if (isPreview()) {
					store = setting.getAutoCategoryTemp();
				} else {
					store = setting.getCatIdAuto();
				}
				Log.i("isImageLoading", "AutoWallpaperChanges: " + isImageLoading);
				if (!TextUtils.isEmpty(store) && !isImageLoading) {
					isImageLoading = true;
					RequestManager manager = new RequestManager(AutoWallpaperChangerService.this);
					manager.autoWallpaperService(store, new NetworkCommunicationManager.CommunicationListnerNew() {
						@Override
						public void onSuccess(IModel response, int operationCode) {
							try {
								WallInfoModel model = (WallInfoModel) response;
								if (model != null && model.getStatus().equalsIgnoreCase("1")) {
									final Wallpapers post = model.getPost().get(0);
									new Thread() {
										@Override
										public void run() {
											super.run();
											downloadImage(post);
											isImageLoading = false;
										}
									}.start();
								}
							} catch (Exception e) {
								isImageLoading = false;
								e.printStackTrace();
							}
						}

						@Override
						public void onStartLoading() {

						}

						@Override
						public void onFail(WebServiceError errorMsg) {
							isImageLoading = false;
						}
					});
				}
			}
		}

		private void downloadImage(Wallpapers post) {
			try {
				String path= CommonFunctions.getDomainImages()+"uhd/"+post.getImg();
				RequestOptions requestOptions = new RequestOptions();
				requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true);
				requestOptions.centerCrop();

				Logger.i("Notiifcation", "Notification ImgPath: " + path);

				Glide.with(getApplicationContext().getApplicationContext())
						.asBitmap()
						.load(path).apply(requestOptions)
						.into(new SimpleTarget<Bitmap>(width, height) {
							@Override
							public void onResourceReady(Bitmap bitmap,
														Transition<? super Bitmap> transition) {
								if (wallList != null)
									wallList.clear();

								String local_path = saveImage(bitmap);
								if (!TextUtils.isEmpty(local_path)) {
									if (isPreview()) {
										setting.setAutoWallImageTemp(local_path);
									} else {
										setting.setAutoWallImage(local_path);
									}
								}

								wallId = 0;
								if (wallList != null) {
									wallList.clear();
								}else
									wallList=new ArrayList<>();

								if(bitmap!=null)
									wallList.add(bitmap);

								draw();
								if (isPreview()) {
									setting.setLastAutoChangedTimeTemp();
								} else {
									setting.setLastAutoChangedTime();
								}
								last_auto_wallpaper_change_time = 0;
								isWallpaperChange = false;
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private String saveImage(Bitmap finalBitmap) {
			String name = CommonFunctions.createAutoChagerDirCache() + "/" + AppConstant.AUTO_WALLPAPER_FILE_NAME;
			File file = new File(name);
			if (file.exists())
				file.delete();
			try {
				FileOutputStream out = new FileOutputStream(file);
				finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
				out.flush();
				out.close();
				return file.getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private void registerUpdateListener() {
			if (isPreview())
				return;
			EventNotifier notifier =
					NotifierFactory.getInstance().getNotifier(
							NotifierFactory.EVENT_NOTIFIER_UPDATE_FOURK_WALL);
			notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);
		}

		private void unregisterUpdateListener() {
			if (isPreview())
				return;
			EventNotifier notifier =
					NotifierFactory.getInstance().getNotifier(
							NotifierFactory.EVENT_NOTIFIER_UPDATE_FOURK_WALL);
			notifier.unRegisterListener(this);
		}

		@Override
		public int eventNotify(int eventType, Object eventObject) {
			switch (eventType) {
				case EventTypes.EVENT_UPDATE_AUTO_WALL:
					String name1 = CommonFunctions.createAutoChagerDirCache() + "/" + AppConstant.AUTO_WALLPAPER_FILE_NAME;
					File file1 = new File(name1);
					if (file1.exists()) {
						Logger.e("WallList", "eventNotify 1");
						baseImage(true);

					} else {
						Logger.e("WallList", "eventNotify 2");
						last_auto_wallpaper_change_time = 0;
						setting.resetLastAutoChangedTime();
					}
					break;
			}
			return 0;
		}
	}
}
