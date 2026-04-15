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

  public BootCompletedReceiver_MembersInjector(Provider<TymeBoxedDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  public static MembersInjector<BootCompletedReceiver> create(
      Provider<TymeBoxedDatabase> databaseProvider) {
    return new BootCompletedReceiver_MembersInjector(databaseProvider);
  }

  @Override
  public void injectMembers(BootCompletedReceiver instance) {
    injectDatabase(instance, databaseProvider.get());
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.service.BootCompletedReceiver.database")
  public static void injectDatabase(BootCompletedReceiver instance, TymeBoxedDatabase database) {
    instance.database = database;
  }
}
