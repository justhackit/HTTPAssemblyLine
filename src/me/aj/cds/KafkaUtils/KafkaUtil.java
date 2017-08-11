package me.aj.cds.KafkaUtils;

import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import me.aj.cds.httputils.MyHttpClientPoolUtil;
import me.aj.cds.vo.HTTPAssemblyLineConstants;
import me.aj.cds.vo.MyHTTPServletRequest;

public class KafkaUtil {

	public static void publishHTTPRequest(MyHTTPServletRequest request) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MyHTTPRequestSerializer.class.getName());

		Producer<String, MyHTTPServletRequest> producer = new KafkaProducer<String, MyHTTPServletRequest>(props);
		TestCallback callback = new TestCallback();
		ProducerRecord<String, MyHTTPServletRequest> message = new ProducerRecord<String, MyHTTPServletRequest>(
				"mytopic3", request);
		producer.send(message, callback);
		// producer.send(message);
		producer.close();
	}

	private static class TestCallback implements Callback {
		@Override
		public void onCompletion(RecordMetadata recordMetadata, Exception e) {
			if (e != null) {
				System.out.println("Error while producing message to topic :" + recordMetadata);
				e.printStackTrace();
			} else {
				String message = String.format("sent message to topic:%s partition:%s  offset:%s",
						recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
				System.out.println(message);
			}
		}
	}

	public static void runConsumer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put("group.id", "None");
		props.put("enable.auto.commit", "false");
		props.put("auto.offset.reset", "latest");
		props.put("max.poll.records", 1);
		props.put("session.timeout.ms", HTTPAssemblyLineConstants.SESSION_TIMEOUT_MS);
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", MyHTTPRequestDeSerializer.class.getName());
		KafkaConsumer<String, MyHTTPServletRequest> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList("mytopic3"));
		boolean running = true;
		MyHttpClientPoolUtil httpClient = new MyHttpClientPoolUtil();
		try {
			while (running) {
				ConsumerRecords<String, MyHTTPServletRequest> records = consumer
						.poll(HTTPAssemblyLineConstants.POLL_WAIT_TIMEOUT);
				long lastPolledAt = System.currentTimeMillis();
				if (records.count() > 0) {
					System.out.println("No. of messages to process : " + records.count());
				} else {
					System.out.println("No message to process for current polling!");
				}
				for (ConsumerRecord<String, MyHTTPServletRequest> record : records) {
					if (record.value().getURL().endsWith("addArticleRating")) {
						HttpResponse response = httpClient.executeService(record.value());
						int respCode = response.getStatusLine().getStatusCode();
						if (Arrays.asList(HTTPAssemblyLineConstants.SERVICE_DOWN_CODES.split(","))
								.contains(String.valueOf(respCode))) {
							consumer.unsubscribe();
							consumer.close();
							handleServiceDownCase(response, record.value());
						} else {
							if (System.currentTimeMillis()
									- lastPolledAt > HTTPAssemblyLineConstants.SESSION_TIMEOUT_MS) {
								System.out.println(
										"WARN : Restarting the consumer as it took too much time to execute the previous HTTP request");
								consumer = stopAndStartConsumer(props, consumer);
							}
							consumer.commitSync();
						}
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		finally {
			consumer.unsubscribe();
			consumer.close();
		}
	}


	private static KafkaConsumer<String, MyHTTPServletRequest> stopAndStartConsumer(Properties props,
			KafkaConsumer<String, MyHTTPServletRequest> consumer) {
		consumer.unsubscribe();
		consumer.close();
		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList("mytopic3"));
		consumer.poll(HTTPAssemblyLineConstants.POLL_WAIT_TIMEOUT);
		return consumer;
	}
	

	public static void handleServiceDownCase(HttpResponse httpResponse, MyHTTPServletRequest httpRequest) {
		MyHttpClientPoolUtil httpClient = new MyHttpClientPoolUtil();
		int respCode = httpResponse.getStatusLine().getStatusCode();
		do {
			try {
				Thread.sleep(HTTPAssemblyLineConstants.HOST_DOWN_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpRequest.setMethod("HEAD");
			respCode = httpClient.executeService(httpRequest).getStatusLine().getStatusCode();
		} while (Arrays.asList(HTTPAssemblyLineConstants.SERVICE_DOWN_CODES.split(","))
				.contains(String.valueOf(respCode)));
		runConsumer();
	}

	public static void main(String[] args) {
		// new Thread(() -> runConsumer(),"KafkaConsumer").start();
		runConsumer();
	}
}
