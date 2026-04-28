package dev.ambitionsoftware.tymeboxed.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao;
import dev.ambitionsoftware.tymeboxed.service.ProfileScheduleAlarmScheduler;
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
public final class ProfileRepository_Factory implements Factory<ProfileRepository> {
  private final Provider<ProfileDao> profileDaoProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider;

  public ProfileRepository_Factory(Provider<ProfileDao> profileDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    this.profileDaoProvider = profileDaoProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.scheduleAlarmSchedulerProvider = scheduleAlarmSchedulerProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(profileDaoProvider.get(), sessionRepositoryProvider.get(), scheduleAlarmSchedulerProvider.get());
  }

  public static ProfileRepository_Factory create(Provider<ProfileDao> profileDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    return new ProfileRepository_Factory(profileDaoProvider, sessionRepositoryProvider, scheduleAlarmSchedulerProvider);
  }

  public static ProfileRepository newInstance(ProfileDao profileDao,
      SessionRepository sessionRepository, ProfileScheduleAlarmScheduler scheduleAlarmScheduler) {
    return new ProfileRepository(profileDao, sessionRepository, scheduleAlarmScheduler);
  }
}
