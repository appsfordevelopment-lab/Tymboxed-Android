package dev.ambitionsoftware.tymeboxed.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.SessionDao;
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
public final class SessionRepository_Factory implements Factory<SessionRepository> {
  private final Provider<SessionDao> sessionDaoProvider;

  public SessionRepository_Factory(Provider<SessionDao> sessionDaoProvider) {
    this.sessionDaoProvider = sessionDaoProvider;
  }

  @Override
  public SessionRepository get() {
    return newInstance(sessionDaoProvider.get());
  }

  public static SessionRepository_Factory create(Provider<SessionDao> sessionDaoProvider) {
    return new SessionRepository_Factory(sessionDaoProvider);
  }

  public static SessionRepository newInstance(SessionDao sessionDao) {
    return new SessionRepository(sessionDao);
  }
}
