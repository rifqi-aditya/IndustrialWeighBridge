package com.rifqi.industrialweighbridge.domain.usecase.partner

import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository
import kotlinx.coroutines.flow.Flow

class GetPartnersByTypeUseCase(private val repository: PartnerRepository) {
    operator fun invoke(type: PartnerType): Flow<List<Partner>> {
        return repository.getPartnersByType(type)
    }
}
