package com.rifqi.industrialweighbridge.domain.usecase.partner

import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository

class UpdatePartnerUseCase(private val repository: PartnerRepository) {
    suspend operator fun invoke(
            id: Long,
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    ) {
        repository.updatePartner(id, name, type, address, phone, code)
    }
}
