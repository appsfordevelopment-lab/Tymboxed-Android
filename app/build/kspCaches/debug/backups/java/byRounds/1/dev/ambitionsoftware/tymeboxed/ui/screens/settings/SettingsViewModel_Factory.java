package dev.ambitionsoftware.tymeboxed.ui.screens.settings;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.TagDao;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import dev.ambitionsoftware.tymeboxed.ui.theme.ThemeController;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<ThemeController> themeControllerProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<ProfileRepository> profileRepositoryProvider;

  private final Provider<TagDao> tagDaoProvider;

  public SettingsViewModel_Factory(Provider<ThemeController> themeControllerProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ProfileRepository> profileRepositoryProvider, Provider<TagDao> tagDaoProvider) {
    this.themeControllerProvider = themeControllerProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.tagDaoProvider = tagDaoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(themeControllerProvider.get(), sessionRepositoryProvider.get(), profileRepositoryProvider.get(), tagDaoProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<ThemeController> themeControllerProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ProfileRepository> profileRepositoryProvider, Provider<TagDao> tagDaoProvider) {
    return new SettingsViewModel_Factory(themeControllerProvider, sessionRepositoryProvider, profileRepositoryProvider, tagDaoProvider);
  }

  public static SettingsViewModel newInstance(ThemeController themeController,
      SessionRepository sessionRepository, ProfileRepository profileRepository, TagDao tagDao) {
    return new SettingsViewModel(themeController, sessionRepository, profileRepository, tagDao);
  }
}
