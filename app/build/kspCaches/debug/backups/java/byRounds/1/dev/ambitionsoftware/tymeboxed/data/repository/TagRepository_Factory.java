package dev.ambitionsoftware.tymeboxed.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.TagDao;
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
public final class TagRepository_Factory implements Factory<TagRepository> {
  private final Provider<TagDao> tagDaoProvider;

  public TagRepository_Factory(Provider<TagDao> tagDaoProvider) {
    this.tagDaoProvider = tagDaoProvider;
  }

  @Override
  public TagRepository get() {
    return newInstance(tagDaoProvider.get());
  }

  public static TagRepository_Factory create(Provider<TagDao> tagDaoProvider) {
    return new TagRepository_Factory(tagDaoProvider);
  }

  public static TagRepository newInstance(TagDao tagDao) {
    return new TagRepository(tagDao);
  }
}
