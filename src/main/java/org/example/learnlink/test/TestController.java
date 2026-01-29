package org.example.learnlink.test;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping
    public ResponseEntity<String> sayHello(){
        return  ResponseEntity.ok("hello world§");
    }
}
