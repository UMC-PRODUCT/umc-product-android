package com.umc.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * 이메일 로그인 API( POST api/v1/auth/login/email ) 를 실제 dev 서버로 때려보는 테스트.
 *
 * - Hilt/DI 배선 없이 혼자 돌아가도록 Retrofit 인스턴스를 직접 만든다.
 * - JVM local unit test 라서 디바이스/에뮬레이터 없이 실행되고, 실제 네트워크도 탄다.
 * - 로깅 인터셉터가 BODY 레벨로 raw JSON 을 전부 println 하므로,
 *   응답이 ApiResponse(success/code/message/result) 로 감싸져 와도 콘솔에서 바로 확인 가능.
 *
 * 실행:
 *   ./gradlew :data:testDebugUnitTest --tests "com.umc.data.EmailLoginApiTest"
 */
class EmailLoginApiTest {

    // ⚠️ 실제로 로그인 성공시키려면 여기에 dev 계정 정보를 채워라.
    //    비워두면 서버가 에러 응답을 주고, 그 에러 바디가 그대로 출력된다.
    private val email = "gacheon_10_schoolpresident@umc.dev"
    private val password = "password12!"

    private val baseUrl = "https://dev.api.university.neordinary.com/"

    // ── DTO ───────────────────────────────────────────────
    data class EmailLoginRequest(
        val email: String,
        val password: String,
        val clientType: String = "ANDROID",
    )

    // 서버는 다른 엔드포인트와 동일하게 ApiResponse(success/code/message/result) 로 감싸서 응답한다.
    // (flat 으로 파싱하면 HTTP 200 인데 토큰이 전부 null 로 나온다 → 래퍼 확인 완료)
    data class ApiEnvelope<T>(
        @SerializedName("success") val success: Boolean = false,
        @SerializedName("code") val code: String? = null,
        @SerializedName("message") val message: String? = null,
        @SerializedName("result") val result: T? = null,
    )

    data class EmailLoginResponse(
        @SerializedName("memberId") val memberId: Long? = null,
        @SerializedName("accessToken") val accessToken: String? = null,
        @SerializedName("refreshToken") val refreshToken: String? = null,
    )

    // ── Retrofit API ──────────────────────────────────────
    interface EmailLoginTestApi {
        @POST("api/v1/auth/login/email")
        fun loginEmail(@Body request: EmailLoginRequest): Call<ApiEnvelope<EmailLoginResponse>>
    }

    private fun createApi(): EmailLoginTestApi {
        val logging = HttpLoggingInterceptor { message -> println(message) }
            .apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailLoginTestApi::class.java)
    }

    @Test
    fun 이메일_로그인_API_호출() {
        val api = createApi()
        val request = EmailLoginRequest(email = email, password = password)

        println("요청 ▶ $request")
        val response = api.loginEmail(request).execute()

        println("HTTP 상태코드 ▶ ${response.code()}")

        if (response.isSuccessful) {
            val body = response.body()
            println("응답 바디 ▶ $body")

            assertNotNull("응답 바디가 null 이면 안 됨", body)
            assertTrue("success 가 false 임 (code=${body?.code}, message=${body?.message})", body?.success == true)
            assertTrue("accessToken 이 비어있음", !body?.result?.accessToken.isNullOrBlank())
            assertTrue("refreshToken 이 비어있음", !body?.result?.refreshToken.isNullOrBlank())
        } else {
            // 계정 정보가 비었거나 틀리면 여기로 온다. 서버가 준 에러 바디를 그대로 보여준다.
            val error = response.errorBody()?.string()
            println("에러 바디 ▶ $error")
            org.junit.Assert.fail(
                "로그인 실패(HTTP ${response.code()}). email/password 를 채웠는지 확인해라.\n$error"
            )
        }
    }
}
