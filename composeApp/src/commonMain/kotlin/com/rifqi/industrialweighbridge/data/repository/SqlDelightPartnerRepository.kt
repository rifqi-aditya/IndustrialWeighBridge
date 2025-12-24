package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightPartnerRepository(db: WeighbridgeDatabase) : PartnerRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllPartners(): Flow<List<Partner>> {
        return queries.selectAllPartners().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getPartnersByType(type: PartnerType): Flow<List<Partner>> {
        return queries.selectPartnersByType(type).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addPartner(
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    ) {
        queries.insertPartner(name, type, address, phone, code)
    }

    override suspend fun updatePartner(
            id: Long,
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    ) {
        queries.updatePartner(name, type, address, phone, code, id)
    }

    override suspend fun deletePartner(id: Long) {
        queries.softDeletePartner(id)
    }
}
