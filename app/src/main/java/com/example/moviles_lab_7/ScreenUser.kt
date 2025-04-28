package com.example.moviles_lab_7

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    var id        by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var dataUser  = remember { mutableStateOf("") }

    val db: UserDatabase = createDatabase(context)

    val dao = db.userDao()

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Usuarios") },
                actions = {
                    IconButton(onClick = {
                        val user = User(0, firstName, lastName)
                        coroutineScope.launch {
                            addUser(user = user, dao = dao)
                            firstName = ""
                            lastName = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val data = getUsers(dao = dao)
                            dataUser.value = data
                        }
                    }) {
                        Icon(Icons.Default.List, contentDescription = "Listar Usuarios")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(50.dp))

            TextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID (solo lectura)") },
                readOnly = true,
                singleLine = true
            )
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name:") },
                singleLine = true
            )
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name:") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        deleteLastUser(dao = dao)
                        val data = getUsers(dao = dao)
                        dataUser.value = data
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar Último Usuario", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = dataUser.value,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun createDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao:UserDao): String {
    var rpta: String = ""
    //LaunchedEffect(Unit) {
    val users = dao.getAll()
    users.forEach { user ->
        val fila = user.firstName + " - " + user.lastName + "\n"
        rpta += fila
    }
    //}
    return rpta
}

suspend fun addUser(user: User, dao:UserDao): Unit {
    //LaunchedEffect(Unit) {
    try {
        dao.insert(user)
    }
    catch (e: Exception) {
        Log.e("User","Error: insert: ${e.message}")
    }
    //}
}

suspend fun deleteLastUser(dao: UserDao){
    try {
        val lastUser = dao.getLastUser()
        if (lastUser != null) {
            dao.deleteById(lastUser.uid)
        } else {
            Log.d("User", "No users found to delete.")
        }
    } catch (e: Exception) {
        Log.e("User", "Error: delete: ${e.message}")
    }
}