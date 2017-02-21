/*
 * Copyright (c) 2017.
 * Modified by Marcelo Benites on 06/01/2017.
 */

package cm.aptoide.pt.v8engine.payment;

import android.content.Context;
import cm.aptoide.accountmanager.AptoideAccountManager;
import rx.Observable;

/**
 * Created by marcelobenites on 06/01/17.
 */
public class Payer {

  public final Context context;

  public Payer(Context context) {
    this.context = context;
  }

  public String getId() {
    return AptoideAccountManager.getUserEmail();
  }

  public boolean isLoggedIn() {
    return AptoideAccountManager.isLoggedIn();
  }

  public Observable<Void> login() {
    return AptoideAccountManager.login(context);
  }
}
