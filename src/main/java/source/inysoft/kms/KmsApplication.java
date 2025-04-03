package source.inysoft.kms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;


@SpringBootApplication
public class KmsApplication {


    public static void main(String[] args) {
        SpringApplication.run(KmsApplication.class, args);
    }

    @RequestMapping("/")
    public String home() {
        return "index.html";
    }

}
