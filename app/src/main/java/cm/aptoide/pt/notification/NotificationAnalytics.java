package cm.aptoide.pt.notification;

import android.os.Bundle;
import cm.aptoide.pt.analytics.Analytics;
import cm.aptoide.pt.analytics.events.FacebookEvent;
import cm.aptoide.pt.analytics.events.KnockEvent;
import com.facebook.appevents.AppEventsLogger;
import okhttp3.OkHttpClient;

/**
 * Created by trinkes on 18/09/2017.
 */

public class NotificationAnalytics {

  private static final String NOTIFICATION_RECEIVED = "Aptoide_Push_Notification_Received";
  private static final String NOTIFICATION_IMPRESSION = "Aptoide_Push_Notification_Impression";
  private static final String NOTIFICATION_PRESSED = "Aptoide_Push_Notification_Click";

  private static final String TYPE = "type";
  private static final String AB_TESTING_GROUP = "ab_testing_group";
  private static final String PACKAGE_NAME = "package_name";
  private static final String CAMPAIGN_ID = "campaign_id";
  private final Analytics analytics;
  private OkHttpClient client;
  private AppEventsLogger facebook;

  public NotificationAnalytics(OkHttpClient client, Analytics analytics, AppEventsLogger facebook) {
    this.client = client;
    this.analytics = analytics;
    this.facebook = facebook;
  }

  public void sendNotificationTouchEvent(String url) {
    analytics.sendEvent(new KnockEvent(url, client));
  }

  public void sendUpdatesNotificationReceivedEvent() {
    analytics.sendEvent(
        new FacebookEvent(facebook, NOTIFICATION_RECEIVED, createUpdateNotificationEventsBundle()));
  }

  public void sendUpdatesNotificationClickEvent() {
    analytics.sendEvent(
        new FacebookEvent(facebook, NOTIFICATION_PRESSED, createUpdateNotificationEventsBundle()));
  }

  public void sendPushNotificationReceivedEvent(@AptoideNotification.NotificationType int type,
      String abTestingGroup, int campaignId, String url) {
    analytics.sendEvent(new FacebookEvent(facebook, NOTIFICATION_RECEIVED,
        createPushNotificationEventBundle(type, abTestingGroup, campaignId, url)));
  }

  public void sendPushNotficationImpressionEvent(@AptoideNotification.NotificationType int type,
      String abTestingGroup, int campaignId, String url) {
    analytics.sendEvent(new FacebookEvent(facebook, NOTIFICATION_IMPRESSION,
        createPushNotificationEventBundle(type, abTestingGroup, campaignId, url)));
  }

  public void sendPushNotificationPressedEvent(@AptoideNotification.NotificationType int type,
      String abTestingGroup, int campaignId, String url) {
    analytics.sendEvent(new FacebookEvent(facebook, NOTIFICATION_PRESSED,
        createPushNotificationEventBundle(type, abTestingGroup, campaignId, url)));
  }

  private Bundle createUpdateNotificationEventsBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(TYPE, NotificationTypes.UPDATES.toString()
        .toLowerCase());
    return bundle;
  }

  private Bundle createPushNotificationEventBundle(@AptoideNotification.NotificationType int type,
      String abTestingGroup, int campaignId, String url) {
    Bundle bundle = new Bundle();
    bundle.putInt(CAMPAIGN_ID, campaignId);
    bundle.putString(TYPE, matchNotificationTypeToString(type).toString()
        .toLowerCase());
    bundle = addToBundleIfNotNull(bundle, abTestingGroup, getPackageNameFromUrl(url));
    return bundle;
  }

  private Bundle addToBundleIfNotNull(Bundle bundle, String abTestingGroup, String url) {
    if (abTestingGroup != null && !abTestingGroup.isEmpty()) {
      bundle.putString(AB_TESTING_GROUP, abTestingGroup);
    }
    if (url != null && !url.isEmpty()) {
      bundle.putString(PACKAGE_NAME, getPackageNameFromUrl(url));
    }
    return bundle;
  }

  private NotificationTypes matchNotificationTypeToString(int type) {
    switch (type) {
      case 0:
        return NotificationTypes.CAMPAIGN;
      case 1:
        return NotificationTypes.LIKE;
      case 2:
        return NotificationTypes.COMMENT;
      case 3:
        return NotificationTypes.POPULAR;
      case 4:
        return NotificationTypes.NEW_FOLLOWER;
      case 5:
        return NotificationTypes.NEW_SHARE;
      case 6:
        return NotificationTypes.NEW_ACTIVITY;
    }
    return NotificationTypes.OTHER;
  }

  private String getPackageNameFromUrl(String url) {
    String[] split = url.split("&");
    for (String part : split) {
      if (part.contains("package")) {
        return part.split("=")[1];
      }
    }
    return "";
  }

  private enum NotificationTypes {
    CAMPAIGN, LIKE, COMMENT, POPULAR, NEW_FOLLOWER, NEW_SHARE, NEW_ACTIVITY, OTHER, UPDATES
  }
}
