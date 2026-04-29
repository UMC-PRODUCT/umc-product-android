package com.umc.presentation.home.schedule.detail;

import com.umc.domain.usecase.GetAuthAccessUseCase;
import com.umc.domain.usecase.schedule.DeleteScheduleUseCase;
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase;
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
public final class ScheduleDetailViewModel_Factory implements Factory<ScheduleDetailViewModel> {
  private final Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider;

  private final Provider<DeleteScheduleUseCase> deleteScheduleUseCaseProvider;

  private final Provider<GetAuthAccessUseCase> getAuthAccessUseCaseProvider;

  private ScheduleDetailViewModel_Factory(
      Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider,
      Provider<DeleteScheduleUseCase> deleteScheduleUseCaseProvider,
      Provider<GetAuthAccessUseCase> getAuthAccessUseCaseProvider) {
    this.getScheduleDetailHomeUseCaseProvider = getScheduleDetailHomeUseCaseProvider;
    this.deleteScheduleUseCaseProvider = deleteScheduleUseCaseProvider;
    this.getAuthAccessUseCaseProvider = getAuthAccessUseCaseProvider;
  }

  @Override
  public ScheduleDetailViewModel get() {
    return newInstance(getScheduleDetailHomeUseCaseProvider.get(), deleteScheduleUseCaseProvider.get(), getAuthAccessUseCaseProvider.get());
  }

  public static ScheduleDetailViewModel_Factory create(
      Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider,
      Provider<DeleteScheduleUseCase> deleteScheduleUseCaseProvider,
      Provider<GetAuthAccessUseCase> getAuthAccessUseCaseProvider) {
    return new ScheduleDetailViewModel_Factory(getScheduleDetailHomeUseCaseProvider, deleteScheduleUseCaseProvider, getAuthAccessUseCaseProvider);
  }

  public static ScheduleDetailViewModel newInstance(
      GetScheduleDetailHomeUseCase getScheduleDetailHomeUseCase,
      DeleteScheduleUseCase deleteScheduleUseCase, GetAuthAccessUseCase getAuthAccessUseCase) {
    return new ScheduleDetailViewModel(getScheduleDetailHomeUseCase, deleteScheduleUseCase, getAuthAccessUseCase);
  }
}
