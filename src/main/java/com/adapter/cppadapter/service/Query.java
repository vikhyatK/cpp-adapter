package com.adapter.cppadapter.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.omxgroup.syssrv.AsyncRunnable;
import com.omxgroup.syssrv.Trace;
import com.omxgroup.xstream.amp.AmpQueryReqChoice;
import com.omxgroup.xstream.amp.AmpTradeRep;
import com.omxgroup.xstream.amp.AmpTradeReq;
import com.omxgroup.xstream.api.Iterator;
import com.omxgroup.xstream.api.QueryReply;
import com.omxgroup.xstream.api.QueryRequest;
import com.omxgroup.xstream.api.Session;
import com.omxgroup.xstream.api.SessionFactory;

@Service
@RefreshScope
public class Query {
	
	@Autowired
	KafkaProducer producer;
	
	@Autowired
	IntermediateService interService;
	
	private Session session = null;
	@Value(value = "${session.username}")
	private String username;
	@Value(value = "${session.password}")
	private String password;
	
	@Value(value = "${session.config}")
	private String configPath;
	
	@Value(value = "${session.cookie.file}")
	private String cookieFlatFile;
	
	private static final Logger LOG = LogManager.getLogger(Query.class);
	
	@Scheduled(cron = "${spring.query.cron.expression}")
	public void query() {
		LOG.info("Query Process started");
		/// unset set this to enable trace
		Trace.setInstance(Trace.noTraceInstance());

		try {
			session = SessionFactory.create_session(configPath);
			boolean run = true;
	        ExecutorService executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			while(run) {
				if (session.logon(username, password)) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								callQuery(session);
							} catch (Exception e) {
								e.printStackTrace();
							}						
						}
					});      
	                Thread.sleep(500);
				}
				run = !breakLoop();
			}
			session.logoff();
			LOG.info("Logged Off");
		} catch (com.omxgroup.xstream.api.XStreamException e) {
			LOG.error("API Exception: " + e);
			e.printStackTrace(System.err);
		} catch (java.lang.Exception e) {
			LOG.error("Java Exception: " + e);
			e.printStackTrace(System.err);
		} finally {
			if (session != null) {
		        /// cleanup any resources allocated by the session object
		        session.dispose(AsyncRunnable.NOOP);
		        session = null;
		      } 
			LOG.info("Task Completed");
		}
	}

	private void callQuery(Session session) throws Exception {
		final AmpQueryReqChoice req = new AmpQueryReqChoice(AmpQueryReqChoice._TRADEREQ, new AmpTradeReq());
		byte[] cookie = loadCookieFromFile();
		QueryReply[] replies = null;
		if(cookie == null) {
			final Iterator iterator = session.query(new QueryRequest(req));
			replies = iterator.next();
			writeCookieToFile(iterator.cookie());
		}
		else {
			final Iterator iteratorWithCookie = session.query(new QueryRequest(req), cookie);
			replies = iteratorWithCookie.next();
			writeCookieToFile(iteratorWithCookie.cookie());
		}
		
		/// print out the responses
		for (int i = 0; i != replies.length; ++i) {
			LOG.info("Query result:"+ replies[i]);
			AmpTradeRep tradeRep = replies[i].message().getTradeRep();
			interService.mapAndSendToKafka(tradeRep);
		}
	}

	private void writeCookieToFile(byte[] cookie) {
		Path path = Paths.get(cookieFlatFile);
		try {
			Files.write(path, cookie);
		} catch (IOException e) {
			LOG.error("Cannot write to file" +e);
			e.printStackTrace();
		}		
	}

	private byte[] loadCookieFromFile() {
		byte[] cookie = null; 	
		Path path = Paths.get(cookieFlatFile);
	    try {
	    	cookie = Files.readAllBytes(path);
		} catch (IOException e) {
			LOG.error("File not found" +e);
			e.printStackTrace();
		}
		return cookie;
	}

	private boolean breakLoop() throws ParseException {
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
		dateFormat.format(date);
		return dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("19:00"));
	}
}
