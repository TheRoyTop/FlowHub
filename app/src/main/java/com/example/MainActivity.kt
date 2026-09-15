package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FlowHubViewModel
import com.example.ui.MainTab
import com.example.ui.components.AccountManagerSheet
import com.example.ui.components.FloatingMultiAccountButton
import com.example.ui.components.MainSegmentedTabs
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.SearchDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopHeader
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.FlowScreen
import com.example.ui.screens.NotebookScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Primary
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.Tertiary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FlowHubApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowHubApp(
    viewModel: FlowHubViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val flowMode by viewModel.flowMode.collectAsStateWithLifecycle()
    val notebookMode by viewModel.notebookMode.collectAsStateWithLifecycle()
    val flowWebUrl by viewModel.flowWebUrl.collectAsStateWithLifecycle()
    val notebookWebUrl by viewModel.notebookWebUrl.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val isRendering by viewModel.isRendering.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInput by viewModel.chatInput.collectAsStateWithLifecycle()
    val continuityRows by viewModel.continuityRows.collectAsStateWithLifecycle()
    val selectedAssetCategory by viewModel.selectedAssetCategory.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAccountSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Listen to notification events
    LaunchedEffect(Unit) {
        viewModel.notificationMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest),
        containerColor = SurfaceContainerLowest,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("app_snackbar")
            )
        },
        topBar = {
            Column {
                TopHeader(
                    activeAccount = activeAccount,
                    onOpenSearch = { showSearchDialog = true },
                    onOpenNotifications = { showNotificationsDialog = true },
                    onOpenSettings = { showSettingsDialog = true },
                    onOpenAccounts = { showAccountSheet = true }
                )
                MainSegmentedTabs(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen content
            when (currentTab) {
                MainTab.FLOW -> {
                    FlowScreen(
                        mode = flowMode,
                        flowWebUrl = flowWebUrl,
                        activeAccount = activeAccount,
                        isRendering = isRendering,
                        zoomLevel = zoomLevel,
                        onModeChange = { viewModel.setFlowMode(it) },
                        onZoomIn = { viewModel.zoomIn() },
                        onZoomOut = { viewModel.zoomOut() },
                        onResetZoom = { viewModel.resetZoom() },
                        onToggleRender = { viewModel.toggleRender() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainTab.NOTEBOOK -> {
                    NotebookScreen(
                        mode = notebookMode,
                        notebookWebUrl = notebookWebUrl,
                        activeAccount = activeAccount,
                        chatMessages = chatMessages,
                        chatInput = chatInput,
                        onModeChange = { viewModel.setNotebookMode(it) },
                        onChatInputChange = { viewModel.setChatInput(it) },
                        onSendChat = { viewModel.sendChatMessage() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MainTab.ASSETS -> {
                    AssetsScreen(
                        continuityRows = continuityRows,
                        selectedCategory = selectedAssetCategory,
                        onCategorySelected = { viewModel.setAssetCategory(it) },
                        onAddContinuityRow = { title, scenario, character, wardrobe, item ->
                            viewModel.addContinuityRow(title, scenario, character, wardrobe, item)
                        },
                        onExecuteSkill = { skillName ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ejecutando prompt skill: $skillName")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Floating Multi-Account Switcher Button
            // Single tap: automatically cycles to next registered Gmail account!
            // Long press / click expand: opens full account manager sheet!
            FloatingMultiAccountButton(
                activeAccount = activeAccount,
                totalAccounts = allAccounts.size,
                onCycleAccount = { viewModel.cycleAccount() },
                onOpenManager = { showAccountSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    // Modal Bottom Sheet for Account Management
    if (showAccountSheet) {
        AccountManagerSheet(
            sheetState = sheetState,
            accounts = allAccounts,
            activeAccount = activeAccount,
            onDismiss = { showAccountSheet = false },
            onSelectAccount = { account ->
                viewModel.selectAccount(account)
                showAccountSheet = false
            },
            onAddNewAccount = { email, name, label, colorHex ->
                viewModel.addGmailAccount(email, name, label, colorHex)
            },
            onDeleteAccount = { account ->
                viewModel.deleteGmailAccount(account)
            },
            onCycleNext = {
                viewModel.cycleAccount()
            }
        )
    }

    // Dialogs
    if (showSettingsDialog) {
        SettingsDialog(
            currentFlowUrl = flowWebUrl,
            currentNotebookUrl = notebookWebUrl,
            activeAccount = activeAccount,
            onSaveUrls = { flow, notebook ->
                viewModel.setFlowWebUrl(flow)
                viewModel.setNotebookWebUrl(notebook)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(onDismiss = { showNotificationsDialog = false })
    }

    if (showSearchDialog) {
        SearchDialog(onDismiss = { showSearchDialog = false })
    }
}

// Kept for Robolectric screenshot & baseline test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
