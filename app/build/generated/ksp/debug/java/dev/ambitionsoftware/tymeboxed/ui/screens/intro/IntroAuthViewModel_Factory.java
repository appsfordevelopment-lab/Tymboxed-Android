package dev.ambitionsoftware.tymeboxed.ui.screens.intro;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.repository.AuthRepository;
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
public final class IntroAuthViewModel_Factory implements Factory<IntroAuthViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public IntroAuthViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public IntroAuthViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static IntroAuthViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new IntroAuthViewModel_Factory(authRepositoryProvider);
  }

  public static IntroAuthViewModel newInstance(AuthRepository authRepository) {
    return new IntroAuthViewModel(authRepository);
  }
}
