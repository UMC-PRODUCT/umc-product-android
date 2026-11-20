package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import org.junit.Test

class LockedScreenOrientationDetectorTest {

    @Test
    fun `방향 고정과 resizeableActivity false 는 오류, 회전을 따르는 값은 무시`() {
        lintWith(
            LockedScreenOrientationDetector.ISSUE,
            manifest(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.umc.product">
                    <application android:resizeableActivity="false">
                        <activity android:name=".PortraitActivity" android:screenOrientation="portrait" />
                        <activity android:name=".LandscapeActivity" android:screenOrientation="landscape" />
                        <activity android:name=".LockedActivity" android:screenOrientation="locked" />
                        <activity android:name=".FreeActivity" android:screenOrientation="fullUser" />
                        <activity android:name=".DefaultActivity" android:screenOrientation="unspecified" />
                        <activity android:name=".ResizableActivity" android:resizeableActivity="true" />
                    </application>
                </manifest>
                """,
            ).indented(),
        ).expect(
            """
            AndroidManifest.xml:2: Error: resizeableActivity="false"는 분할 화면·자유 창에서 크기 조절을 막습니다. 속성을 지우세요. [LockedScreenOrientation]
                <application android:resizeableActivity="false">
                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            AndroidManifest.xml:3: Error: screenOrientation="portrait"는 화면 방향을 고정합니다. 속성을 지우고 회전·창 크기 변화에 대응하세요. [LockedScreenOrientation]
                    <activity android:name=".PortraitActivity" android:screenOrientation="portrait" />
                                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            AndroidManifest.xml:4: Error: screenOrientation="landscape"는 화면 방향을 고정합니다. 속성을 지우고 회전·창 크기 변화에 대응하세요. [LockedScreenOrientation]
                    <activity android:name=".LandscapeActivity" android:screenOrientation="landscape" />
                                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            AndroidManifest.xml:5: Error: screenOrientation="locked"는 화면 방향을 고정합니다. 속성을 지우고 회전·창 크기 변화에 대응하세요. [LockedScreenOrientation]
                    <activity android:name=".LockedActivity" android:screenOrientation="locked" />
                                                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
    }
}
