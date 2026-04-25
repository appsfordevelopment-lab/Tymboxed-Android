package dev.ambitionsoftware.tymeboxed.service;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppSessionController_Factory implements Factory<AppSessionController> {
  private final Provider<Context> appContextProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  public AppSessionController_Factory(Provider<Context> appContextProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    this.appContextProvider = appContextProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public AppSessionController get() {
    return newInstance(appContextProvider.get(), sessionRepositoryProvider.get());
  }

  public static AppSessionController_Factory create(Provider<Context> appContextProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new AppSessionController_Factory(appContextProvider, sessionRepositoryProvider);
  }

  public static AppSessionController newInstance(Context appContext,
      SessionRepository sessionRepository) {
    return new AppSessionController(appContext, sessionRepository);
  }
}
