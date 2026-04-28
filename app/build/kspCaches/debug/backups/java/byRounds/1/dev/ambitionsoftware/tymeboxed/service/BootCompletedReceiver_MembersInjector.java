package dev.ambitionsoftware.tymeboxed.service;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase;
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
public final class BootCompletedReceiver_MembersInjector implements MembersInjector<BootCompletedReceiver> {
  private final Provider<TymeBoxedDatabase> databaseProvider;

  private final Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider;

  public BootCompletedReceiver_MembersInjector(Provider<TymeBoxedDatabase> databaseProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    this.databaseProvider = databaseProvider;
    this.scheduleAlarmSchedulerProvider = scheduleAlarmSchedulerProvider;
  }

  public static MembersInjector<BootCompletedReceiver> create(
      Provider<TymeBoxedDatabase> databaseProvider,
      Provider<ProfileScheduleAlarmScheduler> scheduleAlarmSchedulerProvider) {
    return new BootCompletedReceiver_MembersInjector(databaseProvider, scheduleAlarmSchedulerProvider);
  }

  @Override
  public void injectMembers(BootCompletedReceiver instance) {
    injectDatabase(instance, databaseProvider.get());
    injectScheduleAlarmScheduler(instance, scheduleAlarmSchedulerProvider.get());
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.BootCompletedReceiver.database")
  public static void injectDatabase(BootCompletedReceiver instance, TymeBoxedDatabase database) {
    instance.database = database;
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.BootCompletedReceiver.scheduleAlarmScheduler")
  public static void injectScheduleAlarmScheduler(BootCompletedReceiver instance,
      ProfileScheduleAlarmScheduler scheduleAlarmScheduler) {
    instance.scheduleAlarmScheduler = scheduleAlarmScheduler;
  }
}
