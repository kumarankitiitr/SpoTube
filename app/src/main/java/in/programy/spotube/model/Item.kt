package `in`.programy.spotube.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "items")
data class Item(
        @PrimaryKey(autoGenerate = false)
        val etag: String,
        val id: Id,
        val kind: String,
        val snippet: Snippet,
        var category: String = "none",
        var url: String = "none"
): Serializable