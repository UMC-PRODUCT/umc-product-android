package com.umc.component.fcm;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;
import javax.annotation.processing.Generated;

@OriginatingElement(
    topLevelClass = FirebaseMessageService.class
)
@GeneratedEntryPoint
@InstallIn(ServiceComponent.class)
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.InjectorEntryPointGenerator")
public interface FirebaseMessageService_GeneratedInjector {
  void injectFirebaseMessageService(FirebaseMessageService firebaseMessageService);
}
