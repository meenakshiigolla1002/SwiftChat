
package com.example.swiftchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

// --------------------------------------------------
// SWIFTCHAT COLOUR PALETTE
// --------------------------------------------------

private val DeepPurple = Color(0xFF4F32A1)
private val Purple = Color(0xFF7754D8)
private val Lavender = Color(0xFFEEE7FF)
private val PageBackground = Color(0xFFF6F3FF)
private val HeaderBackground = Color(0xFFF1EBFF)
private val Ink = Color(0xFF25213B)
private val Muted = Color(0xFF777188)
private val White = Color.White

private val SplashGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF9578EC),
        Purple,
        DeepPurple
    )
)

private val PageGradient = Brush.verticalGradient(
    colors = listOf(
        HeaderBackground,
        PageBackground,
        Color(0xFFFCFBFF)
    )
)

// --------------------------------------------------
// DATA MODELS
// --------------------------------------------------

data class ChatMessage(
    val text: String,
    val fromMe: Boolean
)

class ChatThread(
    val id: Int,
    val name: String,
    val isGroup: Boolean = false,
    val messages: SnapshotStateList<ChatMessage> =
        mutableStateListOf()
)

// --------------------------------------------------
// SHARED APPLICATION STATE
// --------------------------------------------------

class SwiftChatState {

    // Settings preferences
    var displayName by mutableStateOf("SwiftChat User")
    var showMessagePreviews by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(true)

    // Temporary chat data
    val chats = mutableStateListOf(
        ChatThread(
            id = 1,
            name = "Alex",
            messages = mutableStateListOf(
                ChatMessage("Hey! How are you?", false),
                ChatMessage("I'm good, thanks!", true)
            )
        ),

        ChatThread(
            id = 2,
            name = "Priya",
            messages = mutableStateListOf(
                ChatMessage(
                    "Are we meeting tomorrow?",
                    false
                )
            )
        ),

        ChatThread(
            id = 3,
            name = "Study Group",
            isGroup = true,
            messages = mutableStateListOf(
                ChatMessage(
                    "Welcome to the study group!",
                    false
                )
            )
        )
    )

    // Send message
    fun sendMessage(chatId: Int, text: String) {

        if (text.isBlank()) return

        chats.find { it.id == chatId }
            ?.messages
            ?.add(
                ChatMessage(
                    text = text.trim(),
                    fromMe = true
                )
            )
    }

    // Create group
    fun createGroup(name: String): Int {

        val newId =
            (chats.maxOfOrNull { it.id } ?: 0) + 1

        chats.add(
            index = 0,
            element = ChatThread(
                id = newId,
                name = name.trim(),
                isGroup = true,
                messages = mutableStateListOf(
                    ChatMessage(
                        "Group created successfully!",
                        false
                    )
                )
            )
        )

        return newId
    }

    // Reset settings
    fun resetSettings() {

        displayName = "SwiftChat User"
        showMessagePreviews = true
        vibrationEnabled = true
    }
}

// --------------------------------------------------
// MAIN ACTIVITY
// --------------------------------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Purple,
                    onPrimary = White,
                    secondary = DeepPurple,
                    background = PageBackground,
                    surface = White,
                    onSurface = Ink
                )
            ) {

                val state = remember {
                    SwiftChatState()
                }

                SwiftChatApp(state)
            }
        }
    }
}

// --------------------------------------------------
// NAVIGATION
// --------------------------------------------------

@Composable
fun SwiftChatApp(state: SwiftChatState) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // Splash Screen
        composable("splash") {

            SplashScreen(
                onFinished = {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Home Screen
        composable("home") {

            HomeScreen(
                state = state,

                onChatClick = { chatId ->
                    navController.navigate("chat/$chatId")
                },

                onNewGroup = {
                    navController.navigate("new_group")
                },

                onSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Chat Screen
        composable(
            route = "chat/{chatId}",
            arguments = listOf(
                navArgument("chatId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->

            val chatId =
                entry.arguments?.getInt("chatId") ?: -1

            ChatScreen(
                state = state,
                chatId = chatId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Create Group
        composable("new_group") {

            CreateGroupScreen(
                state = state,

                onBack = {
                    navController.popBackStack()
                },

                onCreated = { id ->
                    navController.navigate("chat/$id") {
                        popUpTo("home")
                    }
                }
            )
        }

        // Settings
        composable("settings") {

            SettingsScreen(
                state = state,

                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// --------------------------------------------------
// SHARED UI COMPONENTS
// --------------------------------------------------

@Composable
private fun PageSurface(
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageGradient),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwiftTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {

    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        },

        navigationIcon = {
            if (onBack != null) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "‹ Back",
                        color = Purple
                    )
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun ChatAvatar(
    name: String,
    isGroup: Boolean,
    size: Int = 54
) {

    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                Lavender,
                CircleShape
            ),

        contentAlignment = Alignment.Center
    ) {

        Text(
            text = if (isGroup) {
                "👥"
            } else {
                name.take(1).uppercase()
            },

            fontSize = if (isGroup) 23.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = DeepPurple
        )
    }
}

// --------------------------------------------------
// SCREEN 1 - SPLASH SCREEN
// --------------------------------------------------

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashGradient),

        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(108.dp)
                    .background(
                        White.copy(alpha = 0.17f),
                        RoundedCornerShape(30.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "💬",
                    fontSize = 58.sp
                )
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "SwiftChat",
                color = White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Connect. Chat. Share.",
                color = White.copy(alpha = 0.85f),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(34.dp))

            CircularProgressIndicator(
                color = White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// --------------------------------------------------
// SCREEN 2 - HOME SCREEN
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: SwiftChatState,
    onChatClick: (Int) -> Unit,
    onNewGroup: () -> Unit,
    onSettings: () -> Unit
) {

    Scaffold(
        containerColor = Color.Transparent,

        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "SwiftChat",
                        color = DeepPurple,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp
                    )
                },

                actions = {
                    TextButton(onClick = onSettings) {

                        Text(
                            text = "Settings ⚙",
                            color = DeepPurple
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderBackground
                )
            )
        },

        floatingActionButton = {

            ExtendedFloatingActionButton(
                onClick = onNewGroup,
                containerColor = Purple,
                contentColor = White,
                shape = RoundedCornerShape(20.dp)
            ) {

                Text(
                    text = "＋ New Group",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { padding ->

        PageSurface {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 22.dp,
                        bottom = 16.dp
                    )
                ) {

                    Text(
                        text = "Your conversations",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = "Stay close to your favourite people",
                        color = Muted,
                        fontSize = 14.sp
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 105.dp
                    ),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = state.chats,
                        key = { it.id }
                    ) { chat ->

                        Card(
                            onClick = {
                                onChatClick(chat.id)
                            },

                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(22.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),

                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                ChatAvatar(
                                    name = chat.name,
                                    isGroup = chat.isGroup
                                )

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = chat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Ink
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    // Functional message preview setting
                                    Text(
                                        text = if (
                                            state.showMessagePreviews
                                        ) {
                                            chat.messages
                                                .lastOrNull()
                                                ?.text
                                                ?: "No messages yet"
                                        } else {
                                            "Message preview hidden"
                                        },

                                        color = Muted,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "›",
                                    fontSize = 30.sp,
                                    color = Purple
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// SCREEN 3 - CHAT SCREEN
// --------------------------------------------------

@Composable
fun ChatScreen(
    state: SwiftChatState,
    chatId: Int,
    onBack: () -> Unit
) {

    val chat = state.chats.find {
        it.id == chatId
    }

    if (chat == null) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Conversation not found")
        }

        return
    }

    var message by remember(chatId) {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()

    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(chat.messages.size) {

        if (chat.messages.isNotEmpty()) {

            listState.animateScrollToItem(
                chat.messages.lastIndex
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,

        topBar = {

            Surface(
                color = HeaderBackground,
                shadowElevation = 2.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(76.dp)
                        .padding(
                            start = 8.dp,
                            end = 16.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        onClick = onBack,

                        contentPadding = PaddingValues(
                            horizontal = 8.dp
                        )
                    ) {

                        Text(
                            text = "‹",
                            fontSize = 30.sp,
                            color = Purple
                        )
                    }

                    ChatAvatar(
                        name = chat.name,
                        isGroup = chat.isGroup,
                        size = 44
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = chat.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = if (chat.isGroup) {
                                "Group chat"
                            } else {
                                "Conversation"
                            },

                            color = Muted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        },

        bottomBar = {

            Surface(
                color = White,
                shadowElevation = 6.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = message,

                        onValueChange = {
                            message = it
                        },

                        modifier = Modifier.weight(1f),

                        placeholder = {
                            Text("Type a message...")
                        },

                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Lavender,
                            focusedContainerColor = PageBackground,
                            unfocusedContainerColor = PageBackground
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {

                            if (message.isNotBlank()) {

                                state.sendMessage(
                                    chatId = chatId,
                                    text = message
                                )

                                // Functional vibration preference
                                if (state.vibrationEnabled) {

                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                }

                                message = ""
                            }
                        },

                        enabled = message.isNotBlank(),

                        shape = RoundedCornerShape(18.dp),

                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 15.dp
                        )
                    ) {

                        Text("Send")
                    }
                }
            }
        }
    ) { padding ->

        PageSurface {

            LazyColumn(
                state = listState,

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(chat.messages) { item ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = if (item.fromMe) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                    ) {

                        Surface(
                            color = if (item.fromMe) {
                                Purple
                            } else {
                                White
                            },

                            shadowElevation = if (item.fromMe) {
                                0.dp
                            } else {
                                2.dp
                            },

                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,

                                bottomEnd = if (item.fromMe) {
                                    4.dp
                                } else {
                                    18.dp
                                },

                                bottomStart = if (item.fromMe) {
                                    18.dp
                                } else {
                                    4.dp
                                }
                            ),

                            modifier = Modifier.widthIn(
                                max = 285.dp
                            )
                        ) {

                            Text(
                                text = item.text,

                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),

                                color = if (item.fromMe) {
                                    White
                                } else {
                                    Ink
                                },

                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// SCREEN 4 - CREATE GROUP
// --------------------------------------------------

@Composable
fun CreateGroupScreen(
    state: SwiftChatState,
    onBack: () -> Unit,
    onCreated: (Int) -> Unit
) {

    var groupName by remember {
        mutableStateOf("")
    }

    val contacts = listOf(
        "Alex",
        "Priya",
        "Sam",
        "Jordan"
    )

    val selected = remember {
        mutableStateListOf<String>()
    }

    Scaffold(
        containerColor = Color.Transparent,

        topBar = {
            SwiftTopBar(
                title = "Create a group",
                onBack = onBack
            )
        }
    ) { padding ->

        PageSurface {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 18.dp,
                        vertical = 20.dp
                    )
            ) {

                Text(
                    text = "Bring everyone together",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Name your group and choose its members.",
                    color = Muted
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),

                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedTextField(
                        value = groupName,

                        onValueChange = {
                            groupName = it
                        },

                        label = {
                            Text("Group name")
                        },

                        placeholder = {
                            Text("e.g. Weekend Friends")
                        },

                        singleLine = true,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),

                        shape = RoundedCornerShape(15.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Select participants",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {

                    contacts.forEach { contact ->

                        val checked = contact in selected

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    if (checked) {
                                        selected.remove(contact)
                                    } else {
                                        selected.add(contact)
                                    }
                                },

                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 7.dp
                                    ),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                ChatAvatar(
                                    name = contact,
                                    isGroup = false,
                                    size = 42
                                )

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    text = contact,
                                    modifier = Modifier.weight(1f),
                                    color = Ink,
                                    fontWeight = FontWeight.Medium
                                )

                                Checkbox(
                                    checked = checked,

                                    onCheckedChange = { value ->

                                        if (value) {

                                            if (contact !in selected) {
                                                selected.add(contact)
                                            }

                                        } else {
                                            selected.remove(contact)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {

                        val id = state.createGroup(
                            name = groupName
                        )

                        onCreated(id)
                    },

                    enabled = groupName.isNotBlank() &&
                            selected.isNotEmpty(),

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = "Create Group",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// SCREEN 5 - SETTINGS
// --------------------------------------------------

@Composable
fun SettingsScreen(
    state: SwiftChatState,
    onBack: () -> Unit
) {

    var showEditProfile by remember {
        mutableStateOf(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }

    var showResetConfirmation by remember {
        mutableStateOf(false)
    }

    var editedName by remember {
        mutableStateOf(state.displayName)
    }

    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        containerColor = Color.Transparent,

        topBar = {
            SwiftTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { padding ->

        PageSurface {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 18.dp,
                        vertical = 20.dp
                    ),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ----------------------------------
                // PROFILE SECTION
                // ----------------------------------

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Lavender,
                            CircleShape
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = state.displayName
                            .take(1)
                            .uppercase(),

                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepPurple
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = state.displayName,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Your personal profile",
                    color = Muted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(18.dp))

                OutlinedButton(
                    onClick = {
                        editedName = state.displayName
                        showEditProfile = true
                    },

                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text("Edit Profile")
                }

                Spacer(Modifier.height(30.dp))

                // ----------------------------------
                // CHAT PREFERENCES
                // ----------------------------------

                Text(
                    text = "Chat Preferences",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        )
                    ) {

                        SettingsToggleRow(
                            title = "Message Previews",
                            description = "Show recent messages on the home screen",
                            checked = state.showMessagePreviews,

                            onCheckedChange = {
                                state.showMessagePreviews = it
                            }
                        )

                        HorizontalDivider(
                            color = Lavender
                        )

                        SettingsToggleRow(
                            title = "Vibration",
                            description = "Enable haptic feedback in the app",
                            checked = state.vibrationEnabled,

                            onCheckedChange = {
                                state.vibrationEnabled = it
                            }
                        )

                        if (state.vibrationEnabled) {

                            TextButton(
                                onClick = {

                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                }
                            ) {

                                Text(
                                    text = "Test Vibration",
                                    color = DeepPurple
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ----------------------------------
                // APPLICATION OPTIONS
                // ----------------------------------

                Text(
                    text = "Application",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        // About SwiftChat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAbout = true
                                }
                                .padding(vertical = 12.dp),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "About SwiftChat",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "App information and version",
                                    color = Muted,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = "›",
                                fontSize = 26.sp,
                                color = Purple
                            )
                        }

                        HorizontalDivider(
                            color = Lavender
                        )

                        // Reset Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showResetConfirmation = true
                                }
                                .padding(vertical = 12.dp),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "Reset Settings",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Restore default preferences",
                                    color = Muted,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = "›",
                                fontSize = 26.sp,
                                color = Purple
                            )
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "SwiftChat",
                    color = DeepPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Version 1.0 • Local Prototype",
                    color = Muted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // --------------------------------------------------
    // EDIT PROFILE DIALOG
    // --------------------------------------------------

    if (showEditProfile) {

        AlertDialog(
            onDismissRequest = {
                showEditProfile = false
            },

            title = {
                Text("Edit Profile")
            },

            text = {

                Column {

                    Text(
                        text = "Change your display name.",
                        color = Muted
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editedName,

                        onValueChange = {
                            if (it.length <= 30) {
                                editedName = it
                            }
                        },

                        label = {
                            Text("Display Name")
                        },

                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        state.displayName = editedName.trim()
                        showEditProfile = false
                    },

                    enabled = editedName.isNotBlank()
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditProfile = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            shape = RoundedCornerShape(24.dp)
        )
    }

    // --------------------------------------------------
    // ABOUT SWIFTCHAT DIALOG
    // --------------------------------------------------

    if (showAbout) {

        AlertDialog(
            onDismissRequest = {
                showAbout = false
            },

            title = {
                Text("About SwiftChat")
            },

            text = {

                Column {

                    Text(
                        text = "SwiftChat",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepPurple
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "SwiftChat is a lightweight " +
                                "messaging application designed " +
                                "for personal conversations " +
                                "and small group chats."
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Built With",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Kotlin\n" +
                                "Jetpack Compose\n" +
                                "Material Design 3"
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Version: 1.0\n" +
                                "Status: Local Prototype",

                        color = Muted,
                        fontSize = 13.sp
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showAbout = false
                    }
                ) {

                    Text("Close")
                }
            },

            shape = RoundedCornerShape(24.dp)
        )
    }

    // --------------------------------------------------
    // RESET SETTINGS CONFIRMATION
    // --------------------------------------------------

    if (showResetConfirmation) {

        AlertDialog(
            onDismissRequest = {
                showResetConfirmation = false
            },

            title = {
                Text("Reset Settings?")
            },

            text = {

                Text(
                    text = "This will restore your display " +
                            "name and preferences to their " +
                            "default values. Your conversations " +
                            "will not be deleted."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        state.resetSettings()

                        showResetConfirmation = false
                    }
                ) {

                    Text("Reset")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showResetConfirmation = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            shape = RoundedCornerShape(24.dp)
        )
    }
}

// --------------------------------------------------
// REUSABLE SETTINGS TOGGLE ROW
// --------------------------------------------------

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(vertical = 12.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Muted,
                lineHeight = 19.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,

            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = Purple,
                uncheckedTrackColor = Lavender,
                uncheckedThumbColor = Muted
            )
        )
    }
}
