package me.aj.cds.KafkaUtils;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.aj.cds.vo.MyHTTPServletRequest;

public class MyHTTPRequestDeSerializer implements org.apache.kafka.common.serialization.Deserializer<MyHTTPServletRequest>{

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public MyHTTPServletRequest deserialize(String topic, byte[] data) {
		ObjectMapper objectMapper = new ObjectMapper();
        try {
            MyHTTPServletRequest toRet= (MyHTTPServletRequest) objectMapper.readValue(data, MyHTTPServletRequest.class);
            return toRet;
        } catch (IOException e) {
            System.out.println("Error while DeSerializing");
        }
		return null;
	}

}
