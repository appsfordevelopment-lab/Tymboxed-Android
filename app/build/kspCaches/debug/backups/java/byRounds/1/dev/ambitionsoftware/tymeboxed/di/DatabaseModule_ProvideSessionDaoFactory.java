package dev.ambitionsoftware.tymeboxed.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase;
import dev.ambitionsoftware.tymeboxed.data.db.dao.SessionDao;
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
public final class DatabaseModule_ProvideSessionDaoFactory implements Factory<SessionDao> {
  private final Provider<TymeBoxedDatabase> dbProvider;

  public DatabaseModule_ProvideSessionDaoFactory(Provider<TymeBoxedDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SessionDao get() {
    return provideSessionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSessionDaoFactory create(
      Provider<TymeBoxedDatabase> dbProvider) {
    return new DatabaseModule_ProvideSessionDaoFactory(dbProvider);
  }

  public static SessionDao provideSessionDao(TymeBoxedDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSessionDao(db));
  }
}
