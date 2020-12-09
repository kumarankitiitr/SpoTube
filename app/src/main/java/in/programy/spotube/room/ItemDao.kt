package `in`.programy.spotube.room

import `in`.programy.spotube.model.Item
import `in`.programy.spotube.util.Constants.CATEGORY_DOWNLOAD
import `in`.programy.spotube.util.Constants.CATEGORY_FAVOURITE
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Item)
//
//    WHERE category=$CATEGORY_FAVOURITE
    @Query("SELECT * FROM ITEMS ")
    fun readFavourites(): LiveData<List<Item>>

//    WHERE category=$CATEGORY_DOWNLOAD
    @Query("SELECT * FROM ITEMS")
    fun readDownloads(): LiveData<List<Item>>

    @Query("SELECT * FROM ITEMS")
    fun getDownloadsList(): List<Item>

    @Delete
    suspend fun delete(item: Item)
}