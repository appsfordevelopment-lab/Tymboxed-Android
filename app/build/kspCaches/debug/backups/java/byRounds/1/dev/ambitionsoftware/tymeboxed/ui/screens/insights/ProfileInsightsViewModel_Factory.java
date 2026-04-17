package dev.ambitionsoftware.tymeboxed.ui.screens.insights;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
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
public final class ProfileInsightsViewModel_Factory implements Factory<ProfileInsightsViewModel> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  public ProfileInsightsViewModel_Factory(Provider<SessionRepository> sessionRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public ProfileInsightsViewModel get() {
    return newInstance(sessionRepositoryProvider.get());
  }

  public static ProfileInsightsViewModel_Factory create(
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new ProfileInsightsViewModel_Factory(sessionRepositoryProvider);
  }

  public static ProfileInsightsViewModel newInstance(SessionRepository sessionRepository) {
    return new ProfileInsightsViewModel(sessionRepository);
  }
}
