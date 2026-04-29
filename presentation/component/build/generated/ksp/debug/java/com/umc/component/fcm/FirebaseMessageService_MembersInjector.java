package com.umc.component.fcm;

import com.umc.domain.usecase.appDataStore.notification.AddNotificationUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class FirebaseMessageService_MembersInjector implements MembersInjector<FirebaseMessageService> {
  private final Provider<AddNotificationUseCase> addNotificationUseCaseProvider;

  private FirebaseMessageService_MembersInjector(
      Provider<AddNotificationUseCase> addNotificationUseCaseProvider) {
    this.addNotificationUseCaseProvider = addNotificationUseCaseProvider;
  }

  @Override
  public void injectMembers(FirebaseMessageService instance) {
    injectAddNotificationUseCase(instance, addNotificationUseCaseProvider.get());
  }

  public static MembersInjector<FirebaseMessageService> create(
      Provider<AddNotificationUseCase> addNotificationUseCaseProvider) {
    return new FirebaseMessageService_MembersInjector(addNotificationUseCaseProvider);
  }

  @InjectedFieldSignature("com.umc.component.fcm.FirebaseMessageService.addNotificationUseCase")
  public static void injectAddNotificationUseCase(FirebaseMessageService instance,
      AddNotificationUseCase addNotificationUseCase) {
    instance.addNotificationUseCase = addNotificationUseCase;
  }
}
