@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.meusgastos.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HomeScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val types by vm.types.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val overview by vm.overview.collectAsStateWithLifecycle()
    val monthIncomes by vm.monthIncomes.collectAsStateWithLifecycle()

    var showIncomes by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var showNew by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var showTypes by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNew = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo gasto") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.month.value = month.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            month.label(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val hint = when (state.scope) {
                            Scope.RANGE -> "Período personalizado"
                            Scope.SEARCH -> "Buscando em todos os meses"
                            Scope.MONTH -> null
                        }
                        if (hint != null) {
                            Text(
                                hint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { vm.month.value = month.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
                    }
                    IconButton(onClick = { showTypes = true }) {
                        Icon(Icons.Outlined.Category, contentDescription = "Tipos de despesa")
                    }
                }
            }

            item {
                OverviewCard(overview, onIncomeClick = { showIncomes = true })
            }

            item {
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = { vm.setQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Buscar (ex.: coxinha)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (filters.query.isNotEmpty()) {
                                IconButton(onClick = { vm.setQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                                }
                            }
                            IconButton(onClick = { showFilters = true }) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Filtros",
                                    tint = if (filters.activeCount > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }

            item { SummaryCard(state) }

            if (state.items.isEmpty()) {
                item {
                    Text(
                        "Nenhum gasto por aqui.\nToque em \"Novo gasto\" para lançar.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.items, key = { it.id }) { e ->
                ExpenseRow(e, onClick = { editing = e })
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }

    if (showNew) {
        ExpenseSheet(
            initial = null,
            types = types,
            onDismiss = { showNew = false },
            onSave = { vm.save(it); showNew = false },
            onDelete = {},
            onManageTypes = { showTypes = true }
        )
    }

    editing?.let { current ->
        ExpenseSheet(
            initial = current,
            types = types,
            onDismiss = { editing = null },
            onSave = { vm.save(it); editing = null },
            onDelete = { vm.delete(it); editing = null },
            onManageTypes = { showTypes = true }
        )
    }

    if (showFilters) {
        FiltersSheet(
            filters = filters,
            types = types,
            onApply = { vm.applyFilters(it); showFilters = false },
            onDismiss = { showFilters = false }
        )
    }

    if (showIncomes) {
        IncomesSheet(
            month = month,
            incomes = monthIncomes,
            onSave = { vm.saveIncome(it) },
            onDelete = { vm.deleteIncome(it) },
            onDismiss = { showIncomes = false }
        )
    }

    if (showTypes) {
        TypesDialog(
            types = types,
            onAdd = { vm.addType(it) },
            onRename = { old, new -> vm.renameType(old, new) },
            onToggleFixed = { name, fixed -> vm.setTypeFixed(name, fixed) },
            onDelete = { name ->
                vm.deleteType(name) { ok ->
                    if (!ok) {
                        Toast.makeText(
                            context,
                            "Esse tipo tem gastos. Mude-os de tipo antes de excluir.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { showTypes = false }
        )
    }
}

@Composable
private fun SummaryCard(state: UiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Total da lista",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                state.total.toMoney(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "${state.items.size} lançamento(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.byType.isNotEmpty() && state.total > 0) {
                Spacer(Modifier.height(6.dp))
                state.byType.forEach { (name, value) ->
                    val color = typeColor(name)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, modifier = Modifier.weight(1f))
                        Text(value.toMoney(), fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(
                        progress = { value.toFloat() / state.total.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(e: Expense, onClick: () -> Unit) {
    val color = typeColor(e.type)
    val date = LocalDate.ofEpochDay(e.epochDay)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    date.dayOfMonth.toString().padStart(2, '0'),
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    e.description,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${e.type} • ${date.fmt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(e.amountCents.toMoney(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PickDate(initial: LocalDate?, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val st = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                st.selectedDateMillis?.let {
                    onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = st)
    }
}

@Composable
private fun ExpenseSheet(
    initial: Expense?,
    types: List<ExpenseType>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    onManageTypes: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(initial?.type ?: types.firstOrNull()?.name ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var amount by remember { mutableStateOf(initial?.amountCents?.toPlain() ?: "") }
    var date by remember {
        mutableStateOf(initial?.let { LocalDate.ofEpochDay(it.epochDay) } ?: LocalDate.now())
    }
    var pickDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(types) {
        if (type.isBlank()) type = types.firstOrNull()?.name ?: ""
    }

    val cents = parseCents(amount)
    val valid = type.isNotBlank() && desc.isNotBlank() && cents != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (initial == null) "Novo gasto" else "Editar gasto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tipo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                types.forEach { t ->
                    FilterChip(
                        selected = type == t.name,
                        onClick = { type = t.name },
                        label = { Text(t.name) }
                    )
                }
                AssistChip(onClick = onManageTypes, label = { Text("+ Tipo") })
            }
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descrição") },
                placeholder = { Text("ex.: comprei uma coxinha") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Valor") },
                placeholder = { Text("4,50") },
                prefix = { Text("R$ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedButton(
                onClick = { pickDate = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(date.fmt())
            }
            Button(
                onClick = {
                    onSave(
                        Expense(
                            id = initial?.id ?: 0L,
                            type = type,
                            description = desc.trim(),
                            amountCents = cents ?: 0L,
                            epochDay = date.toEpochDay()
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Salvar") }
            if (initial != null) {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir gasto", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (pickDate) {
        PickDate(initial = date, onPick = { date = it }, onDismiss = { pickDate = false })
    }

    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir gasto?") },
            text = { Text("\"${initial.description}\" será removido.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete(initial) }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FiltersSheet(
    filters: Filters,
    types: List<ExpenseType>,
    onApply: (Filters) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selTypes by remember { mutableStateOf(filters.types) }
    var minText by remember { mutableStateOf(filters.minCents?.toPlain() ?: "") }
    var maxText by remember { mutableStateOf(filters.maxCents?.toPlain() ?: "") }
    var from by remember { mutableStateOf(filters.from) }
    var to by remember { mutableStateOf(filters.to) }
    var byValue by remember { mutableStateOf(filters.sortByValue) }
    var pickFrom by remember { mutableStateOf(false) }
    var pickTo by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Filtros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Tipo de despesa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                types.forEach { t ->
                    FilterChip(
                        selected = t.name in selTypes,
                        onClick = {
                            selTypes = if (t.name in selTypes) selTypes - t.name else selTypes + t.name
                        },
                        label = { Text(t.name) }
                    )
                }
            }

            Text(
                "Valor",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minText,
                    onValueChange = { minText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Mínimo") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = maxText,
                    onValueChange = { maxText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Máximo") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Text(
                "Período (substitui o mês)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { pickFrom = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(from?.fmt() ?: "De") }
                OutlinedButton(
                    onClick = { pickTo = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(to?.fmt() ?: "Até") }
            }
            if (from != null || to != null) {
                TextButton(onClick = { from = null; to = null }) { Text("Limpar datas") }
            }

            Text(
                "Ordenar por",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !byValue,
                    onClick = { byValue = false },
                    label = { Text("Data") }
                )
                FilterChip(
                    selected = byValue,
                    onClick = { byValue = true },
                    label = { Text("Maior valor") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onApply(Filters(query = filters.query)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Limpar") }
                Button(
                    onClick = {
                        onApply(
                            filters.copy(
                                types = selTypes,
                                minCents = parseCents(minText),
                                maxCents = parseCents(maxText),
                                from = from,
                                to = to,
                                sortByValue = byValue
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Aplicar") }
            }
        }
    }

    if (pickFrom) PickDate(from, onPick = { from = it }, onDismiss = { pickFrom = false })
    if (pickTo) PickDate(to, onPick = { to = it }, onDismiss = { pickTo = false })
}

@Composable
private fun TypesDialog(
    types: List<ExpenseType>,
    onAdd: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onToggleFixed: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipos de despesa") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(typeColor(t.name))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name)
                            Text(
                                if (t.fixed) "Fixo (toque p/ alternar)" else "Variável (toque p/ alternar)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { onToggleFixed(t.name, !t.fixed) }
                                    .padding(vertical = 4.dp)
                            )
                        }
                        IconButton(onClick = { renaming = t.name; input = t.name }) {
                            Icon(Icons.Default.Edit, contentDescription = "Renomear")
                        }
                        IconButton(onClick = { onDelete(t.name) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (renaming != null) "Novo nome" else "Novo tipo") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = input.isNotBlank(),
                onClick = {
                    val r = renaming
                    if (r != null) onRename(r, input) else onAdd(input)
                    input = ""
                    renaming = null
                }
            ) { Text(if (renaming != null) "Salvar" else "Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
