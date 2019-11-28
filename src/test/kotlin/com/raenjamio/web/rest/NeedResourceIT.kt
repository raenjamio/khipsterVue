package com.raenjamio.web.rest

import com.raenjamio.KhipstervueApp
import com.raenjamio.domain.Need
import com.raenjamio.repository.NeedRepository
import com.raenjamio.service.NeedService
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
 * Integration tests for the [NeedResource] REST controller.
 *
 * @see NeedResource
 */
@SpringBootTest(classes = [KhipstervueApp::class])
class NeedResourceIT {

    @Autowired
    private lateinit var needRepository: NeedRepository

    @Autowired
    private lateinit var needService: NeedService

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

    private lateinit var restNeedMockMvc: MockMvc

    private lateinit var need: Need

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val needResource = NeedResource(needService)
        this.restNeedMockMvc = MockMvcBuilders.standaloneSetup(needResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        need = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createNeed() {
        val databaseSizeBeforeCreate = needRepository.findAll().size

        // Create the Need
        restNeedMockMvc.perform(
            post("/api/needs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(need))
        ).andExpect(status().isCreated)

        // Validate the Need in the database
        val needList = needRepository.findAll()
        assertThat(needList).hasSize(databaseSizeBeforeCreate + 1)
        val testNeed = needList[needList.size - 1]
        assertThat(testNeed.priority).isEqualTo(DEFAULT_PRIORITY)
    }

    @Test
    @Transactional
    fun createNeedWithExistingId() {
        val databaseSizeBeforeCreate = needRepository.findAll().size

        // Create the Need with an existing ID
        need.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restNeedMockMvc.perform(
            post("/api/needs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(need))
        ).andExpect(status().isBadRequest)

        // Validate the Need in the database
        val needList = needRepository.findAll()
        assertThat(needList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllNeeds() {
        // Initialize the database
        needRepository.saveAndFlush(need)

        // Get all the needList
        restNeedMockMvc.perform(get("/api/needs?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(need.id?.toInt())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY)))
    }

    @Test
    @Transactional
    fun getNeed() {
        // Initialize the database
        needRepository.saveAndFlush(need)

        val id = need.id
        assertNotNull(id)

        // Get the need
        restNeedMockMvc.perform(get("/api/needs/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY))
    }

    @Test
    @Transactional
    fun getNonExistingNeed() {
        // Get the need
        restNeedMockMvc.perform(get("/api/needs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateNeed() {
        // Initialize the database
        needService.save(need)

        val databaseSizeBeforeUpdate = needRepository.findAll().size

        // Update the need
        val id = need.id
        assertNotNull(id)
        val updatedNeed = needRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedNeed are not directly saved in db
        em.detach(updatedNeed)
        updatedNeed.priority = UPDATED_PRIORITY

        restNeedMockMvc.perform(
            put("/api/needs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedNeed))
        ).andExpect(status().isOk)

        // Validate the Need in the database
        val needList = needRepository.findAll()
        assertThat(needList).hasSize(databaseSizeBeforeUpdate)
        val testNeed = needList[needList.size - 1]
        assertThat(testNeed.priority).isEqualTo(UPDATED_PRIORITY)
    }

    @Test
    @Transactional
    fun updateNonExistingNeed() {
        val databaseSizeBeforeUpdate = needRepository.findAll().size

        // Create the Need

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNeedMockMvc.perform(
            put("/api/needs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(need))
        ).andExpect(status().isBadRequest)

        // Validate the Need in the database
        val needList = needRepository.findAll()
        assertThat(needList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteNeed() {
        // Initialize the database
        needService.save(need)

        val databaseSizeBeforeDelete = needRepository.findAll().size

        val id = need.id
        assertNotNull(id)

        // Delete the need
        restNeedMockMvc.perform(
            delete("/api/needs/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val needList = needRepository.findAll()
        assertThat(needList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(Need::class)
        val need1 = Need()
        need1.id = 1L
        val need2 = Need()
        need2.id = need1.id
        assertThat(need1).isEqualTo(need2)
        need2.id = 2L
        assertThat(need1).isNotEqualTo(need2)
        need1.id = null
        assertThat(need1).isNotEqualTo(need2)
    }

    companion object {

        private const val DEFAULT_PRIORITY: String = "AAAAAAAAAA"
        private const val UPDATED_PRIORITY = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Need {
            val need = Need(
                priority = DEFAULT_PRIORITY
            )

            return need
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Need {
            val need = Need(
                priority = UPDATED_PRIORITY
            )

            return need
        }
    }
}
