package com.umc.presentation.home.home;

import com.umc.domain.usecase.GetGisuInfoUseCase;
import com.umc.domain.usecase.member.GetMyProfileUseCase;
import com.umc.domain.usecase.organization.GetGisuListUseCase;
import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider;

  private final Provider<GetScheduleMonthUseCase> getScheduleMonthUseCaseProvider;

  private final Provider<GetGisuInfoUseCase> getGisuInfoUseCaseProvider;

  private final Provider<GetGisuListUseCase> getGisuListUseCaseProvider;

  private HomeViewModel_Factory(Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider,
      Provider<GetScheduleMonthUseCase> getScheduleMonthUseCaseProvider,
      Provider<GetGisuInfoUseCase> getGisuInfoUseCaseProvider,
      Provider<GetGisuListUseCase> getGisuListUseCaseProvider) {
    this.getMyProfileUseCaseProvider = getMyProfileUseCaseProvider;
    this.getScheduleMonthUseCaseProvider = getScheduleMonthUseCaseProvider;
    this.getGisuInfoUseCaseProvider = getGisuInfoUseCaseProvider;
    this.getGisuListUseCaseProvider = getGisuListUseCaseProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(getMyProfileUseCaseProvider.get(), getScheduleMonthUseCaseProvider.get(), getGisuInfoUseCaseProvider.get(), getGisuListUseCaseProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<GetMyProfileUseCase> getMyProfileUseCaseProvider,
      Provider<GetScheduleMonthUseCase> getScheduleMonthUseCaseProvider,
      Provider<GetGisuInfoUseCase> getGisuInfoUseCaseProvider,
      Provider<GetGisuListUseCase> getGisuListUseCaseProvider) {
    return new HomeViewModel_Factory(getMyProfileUseCaseProvider, getScheduleMonthUseCaseProvider, getGisuInfoUseCaseProvider, getGisuListUseCaseProvider);
  }

  public static HomeViewModel newInstance(GetMyProfileUseCase getMyProfileUseCase,
      GetScheduleMonthUseCase getScheduleMonthUseCase, GetGisuInfoUseCase getGisuInfoUseCase,
      GetGisuListUseCase getGisuListUseCase) {
    return new HomeViewModel(getMyProfileUseCase, getScheduleMonthUseCase, getGisuInfoUseCase, getGisuListUseCase);
  }
}
