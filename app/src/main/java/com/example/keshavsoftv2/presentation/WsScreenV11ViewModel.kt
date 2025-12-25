import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keshavsoftv2.presentation.VoiceWsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WsScreenV11ViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private var collectingStarted = false

    /* ───────── WS LIFECYCLE ───────── */

    fun onActiveChanged(active: Boolean) {
        if (active) {
            VoiceWsClient.connect()
        }
        // ❌ NEVER close on pager swipe
    }

    fun startCollectingMessagesOnce() {
        if (collectingStarted) return
        collectingStarted = true

        viewModelScope.launch {
            VoiceWsClient.incomingMessages.collectLatest { msg ->
                _messages.value = _messages.value + msg
            }
        }
    }

    fun startCollectingMessagesOnceV1() {
        if (collectingStarted) return
        collectingStarted = true

        viewModelScope.launch {
            VoiceWsClient.incomingMessages.collectLatest { msg ->

                _messages.value =
                    if (_messages.value.isEmpty()) {
                        listOf(msg)
                    } else {
                        _messages.value.dropLast(1) + msg
                    }
            }
        }


    }

    fun onSpokenText(text: String) {
        if (text.isBlank()) return
        VoiceWsClient.sendFinal(text)
        _messages.value = _messages.value + "You: $text"
    }

    override fun onCleared() {
        super.onCleared()
        VoiceWsClient.close()
    }
}
