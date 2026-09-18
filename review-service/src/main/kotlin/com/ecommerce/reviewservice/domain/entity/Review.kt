package com.ecommerce.reviewservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(name = "uk_order_product_id", columnNames = ["order_id", "product_id"])]
)
class Review (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long ? = null,      // 처음에는 null이고 나중에 DB가 값을 채워주기 때문에 var로 선언

    // val은 final. 한번 할당하면 값 변경 불가
    @Column(nullable = false)
    val orderId: Long,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false)
    val memberId: Long,

    // rating, content는 수정될 수 있으니 var
    @Column(nullable = false)
    var rating: Int,

    @Column
    var content: String
) : BaseEntity()