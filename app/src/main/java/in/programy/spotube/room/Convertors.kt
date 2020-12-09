package `in`.programy.spotube.room

import `in`.programy.spotube.model.Id
import `in`.programy.spotube.model.Snippet
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Convertors {
    @TypeConverter
    fun fromId(id: Id): String{
        return id.videoId
    }

    @TypeConverter
    fun toId(id: String): Id{
        return Id(id,id)
    }

    @TypeConverter
    fun fromSnippet(snippet: Snippet): String{
        val gson = Gson()
        return gson.toJson(snippet)
    }

    @TypeConverter
    fun fromJson(json: String): Snippet{
        val listType: Type = object : TypeToken<Snippet>() {}.type
        return Gson().fromJson(json, listType)
    }
}