/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 11/05/2016.
 */

package cm.aptoide.pt.v8engine.view.recycler.widget.implementations.grid;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cm.aptoide.pt.model.v7.store.Store;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.fragment.implementations.StoreFragment;
import cm.aptoide.pt.v8engine.util.CircleTransform;
import cm.aptoide.pt.v8engine.util.FragmentUtils;
import cm.aptoide.pt.v8engine.util.StoreThemeEnum;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid
		.SubscribedStoreDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.widget.Displayables;
import cm.aptoide.pt.v8engine.view.recycler.widget.Widget;

/**
 * Created by neuro on 11-05-2016. //todo: código duplicado, se cair a reflexão, deixa de o ser.
 */
@Displayables({SubscribedStoreDisplayable.class})
public class UnsubscribedStoreWidget extends Widget<SubscribedStoreDisplayable> {

	private static final String TAG = GridStoreWidget.class.getSimpleName();

	private ImageView storeAvatar;
	private TextView storeName;
	private TextView storeUnsubscribe;
	private LinearLayout storeLayout;
	private View infoLayout;

	public UnsubscribedStoreWidget(View itemView) {
		super(itemView);
	}

	@Override
	protected void assignViews(View itemView) {
		storeAvatar = (ImageView) itemView.findViewById(R.id.store_avatar_row);
		storeName = (TextView) itemView.findViewById(R.id.store_name_row);
		storeUnsubscribe = (TextView) itemView.findViewById(R.id.store_unsubscribe_row);
		storeLayout = (LinearLayout) itemView.findViewById(R.id.store_main_layout_row);
		infoLayout = itemView.findViewById(R.id.store_layout_subscribers);
	}

	@Override
	public void bindView(SubscribedStoreDisplayable displayable) {

		final Context context = itemView.getContext();
		final Store store = displayable.getPojo();

		storeName.setText(store.getName());
		infoLayout.setVisibility(View.GONE);

		@ColorInt int color = context.getResources()
				.getColor(StoreThemeEnum.get(store.getAppearance().getTheme()).getStoreHeader());
		storeLayout.setBackgroundColor(color);
		storeLayout.setOnClickListener(v -> FragmentUtils.replaceFragment((FragmentActivity) v
				.getContext(), StoreFragment
				.newInstance(displayable.getPojo().getName())));

		if (store.getId() == -1 || TextUtils.isEmpty(store.getAvatar())) {
			Glide.with(context)
					.fromResource()
					.load(R.drawable.ic_avatar_apps)
					.transform(new CircleTransform(context))
					.into(storeAvatar);
		} else {
			Glide.with(context)
					.load(store.getAvatar())
					.transform(new CircleTransform(context))
					.into(storeAvatar);
		}

		storeUnsubscribe.setOnClickListener(v -> {
			//todo
		});
	}
}