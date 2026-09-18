package com.ecommerce.reviewservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
/** kotlin은 class 기본이 final -> 상속을하려면 open 혹은 abstract로 해야함
 * open class는 인스턴스화 가능(new BaseEntity), abstract는 불가능
 * 이런경우에는 BaseEntity 딴데서 만들 필요 없으니 abstract가 맞을듯
 * */
abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}