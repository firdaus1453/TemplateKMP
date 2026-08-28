---
name: kmp-database
description: >
  Room Multiplatform guidelines for SQLite databases across Android, iOS, and Desktop using KSP. Trigger when: declaring Room Entities, DAOs, RoomDatabase definitions, RoomDatabaseConstructor, TypeConverters, or configuring BundledSQLiteDriver.
---

# Room Multiplatform Database Guidelines

This document outlines conventions for defining and using Room Multiplatform 2.7+ across Android, iOS, and Desktop.

---

## 🗄️ Database Setup (`@Database` & `RoomDatabaseConstructor`)

In Room Multiplatform, every database requires a `RoomDatabaseConstructor` expect/actual declaration to allow KSP code generation:

```kotlin
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

@Database(
    entities = [
        ProductEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val productDao: ProductDao

    companion object {
        const val DB_NAME = "app_template.db"
    }
}

// Suppress expect/actual warning for Room constructor
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

---

## 🏷️ Entity & DAO Standards

### 1. Entity Definition
```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val thumbnail: String,
    val rating: Double,
    val cachedAt: Long = 0L,
)
```

### 2. DAO Interface
```kotlin
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Upsert
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearProducts()
}
```

---

## 🔌 Database Builder & Bundled SQLite Driver

Initialize Room with `BundledSQLiteDriver` to ensure uniform SQLite behavior across Android, iOS, and Desktop:

```kotlin
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getDatabaseBuilder(path: String): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = path,
        factory = { AppDatabaseConstructor.initialize() }
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}
```
