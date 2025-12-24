package com.rifqi.industrialweighbridge.domain.usecase.partner

import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository
import kotlinx.coroutines.flow.Flow

class GetAllPartnersUseCase(private val repository: PartnerRepository) {
    operator fun invoke(): Flow<List<Partner>> {
        return repository.getAllPartners()
    }
}
