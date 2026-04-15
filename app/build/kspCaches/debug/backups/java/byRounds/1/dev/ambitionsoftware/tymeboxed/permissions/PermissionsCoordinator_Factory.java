package dev.ambitionsoftware.tymeboxed.permissions;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PermissionsCoordinator_Factory implements Factory<PermissionsCoordinator> {
  private final Provider<Context> contextProvider;

  public PermissionsCoordinator_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PermissionsCoordinator get() {
    return newInstance(contextProvider.get());
  }

  public static PermissionsCoordinator_Factory create(Provider<Context> contextProvider) {
    return new PermissionsCoordinator_Factory(contextProvider);
  }

  public static PermissionsCoordinator newInstance(Context context) {
    return new PermissionsCoordinator(context);
  }
}
