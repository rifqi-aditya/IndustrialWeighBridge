package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.PartnerType
import kotlinx.coroutines.flow.Flow

interface PartnerRepository {
    fun getAllPartners(): Flow<List<Partner>>
    fun getPartnersByType(type: PartnerType): Flow<List<Partner>>
    suspend fun addPartner(
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    )
    suspend fun updatePartner(
            id: Long,
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    )
    suspend fun deletePartner(id: Long)
}
