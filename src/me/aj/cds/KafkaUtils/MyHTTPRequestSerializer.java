package me.aj.cds.KafkaUtils;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.aj.cds.vo.MyHTTPServletRequest;

public class MyHTTPRequestSerializer implements org.apache.kafka.common.serialization.Serializer<MyHTTPServletRequest>{

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] serialize(String topic, MyHTTPServletRequest data) {
		ObjectMapper objMapper = new ObjectMapper();
		try {
			return objMapper.writeValueAsString(data).getBytes();
		} catch (JsonProcessingException e) {
			System.out.println("Error during serialization");
		}
		return "".getBytes();

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
