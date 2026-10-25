package com.meusgastos.app

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val description: String,
    val amountCents: Long,
    val epochDay: Long
)

@Entity(tableName = "types")
data class ExpenseType(
    @PrimaryKey val name: String,
    /** true = conta como "gasto fixo" no painel do mês; false = "gasto variável". */
    @ColumnInfo(defaultValue = "0") val fixed: Boolean = false
)

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amountCents: Long,
    val epochDay: Long
)

@Dao
abstract class ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY epochDay DESC, id DESC")
    abstract fun observeExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM types ORDER BY name")
    abstract fun observeTypes(): Flow<List<ExpenseType>>

    @Query("SELECT * FROM incomes ORDER BY epochDay DESC, id DESC")
    abstract fun observeIncomes(): Flow<List<Income>>

    @Insert
    abstract suspend fun insert(expense: Expense)

    @Update
    abstract suspend fun update(expense: Expense)

    @Delete
    abstract suspend fun delete(expense: Expense)

    @Insert
    abstract suspend fun insertIncome(income: Income)

    @Update
    abstract suspend fun updateIncome(income: Income)

    @Delete
    abstract suspend fun deleteIncome(income: Income)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertType(type: ExpenseType)

    @Query("DELETE FROM types WHERE name = :name")
    abstract suspend fun deleteType(name: String)

    @Query("UPDATE types SET fixed = :fixed WHERE name = :name")
    abstract suspend fun setTypeFixed(name: String, fixed: Boolean)

    @Query("UPDATE expenses SET type = :newName WHERE type = :oldName")
    abstract suspend fun renameTypeInExpenses(oldName: String, newName: String)

    @Query("UPDATE types SET name = :newName WHERE name = :oldName")
    abstract suspend fun renameTypeRow(oldName: String, newName: String)

    @Query("SELECT COUNT(*) FROM types WHERE name = :name")
    abstract suspend fun typeExists(name: String): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE type = :name")
    abstract suspend fun countByType(name: String): Int

    @Transaction
    open suspend fun renameType(oldName: String, newName: String) {
        if (oldName == newName) return
        renameTypeInExpenses(oldName, newName)
        if (typeExists(newName) > 0) deleteType(oldName) else renameTypeRow(oldName, newName)
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE types ADD COLUMN fixed INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE types SET fixed = 1 WHERE name = 'Fixa'")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS incomes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "amountCents INTEGER NOT NULL, " +
                "epochDay INTEGER NOT NULL)"
        )
    }
}

@Database(
    entities = [Expense::class, ExpenseType::class, Income::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ExpenseDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meus_gastos.db"
                ).addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL("INSERT INTO types(name, fixed) VALUES ('Fixa', 1), ('Variável', 0)")
                        }
                    }).build().also { instance = it }
            }
    }
}
