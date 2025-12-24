package com.rifqi.industrialweighbridge.domain.usecase.partner

import com.rifqi.industrialweighbridge.domain.repository.PartnerRepository

class DeletePartnerUseCase(private val repository: PartnerRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deletePartner(id)
    }
}
