package com.adapter.cppadapter.service;

import org.springframework.stereotype.Service;

import com.adapter.cppadapter.model.DataObject;
import com.adapter.cppadapter.model.Payload;
import com.omxgroup.xstream.amp.AmpTradeRep;

@Service
public class DataTransformer {

	public Payload createPayloadFromResponse(AmpTradeRep data) {
		Payload payload = new Payload();
		payload.setTradeDate(data.getTradeId().getTradeDate().toString());
		data.getStatics().getTime().getDate();
		return payload;
	}
}
