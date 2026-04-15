package dev.ambitionsoftware.tymeboxed.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao;
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

  public ProfileRepository_Factory(Provider<ProfileDao> profileDaoProvider) {
    this.profileDaoProvider = profileDaoProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(profileDaoProvider.get());
  }

  public static ProfileRepository_Factory create(Provider<ProfileDao> profileDaoProvider) {
    return new ProfileRepository_Factory(profileDaoProvider);
  }

  public static ProfileRepository newInstance(ProfileDao profileDao) {
    return new ProfileRepository(profileDao);
  }
}
