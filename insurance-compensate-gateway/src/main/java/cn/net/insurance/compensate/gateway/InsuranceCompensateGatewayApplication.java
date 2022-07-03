package cn.net.insurance.compensate.gateway;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(value = {"cn.net.insurance.compensate"})
@EnableFeignClients(value = {"cn.net.insurance.compensate"})
@EnableDiscoveryClient
@EnableAsync
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
public class InsuranceCompensateGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(InsuranceCompensateGatewayApplication.class, args);
    }

}
