package dev.ambitionsoftware.tymeboxed.ui.screens.home;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
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

  private final Provider<Context> appContextProvider;

  public HomeViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider, Provider<Context> appContextProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(profileRepositoryProvider.get(), sessionRepositoryProvider.get(), appPreferencesProvider.get(), appContextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider, Provider<Context> appContextProvider) {
    return new HomeViewModel_Factory(profileRepositoryProvider, sessionRepositoryProvider, appPreferencesProvider, appContextProvider);
  }

  public static HomeViewModel newInstance(ProfileRepository profileRepository,
      SessionRepository sessionRepository, AppPreferences appPreferences, Context appContext) {
    return new HomeViewModel(profileRepository, sessionRepository, appPreferences, appContext);
  }
}
