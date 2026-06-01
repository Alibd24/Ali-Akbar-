package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // FormProduct converters
    private val productListType = Types.newParameterizedType(List::class.java, FormProduct::class.java)
    private val productListAdapter = moshi.adapter<List<FormProduct>>(productListType)

    @TypeConverter
    fun fromProductList(value: List<FormProduct>?): String {
        return productListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toProductList(value: String?): List<FormProduct> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            productListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // GroupMeta map converters
    private val groupMetaMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        GroupMeta::class.java
    )
    private val groupMetaMapAdapter = moshi.adapter<Map<String, GroupMeta>>(groupMetaMapType)

    @TypeConverter
    fun fromGroupMetaMap(value: Map<String, GroupMeta>?): String {
        return groupMetaMapAdapter.toJson(value ?: emptyMap())
    }

    @TypeConverter
    fun toGroupMetaMap(value: String?): Map<String, GroupMeta> {
        if (value.isNullOrEmpty()) return emptyMap()
        return try {
            groupMetaMapAdapter.fromJson(value) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
