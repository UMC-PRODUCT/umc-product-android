package com.umc.presentation.home.schedule.add;

import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase;
import com.umc.domain.usecase.member.GetMemberProfileUseCase;
import com.umc.domain.usecase.schedule.CreateScheduleUseCase;
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase;
import com.umc.domain.usecase.schedule.UpdateScheduleUseCase;
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
public final class ScheduleAddViewModel_Factory implements Factory<ScheduleAddViewModel> {
  private final Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider;

  private final Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider;

  private final Provider<CreateScheduleUseCase> createScheduleUseCaseProvider;

  private final Provider<UpdateScheduleUseCase> updateScheduleUseCaseProvider;

  private final Provider<GetMemberProfileUseCase> getMemberProfileUseCaseProvider;

  private ScheduleAddViewModel_Factory(Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider,
      Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider,
      Provider<CreateScheduleUseCase> createScheduleUseCaseProvider,
      Provider<UpdateScheduleUseCase> updateScheduleUseCaseProvider,
      Provider<GetMemberProfileUseCase> getMemberProfileUseCaseProvider) {
    this.getUserInfoUseCaseProvider = getUserInfoUseCaseProvider;
    this.getScheduleDetailHomeUseCaseProvider = getScheduleDetailHomeUseCaseProvider;
    this.createScheduleUseCaseProvider = createScheduleUseCaseProvider;
    this.updateScheduleUseCaseProvider = updateScheduleUseCaseProvider;
    this.getMemberProfileUseCaseProvider = getMemberProfileUseCaseProvider;
  }

  @Override
  public ScheduleAddViewModel get() {
    return newInstance(getUserInfoUseCaseProvider.get(), getScheduleDetailHomeUseCaseProvider.get(), createScheduleUseCaseProvider.get(), updateScheduleUseCaseProvider.get(), getMemberProfileUseCaseProvider.get());
  }

  public static ScheduleAddViewModel_Factory create(
      Provider<GetUserInfoUseCase> getUserInfoUseCaseProvider,
      Provider<GetScheduleDetailHomeUseCase> getScheduleDetailHomeUseCaseProvider,
      Provider<CreateScheduleUseCase> createScheduleUseCaseProvider,
      Provider<UpdateScheduleUseCase> updateScheduleUseCaseProvider,
      Provider<GetMemberProfileUseCase> getMemberProfileUseCaseProvider) {
    return new ScheduleAddViewModel_Factory(getUserInfoUseCaseProvider, getScheduleDetailHomeUseCaseProvider, createScheduleUseCaseProvider, updateScheduleUseCaseProvider, getMemberProfileUseCaseProvider);
  }

  public static ScheduleAddViewModel newInstance(GetUserInfoUseCase getUserInfoUseCase,
      GetScheduleDetailHomeUseCase getScheduleDetailHomeUseCase,
      CreateScheduleUseCase createScheduleUseCase, UpdateScheduleUseCase updateScheduleUseCase,
      GetMemberProfileUseCase getMemberProfileUseCase) {
    return new ScheduleAddViewModel(getUserInfoUseCase, getScheduleDetailHomeUseCase, createScheduleUseCase, updateScheduleUseCase, getMemberProfileUseCase);
  }
}
