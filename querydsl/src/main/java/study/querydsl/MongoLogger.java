package study.querydsl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class MongoLogger {

	private MongoTemplate mongoTemplate;
	
	private Logger logger;
	
	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	
	public MongoLogger() {
		
	}
	
	public void setMongo(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public MongoLogger(Class<?> className) {
		logger = LoggerFactory.getLogger(className);
	}
	
	public void trace(String msg) { 
        logger.trace(msg);
        writeMongo(msg, "trace");
    }

    

    public void debug(String msg) { 
        logger.debug(msg);
        writeMongo(msg, "debug");
    }

    

    public void info(String msg) { 
        logger.info(msg);
        writeMongo(msg, "info");
    }

    

    public void warn(String msg) {
        logger.warn(msg);
        writeMongo(msg, "warn");
    }

    

    public void error(String msg) { 
        logger.error(msg);
        writeMongo(msg, "error");

    }
    
    private void writeMongo(String msg, String level) {
    	Map<String, String> log = new HashMap<String, String>();
        log.put("LEVEL", level);
        log.put("DATE_TIME", dateFormat.format(new Date()).toString());
        log.put("REQUESTER", logger.getName());
        log.put("MESSAGE", msg);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder .getRequestAttributes()).getRequest();
        log.put("USER_ID", "유저아이디");
        Object obj = request.getAttribute("params");
        log.put("PARAMS", obj==null?null:obj.toString());
        System.out.println(mongoTemplate == null);

        mongoTemplate.insert(log, "log");
        mongoTemplate.insert(log, "log2");
    }
}
