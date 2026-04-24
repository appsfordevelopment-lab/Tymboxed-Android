package dev.ambitionsoftware.tymeboxed.ui.screens.inapp;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class InAppBlockingViewModel_Factory implements Factory<InAppBlockingViewModel> {
  private final Provider<Context> appProvider;

  public InAppBlockingViewModel_Factory(Provider<Context> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public InAppBlockingViewModel get() {
    return newInstance(appProvider.get());
  }

  public static InAppBlockingViewModel_Factory create(Provider<Context> appProvider) {
    return new InAppBlockingViewModel_Factory(appProvider);
  }

  public static InAppBlockingViewModel newInstance(Context app) {
    return new InAppBlockingViewModel(app);
  }
}
