package org.gridchem.service.test.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.gridchem.service.SessionService;
import org.gridchem.service.dao.SessionDao;
import org.gridchem.service.exceptions.LoginException;
import org.gridchem.service.exceptions.ParameterException;
import org.gridchem.service.exceptions.PermissionException;
import org.gridchem.service.exceptions.ProjectException;
import org.gridchem.service.exceptions.ProviderException;
import org.gridchem.service.exceptions.SessionException;
import org.gridchem.service.impl.SessionServiceImpl;
import org.gridchem.service.model.GMSSession;
import org.gridchem.service.model.enumeration.AccessType;
import org.gridchem.service.test.GMSTestCase;
import org.gridchem.service.util.ServiceUtil;
import org.gridchem.service.util.crypt.SHA1;

public class TeraGridSessionServiceTest extends GMSTestCase {
	private SessionService service;
	private String sMap;
	
	public TeraGridSessionServiceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		service = new SessionServiceImpl();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("dummy.key", "dummy.value");
		sMap = ServiceUtil.xstream.toXML(map);
		
		try {
			FileInputStream in = new FileInputStream("tg-tests");
		
			Properties props = new Properties();
			props.loadFromXML(in);
			in.close();
			TEST_SESSIONTOKEN = props.getProperty("session.key");
		} catch (Exception e) {}
		
	}

	public void testGetAuthenticationTypes() {
		assertNotNull(service.getAuthenticationTypes());
	}

	public void testCreateSessionTeraGridNullUsername() {
		try {
			assertNotNull(service.createSession(null, TEST_PASSWORD, sMap, AccessType.TERAGRID.name()));
			fail("Null username should throw a permission exception");
		} catch (PermissionException e) {
		} catch (Exception e) {
			fail("Null username should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridEmptyUsername() {
		try {
			assertNotNull(service.createSession("", TEST_PASSWORD, sMap, AccessType.TERAGRID.name()));
			fail("Empty username should throw a permission exception");
		} catch (PermissionException e) {
		} catch (Exception e) {
			fail("Empty username should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridInvalidUsername() {
		try {
			assertNotNull(service.createSession("-1", TEST_PASSWORD, sMap, AccessType.TERAGRID.name()));
			fail("Invalid username should throw a permission exception");
		} catch (LoginException e) {
		} catch (Exception e) {
			fail("Invalid username should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridNullPassword() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, null, sMap, AccessType.TERAGRID.name()));
			fail("Null password should throw a permission exception");
		} catch (PermissionException e) {
		} catch (Exception e) {
			fail("Null password should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridEmptyPassword() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, "", sMap, AccessType.TERAGRID.name()));
			fail("Empty password should throw a permission exception");
		} catch (PermissionException e) {
		} catch (Exception e) {
			fail("Empty password should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridInvalidPassword() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, "-1", sMap, AccessType.TERAGRID.name()));
			fail("Invalid password should throw a permission exception");
		} catch (LoginException e) {
		} catch (Exception e) {
			fail("Invalid password should throw a permission exception");
		}
	}

	public void testCreateSessionTeraGridNullAccessType() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, TEST_PASSWORD, sMap, null));
			fail("Null access type should throw a permission exception");
		} catch (ProviderException e) {
		} catch (Exception e) {
			fail("Null access type should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridEmptyAccessType() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, TEST_PASSWORD, sMap, ""));
			fail("Empty access type should throw a permission exception");
		} catch (ProviderException e) {
		} catch (Exception e) {
			fail("Empty access type should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGridInvalidAccessType() {
		try {
			assertNotNull(service.createSession(TEST_USERNAME, TEST_PASSWORD, sMap, "-1"));
			fail("Invalid access type should throw a permission exception");
		} catch (ProviderException e) {
		} catch (Exception e) {
			fail("Invalid access type should throw a permission exception");
		}
	}
	
	public void testCreateSessionTeraGrid() throws PermissionException, SessionException, ProviderException, ParameterException {
		TEST_SESSIONTOKEN = service.createSession(TEST_MYPROXY_USERNAME, TEST_MYPROXY_PASSWORD, sMap, AccessType.TERAGRID.name());
		
		assertTrue(ServiceUtil.isValid(TEST_SESSIONTOKEN));
		
		// make sure it was created, then update the token so we can pull it for the rest of the tests.
		GMSSession session = SessionDao.getByToken(TEST_SESSIONTOKEN);
		assertNotNull(session);
		
	}
	
	public void testRenewSessionNullSessionToken() {
		try {
			service.renewSession(null);
			fail("Null session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Null session id should throw a session exception");
		}
	}
	
	public void testRenewSessionEmptySessionToken() {
		try {
			service.renewSession("");
			fail("Empty session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Empty session id should throw a session exception");
		}
	}
	
	public void testRenewSessionInvalidSessionToken() {
		try {
			service.renewSession("-1");
			fail("Invalid session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Invalid session id should throw a session exception");
		}
	}
	
	public void testRenewSession() {
		
		Calendar cal = SessionDao.getByToken(TEST_SESSIONTOKEN).getExpires();
		
		service.renewSession(TEST_SESSIONTOKEN);
		
		assertTrue(SessionDao.getByToken(TEST_SESSIONTOKEN).getExpires().after(cal));
		
	}

	public void testGetRemainingTimeNullSessionToken() {
		try {
			service.getRemainingTime(null);
			fail("Null session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Null session id should throw a session exception");
		}
	}
	
	public void testGetRemainingTimeEmptySessionToken() {
		try {
			service.getRemainingTime("");
			fail("Empty session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Empty session id should throw a session exception");
		}
	}
	
	public void testGetRemainingTimeInvalidSessionToken() {
		try {
			service.getRemainingTime("-1");
			fail("Invalid session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Invalid session id should throw a session exception");
		}
	}
	
	
	public void testGetRemainingTime() {
		Calendar daoCal = SessionDao.getByToken(TEST_SESSIONTOKEN).getExpires();
		
		long daoRemaining = daoCal.getTimeInMillis() - System.currentTimeMillis();
		long msRemaining = Long.parseLong(service.getRemainingTime(TEST_SESSIONTOKEN));
		
		assertTrue((daoRemaining - msRemaining) < 10000);
	}

	public void testSetSessionProjectNullSessionToken() {
		try {
			service.setSessionProject(null,project.getId().toString());
			fail("Null session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Null session id should throw a session exception");
		}
	}
	
	public void testSetSessionProjectEmptySessionToken() {
		try {
			service.setSessionProject("",project.getId().toString());
			fail("Empty session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Empty session id should throw a session exception");
		}
	}
	
	public void testSetSessionProjectInvalidSessionToken() {
		try {
			service.setSessionProject("-1",project.getId().toString());
			fail("Invalid session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Invalid session id should throw a session exception");
		}
	} 
	
	public void testSetSessionProjectNullProjectId() {
		try {
			service.setSessionProject(TEST_SESSIONTOKEN,null);
			fail("Null project id should throw a session exception");
		} catch (ProjectException e) {
		} catch (Exception e) {
			fail("Null project id should throw a session exception");
		}
	}
	
	public void testSetSessionProjectEmptyProjectId() {
		try {
			service.setSessionProject(TEST_SESSIONTOKEN,"");
			fail("Empty project id should throw a session exception");
		} catch (ProjectException e) {
		} catch (Exception e) {
			fail("Empty project id should throw a session exception");
		}
	}
	
	public void testSetSessionProjectInvalidProjectId() {
		try {
			service.setSessionProject(TEST_SESSIONTOKEN, "-1");
			fail("Invalid project id should throw a session exception");
		} catch (PermissionException e) {
		} catch (Exception e) {
			fail("Invalid project id should throw a session exception");
		}
	} 
	
	public void testSetSessionProject() throws PermissionException, SessionException, ProviderException {
		
		service.setSessionProject(TEST_SESSIONTOKEN, project.getId().toString());
		
		assertTrue(SessionDao.getByToken(TEST_SESSIONTOKEN).getProjectId().equals(project.getId()));
	}

	public void testDestroySessionNullSessionToken() {
		try {
			service.destroySession(null);
			fail("Null session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Null session id should throw a session exception");
		}
	}
	
	public void testDestroySessionEmptySessionToken() {
		try {
			service.destroySession("");
			fail("Empty session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Empty session id should throw a session exception");
		}
	}
	
	public void testDestroySessionInvalidSessionToken() {
		try {
			service.destroySession("-1");
			fail("Invalid session id should throw a session exception");
		} catch (SessionException e) {
		} catch (Exception e) {
			fail("Invalid session id should throw a session exception");
		}
	}
	
	public void testDestroySession() {
		service.destroySession(TEST_SESSIONTOKEN);
		
		assertNotNull(SessionDao.getByToken(TEST_SESSIONTOKEN).getDestroyed());
	}
	
	public void testTearDown() throws Exception {
		// no dummy data to tear down here
	}
	
	public void tearDown() throws Exception {
		if (TEST_SESSIONTOKEN != null) {
			File file = new File("tg-tests");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
		
			Properties props = new Properties();
			props.setProperty("session.key", TEST_SESSIONTOKEN);
			props.storeToXML(out, "Output for teragrid service tests");
			out.close();
		}
		
	}
}
