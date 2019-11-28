package com.raenjamio.service

import com.raenjamio.domain.Need
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service Interface for managing [Need].
 */
interface NeedService {

    /**
     * Save a need.
     *
     * @param need the entity to save.
     * @return the persisted entity.
     */
    fun save(need: Need): Need

    /**
     * Get all the needs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Need>

    /**
     * Get the "id" need.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Need>

    /**
     * Delete the "id" need.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
