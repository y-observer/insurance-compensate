package cn.net.insurance.compensate.outside.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"cn.net.insurance.compensate"})
public class InsuranceCompensateOutsideConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceCompensateOutsideConnectorApplication.class, args);
    }

}
