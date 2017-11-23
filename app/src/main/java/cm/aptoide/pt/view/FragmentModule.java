package cm.aptoide.pt.view;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.View;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.account.AccountAnalytics;
import cm.aptoide.pt.account.view.AccountErrorMapper;
import cm.aptoide.pt.account.view.AccountNavigator;
import cm.aptoide.pt.account.view.ImagePickerNavigator;
import cm.aptoide.pt.account.view.ImagePickerPresenter;
import cm.aptoide.pt.account.view.ImagePickerView;
import cm.aptoide.pt.account.view.ImageValidator;
import cm.aptoide.pt.account.view.PhotoFileGenerator;
import cm.aptoide.pt.account.view.UriToPathResolver;
import cm.aptoide.pt.account.view.store.ManageStoreNavigator;
import cm.aptoide.pt.account.view.store.ManageStorePresenter;
import cm.aptoide.pt.account.view.store.ManageStoreView;
import cm.aptoide.pt.account.view.store.StoreManager;
import cm.aptoide.pt.account.view.user.CreateUserErrorMapper;
import cm.aptoide.pt.account.view.user.ManageUserNavigator;
import cm.aptoide.pt.account.view.user.ManageUserPresenter;
import cm.aptoide.pt.account.view.user.ManageUserView;
import cm.aptoide.pt.ads.AdsRepository;
import cm.aptoide.pt.analytics.Analytics;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.database.accessors.StoreAccessor;
import cm.aptoide.pt.dataprovider.WebService;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.networking.RefreshTokenInvalidator;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.permission.AccountPermissionProvider;
import cm.aptoide.pt.presenter.LoginSignUpCredentialsPresenter;
import cm.aptoide.pt.presenter.LoginSignUpCredentialsView;
import cm.aptoide.pt.search.SearchAnalytics;
import cm.aptoide.pt.search.SearchManager;
import cm.aptoide.pt.search.SearchNavigator;
import cm.aptoide.pt.search.model.SearchAdResult;
import cm.aptoide.pt.search.model.SearchAppResult;
import cm.aptoide.pt.search.view.SearchResultPresenter;
import cm.aptoide.pt.search.view.SearchView;
import cm.aptoide.pt.store.StoreUtils;
import com.facebook.appevents.AppEventsLogger;
import com.jakewharton.rxrelay.PublishRelay;
import dagger.Module;
import dagger.Provides;
import java.util.Arrays;
import javax.inject.Named;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module public class FragmentModule {

  private final Fragment fragment;
  private final boolean isMultiStoreSearch;
  private final boolean dismissToNavigateToMainView;
  private final boolean navigateToHome;
  private final boolean goToHome;
  private final boolean isEditProfile;
  private final boolean isCreateStoreUserPrivacyEnabled;
  private final String packageName;
  private final String defaultStoreName;
  private final String defaultThemeName;


  public FragmentModule(Fragment fragment, boolean isMultiStoreSearch,boolean dismissToNavigateToMainView, boolean navigateToHome, boolean goToHome,
      boolean isEditProfile, boolean isCreateStoreUserPrivacyEnabled, String packageName, String defaultStoreName, String defaultThemeName) {
    this.fragment = fragment;
    this.isMultiStoreSearch = isMultiStoreSearch;
    this.dismissToNavigateToMainView = dismissToNavigateToMainView;
    this.navigateToHome = navigateToHome;
    this.goToHome = goToHome;
    this.isEditProfile = isEditProfile;
    this.isCreateStoreUserPrivacyEnabled = isCreateStoreUserPrivacyEnabled;
    this.packageName = packageName;
    this.defaultStoreName = defaultStoreName;
    this.defaultThemeName = defaultThemeName;
  }

  @Provides @FragmentScope LoginSignUpCredentialsPresenter provideLoginSignUpPresenter(AptoideAccountManager accountManager, AccountNavigator accountNavigator,
      AccountErrorMapper errorMapper, AccountAnalytics accountAnalytics){
    return new LoginSignUpCredentialsPresenter((LoginSignUpCredentialsView) fragment, accountManager, CrashReport.getInstance(),
        dismissToNavigateToMainView, navigateToHome, accountNavigator,
        Arrays.asList("email", "user_friends"), Arrays.asList("email"), errorMapper,
        accountAnalytics);
  }

  @Provides @FragmentScope ImagePickerPresenter provideImagePickerPresenter(AccountPermissionProvider accountPermissionProvider,
      PhotoFileGenerator photoFileGenerator, ImageValidator imageValidator, UriToPathResolver uriToPathResolver, ImagePickerNavigator imagePickerNavigator){
    return new ImagePickerPresenter((ImagePickerView) fragment, CrashReport.getInstance(), accountPermissionProvider, photoFileGenerator,
        imageValidator, AndroidSchedulers.mainThread(), uriToPathResolver, imagePickerNavigator,
        fragment.getActivity().getContentResolver(), ImageLoader.with(fragment.getContext()));
  }

  @FragmentScope @Provides ManageStorePresenter provideManageStorePresenter(StoreManager storeManager, UriToPathResolver uriToPathResolver,
      ManageStoreNavigator manageStoreNavigator){
    return new ManageStorePresenter((ManageStoreView) fragment, CrashReport.getInstance(), storeManager, fragment.getResources(), uriToPathResolver,
        packageName, manageStoreNavigator, goToHome);
  }

  @FragmentScope @Provides ManageUserPresenter provideManageUserPresenter(AptoideAccountManager accountManager, CreateUserErrorMapper errorMapper,
      ManageUserNavigator manageUserNavigator, UriToPathResolver uriToPathResolver){
    return new ManageUserPresenter((ManageUserView) fragment, CrashReport.getInstance(), accountManager, errorMapper, manageUserNavigator,
        isEditProfile, uriToPathResolver, isCreateStoreUserPrivacyEnabled);
  }

  @FragmentScope @Provides ImageValidator provideImageValidator(){
    return new ImageValidator(ImageLoader.with(fragment.getContext()), Schedulers.computation());
  }

  @FragmentScope @Provides CreateUserErrorMapper provideCreateUserErrorMapper(){
    return new CreateUserErrorMapper(fragment.getContext(), new AccountErrorMapper(fragment.getContext()), fragment.getResources());
  }
}
