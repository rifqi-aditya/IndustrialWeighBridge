package com.rifqi.industrialweighbridge.domain.usecase.partner

import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository

class AddPartnerUseCase(private val repository: PartnerRepository) {
    suspend operator fun invoke(
            name: String,
            type: PartnerType,
            address: String?,
            phone: String?,
            code: String?
    ) {
        repository.addPartner(name, type, address, phone, code)
    }
}
