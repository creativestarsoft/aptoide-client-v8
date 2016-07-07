/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 25/05/2016.
 */

package cm.aptoide.pt.dataprovider.ws.v7;

import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.database.realm.Store;
import cm.aptoide.pt.dataprovider.DataProvider;
import io.realm.Realm;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Converter;

/**
 * Created by neuro on 23-05-2016.
 */
public abstract class BaseRequestWithStore<U, B extends BaseBodyWithStore> extends V7<U, B> {

	public BaseRequestWithStore(B body, OkHttpClient httpClient, Converter.Factory converterFactory, String baseHost) {
		super(body, httpClient, converterFactory, baseHost);
	}

	protected static StoreCredentials getStore(Long storeId) {
		@Cleanup Realm realm = Database.get(DataProvider.getContext());

		if (storeId != null) {
			Store store = Database.StoreQ.get(storeId, realm);
			if (store != null) {
				return new StoreCredentials(store.getUsername(), store.getPasswordSha1());
			}
		}
		return new StoreCredentials();
	}

	protected static StoreCredentials getStore(String storeName) {
		@Cleanup Realm realm = Database.get(DataProvider.getContext());
		if (storeName != null) {
			Store store = Database.StoreQ.get(storeName, realm);
			if (store != null) {
				return new StoreCredentials(store.getUsername(), store.getPasswordSha1());
			}
		}
		return new StoreCredentials();
	}

	@AllArgsConstructor
	public static class StoreCredentials {

		@Getter private final String username;
		@Getter private final String passwordSha1;

		public StoreCredentials() {
			username = null;
			passwordSha1 = null;
		}
	}
}