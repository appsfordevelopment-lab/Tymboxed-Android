package dev.ambitionsoftware.tymeboxed.service;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao;
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
public final class ProfileScheduleAlarmScheduler_Factory implements Factory<ProfileScheduleAlarmScheduler> {
  private final Provider<Context> appContextProvider;

  private final Provider<ProfileDao> profileDaoProvider;

  public ProfileScheduleAlarmScheduler_Factory(Provider<Context> appContextProvider,
      Provider<ProfileDao> profileDaoProvider) {
    this.appContextProvider = appContextProvider;
    this.profileDaoProvider = profileDaoProvider;
  }

  @Override
  public ProfileScheduleAlarmScheduler get() {
    return newInstance(appContextProvider.get(), profileDaoProvider.get());
  }

  public static ProfileScheduleAlarmScheduler_Factory create(Provider<Context> appContextProvider,
      Provider<ProfileDao> profileDaoProvider) {
    return new ProfileScheduleAlarmScheduler_Factory(appContextProvider, profileDaoProvider);
  }

  public static ProfileScheduleAlarmScheduler newInstance(Context appContext,
      ProfileDao profileDao) {
    return new ProfileScheduleAlarmScheduler(appContext, profileDao);
  }
}
