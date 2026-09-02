package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Methodology

@Entity(tableName = "methodologies")
data class MethodologyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortDescription: String,
    val fullCriteria: String,
    val scholarlyContext: String,
    val isCustom: Boolean = false,
    val version: Int = 1
) {
    fun toDomain(): Methodology = Methodology(
        id = id,
        name = name,
        shortDescription = shortDescription,
        fullCriteria = fullCriteria,
        scholarlyContext = scholarlyContext,
        isCustom = isCustom,
        version = version
    )

    companion object {
        fun fromDomain(methodology: Methodology): MethodologyEntity = MethodologyEntity(
            id = methodology.id,
            name = methodology.name,
            shortDescription = methodology.shortDescription,
            fullCriteria = methodology.fullCriteria,
            scholarlyContext = methodology.scholarlyContext,
            isCustom = methodology.isCustom,
            version = methodology.version
        )
    }
}
