/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 19/07/2016.
 */

package cm.aptoide.pt.downloadmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.database.realm.FileToDownload;
import cm.aptoide.pt.downloadmanager.interfaces.DownloadNotificationActionsInterface;
import cm.aptoide.pt.downloadmanager.interfaces.DownloadSettingsInterface;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.utils.FileUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import rx.Observable;

/**
 * Created by trinkes on 5/13/16.
 */
public class AptoideDownloadManager {

	public static final String APP_ID_EXTRA = "APTOIDE_APPID_EXTRA";
	public static final String DOWNLOADMANAGER_ACTION_PAUSE = "cm.aptoide.downloadmanager.action.pause";
	public static final String DOWNLOADMANAGER_ACTION_OPEN = "cm.aptoide.downloadmanager.action.open";
	public static final String DOWNLOADMANAGER_ACTION_START_DOWNLOAD = "cm.aptoide.downloadmanager.action.start.download";
	public static final String DOWNLOADMANAGER_ACTION_NOTIFICATION = "cm.aptoide.downloadmanager.action.notification";
	static public final int PROGRESS_MAX_VALUE = 100;
	private static final String TAG = AptoideDownloadManager.class.getSimpleName();
	/***********
	 * Paths
	 *****************/
	static String DOWNLOADS_STORAGE_PATH;
	static String APK_PATH;
	static String OBB_PATH;
	static String GENERIC_PATH;
	private static AptoideDownloadManager instance;
	private static Context context;
	private boolean isDownloading = false;
	@Getter(AccessLevel.MODULE) private DownloadNotificationActionsInterface downloadNotificationActionsInterface;
	@Getter(AccessLevel.MODULE) private DownloadSettingsInterface settingsInterface;

	public static Context getContext() {
		return context;
	}

	public static AptoideDownloadManager getInstance() {
		if (instance == null) {
			instance = new AptoideDownloadManager();
		}
		return instance;
	}

	private static Observable<Download> getDownload(Realm realm, long appId) {
		return Observable.fromCallable(() -> realm.where(Download.class).equalTo("appId", appId).findFirst()).flatMap(download -> {
			if (download == null || (download.getOverallDownloadStatus() == Download.COMPLETED && getInstance().getStateIfFileExists(download) == Download
					.FILE_MISSING)) {
				return Observable.error(new DownloadNotFoundException());
			}
			return download.asObservable();
		});
	}

	void initDownloadService(Context context) {
		AptoideDownloadManager.context = context;
		createDownloadDirs();
	}

	private void createDownloadDirs() {
		FileUtils.createDir(APK_PATH);
		FileUtils.createDir(OBB_PATH);
		FileUtils.createDir(GENERIC_PATH);
	}


	/**
	 * @param download info about the download to be made.
	 *
	 * @return Observable to be subscribed if download updates needed or null if download is done already
	 *
	 * @throws IllegalArgumentException if the appToDownload object is not filled correctly, this exception will be
	 *                                  thrown with the cause in the detail message.
	 */
	public Observable<Download> startDownload(Download download) throws IllegalArgumentException {
		if (getDownloadStatus(download.getAppId()) == Download.COMPLETED) {
			return Observable.fromCallable(() -> download);
		}
		startNewDownload(download);
		return getDownload(download.getAppId());
	}

	private void startNewDownload(Download download) {
		download.setOverallDownloadStatus(Download.IN_QUEUE);
		download.setOverallProgress(0);
		@Cleanup
		Realm realm = Database.get();
		Database.save(download, realm);

		startNextDownload();
	}

	public void pauseDownload(long appId) {
		@Cleanup Realm realm = Database.get();
		Download download = getStoredDownload(appId);
		if (download != null) {
			if (download.getOverallDownloadStatus() != Download.PROGRESS) {
				download.setOverallDownloadStatus(Download.PAUSED);
				Database.save(download, realm);
			}
			for (final FileToDownload fileToDownload : download.getFilesToDownload()) {
				FileDownloader.getImpl().pause(fileToDownload.getDownloadId());
			}
		}
	}

	/**
	 * Observe changes to a download. This observable never completes it will emmit items whenever the download state changes.
	 *
	 * @param appId
	 *
	 * @return observable for download state changes.
	 */
	public Observable<Download> getDownload(long appId) {
		@Cleanup
		Realm realm = Database.get();
		return getDownload(realm, appId).takeUntil(download -> download.getOverallDownloadStatus() == Download.COMPLETED);
	}

	Observable<Download> getCurrentDownload() {
		return getDownloads().flatMapIterable(downloads -> downloads).filter(downloads -> downloads.getOverallDownloadStatus() == Download.PROGRESS);
	}

	Observable<List<Download>> getCurrentDownloads() {
		@Cleanup
		Realm realm = Database.get();
		RealmResults<Download> download = realm.where(Download.class).findAll();
		if (download.size() >= 0) {
			return download.asObservable().map(results -> {
				List<Download> list = new ArrayList<>();
				for (final Download result : results) {
					if (result.getOverallDownloadStatus() == Download.PROGRESS || result.getOverallDownloadStatus() == Download.IN_QUEUE || result
							.getOverallDownloadStatus() == Download.PENDING) {
						list.add(result.clone());
					}
				}
				return list;
			});
		} else {
			return Observable.error(new DownloadNotFoundException());
		}
	}

	Observable<List<Download>> getDownloads() {
		@Cleanup
		Realm realm = Database.get();
		RealmResults<Download> download = realm.where(Download.class).findAll();
		if (download.size() >= 0) {
			return download.asObservable().map(results -> {
				List<Download> list = new ArrayList<>();
				for (final Download result : results) {
					list.add(result.clone());
				}
				return list;
			});
		} else {
			return Observable.error(new DownloadNotFoundException());
		}
	}

	/**
	 * Pause all the downloads
	 */
	public void pauseAllDownloads() {
		FileDownloader.getImpl().pauseAll();
		getCurrentDownloads().first().subscribe(downloads -> {
			@Cleanup
			Realm realm = Database.get();
			for (final Download download : downloads) {
				download.setOverallDownloadStatus(Download.PAUSED);
				Database.save(download, realm);
			}
		}, throwable -> {
			Logger.d(TAG, "pauseAllDownloads: ");
			throwable.printStackTrace();
		});
	}

	private int getDownloadStatus(long appId) {
		Download download = getStoredDownload(appId);
		if (download != null) {
			if (download.getOverallDownloadStatus() == Download.COMPLETED) {
				return getStateIfFileExists(download);
			}
			return download.getOverallDownloadStatus();
		} else {
			return Download.NOT_DOWNLOADED;
		}
	}

	public void init(Context context, DownloadNotificationActionsInterface downloadNotificationActionsInterface, DownloadSettingsInterface settingsInterface) {
		FileDownloader.init(context);
		this.downloadNotificationActionsInterface = downloadNotificationActionsInterface;
		this.settingsInterface = settingsInterface;

		DOWNLOADS_STORAGE_PATH = settingsInterface.getDownloadDir();
		APK_PATH = DOWNLOADS_STORAGE_PATH + "apks/";
		GENERIC_PATH = DOWNLOADS_STORAGE_PATH + "generic/";
		OBB_PATH = settingsInterface.getObbDir();
		if (TextUtils.isEmpty(OBB_PATH)) {
			OBB_PATH = GENERIC_PATH;
		}

	}

	@NonNull
	@Download.DownloadState
	int getStateIfFileExists(Download downloadToCheck) {
		@Download.DownloadState int downloadStatus = Download.COMPLETED;
		for (final FileToDownload fileToDownload : downloadToCheck.getFilesToDownload()) {
			if (!FileUtils.fileExists(fileToDownload.getFilePath())) {
				downloadStatus = Download.FILE_MISSING;
				break;
			}
		}
		return downloadStatus;
	}

	void currentDownloadFinished(long appId) {
		startNextDownload();
	}

	void startNextDownload() {
		if (!isDownloading) {
			Download nextDownload = getNextDownload();
			if (nextDownload != null) {
				isDownloading = true;
				new DownloadTask(nextDownload).startDownload();
			} else {
				CacheHelper.cleanCache(settingsInterface, DOWNLOADS_STORAGE_PATH);
			}
		}
	}

	/**
	 * check if there is any download in progress
	 *
	 * @return true if there is at least 1 download in progress, false otherwise
	 */
	public boolean isDownloading() {
		return isDownloading;
	}

	public void setDownloading(boolean downloading) {
		isDownloading = downloading;
	}

	public Download getNextDownload() {
		@Cleanup
		Realm realm = Database.get();
		RealmResults<Download> sortedDownloads = realm.where(Download.class)
				.equalTo("overallDownloadStatus", Download.IN_QUEUE)
				.findAllSorted("timeStamp", Sort.ASCENDING);
		if (sortedDownloads.size() > 0) {
			return sortedDownloads.get(0).clone();
		} else {
			return null;
		}
	}

	public void removeDownload(long appId) {
		@Cleanup
		Realm realm1 = Database.get();
		getDownload(realm1, appId).subscribe(download -> {
			for (final FileToDownload fileToDownload : download.getFilesToDownload()) {
				FileUtils.removeFile(fileToDownload.getFilePath());
			}
			@Cleanup
			Realm realm = Database.get();
			Database.delete(download, realm);
		}, Throwable::printStackTrace);
	}

	public Download getStoredDownload(long appId) {
		@Cleanup
		Realm realm = Database.get();
		Download download = realm.where(Download.class).equalTo("appId", appId).findFirst();
		return download != null ? download.clone() : null;
	}
}
