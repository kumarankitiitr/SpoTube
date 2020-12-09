package `in`.programy.spotube.room

import `in`.programy.spotube.model.Id
import `in`.programy.spotube.model.Item
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Item::class],version = 1,exportSchema = false)
@TypeConverters(Convertors::class)
abstract class ItemDatabase: RoomDatabase() {
    abstract fun getItemDao(): ItemDao

    companion object{
        @Volatile
        private var instance: ItemDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: createDatabse(context).also {
                instance = it
            }
        }

        private fun createDatabse(context: Context): ItemDatabase{
            return Room.databaseBuilder(context.applicationContext,ItemDatabase::class.java,"item_db")
                    .allowMainThreadQueries()
                    .build()
        }
    }
}