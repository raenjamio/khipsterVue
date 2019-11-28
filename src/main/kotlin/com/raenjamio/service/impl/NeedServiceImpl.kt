package com.raenjamio.service.impl

import com.raenjamio.domain.Need
import com.raenjamio.repository.NeedRepository
import com.raenjamio.service.NeedService
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Need].
 */
@Service
@Transactional
class NeedServiceImpl(
    private val needRepository: NeedRepository
) : NeedService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a need.
     *
     * @param need the entity to save.
     * @return the persisted entity.
     */
    override fun save(need: Need): Need {
        log.debug("Request to save Need : {}", need)
        return needRepository.save(need)
    }

    /**
     * Get all the needs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<Need> {
        log.debug("Request to get all Needs")
        return needRepository.findAll(pageable)
    }

    /**
     * Get one need by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Need> {
        log.debug("Request to get Need : {}", id)
        return needRepository.findById(id)
    }

    /**
     * Delete the need by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Need : {}", id)

        needRepository.deleteById(id)
    }
}
