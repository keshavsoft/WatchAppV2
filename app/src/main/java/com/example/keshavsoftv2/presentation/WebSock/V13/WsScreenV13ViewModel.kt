import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keshavsoftv2.presentation.VoiceWsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WsScreenV13ViewModel : ViewModel() {

    /* ───────── UI STATE ───────── */

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages
    private var lastServerMessage: String? = null

    private var isCollecting = false

    /* ───────── SCREEN / WS LIFECYCLE ───────── */

    fun onScreenActive(active: Boolean) {
        if (active) {
            VoiceWsClient.connect()
        }
        // ❌ never close on pager swipe
    }

    /* ───────── WS INCOMING ───────── */
    fun startListeningOnce() {
        if (isCollecting) return
        isCollecting = true

        viewModelScope.launch {
            VoiceWsClient.incomingMessages.collectLatest { msg ->

                // Ignore exact repeats
                if (msg == lastServerMessage) return@collectLatest

                lastServerMessage = msg
                _messages.value = _messages.value + msg
            }
        }

    }

    /* ───────── USER ACTION ───────── */

    fun onUserSpokenV1(text: String) {
        if (text.isBlank()) return

        VoiceWsClient.sendFinal(text)
        appendMessage("You: $text")
    }

    fun onUserSpoken(text: String) {
        if (text.isBlank()) return

        lastServerMessage = null   // 🔑 reset server dedupe
        VoiceWsClient.sendFinal(text)
        _messages.value = _messages.value + "You: $text"
    }

    /* ───────── HELPERS ───────── */

    private fun appendMessage(message: String) {
        _messages.value = _messages.value + message
    }

    override fun onCleared() {
        super.onCleared()
        VoiceWsClient.close()
    }
}
