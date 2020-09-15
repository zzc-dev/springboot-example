package com.zzc.springboot_jdbc;

import com.zzc.demo_starter.service.DemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class SpringbootJdbcApplicationTests {

    @Autowired
    private DataSource source;

    @Test
    void contextLoads() throws SQLException {
        // com.zaxxer.hikari.HikariDataSource
        System.out.println(source.getClass());
        Connection connection = source.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Autowired
    private DemoService service;

    @Test
    void testDemo(){
        service.test("1");
    }

}
