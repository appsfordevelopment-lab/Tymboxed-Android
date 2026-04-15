package dev.ambitionsoftware.tymeboxed.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase;
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao;
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
public final class DatabaseModule_ProvideProfileDaoFactory implements Factory<ProfileDao> {
  private final Provider<TymeBoxedDatabase> dbProvider;

  public DatabaseModule_ProvideProfileDaoFactory(Provider<TymeBoxedDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProfileDao get() {
    return provideProfileDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideProfileDaoFactory create(
      Provider<TymeBoxedDatabase> dbProvider) {
    return new DatabaseModule_ProvideProfileDaoFactory(dbProvider);
  }

  public static ProfileDao provideProfileDao(TymeBoxedDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProfileDao(db));
  }
}
