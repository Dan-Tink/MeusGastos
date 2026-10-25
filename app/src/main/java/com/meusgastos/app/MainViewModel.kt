package com.meusgastos.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.YearMonth

data class Filters(
    val query: String = "",
    val types: Set<String> = emptySet(),
    val minCents: Long? = null,
    val maxCents: Long? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val sortByValue: Boolean = false
) {
    val dateActive: Boolean get() = from != null || to != null
    val activeCount: Int
        get() = listOf(
            types.isNotEmpty(),
            minCents != null || maxCents != null,
            dateActive,
            sortByValue
        ).count { it }
}

enum class Scope { MONTH, RANGE, SEARCH }

data class UiState(
    val items: List<Expense> = emptyList(),
    val total: Long = 0,
    val byType: List<Pair<String, Long>> = emptyList(),
    val scope: Scope = Scope.MONTH
)

/** Resumo do mês exibido no topo: não depende de busca nem de filtros. */
data class Overview(
    val income: Long = 0,
    val fixed: Long = 0,
    val variable: Long = 0
) {
    val balance: Long get() = income - fixed - variable
}

private fun String.norm(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

private fun inMonth(epochDay: Long, m: YearMonth): Boolean =
    YearMonth.from(LocalDate.ofEpochDay(epochDay)) == m

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()

    val month = MutableStateFlow(YearMonth.now())
    val filters = MutableStateFlow(Filters())

    val types: StateFlow<List<ExpenseType>> = dao.observeTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthIncomes: StateFlow<List<Income>> = combine(dao.observeIncomes(), month) { list, m ->
        list.filter { inMonth(it.epochDay, m) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overview: StateFlow<Overview> = combine(
        dao.observeExpenses(),
        dao.observeIncomes(),
        dao.observeTypes(),
        month
    ) { expenses, incomes, typeList, m ->
        val fixedNames = typeList.filter { it.fixed }.map { it.name }.toSet()
        val monthExpenses = expenses.filter { inMonth(it.epochDay, m) }
        val fixed = monthExpenses.filter { it.type in fixedNames }.sumOf { it.amountCents }
        val all = monthExpenses.sumOf { it.amountCents }
        val income = incomes.filter { inMonth(it.epochDay, m) }.sumOf { it.amountCents }
        Overview(income = income, fixed = fixed, variable = all - fixed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Overview())

    val state: StateFlow<UiState> = combine(dao.observeExpenses(), month, filters) { all, m, f ->
        val q = f.query.norm().trim()
        val from = f.from
        val to = f.to
        val minC = f.minCents
        val maxC = f.maxCents
        val scope = when {
            f.dateActive -> Scope.RANGE
            q.isNotEmpty() -> Scope.SEARCH
            else -> Scope.MONTH
        }
        var list = all.filter { e ->
            val d = LocalDate.ofEpochDay(e.epochDay)
            val inScope = when (scope) {
                Scope.RANGE -> (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to))
                Scope.SEARCH -> true
                Scope.MONTH -> YearMonth.from(d) == m
            }
            inScope &&
                (q.isEmpty() || e.description.norm().contains(q) || e.type.norm().contains(q)) &&
                (f.types.isEmpty() || e.type in f.types) &&
                (minC == null || e.amountCents >= minC) &&
                (maxC == null || e.amountCents <= maxC)
        }
        if (f.sortByValue) list = list.sortedByDescending { it.amountCents }
        val byType = list.groupBy { it.type }
            .map { (name, items) -> name to items.sumOf { it.amountCents } }
            .sortedByDescending { it.second }
        UiState(list, list.sumOf { it.amountCents }, byType, scope)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setQuery(q: String) = filters.update { it.copy(query = q) }

    fun applyFilters(f: Filters) {
        filters.value = f
    }

    fun save(e: Expense) {
        viewModelScope.launch {
            if (e.id == 0L) dao.insert(e) else dao.update(e)
            if (!filters.value.dateActive) month.value = YearMonth.from(LocalDate.ofEpochDay(e.epochDay))
        }
    }

    fun delete(e: Expense) {
        viewModelScope.launch { dao.delete(e) }
    }

    fun saveIncome(i: Income) {
        viewModelScope.launch {
            if (i.id == 0L) dao.insertIncome(i) else dao.updateIncome(i)
            month.value = YearMonth.from(LocalDate.ofEpochDay(i.epochDay))
        }
    }

    fun deleteIncome(i: Income) {
        viewModelScope.launch { dao.deleteIncome(i) }
    }

    fun addType(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        viewModelScope.launch { dao.insertType(ExpenseType(n)) }
    }

    fun setTypeFixed(name: String, fixed: Boolean) {
        viewModelScope.launch { dao.setTypeFixed(name, fixed) }
    }

    fun renameType(old: String, new: String) {
        val n = new.trim()
        if (n.isEmpty()) return
        viewModelScope.launch {
            dao.renameType(old, n)
            filters.update { f -> f.copy(types = f.types.map { if (it == old) n else it }.toSet()) }
        }
    }

    fun deleteType(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (dao.countByType(name) > 0) {
                onResult(false)
            } else {
                dao.deleteType(name)
                filters.update { f -> f.copy(types = f.types - name) }
                onResult(true)
            }
        }
    }
}
