@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.pawtrackr.ui.clients

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawtrackr.R
import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.Pet
import com.example.pawtrackr.domain.model.PetGender
import com.example.pawtrackr.domain.model.Species
import com.example.pawtrackr.ui.checkout.CheckoutSheet
import com.example.pawtrackr.ui.checkout.CheckoutViewModel
import com.example.pawtrackr.ui.components.PawtrackrAccentEdge
import com.example.pawtrackr.ui.components.PawtrackrCard
import com.example.pawtrackr.ui.components.PawtrackrChip
import com.example.pawtrackr.ui.components.PawtrackrChipStyle
import com.example.pawtrackr.ui.components.PawtrackrChipTone
import com.example.pawtrackr.ui.components.PawtrackrEmptyState
import com.example.pawtrackr.ui.components.PawtrackrFab
import com.example.pawtrackr.ui.components.PawtrackrKpiCard
import com.example.pawtrackr.ui.components.PawtrackrPhotoWell
import com.example.pawtrackr.ui.components.PawtrackrSearchField
import com.example.pawtrackr.ui.components.PawtrackrSectionTitle
import com.example.pawtrackr.ui.components.PawtrackrTimelineItem
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun money(v: BigDecimal): String = "$" + v.setScale(2, RoundingMode.HALF_UP).toPlainString()
private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())
private fun fmtDate(ms: Long): String = dateFmt.format(Instant.ofEpochMilli(ms))
private val AGGRESSIVE_RED = PawtrackrSemanticColor.Danger

private data class CheckoutTarget(val visitId: String, val petId: String, val clientId: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: ClientsViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    checkoutFactoryProvider: (visitId: String, petId: String, clientId: String?) -> ViewModelProvider.Factory,
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
    var checkoutTarget by remember { mutableStateOf<CheckoutTarget?>(null) }
    var editClient by remember { mutableStateOf<Client?>(null) }
    var deleteClientConfirm by remember { mutableStateOf<Client?>(null) }
    var editPet by remember { mutableStateOf<Pet?>(null) }
    var deletePetConfirm by remember { mutableStateOf<Pet?>(null) }
    var messageClient by remember { mutableStateOf<Client?>(null) }
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    val onCheckIn: (String) -> Unit = viewModel::checkIn
    val onStartCheckout: (Pet) -> Unit = { pet ->
        pet.visits.firstOrNull { it.isActive }?.let { active ->
            checkoutTarget = CheckoutTarget(active.id, pet.id, pet.ownerId)
        }
    }

    // Phone back navigation: pet -> client -> list.
    BackHandler(enabled = !isExpanded && selectedClientId != null) {
        if (selectedPetId != null) viewModel.selectPet(null) else viewModel.clearClientSelection()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (!isExpanded && selectedClient != null) selectedClient.fullName else stringResource(R.string.clients_title)) },
                navigationIcon = {
                    if (!isExpanded && selectedClientId != null) {
                        IconButton(onClick = {
                            if (selectedPetId != null) viewModel.selectPet(null) else viewModel.clearClientSelection()
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back)) }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isExpanded || selectedClientId == null) {
                PawtrackrFab(
                    label = stringResource(R.string.clients_new_client),
                    onClick = { showAddClient = true }
                )
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
                        DetailPane(selectedClient, selectedPet, viewModel, now, { addPetForClientId = it }, onCheckIn, onStartCheckout,
                            onEditClient = { editClient = it }, onDeleteClient = { deleteClientConfirm = it },
                            onEditPet = { editPet = it }, onDeletePet = { deletePetConfirm = it },
                            onMessageClient = { messageClient = it })
                    }
                }
            } else {
                if (selectedClientId == null) {
                    ClientsListPane(state, selectedClientId, viewModel, now)
                } else {
                    DetailPane(selectedClient, selectedPet, viewModel, now, { addPetForClientId = it }, onCheckIn, onStartCheckout,
                            onEditClient = { editClient = it }, onDeleteClient = { deleteClientConfirm = it },
                            onEditPet = { editPet = it }, onDeletePet = { deletePetConfirm = it },
                            onMessageClient = { messageClient = it })
                }
            }
        }
    }

    if (showAddClient) {
        ClientFormDialog(
            title = stringResource(R.string.clients_new_client), confirmLabel = stringResource(R.string.action_add),
            onDismiss = { showAddClient = false },
            onConfirm = { f, l, p, e -> viewModel.addClient(f, l, p, e); showAddClient = false }
        )
    }
    editClient?.let { c ->
        ClientFormDialog(
            title = stringResource(R.string.clients_edit_client), confirmLabel = stringResource(R.string.action_save),
            initialFirst = c.firstName, initialLast = c.lastName,
            initialPhone = c.phone.orEmpty(), initialEmail = c.email.orEmpty(),
            onDismiss = { editClient = null },
            onConfirm = { f, l, p, e -> viewModel.updateClient(c.id, f, l, p, e); editClient = null }
        )
    }
    deleteClientConfirm?.let { c ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.clients_delete_client_title, c.fullName),
            message = stringResource(R.string.clients_delete_client_message),
            onDismiss = { deleteClientConfirm = null },
            onConfirm = { viewModel.deleteClient(c.id); deleteClientConfirm = null }
        )
    }
    addPetForClientId?.let { ownerId ->
        PetFormDialog(
            title = stringResource(R.string.clients_new_pet), confirmLabel = stringResource(R.string.action_add),
            onDismiss = { addPetForClientId = null },
            onConfirm = { name, species, gender, breed ->
                viewModel.addPet(ownerId, name, species, gender, breed); addPetForClientId = null
            }
        )
    }
    editPet?.let { p ->
        PetFormDialog(
            title = stringResource(R.string.clients_edit_pet), confirmLabel = stringResource(R.string.action_save),
            initialName = p.name, initialBreed = p.breed.orEmpty(),
            initialSpecies = p.species, initialGender = p.gender,
            onDismiss = { editPet = null },
            onConfirm = { name, species, gender, breed -> viewModel.updatePet(p.id, name, species, gender, breed); editPet = null }
        )
    }
    deletePetConfirm?.let { p ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.clients_delete_pet_title, p.name),
            message = stringResource(R.string.clients_delete_pet_message),
            onDismiss = { deletePetConfirm = null },
            onConfirm = { viewModel.deletePet(p.id); deletePetConfirm = null }
        )
    }
    messageClient?.let { c ->
        MessageSheet(
            templates = templates,
            ownerName = c.firstName.ifBlank { c.fullName },
            petName = c.pets.firstOrNull()?.name,
            phone = c.phone,
            onDismiss = { messageClient = null }
        )
    }
    checkoutTarget?.let { target ->
        val checkoutVm: CheckoutViewModel = viewModel(
            key = "checkout-${target.visitId}",
            factory = checkoutFactoryProvider(target.visitId, target.petId, target.clientId)
        )
        CheckoutSheet(
            viewModel = checkoutVm,
            onDismiss = { checkoutTarget = null },
            onComplete = { checkoutTarget = null }
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
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)
    ) {
        PawtrackrSearchField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            placeholder = stringResource(R.string.clients_search_placeholder),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs),
                verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
            ) {
                ClientFilter.entries.forEach { f ->
                    PawtrackrChip(
                        label = f.label,
                        tone = if (state.filter == f) PawtrackrChipTone.Brand else PawtrackrChipTone.Neutral,
                        style = if (state.filter == f) PawtrackrChipStyle.Filled else PawtrackrChipStyle.Outline,
                        onClick = { viewModel.setFilter(f) },
                    )
                }
            }
            SortMenu(state.sort, viewModel::setSort)
        }
        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.isEmpty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PawtrackrEmptyState(
                    title = stringResource(R.string.clients_empty_title),
                    body = stringResource(R.string.clients_empty_body)
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                section(R.string.clients_section_in_session, state.inSession, selectedClientId, viewModel, now)
                section(R.string.clients_section_needs_attention, state.needsAttention, selectedClientId, viewModel, now)
                section(
                    if (state.inSession.isEmpty() && state.needsAttention.isEmpty()) null else R.string.clients_section_all_clients,
                    state.others,
                    selectedClientId,
                    viewModel,
                    now
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    titleRes: Int?,
    clients: List<Client>,
    selectedClientId: String?,
    viewModel: ClientsViewModel,
    now: Long
) {
    if (clients.isEmpty()) return
    if (titleRes != null) {
        item(key = "header-$titleRes") {
            PawtrackrSectionTitle(
                title = stringResource(titleRes),
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
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
    val accent = clientAccentColor(client, now)
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accent,
        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            InitialsAvatar(name = client.fullName)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                    Text(client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (client.hasAggressivePet) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = stringResource(R.string.clients_aggressive_pet),
                            tint = AGGRESSIVE_RED,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
                val sub = client.primaryContact.ifBlank { stringResource(R.string.clients_no_contact) }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (client.pets.isNotEmpty()) {
                    Text(
                        client.pets.take(3).joinToString { it.name },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                PawtrackrChip(
                    label = petCountLabel(client.petCount),
                    tone = PawtrackrChipTone.Neutral,
                    style = PawtrackrChipStyle.Outline
                )
                when {
                    client.hasActiveVisit -> PawtrackrChip(stringResource(R.string.clients_status_in_session), tone = PawtrackrChipTone.Success)
                    client.needsAttention(now) -> PawtrackrChip(stringResource(R.string.clients_status_overdue), tone = PawtrackrChipTone.Danger)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenu(current: ClientSort, onPick: (ClientSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text(stringResource(R.string.clients_sort_prefix, current.label)) }
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
    onAddPet: (String) -> Unit,
    onCheckIn: (String) -> Unit,
    onStartCheckout: (Pet) -> Unit,
    onEditClient: (Client) -> Unit,
    onDeleteClient: (Client) -> Unit,
    onEditPet: (Pet) -> Unit,
    onDeletePet: (Pet) -> Unit,
    onMessageClient: (Client) -> Unit
) {
    when {
        client == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.clients_select_client), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        pet != null -> PetDetailPane(
            pet, now,
            onCheckIn = { onCheckIn(pet.id) },
            onStartCheckout = { onStartCheckout(pet) },
            onEdit = { onEditPet(pet) },
            onDelete = { onDeletePet(pet) }
        )
        else -> ClientDetailPane(
            client, now,
            onSelectPet = viewModel::selectPet,
            onAddPet = { onAddPet(client.id) },
            onEdit = { onEditClient(client) },
            onDelete = { onDeleteClient(client) },
            onMessage = { onMessageClient(client) }
        )
    }
}

@Composable
private fun petCountLabel(count: Int): String =
    if (count == 1) stringResource(R.string.clients_pet_count_singular)
    else stringResource(R.string.clients_pet_count_plural, count)

private fun clientAccentColor(client: Client, now: Long): Color =
    when {
        client.hasAggressivePet -> PawtrackrSemanticColor.Danger
        client.hasActiveVisit -> PawtrackrSemanticColor.Success
        client.needsAttention(now) -> PawtrackrSemanticColor.Warning
        else -> PawtrackrStaticColor.BrandPrimary
    }

@Composable
private fun InitialsAvatar(name: String, modifier: Modifier = Modifier) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "P" }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PawtrackrStaticColor.BrandPrimary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            color = PawtrackrStaticColor.BrandPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ClientDetailPane(
    client: Client,
    now: Long,
    onSelectPet: (String) -> Unit,
    onAddPet: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMessage: () -> Unit
) {
    val revenue = client.pets.fold(BigDecimal.ZERO) { acc, p -> acc + p.lifetimeValue }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
        item {
            PawtrackrCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = if (client.hasAggressivePet) PawtrackrSemanticColor.Danger else PawtrackrStaticColor.BrandPrimary,
                accentEdge = PawtrackrAccentEdge.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
                    ) {
                        InitialsAvatar(client.fullName, Modifier.size(52.dp))
                        Column(Modifier.weight(1f)) {
                            Text(client.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (client.primaryContact.isNotBlank()) {
                                Text(client.primaryContact, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                        if (client.hasAggressivePet) {
                            PawtrackrChip(
                                label = stringResource(R.string.clients_aggressive_pet),
                                tone = PawtrackrChipTone.Danger,
                                leadingIcon = Icons.Default.Warning
                            )
                        }
                        if (client.hasMissingInfo) {
                            PawtrackrChip(label = stringResource(R.string.clients_missing_info), tone = PawtrackrChipTone.Warning)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                        TextButton(onClick = onMessage) { Text(stringResource(R.string.clients_message)) }
                        TextButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
                        TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = AGGRESSIVE_RED) }
                    }
                }
            }
        }
        item {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                PawtrackrKpiCard(stringResource(R.string.clients_pets), client.petCount.toString(), Modifier.weight(1f), PawtrackrStaticColor.BrandPrimary)
                PawtrackrKpiCard(stringResource(R.string.clients_visits), client.pets.sumOf { it.completedVisitCount }.toString(), Modifier.weight(1f), PawtrackrSemanticColor.Info)
                PawtrackrKpiCard(stringResource(R.string.clients_revenue), money(revenue), Modifier.weight(1f), PawtrackrSemanticColor.Success)
            }
        }
        item {
            PawtrackrSectionTitle(
                title = stringResource(R.string.clients_pets),
                trailing = {
                    TextButton(onClick = onAddPet) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.clients_add_pet))
                    }
                }
            )
        }
        if (client.pets.isEmpty()) item {
            PawtrackrCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.clients_no_pets), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(client.pets, key = { it.id }) { p -> PetRow(p, now) { onSelectPet(p.id) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetRow(pet: Pet, now: Long, onClick: () -> Unit) {
    val accent = when {
        pet.isAggressive -> PawtrackrSemanticColor.Danger
        pet.hasActiveVisit -> PawtrackrSemanticColor.Success
        pet.needsAttention(now) -> PawtrackrSemanticColor.Warning
        else -> PawtrackrStaticColor.BrandPrimary
    }
    PawtrackrCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accent
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            InitialsAvatar(pet.name, Modifier.size(40.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                    Text(pet.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (pet.isAggressive) Icon(Icons.Default.Warning, stringResource(R.string.clients_aggressive_pet), tint = AGGRESSIVE_RED, modifier = Modifier.height(16.dp))
                }
                val age = pet.ageString(now)?.let { " • $it" } ?: ""
                Text(pet.shortDescriptor + age, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when {
                pet.hasActiveVisit -> PawtrackrChip(stringResource(R.string.clients_status_in_session), tone = PawtrackrChipTone.Success)
                pet.needsAttention(now) -> PawtrackrChip(stringResource(R.string.clients_status_overdue), tone = PawtrackrChipTone.Warning)
            }
        }
    }
}

@Composable
private fun PetDetailPane(
    pet: Pet,
    now: Long,
    onCheckIn: () -> Unit,
    onStartCheckout: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
        item {
            PawtrackrCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = if (pet.isAggressive) PawtrackrSemanticColor.Danger else PawtrackrStaticColor.BrandPrimary,
                accentEdge = PawtrackrAccentEdge.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                        InitialsAvatar(pet.name, Modifier.size(52.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pet.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(pet.shortDescriptor, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            pet.ageString(now)?.let { Text(stringResource(R.string.clients_age_prefix, it), style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                        TextButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
                        TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = AGGRESSIVE_RED) }
                    }
                }
            }
        }
        item {
            if (pet.hasActiveVisit) {
                Button(onClick = onStartCheckout, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.clients_checkout_end_session))
                }
            } else {
                Button(onClick = onCheckIn, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.clients_check_in))
                }
            }
        }
        if (pet.isAggressive) item {
            PawtrackrCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = PawtrackrSemanticColor.Danger,
                containerColor = PawtrackrSemanticColor.Danger.copy(alpha = 0.10f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = AGGRESSIVE_RED)
                    Spacer(Modifier.width(PawtrackrTokens.sm))
                    Text(stringResource(R.string.clients_safety_banner), color = AGGRESSIVE_RED, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (pet.behaviorTags.isNotEmpty()) item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
                pet.behaviorTags.forEach { PawtrackrChip(label = it, tone = if (pet.isAggressive) PawtrackrChipTone.Danger else PawtrackrChipTone.Neutral) }
            }
        }
        item {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
                PawtrackrKpiCard(stringResource(R.string.clients_visits), pet.completedVisitCount.toString(), Modifier.weight(1f), PawtrackrSemanticColor.Info)
                PawtrackrKpiCard(stringResource(R.string.clients_lifetime), money(pet.lifetimeValue), Modifier.weight(1f), PawtrackrSemanticColor.Success)
            }
        }
        item { PawtrackrSectionTitle(stringResource(R.string.clients_visit_history)) }
        val sorted = pet.visits.sortedByDescending { it.sortKeyDate }
        if (sorted.isEmpty()) item {
            PawtrackrCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.clients_no_visits), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(sorted, key = { it.id }) { v ->
            PawtrackrCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)) {
                    PawtrackrTimelineItem(
                        title = fmtDate(v.sortKeyDate),
                        subtitle = if (v.isActive) stringResource(R.string.clients_status_in_session) else money(v.effectiveTotal),
                        accentColor = if (v.isActive) PawtrackrSemanticColor.Success else PawtrackrStaticColor.BrandPrimary,
                        showConnector = false,
                        trailing = {
                            if (v.isActive) PawtrackrChip(stringResource(R.string.clients_status_in_session), tone = PawtrackrChipTone.Success)
                            else Text(money(v.effectiveTotal), fontWeight = FontWeight.SemiBold)
                        }
                    )
                    if (v.hasPhotos) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            v.beforeThumb?.let {
                                PawtrackrPhotoWell(
                                    label = stringResource(R.string.clients_photo_before),
                                    bytes = it,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            v.afterThumb?.let {
                                PawtrackrPhotoWell(
                                    label = stringResource(R.string.clients_photo_after),
                                    bytes = it,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientFormDialog(
    title: String,
    confirmLabel: String,
    initialFirst: String = "",
    initialLast: String = "",
    initialPhone: String = "",
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit
) {
    var first by remember { mutableStateOf(initialFirst) }
    var last by remember { mutableStateOf(initialLast) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(first, { first = it }, label = { Text(stringResource(R.string.clients_first_name)) }, singleLine = true)
                OutlinedTextField(last, { last = it }, label = { Text(stringResource(R.string.clients_last_name)) }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.clients_phone)) }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.clients_email)) }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = first.isNotBlank() || last.isNotBlank(),
                onClick = { onConfirm(first, last, phone.ifBlank { null }, email.ifBlank { null }) }
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetFormDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialBreed: String = "",
    initialSpecies: Species = Species.DOG,
    initialGender: PetGender = PetGender.MALE,
    onDismiss: () -> Unit,
    onConfirm: (String, Species, PetGender, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var breed by remember { mutableStateOf(initialBreed) }
    var species by remember { mutableStateOf(initialSpecies) }
    var gender by remember { mutableStateOf(initialGender) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.clients_name)) }, singleLine = true)
                OutlinedTextField(breed, { breed = it }, label = { Text(stringResource(R.string.clients_breed_optional)) }, singleLine = true)
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
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name, species, gender, breed.ifBlank { null }) }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun ConfirmDeleteDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete), color = AGGRESSIVE_RED) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
