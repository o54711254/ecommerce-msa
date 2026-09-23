package com.ecommerce.reviewservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.mysql.MySQLContainer

@SpringBootTest
abstract class AbstractIntegrationTest {

    companion object {
        // @Testcontainers 대신 static 초기화로 JVM 전체에서 컨테이너 공유
        // Ryuk이 JVM 종료 시 자동 정리
        // @JvmField로 실제 static 필드가 되어야 @ServiceConnection이 적용됨
        @ServiceConnection
        @JvmField
        val MYSQL: MySQLContainer = MySQLContainer("mysql:8.0")

        init {
            MYSQL.start()
        }
    }
}
