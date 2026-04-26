package dev.ambitionsoftware.tymeboxed.ui.screens.home;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences;
import dev.ambitionsoftware.tymeboxed.data.repository.AuthRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import dev.ambitionsoftware.tymeboxed.service.AppSessionController;
import dev.ambitionsoftware.tymeboxed.service.SessionReminderScheduler;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<AppSessionController> appSessionControllerProvider;

  private final Provider<SessionReminderScheduler> sessionReminderSchedulerProvider;

  private final Provider<Context> appContextProvider;

  public HomeViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider,
      Provider<SessionReminderScheduler> sessionReminderSchedulerProvider,
      Provider<Context> appContextProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.appSessionControllerProvider = appSessionControllerProvider;
    this.sessionReminderSchedulerProvider = sessionReminderSchedulerProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(profileRepositoryProvider.get(), sessionRepositoryProvider.get(), appPreferencesProvider.get(), authRepositoryProvider.get(), appSessionControllerProvider.get(), sessionReminderSchedulerProvider.get(), appContextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider,
      Provider<SessionReminderScheduler> sessionReminderSchedulerProvider,
      Provider<Context> appContextProvider) {
    return new HomeViewModel_Factory(profileRepositoryProvider, sessionRepositoryProvider, appPreferencesProvider, authRepositoryProvider, appSessionControllerProvider, sessionReminderSchedulerProvider, appContextProvider);
  }

  public static HomeViewModel newInstance(ProfileRepository profileRepository,
      SessionRepository sessionRepository, AppPreferences appPreferences,
      AuthRepository authRepository, AppSessionController appSessionController,
      SessionReminderScheduler sessionReminderScheduler, Context appContext) {
    return new HomeViewModel(profileRepository, sessionRepository, appPreferences, authRepository, appSessionController, sessionReminderScheduler, appContext);
  }
}
