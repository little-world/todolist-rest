
package pro.littleworld.todolist_rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pro.littleworld.todolist_rest.ui.theme.TodolistrestTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodolistrestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoListAppWithApiClient()
                }
            }
        }
    }
}

data class TodoItem(
    val id: Int = 0,
    val title: String = "",
    val completed: Boolean = false
)

interface TodoApi {
    @GET("/todos")
    suspend fun getAll(): List<TodoItem>

    @POST("/todos")
    suspend fun create(@Body item: TodoItem): TodoItem

    @PUT("/todos/{id}")
    suspend fun update(@Path("id") id: Int, @Body item: TodoItem): TodoItem

    @DELETE("/todos/{id}")
    suspend fun delete(@Path("id") id: Int)
}

class TodoApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(TodoApi::class.java)

    suspend fun getAll(): List < TodoItem > {
        val todos = api.getAll ()
        return todos
    }

    suspend fun create(item: TodoItem): TodoItem = api.create(item)

    suspend fun update(item: TodoItem): TodoItem = api.update(item.id, item)

    suspend fun delete(id: Int): Unit = api.delete(id)
}

@Composable
fun TodoListApp(apiClient: TodoApiClient) {
    val todoItems = remember { mutableStateListOf<TodoItem>() }
    var newItemText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newItemText,
                onValueChange = {
                    newItemText = it
                },
                label = {
                    Text("New item")
                },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newItemText.isNotBlank()) {
                        val newItem = TodoItem(
                            id = todoItems.size + 1,
                            title = newItemText,
                            completed = false
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            apiClient.create(newItem)
                            todoItems.add(newItem)
                            newItemText = ""
                        }
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(todoItems) {
                    item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.completed,
                        onCheckedChange = {
                            val updatedItem = item.copy(completed = it)
                            CoroutineScope(Dispatchers.IO).launch {
                                apiClient.update(updatedItem)
                                todoItems[todoItems.indexOf(item)] = updatedItem
                            }
                        }
                    )
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                apiClient.delete(item.id)
                                todoItems.remove(item)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        todoItems.addAll(apiClient.getAll())
    }
}


@Composable
fun TodoListAppWithApiClient() {
    val apiClient = TodoApiClient()
    TodoListApp(apiClient)
}


