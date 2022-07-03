package cn.net.insurance.compensate.server.gateway;

import com.alibaba.druid.filter.logging.LogFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {"cn.net.insurance.compensate.server.gateway", "cn.net.insurance.core", "cn.net.insurance.compensate.common"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,classes = {LogFilter.class})})
@EnableFeignClients(basePackages = {"cn.net.insurance.compensate.common"})
@EnableDiscoveryClient
public class InsuranceCompensateServerGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceCompensateServerGatewayApplication.class, args);
    }

}
