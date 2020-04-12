package com.adapter.cppadapter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.adapter.cppadapter.model.Payload;
import com.omxgroup.xstream.amp.AmpTradeRep;

@Service
@RefreshScope
public class IntermediateService {

	@Autowired
	private DataTransformer dataTransformer;
	@Autowired
	private KafkaProducer producer;
	
	public void mapAndSendToKafka(AmpTradeRep data) throws Exception {
		Payload payload = dataTransformer.createPayloadFromResponse(data);
		producer.produceJsonData(payload);
	}

}
