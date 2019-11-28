package com.raenjamio.web.rest

import com.raenjamio.domain.Need
import com.raenjamio.service.NeedService
import com.raenjamio.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val ENTITY_NAME = "need"
/**
 * REST controller for managing [com.raenjamio.domain.Need].
 */
@RestController
@RequestMapping("/api")
class NeedResource(
    private val needService: NeedService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /needs` : Create a new need.
     *
     * @param need the need to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new need, or with status `400 (Bad Request)` if the need has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/needs")
    fun createNeed(@RequestBody need: Need): ResponseEntity<Need> {
        log.debug("REST request to save Need : {}", need)
        if (need.id != null) {
            throw BadRequestAlertException(
                "A new need cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = needService.save(need)
        return ResponseEntity.created(URI("/api/needs/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /needs` : Updates an existing need.
     *
     * @param need the need to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated need,
     * or with status `400 (Bad Request)` if the need is not valid,
     * or with status `500 (Internal Server Error)` if the need couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/needs")
    fun updateNeed(@RequestBody need: Need): ResponseEntity<Need> {
        log.debug("REST request to update Need : {}", need)
        if (need.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = needService.save(need)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     need.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /needs` : get all the needs.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of needs in body.
     */
    @GetMapping("/needs")
    fun getAllNeeds(
        pageable: Pageable
    ): ResponseEntity<MutableList<Need>> {
        log.debug("REST request to get a page of Needs")
        val page = needService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /needs/:id` : get the "id" need.
     *
     * @param id the id of the need to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the need, or with status `404 (Not Found)`.
     */
    @GetMapping("/needs/{id}")
    fun getNeed(@PathVariable id: Long): ResponseEntity<Need> {
        log.debug("REST request to get Need : {}", id)
        val need = needService.findOne(id)
        return ResponseUtil.wrapOrNotFound(need)
    }
    /**
     *  `DELETE  /needs/:id` : delete the "id" need.
     *
     * @param id the id of the need to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/needs/{id}")
    fun deleteNeed(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Need : {}", id)
        needService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
