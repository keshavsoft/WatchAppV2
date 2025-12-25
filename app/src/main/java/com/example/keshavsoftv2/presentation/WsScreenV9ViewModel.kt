import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keshavsoftv2.presentation.VoiceWsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WsScreenV9ViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    /* ───────── WEBSOCKET ───────── */

    fun onActiveChanged(active: Boolean) {
        if (active) VoiceWsClient.connect()
        else VoiceWsClient.close()
    }

    fun startCollectingMessages() {
        viewModelScope.launch {
            VoiceWsClient.incomingMessages.collectLatest { msg ->
                _messages.value = _messages.value + msg
            }
        }
    }

    /* ───────── VOICE CALLBACK ───────── */

    fun onSpokenText(text: String) {
        VoiceWsClient.sendFinal(text)
        _messages.value = _messages.value + "You: $text"
    }
}
