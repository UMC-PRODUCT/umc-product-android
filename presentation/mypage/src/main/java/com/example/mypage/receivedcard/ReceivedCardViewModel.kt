package com.example.mypage.receivedcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.mypage.CardBook
import com.umc.domain.model.mypage.CardSession
import com.umc.domain.model.mypage.CardWriteContext
import com.umc.domain.model.mypage.StoredCard
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.repository.AppDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceivedCardViewModel @Inject constructor(
    private val repository: AppDataStoreRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<ReceivedCardUiState, ReceivedCardEvent>(
    ReceivedCardUiState(searchQuery = savedStateHandle["cardSearch"] ?: ""),
) {
    private var book = CardBook()
    private var session: CardSession? = null
    private var observer: Job? = null
    private var receivedInitialState = false

    init { retry() }

    fun retry() {
        observer?.cancel()
        updateState { copy(isLoading = true, error = null) }
        observer = viewModelScope.launch {
            repository.getCardBook().catch {
                updateState { copy(isLoading = false, error = "명함을 불러오지 못했어요. 다시 시도해주세요.") }
            }.collect { state ->
                val changedAccount = if (receivedInitialState) session != state.session else
                    savedStateHandle.get<Long>("cardSearchOwner")?.let { it != (state.session?.ownerId ?: 0L) } ?: false
                receivedInitialState = true
                if (changedAccount) savedStateHandle["cardSearch"] = ""
                session = state.session
                savedStateHandle["cardSearchOwner"] = state.session?.ownerId ?: 0L
                book = state.book
                updateState {
                    val query = if (changedAccount) "" else searchQuery
                    val cards = book.visibleCards.map { it.card }.toImmutableList()
                    copy(
                        searchQuery = query, allCards = cards, filteredCards = filterCards(cards, query),
                        isLoading = false,
                        error = if (state.storageUnavailable) "명함 저장소를 읽을 수 없어요. 원본을 보존했으니 다시 시도해주세요." else null,
                        hasLegacyRecords = state.hasLegacyRecords,
                        selectedId = if (changedAccount || book.cards[selectedId]?.pendingDelete != false) null else selectedId,
                        noteDraft = if (changedAccount) "" else noteDraft,
                        mutationError = if (changedAccount) null else mutationError,
                        isSaving = if (changedAccount) false else isSaving,
                        entries = book.cards,
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle["cardSearch"] = query
        updateState { copy(searchQuery = query, filteredCards = filterCards(allCards, query)) }
    }
    fun clearSearchQuery() = onSearchQueryChanged("")

    // 아래 상세(메모·삭제) 함수는 명함 상세 화면 디자인이 나오면 다시 연결한다. 지금은 화면에서 부르지 않는다.
    fun selectCard(id: String) {
        val entry = book.cards[id]?.takeUnless { it.pendingDelete } ?: return
        updateState { copy(selectedId = id, noteDraft = entry.note, mutationError = null, showDeleteConfirmation = false) }
    }
    fun editNote(value: String) { if (!uiState.value.isSaving) updateState { copy(noteDraft = value, mutationError = null) } }
    fun closeDetail() { if (!uiState.value.isSaving) updateState { copy(selectedId = null, mutationError = null, showDeleteConfirmation = false) } }
    fun confirmDelete(show: Boolean) = updateState { copy(showDeleteConfirmation = show) }
    fun saveNote() = mutate(delete = false)
    fun deleteCard() = mutate(delete = true)

    private fun mutate(delete: Boolean) {
        if (uiState.value.isSaving) return
        val id = uiState.value.selectedId ?: return
        val expectedSession = session ?: return
        val context = CardWriteContext(expectedSession, book.revision)
        val note = uiState.value.noteDraft
        updateState { copy(isSaving = true, mutationError = null) }
        viewModelScope.launch {
            try {
                if (delete) repository.removeUserCard(id, context) else repository.updateCardNote(id, note, context)
                if (session == expectedSession) updateState {
                    copy(isSaving = false, selectedId = null, showDeleteConfirmation = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (session == expectedSession) updateState {
                    copy(isSaving = false, mutationError = "저장하지 못했어요. 내용을 유지했으니 다시 시도해주세요.")
                }
            }
        }
    }
}

private fun filterCards(cards: List<UserCard>, query: String): ImmutableList<UserCard> {
    val keyword = query.trim()
    return cards.filter { card -> keyword.isEmpty() || listOf(card.name, card.nickname, card.part, card.university)
        .any { it.contains(keyword, ignoreCase = true) } }.toImmutableList()
}

data class ReceivedCardUiState(
    val searchQuery: String = "",
    val allCards: ImmutableList<UserCard> = persistentListOf(),
    val filteredCards: ImmutableList<UserCard> = persistentListOf(),
    val entries: Map<String, StoredCard> = emptyMap(),
    val selectedId: String? = null,
    val noteDraft: String = "",
    val isSaving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val mutationError: String? = null,
    val hasLegacyRecords: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false,
) : UiState

sealed interface ReceivedCardEvent : UiEvent
