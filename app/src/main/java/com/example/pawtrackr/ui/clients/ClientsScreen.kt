@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.pawtrackr.ui.clients

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.Pet
import com.example.pawtrackr.domain.model.PetGender
import com.example.pawtrackr.domain.model.Species
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()
private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())
private fun fmtDate(ms: Long): String = dateFmt.format(Instant.ofEpochMilli(ms))
private val AGGRESSIVE_RED = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedClientId by viewModel.selectedClientId.collectAsStateWithLifecycle()
    val selectedPetId by viewModel.selectedPetId.collectAsStateWithLifecycle()
    val now = remember { System.currentTimeMillis() }

    val selectedClient = state.all.firstOrNull { it.id == selectedClientId }
    val selectedPet = selectedClient?.pets?.firstOrNull { it.id == selectedPetId }
    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    var showAddClient by remember { mutableStateOf(false) }
    var addPetForClientId by remember { mutableStateOf<String?>(null) }

    // Phone back navigation: pet -> client -> list.
    BackHandler(enabled = !isExpanded && selectedClientId != null) {
        if (selectedPetId != null) viewModel.selectPet(null) else viewModel.clearClientSelection()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (!isExpanded && selectedClient != null) selectedClient.fullName else "Clients") },
                navigationIcon = {
                    if (!isExpanded && selectedClientId != null) {
                        IconButton(onClick = {
                            if (selectedPetId != null) viewModel.selectPet(null) else viewModel.clearClientSelection()
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isExpanded || selectedClientId == null) {
                FloatingActionButton(onClick = { showAddClient = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add client")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isExpanded) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        ClientsListPane(state, selectedClientId, viewModel, now)
                    }
                    Box(Modifier.weight(1.6f).fillMaxHeight().padding(start = 8.dp)) {
                        DetailPane(selectedClient, selectedPet, viewModel, now, onAddPet = { addPetForClientId = it })
                    }
                }
            } else {
                if (selectedClientId == null) {
                    ClientsListPane(state, selectedClientId, viewModel, now)
                } else {
                    DetailPane(selectedClient, selectedPet, viewModel, now, onAddPet = { addPetForClientId = it })
                }
            }
        }
    }

    if (showAddClient) {
        AddClientDialog(
            onDismiss = { showAddClient = false },
            onConfirm = { f, l, p, e -> viewModel.addClient(f, l, p, e); showAddClient = false }
        )
    }
    addPetForClientId?.let { ownerId ->
        AddPetDialog(
            onDismiss = { addPetForClientId = null },
            onConfirm = { name, species, gender, breed ->
                viewModel.addPet(ownerId, name, species, gender, breed); addPetForClientId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientsListPane(
    state: ClientsUiState,
    selectedClientId: String?,
    viewModel: ClientsViewModel,
    now: Long
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search clients & pets") },
            singleLine = true
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ClientFilter.entries.forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) }
                    )
                }
            }
            SortMenu(state.sort, viewModel::setSort)
        }
        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.isEmpty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No clients match.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                section("In Session", state.inSession, selectedClientId, viewModel, now)
                section("Needs Attention", state.needsAttention, selectedClientId, viewModel, now)
                section(if (state.inSession.isEmpty() && state.needsAttention.isEmpty()) "" else "All Clients", state.others, selectedClientId, viewModel, now)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    clients: List<Client>,
    selectedClientId: String?,
    viewModel: ClientsViewModel,
    now: Long
) {
    if (clients.isEmpty()) return
    if (title.isNotEmpty()) {
        item(key = "header-$title") {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
    }
    items(clients, key = { it.id }) { client ->
        ClientRow(client, selected = client.id == selectedClientId, now = now) { viewModel.selectClient(client.id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientRow(client: Client, selected: Boolean, now: Long, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else CardDefaults.cardColors()
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (client.hasAggressivePet) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Warning, contentDescription = "Aggressive pet", tint = AGGRESSIVE_RED, modifier = Modifier.height(18.dp))
                    }
                }
                val sub = client.primaryContact.ifBlank { "No contact info" }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${client.petCount} ${if (client.petCount == 1) "pet" else "pets"}", style = MaterialTheme.typography.labelMedium)
                if (client.hasActiveVisit) Text("In session", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                else if (client.needsAttention(now)) Text("Overdue", style = MaterialTheme.typography.labelSmall, color = AGGRESSIVE_RED)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenu(current: ClientSort, onPick: (ClientSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text("Sort: ${current.label}") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ClientSort.entries.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.label + if (s == current) "  ✓" else "") },
                    onClick = { onPick(s); open = false }
                )
            }
        }
    }
}

@Composable
private fun DetailPane(
    client: Client?,
    pet: Pet?,
    viewModel: ClientsViewModel,
    now: Long,
    onAddPet: (String) -> Unit
) {
    when {
        client == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a client", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        pet != null -> PetDetailPane(pet, now)
        else -> ClientDetailPane(client, now, onSelectPet = viewModel::selectPet, onAddPet = { onAddPet(client.id) })
    }
}

@Composable
private fun ClientDetailPane(client: Client, now: Long, onSelectPet: (String) -> Unit, onAddPet: () -> Unit) {
    val revenue = client.pets.fold(BigDecimal.ZERO) { acc, p -> acc + p.lifetimeValue }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(client.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (client.primaryContact.isNotBlank()) Text(client.primaryContact, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (client.hasAggressivePet) AssistChip(onClick = {}, label = { Text("Aggressive pet") },
                    leadingIcon = { Icon(Icons.Default.Warning, null, tint = AGGRESSIVE_RED) })
                if (client.hasMissingInfo) AssistChip(onClick = {}, label = { Text("Missing info") })
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Pets", client.petCount.toString(), Modifier.weight(1f))
                StatCard("Visits", client.pets.sumOf { it.completedVisitCount }.toString(), Modifier.weight(1f))
                StatCard("Revenue", money(revenue), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Pets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddPet) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Add pet") }
            }
        }
        if (client.pets.isEmpty()) item { Text("No pets yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(client.pets, key = { it.id }) { p -> PetRow(p, now) { onSelectPet(p.id) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetRow(pet: Pet, now: Long, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pet.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (pet.isAggressive) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Warning, "Aggressive", tint = AGGRESSIVE_RED, modifier = Modifier.height(16.dp))
                    }
                }
                val age = pet.ageString(now)?.let { " • $it" } ?: ""
                Text(pet.shortDescriptor + age, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (pet.hasActiveVisit) Text("In session", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            else if (pet.needsAttention(now)) Text("Overdue", style = MaterialTheme.typography.labelSmall, color = AGGRESSIVE_RED)
        }
    }
}

@Composable
private fun PetDetailPane(pet: Pet, now: Long) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(pet.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(pet.shortDescriptor, color = MaterialTheme.colorScheme.onSurfaceVariant)
            pet.ageString(now)?.let { Text("Age: $it", style = MaterialTheme.typography.bodyMedium) }
        }
        if (pet.isAggressive) item {
            Card(colors = CardDefaults.cardColors(containerColor = AGGRESSIVE_RED.copy(alpha = 0.12f))) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = AGGRESSIVE_RED)
                    Spacer(Modifier.width(8.dp))
                    Text("Handle with care — flagged aggressive", color = AGGRESSIVE_RED, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (pet.behaviorTags.isNotEmpty()) item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pet.behaviorTags.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Visits", pet.completedVisitCount.toString(), Modifier.weight(1f))
                StatCard("Lifetime", money(pet.lifetimeValue), Modifier.weight(1f))
            }
        }
        item { Text("Visit history", style = MaterialTheme.typography.titleMedium) }
        val sorted = pet.visits.sortedByDescending { it.sortKeyDate }
        if (sorted.isEmpty()) item { Text("No visits yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(sorted, key = { it.id }) { v ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fmtDate(v.sortKeyDate))
                    if (v.isActive) Text("In session", color = MaterialTheme.colorScheme.primary)
                    else Text(money(v.effectiveTotal), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String?, String?) -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New client") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(first, { first = it }, label = { Text("First name") }, singleLine = true)
                OutlinedTextField(last, { last = it }, label = { Text("Last name") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = first.isNotBlank() || last.isNotBlank(),
                onClick = { onConfirm(first, last, phone.ifBlank { null }, email.ifBlank { null }) }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPetDialog(onDismiss: () -> Unit, onConfirm: (String, Species, PetGender, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var species by remember { mutableStateOf(Species.DOG) }
    var gender by remember { mutableStateOf(PetGender.MALE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(breed, { breed = it }, label = { Text("Breed (optional)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Species.entries.forEach { s ->
                        FilterChip(selected = species == s, onClick = { species = s }, label = { Text(s.displayName) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PetGender.entries.forEach { g ->
                        FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g.displayName) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name, species, gender, breed.ifBlank { null }) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
