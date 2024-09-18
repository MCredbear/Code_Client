@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package moe.redbear.code_client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import moe.redbear.code_client.ui.theme.CodeClientTheme

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    CodeClientTheme {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(title = { Text(text = "Code Client") }) }
        ) { innerPadding ->
            var newHostName by remember { mutableStateOf("") }
            var newHostUrl by remember { mutableStateOf("") }

            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier
                        .padding(all = 50.dp)
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    OutlinedTextField(
                        value = newHostName,
                        onValueChange = { newHostName = it },
                        label = { Text("Host Name") },
                        modifier = Modifier.weight(3f)
                    )

                    OutlinedTextField(
                        value = newHostUrl,
                        onValueChange = { newHostUrl = it },
                        label = { Text("Host URL") },
                        modifier = Modifier
                            .weight(7f)
                            .padding(horizontal = 10.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri)
                    )

                    Button(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add a new host",
                            modifier = Modifier.size(40.dp, 50.dp)
                        )
                    }
                }

                var selectedHost by remember { mutableStateOf<Host?>(null) }
                var showEditDialog by remember { mutableStateOf(false) }

                FlowRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(50.dp),
                    maxItemsInEachRow = 2
                ) {


                    Settings.hosts.map { host ->
                        Card(
                            modifier = Modifier
                                .padding(PaddingValues(10.dp))
                                .weight(1f)
                                .fillMaxWidth()
                                .height(80.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = host.name
                                        )
                                        Text(
                                            text = host.url
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            selectedHost = host
                                            showEditDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Set this code server",
                                                modifier = Modifier.size(50.dp, 50.dp)
                                            )
                                        }
                                        IconButton(onClick = { navController.navigate("webview/${host.name}") }) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Access to this code server",
                                                modifier = Modifier.size(50.dp, 50.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showEditDialog) {
                    val editingHostName = remember { mutableStateOf(selectedHost?.name ?: "") }
                    val editingHostUrl = remember { mutableStateOf(selectedHost?.url ?: "") }

                    Dialog(onDismissRequest = { showEditDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Edit Host",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                TextField(
                                    value = editingHostName.value,
                                    onValueChange = { editingHostName.value = it },
                                    label = { Text("Host Name") }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TextField(
                                    value = editingHostUrl.value,
                                    onValueChange = { editingHostUrl.value = it },
                                    label = { Text("Host URL") }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            selectedHost?.name = editingHostName.value
                                            selectedHost?.url = editingHostUrl.value
                                            Settings.save(context)
                                            showEditDialog = false
                                        }
                                    ) {
                                        Text("Save")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            showEditDialog = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            Settings.hosts.remove(selectedHost)
                                            Settings.save(context)
                                            showEditDialog = false
                                        }
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}