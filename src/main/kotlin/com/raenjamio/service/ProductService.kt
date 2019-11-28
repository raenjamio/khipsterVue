package com.raenjamio.service

import com.raenjamio.domain.Product
import com.raenjamio.repository.ProductRepository
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Product].
 */
@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a product.
     *
     * @param product the entity to save.
     * @return the persisted entity.
     */
    fun save(product: Product): Product {
        log.debug("Request to save Product : {}", product)
        return productRepository.save(product)
    }

    /**
     * Get all the products.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Product> {
        log.debug("Request to get all Products")
        return productRepository.findAll()
    }

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<Product> {
        log.debug("Request to get Product : {}", id)
        return productRepository.findById(id)
    }

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete Product : {}", id)

        productRepository.deleteById(id)
    }
}
