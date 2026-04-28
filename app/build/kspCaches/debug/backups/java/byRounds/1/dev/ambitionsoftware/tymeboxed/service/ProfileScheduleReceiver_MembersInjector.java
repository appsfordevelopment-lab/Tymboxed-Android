package dev.ambitionsoftware.tymeboxed.service;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ProfileScheduleReceiver_MembersInjector implements MembersInjector<ProfileScheduleReceiver> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<AppSessionController> appSessionControllerProvider;

  private final Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider;

  public ProfileScheduleReceiver_MembersInjector(
      Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.appSessionControllerProvider = appSessionControllerProvider;
    this.scheduleAlarmSchedulerProvider = scheduleAlarmSchedulerProvider;
  }

  public static MembersInjector<ProfileScheduleReceiver> create(
      Provider<ProfileRepository> profileRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<AppSessionController> appSessionControllerProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    return new ProfileScheduleReceiver_MembersInjector(profileRepositoryProvider, sessionRepositoryProvider, appSessionControllerProvider, scheduleAlarmSchedulerProvider);
  }

  @Override
  public void injectMembers(ProfileScheduleReceiver instance) {
    injectProfileRepository(instance, profileRepositoryProvider.get());
    injectSessionRepository(instance, sessionRepositoryProvider.get());
    injectAppSessionController(instance, appSessionControllerProvider.get());
    injectScheduleAlarmScheduler(instance, scheduleAlarmSchedulerProvider.get());
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.ProfileScheduleReceiver.profileRepository")
  public static void injectProfileRepository(ProfileScheduleReceiver instance,
      ProfileRepository profileRepository) {
    instance.profileRepository = profileRepository;
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.ProfileScheduleReceiver.sessionRepository")
  public static void injectSessionRepository(ProfileScheduleReceiver instance,
      SessionRepository sessionRepository) {
    instance.sessionRepository = sessionRepository;
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.ProfileScheduleReceiver.appSessionController")
  public static void injectAppSessionController(ProfileScheduleReceiver instance,
      AppSessionController appSessionController) {
    instance.appSessionController = appSessionController;
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.ProfileScheduleReceiver.scheduleAlarmScheduler")
  public static void injectScheduleAlarmScheduler(ProfileScheduleReceiver instance,
      ProfileScheduleAlarmScheduler scheduleAlarmScheduler) {
    instance.scheduleAlarmScheduler = scheduleAlarmScheduler;
  }
}
