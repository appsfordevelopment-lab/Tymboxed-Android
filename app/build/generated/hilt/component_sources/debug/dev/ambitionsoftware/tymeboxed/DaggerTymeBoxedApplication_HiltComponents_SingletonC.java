package dev.ambitionsoftware.tymeboxed;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase;
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao;
import dev.ambitionsoftware.tymeboxed.data.db.dao.SessionDao;
import dev.ambitionsoftware.tymeboxed.data.db.dao.TagDao;
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences;
import dev.ambitionsoftware.tymeboxed.data.repository.AuthRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import dev.ambitionsoftware.tymeboxed.di.DatabaseModule_ProvideDatabaseFactory;
import dev.ambitionsoftware.tymeboxed.di.DatabaseModule_ProvideProfileDaoFactory;
import dev.ambitionsoftware.tymeboxed.di.DatabaseModule_ProvideSessionDaoFactory;
import dev.ambitionsoftware.tymeboxed.di.DatabaseModule_ProvideTagDaoFactory;
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsCoordinator;
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel;
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.service.BootCompletedReceiver;
import dev.ambitionsoftware.tymeboxed.service.BootCompletedReceiver_MembersInjector;
import dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.inapp.InAppBlockingViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.inapp.InAppBlockingViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroAuthViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroAuthViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileSessionsViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileSessionsViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel;
import dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel_HiltModules;
import dev.ambitionsoftware.tymeboxed.ui.theme.ThemeController;
import dev.ambitionsoftware.tymeboxed.ui.theme.ThemeViewModel;
import dev.ambitionsoftware.tymeboxed.ui.theme.ThemeViewModel_HiltModules;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DaggerTymeBoxedApplication_HiltComponents_SingletonC {
  private DaggerTymeBoxedApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TymeBoxedApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TymeBoxedApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TymeBoxedApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TymeBoxedApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TymeBoxedApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TymeBoxedApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TymeBoxedApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TymeBoxedApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TymeBoxedApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TymeBoxedApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TymeBoxedApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TymeBoxedApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TymeBoxedApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(9).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel, InAppBlockingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel, IntroAuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel, PermissionsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel, ProfileEditViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel, ProfileInsightsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel, ProfileSessionsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel, ThemeViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectAppPreferences(instance, singletonCImpl.appPreferencesProvider.get());
      MainActivity_MembersInjector.injectPermissionsCoordinator(instance, singletonCImpl.permissionsCoordinatorProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileSessionsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.inapp.InAppBlockingViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel";

      static String dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel = "dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroAuthViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel = "dev.ambitionsoftware.tymeboxed.ui.theme.ThemeViewModel";

      @KeepFieldType
      ProfileEditViewModel dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel2;

      @KeepFieldType
      ProfileSessionsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel2;

      @KeepFieldType
      ProfileInsightsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel2;

      @KeepFieldType
      SettingsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      InAppBlockingViewModel dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel2;

      @KeepFieldType
      HomeViewModel dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      PermissionsViewModel dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel2;

      @KeepFieldType
      IntroAuthViewModel dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel2;

      @KeepFieldType
      ThemeViewModel dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel2;
    }
  }

  private static final class ViewModelCImpl extends TymeBoxedApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<InAppBlockingViewModel> inAppBlockingViewModelProvider;

    private Provider<IntroAuthViewModel> introAuthViewModelProvider;

    private Provider<PermissionsViewModel> permissionsViewModelProvider;

    private Provider<ProfileEditViewModel> profileEditViewModelProvider;

    private Provider<ProfileInsightsViewModel> profileInsightsViewModelProvider;

    private Provider<ProfileSessionsViewModel> profileSessionsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<ThemeViewModel> themeViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.inAppBlockingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.introAuthViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.permissionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.profileEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.profileInsightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.profileSessionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.themeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(9).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel, ((Provider) inAppBlockingViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel, ((Provider) introAuthViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel, ((Provider) permissionsViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel, ((Provider) profileEditViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel, ((Provider) profileInsightsViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel, ((Provider) profileSessionsViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel, ((Provider) themeViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel = "dev.ambitionsoftware.tymeboxed.ui.theme.ThemeViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.inapp.InAppBlockingViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileSessionsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroAuthViewModel";

      static String dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel = "dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel";

      static String dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel = "dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsViewModel";

      @KeepFieldType
      ThemeViewModel dev_ambitionsoftware_tymeboxed_ui_theme_ThemeViewModel2;

      @KeepFieldType
      InAppBlockingViewModel dev_ambitionsoftware_tymeboxed_ui_screens_inapp_InAppBlockingViewModel2;

      @KeepFieldType
      ProfileSessionsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileSessionsViewModel2;

      @KeepFieldType
      SettingsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      ProfileEditViewModel dev_ambitionsoftware_tymeboxed_ui_screens_profile_ProfileEditViewModel2;

      @KeepFieldType
      IntroAuthViewModel dev_ambitionsoftware_tymeboxed_ui_screens_intro_IntroAuthViewModel2;

      @KeepFieldType
      PermissionsViewModel dev_ambitionsoftware_tymeboxed_permissions_PermissionsViewModel2;

      @KeepFieldType
      HomeViewModel dev_ambitionsoftware_tymeboxed_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      ProfileInsightsViewModel dev_ambitionsoftware_tymeboxed_ui_screens_insights_ProfileInsightsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.profileRepositoryProvider.get(), singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.appPreferencesProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // dev.ambitionsoftware.tymeboxed.ui.screens.inapp.InAppBlockingViewModel 
          return (T) new InAppBlockingViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroAuthViewModel 
          return (T) new IntroAuthViewModel(singletonCImpl.authRepositoryProvider.get());

          case 3: // dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel 
          return (T) new PermissionsViewModel(singletonCImpl.permissionsCoordinatorProvider.get());

          case 4: // dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditViewModel 
          return (T) new ProfileEditViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.profileRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsViewModel 
          return (T) new ProfileInsightsViewModel(singletonCImpl.sessionRepositoryProvider.get());

          case 6: // dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileSessionsViewModel 
          return (T) new ProfileSessionsViewModel(singletonCImpl.sessionRepositoryProvider.get());

          case 7: // dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.themeControllerProvider.get(), singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.profileRepositoryProvider.get(), singletonCImpl.tagDao());

          case 8: // dev.ambitionsoftware.tymeboxed.ui.theme.ThemeViewModel 
          return (T) new ThemeViewModel(singletonCImpl.themeControllerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TymeBoxedApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TymeBoxedApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends TymeBoxedApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TymeBoxedDatabase> provideDatabaseProvider;

    private Provider<PermissionsCoordinator> permissionsCoordinatorProvider;

    private Provider<AppPreferences> appPreferencesProvider;

    private Provider<ProfileRepository> profileRepositoryProvider;

    private Provider<SessionRepository> sessionRepositoryProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<ThemeController> themeControllerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private ProfileDao profileDao() {
      return DatabaseModule_ProvideProfileDaoFactory.provideProfileDao(provideDatabaseProvider.get());
    }

    private SessionDao sessionDao() {
      return DatabaseModule_ProvideSessionDaoFactory.provideSessionDao(provideDatabaseProvider.get());
    }

    private TagDao tagDao() {
      return DatabaseModule_ProvideTagDaoFactory.provideTagDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<TymeBoxedDatabase>(singletonCImpl, 0));
      this.permissionsCoordinatorProvider = DoubleCheck.provider(new SwitchingProvider<PermissionsCoordinator>(singletonCImpl, 1));
      this.appPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<AppPreferences>(singletonCImpl, 2));
      this.profileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ProfileRepository>(singletonCImpl, 3));
      this.sessionRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SessionRepository>(singletonCImpl, 4));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 5));
      this.themeControllerProvider = DoubleCheck.provider(new SwitchingProvider<ThemeController>(singletonCImpl, 6));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectTymeBoxedApplication(TymeBoxedApplication tymeBoxedApplication) {
    }

    @Override
    public TymeBoxedDatabase database() {
      return provideDatabaseProvider.get();
    }

    @Override
    public PermissionsCoordinator permissionsCoordinator() {
      return permissionsCoordinatorProvider.get();
    }

    @Override
    public void injectBootCompletedReceiver(BootCompletedReceiver bootCompletedReceiver) {
      injectBootCompletedReceiver2(bootCompletedReceiver);
    }

    @CanIgnoreReturnValue
    private BootCompletedReceiver injectBootCompletedReceiver2(BootCompletedReceiver instance) {
      BootCompletedReceiver_MembersInjector.injectDatabase(instance, provideDatabaseProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // dev.ambitionsoftware.tymeboxed.permissions.PermissionsCoordinator 
          return (T) new PermissionsCoordinator(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences 
          return (T) new AppPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository 
          return (T) new ProfileRepository(singletonCImpl.profileDao());

          case 4: // dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository 
          return (T) new SessionRepository(singletonCImpl.sessionDao());

          case 5: // dev.ambitionsoftware.tymeboxed.data.repository.AuthRepository 
          return (T) new AuthRepository();

          case 6: // dev.ambitionsoftware.tymeboxed.ui.theme.ThemeController 
          return (T) new ThemeController(singletonCImpl.appPreferencesProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
