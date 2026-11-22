package com.example.swiftchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private val Purple = Color(0xFF6750A4)
private val LightPurple = Color(0xFFEADDFF)

// Temporary models - no database required
data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
)

class ChatThread(
    val id: Int,
    val name: String,
    val isGroup: Boolean = false,
    val messages: SnapshotStateList<ChatMessage> =
        mutableStateListOf(),
)

// Shared data while the app is running
class SwiftChatState {
    val chats = mutableStateListOf(
        ChatThread(
            1, "Alex",
            messages = mutableStateListOf(
                ChatMessage("Hey! How are you?", fromMe = false),
                ChatMessage("I'm good, thanks!", fromMe = true),
            ),
        ),
        ChatThread(
            2, "Priya",
            messages = mutableStateListOf(
                ChatMessage("Are we meeting tomorrow?", fromMe = false),
            ),
        ),
        ChatThread(
            3, "Study Group", isGroup = true,
            messages = mutableStateListOf(
                ChatMessage("Welcome to the study group!", fromMe = false),
            ),
        )
    )

    fun sendMessage(chatId: Int, text: String) {
        if (text.isBlank()) return
        chats.find { it.id == chatId }
            ?.messages?.add(ChatMessage(text.trim(), fromMe = true))
    }

    fun createGroup(name: String): Int {
        val newId = (chats.maxOfOrNull { it.id } ?: 0) + 1
        chats.add(
            0,
            ChatThread(
                newId,
                name.trim(),
                isGroup = true,
                messages = mutableStateListOf(
                    ChatMessage("Group created successfully!", fromMe = false),
                )
            )
        )
        return newId
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state = remember { SwiftChatState() }
                SwiftChatApp(state)
            }
        }
    }
}

@Composable
fun SwiftChatApp(state: SwiftChatState) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("home") {
            HomeScreen(
                state = state,
                onChatClick = { id ->
                    navController.navigate("chat/$id")
                },
                onNewGroup = {
                    navController.navigate("new_group")
                },
                onSettings = {
                    navController.navigate("settings")
                }
            )
        }

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
                onBack = { navController.popBackStack() }
            )
        }

        composable("new_group") {
            CreateGroupScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate("chat/$id") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// SCREEN 1: SPLASH

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500.milliseconds)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Purple),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "SwiftChat",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Connect. Chat. Share.",
                color = Color.White
            )
        }
    }
}

// SCREEN 2: HOME

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: SwiftChatState,
    onChatClick: (Int) -> Unit,
    onNewGroup: () -> Unit,
    onSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SwiftChat", fontWeight = FontWeight.Bold)
                },
                actions = {
                    TextButton(onClick = onSettings) {
                        Text("Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewGroup,
                containerColor = Purple,
                contentColor = Color.White
            ) {
                Text("+ New Group")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                "Your conversations",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn {
                items(state.chats, key = { it.id }) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatClick(chat.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(LightPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (chat.isGroup) "👥"
                                else chat.name.take(1).uppercase(),
                                fontSize = 22.sp,
                                color = Purple
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                chat.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                chat.messages.lastOrNull()?.text
                                    ?: "No messages yet",
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }

                        Text("›", fontSize = 28.sp, color = Purple)
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}

// SCREEN 3: ONE-TO-ONE OR GROUP CHAT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: SwiftChatState,
    chatId: Int,
    onBack: () -> Unit
) {
    val chat = state.chats.find { it.id == chatId }

    if (chat == null) {
        Text("Conversation not found")
        return
    }

    var message by remember(chatId) { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chat.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        state.sendMessage(chatId, message)
                        message = ""
                    },
                    enabled = message.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chat.messages) { (text, fromMe) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        if (fromMe) Arrangement.End
                        else Arrangement.Start
                ) {
                    Surface(
                        color = if (fromMe) Purple
                        else LightPurple,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(12.dp),
                            color = if (fromMe) Color.White
                            else Color.Black
                        )
                    }
                }
            }
        }
    }
}

// SCREEN 4: CREATE GROUP

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    state: SwiftChatState,
    onBack: () -> Unit,
    onCreated: (Int) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val contacts = listOf("Alex", "Priya", "Sam", "Jordan")
    val selected = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Select participants",
                fontWeight = FontWeight.Bold
            )

            contacts.forEach { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (contact in selected)
                                selected.remove(contact)
                            else selected.add(contact)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = contact in selected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (contact !in selected)
                                    selected.add(contact)
                            } else {
                                selected.remove(contact)
                            }
                        }
                    )
                    Text(contact)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val id = state.createGroup(groupName)
                    onCreated(id)
                },
                enabled = groupName.isNotBlank() &&
                        selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Group")
            }
        }
    }
}

// SCREEN 5: SETTINGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .background(LightPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("U", fontSize = 36.sp, color = Purple)
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "SwiftChat User",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Demo profile", color = Color.Gray)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Text(
                "About SwiftChat",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "A simple messaging application built with " +
                        "Kotlin and Jetpack Compose.",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Version 1.0 - Core functionality prototype",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
