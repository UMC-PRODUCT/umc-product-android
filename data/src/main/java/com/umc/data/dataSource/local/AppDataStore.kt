package com.umc.data.dataSource.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.CurrentGisuMemberInfo
import com.umc.domain.model.ProfileInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.home.NotificationItem

import com.umc.domain.model.UserRole
import com.umc.domain.model.mypage.CardBook
import com.umc.domain.model.mypage.CardBookState
import com.umc.domain.model.mypage.CardSession
import com.umc.domain.model.mypage.CardWriteContext
import com.umc.domain.model.mypage.withoutUnverifiedContacts
import java.util.UUID
import kotlinx.coroutines.flow.distinctUntilChanged
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.mypage.UserOutLink
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.dataStore
    private val gson = Gson()


    // 아웃링크 Flow (마이페이지 깃허브/블로그/링크드인)
    val userOutLinkFlow: Flow<UserOutLink> = store.data.map { prefs ->
        UserOutLink(
            github = prefs[KEY_GITHUB] ?: "",
            linkedin = prefs[KEY_LINKEDIN] ?: "",
            blog = prefs[KEY_BLOG] ?: ""
        )
    }

    // 아웃 링크 저장
    suspend fun saveOutLink(outLink: UserOutLink) {
        store.edit { prefs ->
            prefs[KEY_GITHUB] = outLink.github
            prefs[KEY_LINKEDIN] = outLink.linkedin
            prefs[KEY_BLOG] = outLink.blog
        }
    }

    // 유저 정보 Flow
    val userInfoFlow: Flow<UserInfo> = store.data.map { prefs ->
        val rolesJson = prefs[KEY_ROLES] ?: "[]"
        val rolesList = gson.fromJson(rolesJson, Array<UserRole>::class.java).toList()

        val recordsJson = prefs[KEY_RECORDS] ?: "[]"
        val recordsList = gson.fromJson(recordsJson, Array<ChallengerRecord>::class.java).toList()

        val profileJson = prefs[KEY_PROFILE] ?: ""
        val profileData = if (profileJson.isNotEmpty()) {
            gson.fromJson(profileJson, ProfileInfo::class.java)
        } else {
            // 기본값 처리
            ProfileInfo(0, "", "", "", "", "")
        }

        val currentGisuJson = prefs[KEY_CURRENT_GISU_INFO] ?: ""
        val currentGisuData = if (currentGisuJson.isNotEmpty()) {
            runCatching { gson.fromJson(currentGisuJson, CurrentGisuMemberInfo::class.java) }.getOrNull()
        } else null

        UserInfo(
            id = prefs[KEY_ID] ?: 0L,
            name = prefs[KEY_NAME] ?: "",
            nickname = prefs[KEY_NICKNAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            schoolId = prefs[KEY_SCHOOL_ID] ?: 0L,
            schoolName = prefs[KEY_SCHOOL_NAME] ?: "",
            profileImageLink = prefs[KEY_PROFILE_IMAGE] ?: "",
            status = prefs[KEY_STATUS] ?: "ACTIVE",
            roles = rolesList,
            challengerRecords = recordsList,
            profile = profileData,

            hasLocalCredential = prefs[KEY_HAS_LOCAL_CREDENTIAL] ?: false,
            totalActivityDays = prefs[KEY_TOTAL_ACTIVITY_DAYS] ?: 0L,
            currentGisuMemberInfo = currentGisuData,
            isSuperAdmin = prefs[KEY_IS_SUPER_ADMIN] ?: false
        )
    // store.data는 사용자 정보와 무관한 값(토큰, 읽은 공지, 검색 기록 등)이 바뀌어도 방출한다.
    // 같은 사용자 정보를 다시 내보내면 이를 구독해 목록을 다시 불러오는 화면이 매번 재조회한다
    }.distinctUntilChanged()

    // 유저 정보를 저장 (User Class 이용)
    suspend fun saveUserInfo(userInfo: UserInfo, expected: CardSession? = null) {
        store.edit { prefs ->
            check(expected == null || (session(prefs) == expected && !prefs[KEY_ACCESS_TOKEN].isNullOrBlank())) { "계정이 변경됐어요." }
            if (prefs[KEY_ID] != userInfo.id) prefs[KEY_CARD_EPOCH] = (prefs[KEY_CARD_EPOCH] ?: 0L) + 1
            prefs[KEY_ID] = userInfo.id
            prefs[KEY_NAME] = userInfo.name
            prefs[KEY_NICKNAME] = userInfo.nickname
            prefs[KEY_EMAIL] = userInfo.email
            prefs[KEY_SCHOOL_ID] = userInfo.schoolId
            prefs[KEY_SCHOOL_NAME] = userInfo.schoolName
            prefs[KEY_PROFILE_IMAGE] = userInfo.profileImageLink
            prefs[KEY_STATUS] = userInfo.status
            prefs[KEY_ROLES] = gson.toJson(userInfo.roles)
            prefs[KEY_RECORDS] = gson.toJson(userInfo.challengerRecords)
            prefs[KEY_PROFILE] = gson.toJson(userInfo.profile)
            prefs[KEY_HAS_LOCAL_CREDENTIAL] = userInfo.hasLocalCredential
            prefs[KEY_TOTAL_ACTIVITY_DAYS] = userInfo.totalActivityDays
            prefs[KEY_IS_SUPER_ADMIN] = userInfo.isSuperAdmin
            if (userInfo.currentGisuMemberInfo != null) {
                prefs[KEY_CURRENT_GISU_INFO] = gson.toJson(userInfo.currentGisuMemberInfo)
            } else {
                prefs.remove(KEY_CURRENT_GISU_INFO)
            }
        }
    }

    // Existing unowned records remain untouched under KEY_USER_CARDS; never assign them to whoever logs in next.
    private fun cardBookKey(ownerId: Long) = stringPreferencesKey("card_book_v2_$ownerId")
    private fun session(prefs: Preferences) = CardSession(prefs[KEY_ID] ?: 0, prefs[KEY_CARD_EPOCH] ?: 0)
    private fun readBook(prefs: Preferences, ownerId: Long): CardBook {
        val raw = prefs[cardBookKey(ownerId)] ?: return CardBook()
        // Corruption must surface as an error, not become an empty writable collection.
        val book = requireNotNull(gson.fromJson(raw, CardBook::class.java))
        require(book.revision >= 0 && book.journal.all { it.version <= book.revision })
        require(book.cards.all { (id, entry) ->
            id.toLongOrNull()?.let { it > 0 } == true && entry.card.cardId == id && entry.version <= book.revision
        })
        return book
    }

    val cardBookFlow: Flow<CardBookState> = store.data.map { prefs ->
        val current = session(prefs)
        val active = current.ownerId > 0 && !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()
        val loaded = runCatching { if (active) readBook(prefs, current.ownerId) else CardBook() }
        CardBookState(
            session = current.takeIf { active },
            book = loaded.getOrDefault(CardBook()),
            storageUnavailable = loaded.isFailure,
            hasLegacyRecords = !prefs[KEY_USER_CARDS].isNullOrBlank() && prefs[KEY_USER_CARDS] != "[]",
        )
    }.onStart {
        // Remove content retained by older tombstones, including accounts not currently active.
        store.edit { prefs ->
            prefs.asMap().keys.toList().forEach { key ->
                val ownerId = key.name.removePrefix("card_book_v2_").toLongOrNull()
                if (!key.name.startsWith("card_book_v2_") || ownerId == null) return@forEach
                val book = runCatching { readBook(prefs, ownerId) }.getOrNull() ?: return@forEach
                val normalized = book.withoutDeletedContent()
                if (normalized != book) prefs[cardBookKey(ownerId)] = gson.toJson(normalized)
            }
        }
    }.distinctUntilChanged()

    val userCardFlow: Flow<List<UserCard>> = cardBookFlow.map { state ->
        state.book.visibleCards.map { it.card }
    }

    suspend fun profileSession(): CardSession = session(store.data.first())

    suspend fun cardWriteContext(): CardWriteContext {
        val prefs = store.data.first()
        val current = session(prefs)
        check(current.ownerId > 0 && !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()) { "로그인 정보를 확인해주세요." }
        return CardWriteContext(current, readBook(prefs, current.ownerId).revision)
    }

    private suspend fun changeCardBook(expected: CardWriteContext, change: (CardBook) -> CardBook) {
        store.edit { prefs ->
            check(session(prefs) == expected.session && !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()) {
                "계정이 변경됐어요. 다시 확인해주세요."
            }
            val book = readBook(prefs, expected.session.ownerId)
            // Card mutation and its journal entry share the same durable DataStore transaction.
            prefs[cardBookKey(expected.session.ownerId)] = gson.toJson(change(book))
        }
    }

    suspend fun saveUserCard(card: UserCard) = saveUserCard(card, cardWriteContext())
    suspend fun saveUserCard(card: UserCard, expected: CardWriteContext,
        operationId: String = "save:${expected.session.ownerId}:${expected.session.epoch}:${expected.revision}:${card.cardId}",
    ) = changeCardBook(expected) { book ->
        book.withoutDeletedContent().save(card.withoutUnverifiedContacts(), expected, System.currentTimeMillis(), operationId)
    }

    suspend fun removeUserCard(cardId: String) = removeUserCard(cardId, cardWriteContext())
    suspend fun removeUserCard(cardId: String, expected: CardWriteContext,
        operationId: String = "delete:${expected.session.ownerId}:${expected.session.epoch}:${expected.revision}:$cardId",
    ) = changeCardBook(expected) { book ->
        book.withoutDeletedContent().delete(cardId, System.currentTimeMillis(), operationId)
    }

    suspend fun updateCardNote(cardId: String, note: String, expected: CardWriteContext) = changeCardBook(expected) {
        it.updateNote(cardId, note)
    }

    suspend fun clearUserCards() = changeCardBook(cardWriteContext()) { book ->
        book.visibleCards.fold(book) { result, entry ->
            result.delete(entry.card.cardId, System.currentTimeMillis(), UUID.randomUUID().toString())
        }
    }

    suspend fun purgeCurrentCardBook(expected: CardSession) {
        store.edit { prefs ->
            check(session(prefs) == expected) { "계정이 변경됐어요." }
            prefs.remove(cardBookKey(expected.ownerId))
            prefs[KEY_CARD_EPOCH] = expected.epoch + 1
            prefs.remove(KEY_ID)
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    // 일정 생성 : 최근 장소 검색어 Flow
    val recentSearchesPlaceFlow: Flow<List<String>> = store.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_PLACE] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 일정 생성 : 최근 장소 검색어 추가 = 최대 10개 저장
    suspend fun addSearchPlaceHistory(query: String) {
        store.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_PLACE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_PLACE] = gson.toJson(currentList.take(10))
        }
    }

    // 커뮤니티 : 게시글 검색 기록 flow
    val recentSearchesPostFlow: Flow<List<String>> = store.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 커뮤니티 : 게시글 검색어 추가 (최대 20개, 중복 제거 후 맨 앞으로)
    suspend fun addSearchPostHistory(query: String) {
        if (query.isBlank()) return
        store.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_POST] = gson.toJson(currentList.take(20))
        }
    }

    // 커뮤니티 : 개별 검색어 삭제 (X 버튼 로직)
    suspend fun removeSearchPostHistory(query: String) {
        store.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            prefs[KEY_RECENT_SEARCHES_POST] = gson.toJson(currentList)
        }
    }

    // 커뮤니티 : 전체 검색어 삭제 로직
    suspend fun clearSearchPostHistory() {
        store.edit { prefs ->
            prefs.remove(KEY_RECENT_SEARCHES_POST)
        }
    }

    // 공지사항 : 검색 기록 flow
    val recentSearchesNoticeFlow: Flow<List<String>> = store.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 공지사항 : 검색어 추가 (최대 20개, 중복 제거 후 맨 앞으로)
    suspend fun addSearchNoticeHistory(query: String) {
        if (query.isBlank()) return
        store.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_NOTICE] = gson.toJson(currentList.take(20))
        }
    }

    // 공지사항 : 개별 검색어 삭제 (X 버튼 로직)
    suspend fun removeSearchNoticeHistory(query: String) {
        store.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            prefs[KEY_RECENT_SEARCHES_NOTICE] = gson.toJson(currentList)
        }
    }

    // 공지사항 : 전체 검색어 삭제 로직
    suspend fun clearSearchNoticeHistory() {
        store.edit { prefs ->
            prefs.remove(KEY_RECENT_SEARCHES_NOTICE)
        }
    }


    // 공지사항 : 읽은 공지 ID 목록 Flow
    val readNoticeIdsFlow: Flow<Set<Long>> = store.data.map { prefs ->
        val json = prefs[KEY_READ_NOTICE_IDS] ?: "[]"
        gson.fromJson(json, Array<Long>::class.java).toSet()
    }

    // 공지사항 : 읽은 공지 ID 추가
    suspend fun addReadNoticeId(noticeId: Long) {
        store.edit { prefs ->
            val currentJson = prefs[KEY_READ_NOTICE_IDS] ?: "[]"
            val currentSet = gson.fromJson(currentJson, Array<Long>::class.java).toMutableSet()
            currentSet.add(noticeId)
            // 최대 1000개까지만 저장 (메모리 관리)
            val trimmedSet = if (currentSet.size > 1000) {
                currentSet.drop(currentSet.size - 1000).toSet()
            } else {
                currentSet
            }
            prefs[KEY_READ_NOTICE_IDS] = gson.toJson(trimmedSet)
        }
    }

    // 공지사항 : 읽은 공지 ID 목록 전체 삭제
    suspend fun clearReadNoticeIds() {
        store.edit { prefs ->
            prefs.remove(KEY_READ_NOTICE_IDS)
        }
    }


    //------------JWT 관련 로직-----------------//
    suspend fun saveTokens(accessToken: String, refreshToken: String, newSession: Boolean = false) {
        store.edit { prefs ->
            if (newSession) {
                prefs[KEY_CARD_EPOCH] = (prefs[KEY_CARD_EPOCH] ?: 0L) + 1
                prefs.remove(KEY_ID)
            }
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String {
        return store.data.first()[KEY_ACCESS_TOKEN] ?: ""
    }

    suspend fun getRefreshToken(): String {
        return store.data.first()[KEY_REFRESH_TOKEN] ?: ""
    }

    suspend fun clearTokens() {
        store.edit { prefs ->
            prefs[KEY_CARD_EPOCH] = (prefs[KEY_CARD_EPOCH] ?: 0L) + 1
            prefs.remove(KEY_ID)
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    suspend fun clearAllData() {
        store.edit { prefs ->
            val epoch = (prefs[KEY_CARD_EPOCH] ?: 0L) + 1
            val retained = prefs.asMap().filterKeys { it.name.startsWith("card_book_v2_") || it.name == KEY_USER_CARDS.name }
            prefs.clear()
            retained.forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                prefs[key as Preferences.Key<String>] = value as String
            }
            prefs[KEY_CARD_EPOCH] = epoch
        }
    }

    // 알림 목록 Flow
    val notificationsFlow: Flow<List<NotificationItem>> = store.data.map { prefs ->
        val json = prefs[KEY_NOTIFICATIONS] ?: "[]"
        gson.fromJson(json, Array<NotificationItem>::class.java).toList()
    }

    // 알림 추가 (최대 50개 저장, 중복 제거)
    suspend fun addNotification(notification: NotificationItem) {
        store.edit { prefs ->
            val currentJson = prefs[KEY_NOTIFICATIONS] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<NotificationItem>::class.java).toMutableList()
            // 중복 체크: 동일한 title과 content가 있으면 제거 후 새로 추가
            currentList.removeAll { it.title == notification.title && it.content == notification.content }
            currentList.add(0, notification)
            prefs[KEY_NOTIFICATIONS] = gson.toJson(currentList.take(50))
        }
    }

    // 알림 전체 삭제
    suspend fun clearNotifications() {
        store.edit { prefs ->
            prefs.remove(KEY_NOTIFICATIONS)
        }
    }


    // 여기서 Datastore에 들어갈 key들 정의
    companion object {
        //외부 링크 KEY
        val KEY_GITHUB = stringPreferencesKey("github_url")
        val KEY_LINKEDIN = stringPreferencesKey("linkedin_url")
        val KEY_BLOG = stringPreferencesKey("blog_url")

        //유저 정보 KEY
        val KEY_ID = longPreferencesKey("id")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_NICKNAME = stringPreferencesKey("nickname")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_SCHOOL_ID = longPreferencesKey("school_id")
        val KEY_SCHOOL_NAME = stringPreferencesKey("school_name")
        val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image")
        val KEY_STATUS = stringPreferencesKey("status")
        val KEY_ROLES = stringPreferencesKey("roles")
        val KEY_RECORDS = stringPreferencesKey("challenger_records")
        val KEY_PROFILE = stringPreferencesKey("profile_info")
        val KEY_HAS_LOCAL_CREDENTIAL = booleanPreferencesKey("has_local_credential")
        val KEY_TOTAL_ACTIVITY_DAYS = longPreferencesKey("total_activity_days")
        val KEY_CURRENT_GISU_INFO = stringPreferencesKey("current_gisu_info")
        val KEY_IS_SUPER_ADMIN = booleanPreferencesKey("is_super_admin")


        //유저 명하 목록 KEY
        val KEY_CARD_EPOCH = longPreferencesKey("card_session_epoch")
        val KEY_USER_CARDS = stringPreferencesKey("user_cards")

        //일정 추가에서 장소 기록 KEY
        val KEY_RECENT_SEARCHES_PLACE = stringPreferencesKey("recent_searches_place")

        //게시글 검색에서 게시글 검색 기록 KEY
        val KEY_RECENT_SEARCHES_POST = stringPreferencesKey("recent_searches_post")

        //공지사항 검색에서 검색 기록 KEY
        val KEY_RECENT_SEARCHES_NOTICE = stringPreferencesKey("recent_searches_notice")

        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        // 알림 저장 KEY
        val KEY_NOTIFICATIONS = stringPreferencesKey("notifications")

        // 읽은 공지사항 ID 목록 KEY
        val KEY_READ_NOTICE_IDS = stringPreferencesKey("read_notice_ids")
    }

}
