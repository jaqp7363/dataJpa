package study.querydsl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import study.querydsl.MongoLogger;

@RestController
public class HelloController {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	MongoLogger mongoLogger = new MongoLogger(HelloController.class);
	
	
	@RequestMapping("/hello")
	public String hello() {
		
		System.out.println("1"+mongoTemplate==null);
		mongoLogger.setMongo(mongoTemplate);
		mongoLogger.info("테스트 입력");
		return "hello~~!";
	}
}
