package com.example.dinnerrecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dinnerrecipe.ui.theme.DinnerRecipeTheme

/* ---------------------- Data + ViewModel ---------------------- */

data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: String,
    val steps: String
)

class RecipeViewModel : ViewModel() {
    private val _recipes = mutableStateListOf(
        Recipe(1, "Tomato Pasta", "Pasta\nTomato Sauce\nOlive Oil", "1) Boil pasta\n2) Add sauce\n3) Serve"),
        Recipe(2, "Avocado Toast", "Bread\nAvocado\nSalt\nPepper", "1) Toast bread\n2) Spread avocado\n3) Season")
    )
    val recipes: List<Recipe> get() = _recipes
    private var nextId = (_recipes.maxOfOrNull { it.id } ?: 0) + 1

    fun addRecipe(title: String, ingredients: String, steps: String): Int {
        val id = nextId++
        _recipes.add(Recipe(id, title.trim(), ingredients.trim(), steps.trim()))
        return id
    }

    fun getRecipe(id: Int): Recipe? = _recipes.firstOrNull { it.id == id }
}

/* ---------------------- Routes ---------------------- */

sealed class Route(val route: String) {
    object Home : Route("home")
    object Add : Route("add")
    object Settings : Route("settings")
    object Detail : Route("detail") {
        const val withArg = "detail/{id}"
        fun path(id: Int) = "detail/$id"
    }
}

/* ---------------------- Activity ---------------------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DinnerRecipeTheme { RecipeApp() } }
    }
}

/* ---------------------- App Scaffold ---------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeApp(vm: RecipeViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Route.Home.route

    val title = when {
        currentRoute.startsWith(Route.Detail.route) -> "Recipe Details"
        currentRoute == Route.Add.route -> "Add Recipe"
        currentRoute == Route.Settings.route -> "Settings"
        else -> "Recipes"
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        bottomBar = { BottomNavBar(nav) }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Route.Home.route) {
                HomeScreen(
                    recipes = vm.recipes,
                    onOpen = { id ->
                        nav.navigate(Route.Detail.path(id)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Route.Add.route) {
                AddRecipeScreen(
                    onSave = { t, i, s ->
                        val newId = vm.addRecipe(t, i, s)
                        nav.navigate(Route.Detail.path(newId)) {
                            launchSingleTop = true
                            // remove Add from backstack so Back doesn’t return to it
                            popUpTo(Route.Add.route) { inclusive = true }
                        }
                    },
                    onCancel = { nav.popBackStack() }
                )
            }
            composable(Route.Settings.route) {
                SettingsScreen()
            }
            composable(Route.Detail.withArg) { entry ->
                val id = entry.arguments?.getString("id")?.toIntOrNull()
                val recipe = id?.let(vm::getRecipe)
                DetailScreen(recipe = recipe, onBack = { nav.popBackStack() })
            }
        }
    }
}

/* ---------------------- Screens ---------------------- */

@Composable
fun HomeScreen(recipes: List<Recipe>, onOpen: (Int) -> Unit) {
    if (recipes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recipes yet. Add one from below.")
        }
    } else {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes, key = { it.id }) { r ->
                ElevatedCard(
                    onClick = { onOpen(r.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            r.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            r.ingredients.lineSequence().firstOrNull() ?: "No ingredients",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(recipe: Recipe?, onBack: () -> Unit) {
    if (recipe == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Recipe not found")
        }
        return
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(recipe.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Divider()
        Text("Ingredients", style = MaterialTheme.typography.titleMedium)
        Text(recipe.ingredients)
        Text("Steps", style = MaterialTheme.typography.titleMedium)
        Text(recipe.steps)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
fun AddRecipeScreen(onSave: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var ing by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    val canSave = title.isNotBlank() && ing.isNotBlank() && steps.isNotBlank()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Recipe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ing, onValueChange = { ing = it }, label = { Text("Ingredients") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = steps, onValueChange = { steps = it }, label = { Text("Steps") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(enabled = canSave, onClick = { onSave(title, ing, steps) }) { Text("Save") }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings (Placeholder)")
    }
}

/* ---------------------- Bottom Navigation ---------------------- */

@Composable
fun BottomNavBar(nav: NavHostController) {
    val items = listOf(Route.Home, Route.Add, Route.Settings)
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current?.route == item.route,
                onClick = {
                    nav.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Box(Modifier.size(1.dp)) },
                label = { Text(item.route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

/* ---------------------- Preview ---------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDinnerRecipe() {
    DinnerRecipeTheme {
        HomeScreen(
            recipes = listOf(
                Recipe(1, "Sample", "A\nB", "Do something"),
                Recipe(2, "Another", "X\nY", "Mix things")
            ),
            onOpen = {}
        )
    }
}