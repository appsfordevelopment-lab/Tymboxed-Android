package dev.ambitionsoftware.tymeboxed.ui.screens.profile;

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
public final class ProfileSessionsViewModel_Factory implements Factory<ProfileSessionsViewModel> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  public ProfileSessionsViewModel_Factory(Provider<SessionRepository> sessionRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public ProfileSessionsViewModel get() {
    return newInstance(sessionRepositoryProvider.get());
  }

  public static ProfileSessionsViewModel_Factory create(
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new ProfileSessionsViewModel_Factory(sessionRepositoryProvider);
  }

  public static ProfileSessionsViewModel newInstance(SessionRepository sessionRepository) {
    return new ProfileSessionsViewModel(sessionRepository);
  }
}
