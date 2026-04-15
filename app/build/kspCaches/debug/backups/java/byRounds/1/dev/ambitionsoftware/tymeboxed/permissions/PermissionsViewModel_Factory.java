package dev.ambitionsoftware.tymeboxed.permissions;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class PermissionsViewModel_Factory implements Factory<PermissionsViewModel> {
  private final Provider<PermissionsCoordinator> coordinatorProvider;

  public PermissionsViewModel_Factory(Provider<PermissionsCoordinator> coordinatorProvider) {
    this.coordinatorProvider = coordinatorProvider;
  }

  @Override
  public PermissionsViewModel get() {
    return newInstance(coordinatorProvider.get());
  }

  public static PermissionsViewModel_Factory create(
      Provider<PermissionsCoordinator> coordinatorProvider) {
    return new PermissionsViewModel_Factory(coordinatorProvider);
  }

  public static PermissionsViewModel newInstance(PermissionsCoordinator coordinator) {
    return new PermissionsViewModel(coordinator);
  }
}
