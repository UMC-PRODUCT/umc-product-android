package com.umc.presentation.splash;

import com.umc.domain.usecase.member.GetMyProfileUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider;

  private SplashViewModel_Factory(Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider) {
    this.getMyProfileUseCaseProvider = getMyProfileUseCaseProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(getMyProfileUseCaseProvider.get());
  }

  public static SplashViewModel_Factory create(
      Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider) {
    return new SplashViewModel_Factory(getMyProfileUseCaseProvider);
  }

  public static SplashViewModel newInstance(GetMyProfileUseCase getMyProfileUseCase) {
    return new SplashViewModel(getMyProfileUseCase);
  }
}
