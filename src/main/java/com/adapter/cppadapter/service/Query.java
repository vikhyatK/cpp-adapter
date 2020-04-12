package com.adapter.cppadapter.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.omxgroup.syssrv.Disposer;
import com.omxgroup.syssrv.Trace;
import com.omxgroup.xstream.amp.AmpQueryReqChoice;
import com.omxgroup.xstream.amp.AmpTradeRep;
import com.omxgroup.xstream.amp.AmpTradeReq;
import com.omxgroup.xstream.api.Iterator;
import com.omxgroup.xstream.api.MarshalError;
import com.omxgroup.xstream.api.QueryReply;
import com.omxgroup.xstream.api.QueryRequest;
import com.omxgroup.xstream.api.Session;
import com.omxgroup.xstream.api.SessionFactory;
import com.omxgroup.xstream.api.XStreamException;

public class Query {
	
	@Autowired
	KafkaProducer producer;
	
	private Session session = null;
	@Value(value = "${session.username}")
	private String username;
	@Value(value = "${session.password}")
	private String password;
	
	@Scheduled(cron = "${spring.query.cron.expression}")
	public void query(String argv[]) {

		/// unset set this to enable trace
		Trace.setInstance(Trace.noTraceInstance());

		/// Create a disposer object to ensure objects are neatly cleaned up
		final Disposer disposer = new Disposer();
		try {
			session = disposer.disposes(SessionFactory.create_session("../../etc/config.ini"));
			boolean run = true;
			while(run) {
				if (session.logon(username, password)) {
					callQuery(session);
					try {
						Thread.sleep(500);
					} catch(InterruptedException ex) {
						// do nothing
					}
				}
				run = !breakLoop();
			}
			session.logoff();
		} catch (com.omxgroup.xstream.api.XStreamException e) {
			System.err.println("API Exception: " + e);
			e.printStackTrace(System.err);
		} catch (java.lang.Exception e) {
			System.err.println("Java Exception: " + e);
			e.printStackTrace(System.err);
		} finally {
			disposer.dispose();
		}
	}

	private void callQuery(Session session) throws MarshalError, XStreamException {
		final AmpQueryReqChoice req = new AmpQueryReqChoice(AmpQueryReqChoice._TRADEREQ, new AmpTradeReq());
		final Iterator iterator = session.query(new QueryRequest(req));
		QueryReply[] replies = iterator.next();
		/// print out the responses
		for (int i = 0; i != replies.length; ++i) {
			replies[i].print(System.out);
			AmpTradeRep tradeRep = replies[i].message().getTradeRep();
			doMappingAndPushToKafka(tradeRep);
		}
	}

	private void doMappingAndPushToKafka(AmpTradeRep tradeRep) {
		producer
	}

	private boolean breakLoop() throws ParseException {
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
		dateFormat.format(date);
		return dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("19:00"));
	}
}
