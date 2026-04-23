package dev.ambitionsoftware.tymeboxed;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences;
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsCoordinator;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<PermissionsCoordinator> permissionsCoordinatorProvider;

  public MainActivity_MembersInjector(Provider<AppPreferences> appPreferencesProvider,
      Provider<PermissionsCoordinator> permissionsCoordinatorProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
    this.permissionsCoordinatorProvider = permissionsCoordinatorProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<AppPreferences> appPreferencesProvider,
      Provider<PermissionsCoordinator> permissionsCoordinatorProvider) {
    return new MainActivity_MembersInjector(appPreferencesProvider, permissionsCoordinatorProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectAppPreferences(instance, appPreferencesProvider.get());
    injectPermissionsCoordinator(instance, permissionsCoordinatorProvider.get());
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.MainActivity.appPreferences")
  public static void injectAppPreferences(MainActivity instance, AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }

  @InjectedFieldSignature("dev.ambitionsoftware.tymeboxed.MainActivity.permissionsCoordinator")
  public static void injectPermissionsCoordinator(MainActivity instance,
      PermissionsCoordinator permissionsCoordinator) {
    instance.permissionsCoordinator = permissionsCoordinator;
  }
}
