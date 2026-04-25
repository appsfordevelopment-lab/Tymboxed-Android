package dev.ambitionsoftware.tymeboxed.service;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SessionTimerHandler_Factory implements Factory<SessionTimerHandler> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<AppSessionController> appSessionControllerProvider;

  public SessionTimerHandler_Factory(Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.appSessionControllerProvider = appSessionControllerProvider;
  }

  @Override
  public SessionTimerHandler get() {
    return newInstance(sessionRepositoryProvider.get(), appSessionControllerProvider.get());
  }

  public static SessionTimerHandler_Factory create(
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider) {
    return new SessionTimerHandler_Factory(sessionRepositoryProvider, appSessionControllerProvider);
  }

  public static SessionTimerHandler newInstance(SessionRepository sessionRepository,
      AppSessionController appSessionController) {
    return new SessionTimerHandler(sessionRepository, appSessionController);
  }
}
