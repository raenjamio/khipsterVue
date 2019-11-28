package com.raenjamio.repository
import com.raenjamio.domain.Need
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Need] entity.
 */
@Suppress("unused")
@Repository
interface NeedRepository : JpaRepository<Need, Long>
