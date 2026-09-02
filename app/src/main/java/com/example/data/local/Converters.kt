package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EvidenceItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromFloatList(list: List<Float>?): String {
        if (list == null) return ""
        return list.joinToString(",")
    }

    @TypeConverter
    fun toFloatList(data: String?): List<Float> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            data.split(",").mapNotNull { it.trim().toFloatOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromEvidenceList(list: List<EvidenceItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, EvidenceItem::class.java)
        val adapter = moshi.adapter<List<EvidenceItem>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toEvidenceList(json: String?): List<EvidenceItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, EvidenceItem::class.java)
            val adapter = moshi.adapter<List<EvidenceItem>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
