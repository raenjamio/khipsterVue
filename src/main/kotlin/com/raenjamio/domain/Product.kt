package com.raenjamio.domain

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Product.
 */
@Entity
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get: NotNull
    @Column(name = "code", nullable = false, unique = true)
    var code: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "priority")
    var priority: Int? = null,

    @Column(name = "colour")
    var colour: String? = null,

    @OneToMany(mappedBy = "product")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var needs: MutableSet<Need> = mutableSetOf()

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {

    fun addNeeds(need: Need): Product {
        this.needs.add(need)
        need.product = this
        return this
    }

    fun removeNeeds(need: Need): Product {
        this.needs.remove(need)
        need.product = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Product{" +
        "id=$id" +
        ", code='$code'" +
        ", description='$description'" +
        ", priority=$priority" +
        ", colour='$colour'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
