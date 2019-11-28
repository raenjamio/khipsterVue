package com.raenjamio.web.rest

import com.raenjamio.KhipstervueApp
import com.raenjamio.domain.Product
import com.raenjamio.repository.ProductRepository
import com.raenjamio.service.ProductService
import com.raenjamio.web.rest.errors.ExceptionTranslator
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [ProductResource] REST controller.
 *
 * @see ProductResource
 */
@SpringBootTest(classes = [KhipstervueApp::class])
class ProductResourceIT {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restProductMockMvc: MockMvc

    private lateinit var product: Product

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val productResource = ProductResource(productService)
        this.restProductMockMvc = MockMvcBuilders.standaloneSetup(productResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        product = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProduct() {
        val databaseSizeBeforeCreate = productRepository.findAll().size

        // Create the Product
        restProductMockMvc.perform(
            post("/api/products")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isCreated)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1)
        val testProduct = productList[productList.size - 1]
        assertThat(testProduct.code).isEqualTo(DEFAULT_CODE)
        assertThat(testProduct.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testProduct.priority).isEqualTo(DEFAULT_PRIORITY)
        assertThat(testProduct.colour).isEqualTo(DEFAULT_COLOUR)
    }

    @Test
    @Transactional
    fun createProductWithExistingId() {
        val databaseSizeBeforeCreate = productRepository.findAll().size

        // Create the Product with an existing ID
        product.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc.perform(
            post("/api/products")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkCodeIsRequired() {
        val databaseSizeBeforeTest = productRepository.findAll().size
        // set the field null
        product.code = null

        // Create the Product, which fails.

        restProductMockMvc.perform(
            post("/api/products")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isBadRequest)

        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun getAllProducts() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList
        restProductMockMvc.perform(get("/api/products?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.id?.toInt())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
            .andExpect(jsonPath("$.[*].colour").value(hasItem(DEFAULT_COLOUR)))
    }

    @Test
    @Transactional
    fun getProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val id = product.id
        assertNotNull(id)

        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY))
            .andExpect(jsonPath("$.colour").value(DEFAULT_COLOUR))
    }

    @Test
    @Transactional
    fun getNonExistingProduct() {
        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateProduct() {
        // Initialize the database
        productService.save(product)

        val databaseSizeBeforeUpdate = productRepository.findAll().size

        // Update the product
        val id = product.id
        assertNotNull(id)
        val updatedProduct = productRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedProduct are not directly saved in db
        em.detach(updatedProduct)
        updatedProduct.code = UPDATED_CODE
        updatedProduct.description = UPDATED_DESCRIPTION
        updatedProduct.priority = UPDATED_PRIORITY
        updatedProduct.colour = UPDATED_COLOUR

        restProductMockMvc.perform(
            put("/api/products")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedProduct))
        ).andExpect(status().isOk)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList[productList.size - 1]
        assertThat(testProduct.code).isEqualTo(UPDATED_CODE)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.priority).isEqualTo(UPDATED_PRIORITY)
        assertThat(testProduct.colour).isEqualTo(UPDATED_COLOUR)
    }

    @Test
    @Transactional
    fun updateNonExistingProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size

        // Create the Product

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put("/api/products")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteProduct() {
        // Initialize the database
        productService.save(product)

        val databaseSizeBeforeDelete = productRepository.findAll().size

        val id = product.id
        assertNotNull(id)

        // Delete the product
        restProductMockMvc.perform(
            delete("/api/products/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(Product::class)
        val product1 = Product()
        product1.id = 1L
        val product2 = Product()
        product2.id = product1.id
        assertThat(product1).isEqualTo(product2)
        product2.id = 2L
        assertThat(product1).isNotEqualTo(product2)
        product1.id = null
        assertThat(product1).isNotEqualTo(product2)
    }

    companion object {

        private const val DEFAULT_CODE: String = "AAAAAAAAAA"
        private const val UPDATED_CODE = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION: String = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_PRIORITY: Int = 1
        private const val UPDATED_PRIORITY: Int = 2

        private const val DEFAULT_COLOUR: String = "AAAAAAAAAA"
        private const val UPDATED_COLOUR = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Product {
            val product = Product(
                code = DEFAULT_CODE,
                description = DEFAULT_DESCRIPTION,
                priority = DEFAULT_PRIORITY,
                colour = DEFAULT_COLOUR
            )

            return product
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Product {
            val product = Product(
                code = UPDATED_CODE,
                description = UPDATED_DESCRIPTION,
                priority = UPDATED_PRIORITY,
                colour = UPDATED_COLOUR
            )

            return product
        }
    }
}
