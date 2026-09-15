package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountRepository
import com.example.data.AppDatabase
import com.example.data.GmailAccount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    FLOW,
    NOTEBOOK,
    ASSETS
}

enum class FlowViewMode {
    CANVAS_STUDIO,
    WEB_OFFICIAL
}

enum class NotebookViewMode {
    SYNTHESIS_COPILOT,
    WEB_OFFICIAL
}

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Ahora"
)

data class ContinuityRow(
    val id: String,
    val blockTitle: String,
    val status: String,
    val scenario: String,
    val character: String,
    val wardrobe: String,
    val keyObject: String
)

class FlowHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountRepository

    val allAccounts: StateFlow<List<GmailAccount>>
    val activeAccount: StateFlow<GmailAccount?>

    private val _currentTab = MutableStateFlow(MainTab.FLOW)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _flowMode = MutableStateFlow(FlowViewMode.CANVAS_STUDIO)
    val flowMode: StateFlow<FlowViewMode> = _flowMode.asStateFlow()

    private val _notebookMode = MutableStateFlow(NotebookViewMode.SYNTHESIS_COPILOT)
    val notebookMode: StateFlow<NotebookViewMode> = _notebookMode.asStateFlow()

    private val _flowWebUrl = MutableStateFlow("https://flow.google")
    val flowWebUrl: StateFlow<String> = _flowWebUrl.asStateFlow()

    private val _notebookWebUrl = MutableStateFlow("https://notebooklm.google.com")
    val notebookWebUrl: StateFlow<String> = _notebookWebUrl.asStateFlow()

    // Status / Snack notifications
    private val _notificationMessage = MutableSharedFlow<String>()
    val notificationMessage: SharedFlow<String> = _notificationMessage.asSharedFlow()

    // NotebookLM Chat
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "1",
                sender = "Tú",
                text = "¿Podemos verificar si el color del vestuario en la escena 2 tiene conflicto con la iluminación nocturna?",
                isUser = true,
                timestamp = "10:14"
            ),
            ChatMessage(
                id = "2",
                sender = "NotebookLM",
                text = "De acuerdo a la Tabla de Continuidad, la chaqueta es 'Chaqueta cuero negro con forro reflectante'. Contrastará con claridad bajo la iluminación azul neón.",
                isUser = false,
                timestamp = "10:15"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    // Continuity Table Rows
    private val _continuityRows = MutableStateFlow(
        listOf(
            ContinuityRow(
                id = "1",
                blockTitle = "Bloque 01: Introducción",
                status = "Consistente",
                scenario = "Azotea Neón Lluviosa",
                character = "Alex Walker",
                wardrobe = "Chaqueta cuero negro con forro reflectante",
                keyObject = "Unidad Neural USB-C"
            ),
            ContinuityRow(
                id = "2",
                blockTitle = "Bloque 02: Persecución",
                status = "Revisar Props",
                scenario = "Mercado Subterráneo B3",
                character = "Alex Walker",
                wardrobe = "Chaqueta rota manga der.",
                keyObject = "Terminal Portátil Holo"
            )
        )
    )
    val continuityRows: StateFlow<List<ContinuityRow>> = _continuityRows.asStateFlow()

    // Flow Canvas Node state
    private val _pipelineId = MutableStateFlow("Pipeline #408")
    val pipelineId: StateFlow<String> = _pipelineId.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering: StateFlow<Boolean> = _isRendering.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    // Asset filter
    private val _selectedAssetCategory = MutableStateFlow("All")
    val selectedAssetCategory: StateFlow<String> = _selectedAssetCategory.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AccountRepository(db.gmailAccountDao())

        allAccounts = repository.allAccounts.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        activeAccount = repository.activeAccount.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setFlowMode(mode: FlowViewMode) {
        _flowMode.value = mode
    }

    fun setNotebookMode(mode: NotebookViewMode) {
        _notebookMode.value = mode
    }

    fun setFlowWebUrl(url: String) {
        _flowWebUrl.value = url
    }

    fun setNotebookWebUrl(url: String) {
        _notebookWebUrl.value = url
    }

    fun setAssetCategory(cat: String) {
        _selectedAssetCategory.value = cat
    }

    /**
     * Cycles to the next Gmail account in order, exactly as requested:
     * "un botón que es multicuenta que cada vez que uno le clica Ese botón va a cambiar de cuenta
     * o sea uno va a poder registrar varias cuentas Gmail que van a estar cargadas
     * y cuando yo quiera cambiar de cuentas de flow solo hay un clic a Ese botón y cambiala a la otra cuenta
     * y así sucesivamente va cambiando de cuenta cada vez que le doy un click"
     */
    fun cycleAccount() {
        viewModelScope.launch {
            val next = repository.cycleToNextAccount()
            if (next != null) {
                _notificationMessage.emit("Cuenta cambiada a: ${next.displayName} (${next.email})")
            } else {
                _notificationMessage.emit("No hay cuentas registradas. Agrega una cuenta Gmail.")
            }
        }
    }

    fun selectAccount(account: GmailAccount) {
        viewModelScope.launch {
            repository.setActive(account.id)
            _notificationMessage.emit("Cuenta activa: ${account.displayName} (${account.email})")
        }
    }

    fun addGmailAccount(email: String, name: String, label: String, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(email, name, label, colorHex)
            _notificationMessage.emit("Cuenta añadida: $email")
        }
    }

    fun deleteGmailAccount(account: GmailAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            _notificationMessage.emit("Cuenta eliminada: ${account.email}")
        }
    }

    fun setChatInput(input: String) {
        _chatInput.value = input
    }

    fun sendChatMessage() {
        val query = _chatInput.value.trim()
        if (query.isEmpty()) return

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "Tú",
            text = query,
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _chatInput.value = ""

        viewModelScope.launch {
            val active = activeAccount.value?.displayName ?: "Usuario"
            val aiResponse = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "NotebookLM",
                text = "Análisis para $active: Procesando '$query'. Según las fuentes enlazadas del proyecto, los parámetros narrativos y modelos Lumina están validados.",
                isUser = false
            )
            _chatMessages.value = _chatMessages.value + aiResponse
        }
    }

    fun addContinuityRow(title: String, scenario: String, character: String, wardrobe: String, item: String) {
        val newRow = ContinuityRow(
            id = System.currentTimeMillis().toString(),
            blockTitle = title.ifEmpty { "Bloque 0${_continuityRows.value.size + 1}" },
            status = "Consistente",
            scenario = scenario.ifEmpty { "Locación Principal" },
            character = character.ifEmpty { "Alex Walker" },
            wardrobe = wardrobe.ifEmpty { "Indumentaria Escénica" },
            keyObject = item.ifEmpty { "Accesorio #1" }
        )
        _continuityRows.value = _continuityRows.value + newRow
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 0.15f).coerceAtMost(2.5f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 0.15f).coerceAtLeast(0.6f)
    }

    fun resetZoom() {
        _zoomLevel.value = 1.0f
    }

    fun toggleRender() {
        _isRendering.value = !_isRendering.value
        viewModelScope.launch {
            val state = if (_isRendering.value) "Render iniciado con Lumina Motion V2" else "Render pausado"
            _notificationMessage.emit(state)
        }
    }
}
