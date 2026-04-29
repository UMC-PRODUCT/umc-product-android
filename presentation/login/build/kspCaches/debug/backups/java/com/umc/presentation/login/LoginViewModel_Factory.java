package com.umc.presentation.login;

import com.umc.domain.usecase.PostLoginUseCase;
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase;
import com.umc.domain.usecase.member.GetMyProfileUseCase;
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<PostLoginUseCase> postLoginUseCaseProvider;

  private final Provider<SaveTokenUseCase> saveTokenUseCaseProvider;

  private final Provider<RegisterFcmTokenUseCase> registerFcmTokenUseCaseProvider;

  private final Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider;

  private LoginViewModel_Factory(Provider<PostLoginUseCase> postLoginUseCaseProvider,
      Provider<SaveTokenUseCase> saveTokenUseCaseProvider,
      Provider<RegisterFcmTokenUseCase> registerFcmTokenUseCaseProvider,
      Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider) {
    this.postLoginUseCaseProvider = postLoginUseCaseProvider;
    this.saveTokenUseCaseProvider = saveTokenUseCaseProvider;
    this.registerFcmTokenUseCaseProvider = registerFcmTokenUseCaseProvider;
    this.getMyProfileUseCaseProvider = getMyProfileUseCaseProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(postLoginUseCaseProvider.get(), saveTokenUseCaseProvider.get(), registerFcmTokenUseCaseProvider.get(), getMyProfileUseCaseProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<PostLoginUseCase> postLoginUseCaseProvider,
      Provider<SaveTokenUseCase> saveTokenUseCaseProvider,
      Provider<RegisterFcmTokenUseCase> registerFcmTokenUseCaseProvider,
      Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider) {
    return new LoginViewModel_Factory(postLoginUseCaseProvider, saveTokenUseCaseProvider, registerFcmTokenUseCaseProvider, getMyProfileUseCaseProvider);
  }

  public static LoginViewModel newInstance(PostLoginUseCase postLoginUseCase,
      SaveTokenUseCase saveTokenUseCase, RegisterFcmTokenUseCase registerFcmTokenUseCase,
      GetMyProfileUseCase getMyProfileUseCase) {
    return new LoginViewModel(postLoginUseCase, saveTokenUseCase, registerFcmTokenUseCase, getMyProfileUseCase);
  }
}
