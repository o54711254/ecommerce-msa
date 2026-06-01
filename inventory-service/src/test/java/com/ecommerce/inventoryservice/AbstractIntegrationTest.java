package com.ecommerce.inventoryservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    // @Testcontainers를 사용하면 테스트 클래스가 끝날 때 static 컨테이너도 종료됨
    // 여러 테스트 클래스가 공유할 수 있도록 static 초기화 블록으로 직접 시작
    // JVM 종료 시 Ryuk이 컨테이너를 자동으로 정리함
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0");

    static {
        MYSQL.start();
    }
}
