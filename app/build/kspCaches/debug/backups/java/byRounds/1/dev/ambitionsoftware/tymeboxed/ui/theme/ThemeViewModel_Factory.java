package dev.ambitionsoftware.tymeboxed.ui.theme;

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
public final class ThemeViewModel_Factory implements Factory<ThemeViewModel> {
  private final Provider<ThemeController> controllerProvider;

  public ThemeViewModel_Factory(Provider<ThemeController> controllerProvider) {
    this.controllerProvider = controllerProvider;
  }

  @Override
  public ThemeViewModel get() {
    return newInstance(controllerProvider.get());
  }

  public static ThemeViewModel_Factory create(Provider<ThemeController> controllerProvider) {
    return new ThemeViewModel_Factory(controllerProvider);
  }

  public static ThemeViewModel newInstance(ThemeController controller) {
    return new ThemeViewModel(controller);
  }
}
