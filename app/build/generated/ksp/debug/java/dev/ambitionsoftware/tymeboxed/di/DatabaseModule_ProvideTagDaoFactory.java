package dev.ambitionsoftware.tymeboxed.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase;
import dev.ambitionsoftware.tymeboxed.data.db.dao.TagDao;
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
public final class DatabaseModule_ProvideTagDaoFactory implements Factory<TagDao> {
  private final Provider<TymeBoxedDatabase> dbProvider;

  public DatabaseModule_ProvideTagDaoFactory(Provider<TymeBoxedDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TagDao get() {
    return provideTagDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTagDaoFactory create(Provider<TymeBoxedDatabase> dbProvider) {
    return new DatabaseModule_ProvideTagDaoFactory(dbProvider);
  }

  public static TagDao provideTagDao(TymeBoxedDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTagDao(db));
  }
}
