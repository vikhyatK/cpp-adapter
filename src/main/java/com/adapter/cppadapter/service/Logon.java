package com.adapter.cppadapter.service;

import com.omxgroup.xstream.api.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import com.omxgroup.syssrv.*;

public class Logon {
	
	private Session session = null;
	
//	@Value(value = "${session.username}")
//	private String username;
//	@Value(value = "${session.password}")
//	private String password;
	
//	@Scheduled(cron = "${spring.logon.cron.time}")
	public void logon() {
		/// unset set this to enable trace
		Trace.setInstance(Trace.noTraceInstance());

		try {
			/// create a session object using the connection details in the config file
			session = SessionFactory.create_session("../../etc/config.ini");
			/// connection and logon using the username and password from command line
			if (session.logon(username, password)) {
				/// print out session id just to show we are connected
				System.out.println("Session Id: " + session.session_id());
				/// example complete, lets just logoff
				session.logoff();
			}
		} catch (final com.omxgroup.xstream.api.XStreamException e) {
			System.err.println("API Exception: " + e);
			e.printStackTrace(System.err);
		} catch (final java.lang.Exception e) {
			System.err.println("Java Exception: " + e);
			e.printStackTrace(System.err);
		} finally {
		}

		if (session != null) {
			/// cleanup any resources allocated by the session object
			session.dispose(AsyncRunnable.NOOP);
			session = null;
		}
	}
	
	@Scheduled(cron = "${spring.logoff.cron.time}")
	public void logoff() throws XStreamException {
		session.logoff();
	}
}
