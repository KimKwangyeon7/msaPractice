package com.msa.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		//System.out.println("TrustStore: " + System.getProperty("javax.net.ssl.trustStore"));
		//System.out.println("TrustStore Password: " + System.getProperty("javax.net.ssl.trustStorePassword"));
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
