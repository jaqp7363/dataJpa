package study.querydsl.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import study.querydsl.MongoLogger;
import study.querydsl.service.UserService;

@RestController
public class HelloController {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private UserService userService;
	
	MongoLogger mongoLogger = new MongoLogger(HelloController.class);
	
	
	@RequestMapping("/hello")
	public String hello() {
		
		mongoLogger.setMongo(mongoTemplate);
		mongoLogger.info("�׽�Ʈ �Է�");
		return "hello~~!";
	}
	
	// 회원가입
    @PostMapping("/join")
    public Long join(@RequestBody Map<String, String> user) {
        return userService.joinUser(user);
    }

    // 로그인
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) throws Exception {
        return userService.login(user);
    }
}
