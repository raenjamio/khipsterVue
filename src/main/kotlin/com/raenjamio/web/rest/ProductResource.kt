package com.raenjamio.web.rest

import com.raenjamio.domain.Product
import com.raenjamio.service.ProductService
import com.raenjamio.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import javax.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ENTITY_NAME = "product"
/**
 * REST controller for managing [com.raenjamio.domain.Product].
 */
@RestController
@RequestMapping("/api")
class ProductResource(
    private val productService: ProductService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /products` : Create a new product.
     *
     * @param product the product to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new product, or with status `400 (Bad Request)` if the product has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to save Product : {}", product)
        if (product.id != null) {
            throw BadRequestAlertException(
                "A new product cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = productService.save(product)
        return ResponseEntity.created(URI("/api/products/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /products` : Updates an existing product.
     *
     * @param product the product to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated product,
     * or with status `400 (Bad Request)` if the product is not valid,
     * or with status `500 (Internal Server Error)` if the product couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/products")
    fun updateProduct(@Valid @RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to update Product : {}", product)
        if (product.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = productService.save(product)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     product.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /products` : get all the products.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of products in body.
     */
    @GetMapping("/products")
    fun getAllProducts(): MutableList<Product> {
        log.debug("REST request to get all Products")
        return productService.findAll()
    }

    /**
     * `GET  /products/:id` : get the "id" product.
     *
     * @param id the id of the product to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the product, or with status `404 (Not Found)`.
     */
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<Product> {
        log.debug("REST request to get Product : {}", id)
        val product = productService.findOne(id)
        return ResponseUtil.wrapOrNotFound(product)
    }
    /**
     *  `DELETE  /products/:id` : delete the "id" product.
     *
     * @param id the id of the product to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Product : {}", id)
        productService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
