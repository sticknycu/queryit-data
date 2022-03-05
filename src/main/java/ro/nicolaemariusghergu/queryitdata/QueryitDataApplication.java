package ro.nicolaemariusghergu.queryitdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class QueryitDataApplication {

    public static void main(String[] args) {

        SpringApplication.run(QueryitDataApplication.class, args);
    }

}
