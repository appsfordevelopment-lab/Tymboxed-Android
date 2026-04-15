package dev.ambitionsoftware.tymeboxed.ui.screens.profile;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ProfileEditViewModel_Factory implements Factory<ProfileEditViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ProfileRepository> profileRepositoryProvider;

  private final Provider<Context> appContextProvider;

  public ProfileEditViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ProfileRepository> profileRepositoryProvider, Provider<Context> appContextProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public ProfileEditViewModel get() {
    return newInstance(savedStateHandleProvider.get(), profileRepositoryProvider.get(), appContextProvider.get());
  }

  public static ProfileEditViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ProfileRepository> profileRepositoryProvider, Provider<Context> appContextProvider) {
    return new ProfileEditViewModel_Factory(savedStateHandleProvider, profileRepositoryProvider, appContextProvider);
  }

  public static ProfileEditViewModel newInstance(SavedStateHandle savedStateHandle,
      ProfileRepository profileRepository, Context appContext) {
    return new ProfileEditViewModel(savedStateHandle, profileRepository, appContext);
  }
}
