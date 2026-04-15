package dev.ambitionsoftware.tymeboxed.ui.theme;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences;
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
public final class ThemeController_Factory implements Factory<ThemeController> {
  private final Provider<AppPreferences> prefsProvider;

  public ThemeController_Factory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ThemeController get() {
    return newInstance(prefsProvider.get());
  }

  public static ThemeController_Factory create(Provider<AppPreferences> prefsProvider) {
    return new ThemeController_Factory(prefsProvider);
  }

  public static ThemeController newInstance(AppPreferences prefs) {
    return new ThemeController(prefs);
  }
}
