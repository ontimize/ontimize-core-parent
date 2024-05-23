package com.ontimize.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.login.AliasCertPair;
import com.ontimize.jee.common.util.Base64Utils;
import com.ontimize.security.provider.SecurityProvider;

public final class CertificateUtils {

	private static final Logger logger = LoggerFactory.getLogger(CertificateUtils.class);

	public static final String PROVIDERS = "com/ontimize/security/provider/providers.properties";

	public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

	public static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";

	public static final String USER_CERTIFICATE = "-----USER_CERTIFICATE-----";

	public static final String SIGNED_TOKEN = "-----SIGNED_TOKEN-----";

	public static final String TOKEN = "-----TOKEN-----";

	public static final String TOKEN_ERROR = "TOKEN_ERROR";

	public static final String SIGNED_ERROR = "SIGNED_ERROR";

	public static final String TRUST_CA_ERROR = "TRUST_CA_ERROR";

	public static final String TRUST_CRL_ERROR = "TRUST_CRL_ERROR";

	public static final String VALIDITY_ERROR = "VALIDITY_ERROR";

	public static final String DNIe = "DNIe";

	public static final String DNIe_CN = "CN";

	public static final String DNIe_G = "G";

	public static final String DNIe_SN = "SN";

	public static final String DNIe_SERIALNUMBER = "SERIALNUMBER";

	public static final String DNIe_C = "C";

	protected static int OFFSET = 3;

	protected static String OFFSET_STRING = "e1f6468168wefwec5we1654wefwef2165467664654efwedf4efd64ef6";

	public static String providers = CertificateUtils.PROVIDERS;

	public static String beginCertificateToken = CertificateUtils.USER_CERTIFICATE;

	private static KeyStore smcks = null;

	// Installed providers
	private static Map<Object, Object>	providerInstalled		= new HashMap<>();

	public static void installCertProviders() {

		InputStream is = CertificateUtils.class.getClassLoader().getResourceAsStream(CertificateUtils.providers);
		// check whether file
		if (is == null) {
			try {
				is = new FileInputStream(new File(CertificateUtils.providers));
			} catch (FileNotFoundException exc) {
				CertificateUtils.logger.error(null, exc);
			}
		}
		if (is != null) {
			Properties p = new Properties();
			try {
				p.load(is);
			} catch (Exception ex) {
				if (ApplicationManager.DEBUG) {
					CertificateUtils.logger.error(null, ex);
				} else {
					CertificateUtils.logger.trace(null, ex);
				}
				p = null;
			}
			if (p != null) {
				Enumeration<?> en = p.keys();
				while (en.hasMoreElements()) {
					Object key = en.nextElement();
					Object value = p.get(key);
					try {
						Class<?> c = Class.forName(value.toString());
						Object obj = c.newInstance();
						if (obj instanceof SecurityProvider) {
							Provider prov = ((SecurityProvider) obj).getProvider();
							CertificateUtils.installProvider(key.toString(), prov);
						}
					} catch (Exception ex) {
						if (ApplicationManager.DEBUG) {
							CertificateUtils.logger.error(null, ex);
						} else {
							CertificateUtils.logger.trace(null, ex);
						}
					}
				}
			}
		}
	}

	private CertificateUtils() {
	}

	public static void installProvider(String key, Provider provider) {
		if (provider == null) {
			return;
		}
		Security.addProvider(provider);
		CertificateUtils.providerInstalled.put(key.toString(), provider);
	}

	public static Enumeration getProvidersInstalled() {
		return Collections.enumeration(CertificateUtils.providerInstalled.keySet());
	}

	public static Provider getProviderInstalled(String key) {
		return (Provider) CertificateUtils.providerInstalled.get(key);
	}

	public static boolean isProviderInstalled(String key) {
		return CertificateUtils.providerInstalled.containsKey(key);
	}

	public static String encrypt(String password) {
		try {
			return CertificateUtils.encrypt(password, CertificateUtils.OFFSET_STRING, CertificateUtils.OFFSET);
		} catch (Exception ex) {
			CertificateUtils.logger.error(null, ex);
			return null;
		}
	}


	public static byte[] encrypt(byte[] bytes, String key)
			throws IllegalArgumentException, UnsupportedEncodingException {
		if ((bytes == null) || (bytes.length == 0)) {
			throw new IllegalArgumentException(
					"Error: invalid string. If can not be null and the lenght must be greater than 0");
		}
		byte[] res = new byte[bytes.length];
		byte[] llave = key.getBytes("ISO-8859-1");
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			byte bRes = b;
			for (byte element : llave) {
				bRes = (byte) (bRes ^ element);
			}
			res[i] = bRes;
		}
		return res;
	}

	public static String encrypt(String password, String key, int number)
			throws IllegalArgumentException, UnsupportedEncodingException {
		if ((password == null) || (password.length() == 0)) {
			throw new IllegalArgumentException(
					"Error: invalid string. If can not be null and the lenght must be greater than 0");
		}
		byte[] bytes = password.getBytes();
		byte[] res = new byte[bytes.length];
		byte[] llave = key.getBytes("ISO-8859-1");
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			byte bRes = b;
			for (int j = 0; j < llave.length; j++) {
				bRes = (byte) (bRes ^ llave[(j + number) % llave.length]);
			}
			res[i] = bRes;
		}
		return new String(res);
	}

	/*
	 * public static KeyStore installPKCS11(Provider pkcs11Provider) throws Exception {
	 * Security.addProvider(pkcs11Provider); return KeyStore.getInstance("PKCS11"); }
	 */

	public static String encodeCertificate(Certificate cert) {
		try {
			// Get the encoded form which is suitable for exporting
			byte[] buf = cert.getEncoded();
			StringBuilder os = new StringBuilder();
			// Write in text form
			os.append(CertificateUtils.BEGIN_CERTIFICATE);
			os.append(Base64Utils.getBase64JV().getEncoder().encodeByteArrayToString(buf));
			os.append(CertificateUtils.END_CERTIFICATE);
			return os.toString();
		} catch (Exception exc) {
			CertificateUtils.logger.trace(null, exc);
			return null;
		}
	}

	public static Certificate decodeCertificate(String certificate) throws Exception {
		ByteArrayInputStream bIn = new ByteArrayInputStream(certificate.getBytes());
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return cf.generateCertificate(bIn);
	}

	public static String createTokenToSend(Certificate cert, String token, String signedToken) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(CertificateUtils.USER_CERTIFICATE);
			sb.append(CertificateUtils.encodeCertificate(cert));
			sb.append(CertificateUtils.TOKEN);
			sb.append(token);
			sb.append(CertificateUtils.SIGNED_TOKEN);
			sb.append(signedToken);
		} catch (Exception ex) {
			CertificateUtils.logger.error(null, ex);
			return null;
		}
		return sb.toString();
	}

	public static Map<Object, Object> parseTokenReceived(String token) throws Exception {
		Map<Object, Object> h = new HashMap<>();
		if (!token.startsWith(CertificateUtils.USER_CERTIFICATE)) {
			return null;
		}
		String tk = token.substring(CertificateUtils.USER_CERTIFICATE.length());
		int i = tk.indexOf(CertificateUtils.TOKEN);
		if (i == -1) {
			return null;
		}
		String stringCert = tk.substring(0, i);
		h.put(CertificateUtils.USER_CERTIFICATE, CertificateUtils.decodeCertificate(stringCert));
		tk = tk.substring(i + CertificateUtils.TOKEN.length());
		i = tk.indexOf(CertificateUtils.SIGNED_TOKEN);
		if (i == -1) {
			return null;
		}
		h.put(CertificateUtils.TOKEN, tk.substring(0, i));
		h.put(CertificateUtils.SIGNED_TOKEN, tk.substring(i + CertificateUtils.SIGNED_TOKEN.length()));
		return h;
	}

	public static String getSignedToken(String token, Certificate cert, PrivateKey sk) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(sk);
			sig.update(token.getBytes());
			byte[] signatureBytes = sig.sign();
			return Base64Utils.getBase64JV().getEncoder().encodeByteArrayToString(signatureBytes);
		} catch (Exception ex) {
			CertificateUtils.logger.error(null, ex);
		}
		return null;
	}

	public static Certificate[] loadCerfificatesFromURL(String url) throws Exception {
		InputStream is = CertificateUtils.class.getClassLoader().getResourceAsStream(url);
		return CertificateUtils.loadCerfificatesFromStream(is);
	}

	public static Certificate[] loadCerfificatesFromStream(InputStream is) throws Exception {
		Properties prop = new Properties();
		prop.load(is);
		return CertificateUtils.loadCerfificatesFromProperties(prop);
	}

	public static Certificate[] loadCerfificatesFromProperties(Properties properties) throws Exception {
		return (java.security.cert.Certificate[]) CertificateUtils.loadCertificatesFromProperies(properties, false);
	}

	public static Object loadCerfificatesFromDir(List<Object> dirlist, boolean crl) throws Exception {
		if (dirlist == null) {
			if (crl) {
				return new CRL[0];
			} else {
				return new Certificate[0];
			}
		}
		List<Object> certList = new ArrayList<>();
		for (Object element : dirlist) {
			String currentDir = element.toString();
			File dir = new File(currentDir);
			if (dir.isFile()) {
				Object certificate = CertificateUtils.loadCertificateFromFile(currentDir, crl);
				if (certificate != null) {
					certList.add(certificate);
				}
			}
			if (dir.isDirectory()) {
				File[] fileList = dir.listFiles();
				for (File element2 : fileList) {
					Object certificate = CertificateUtils.loadCertificateFromFile(currentDir, crl);
					if (certificate != null) {
						certList.add(certificate);
					}
				}
			}
		}
		if (crl) {
			return certList.toArray(new java.security.cert.CRL[certList.size()]);
		} else {
			return certList.toArray(new java.security.cert.Certificate[certList.size()]);
		}
	}

	public static Object loadCertificateFromFile(String filePath, boolean crl) throws Exception {
		File file = new File(filePath);
		return CertificateUtils.loadCertificateFromFile(file, crl);
	}

	public static Object loadCertificateFromFile(File file, boolean crl) throws Exception {
		if (file.exists()) {
			FileInputStream fisCert = null;
			try {
				fisCert = new FileInputStream(file);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				try {
					if (crl) {
						return cf.generateCRL(fisCert);
					} else {
						return cf.generateCertificate(fisCert);
					}
				} catch (CertificateException ex) {
					CertificateUtils.logger.error(
							CertificateUtils.class.getName() + ": Error loading certificate: " + file.getAbsolutePath(),
							ex);
					CertificateUtils.logger.error(
							"NOTE: .crt file must start with text: '-----BEGIN CERTIFICATE-----'. You must remove previous initial human-readable info added automatically to be valid.");
				}
			} catch (Exception e) {
				CertificateUtils.logger.error(null, e);
			} finally {
				try {
					if (fisCert != null) {
						fisCert.close();
					}
				} catch (Exception e3) {
					CertificateUtils.logger.error(null, e3);
				}
			}
		}
		return null;
	}

	private static Object loadCertificatesFromProperies(Properties properties, boolean crl) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		if (cf == null) {
			CertificateUtils.logger.debug("X.509 CertificateFactory is null");
			return null;
		}
		List<Object> l = new ArrayList<>();
		Enumeration<?> props = properties.keys();
		while (props.hasMoreElements()) {
			String prop = (String) props.nextElement();
			String cert = properties.getProperty(prop);
			URL urlCert = CertificateUtils.class.getClassLoader().getResource(cert);
			if (urlCert == null) {
				CertificateUtils.logger.debug("Certificate not found in: " + cert);
				continue;
			}
			Certificate certificate = null;
			CRL crlcert = null;
			try {
				if (crl) {
					crlcert = cf.generateCRL(urlCert.openStream());
				} else {
					certificate = cf.generateCertificate(urlCert.openStream());
				}
			} catch (Exception ex) {
				CertificateUtils.logger.debug("Reading certificate problems " + cert);
				CertificateUtils.logger.error(null, ex);
				certificate = null;
				crlcert = null;
			}
			if (crl && (crlcert != null)) {
				l.add(crlcert);
			}
			if (!crl && (certificate != null)) {
				l.add(certificate);
			}
		}
		if (crl) {
			return l.toArray(new java.security.cert.CRL[l.size()]);
		} else {
			return l.toArray(new java.security.cert.Certificate[l.size()]);
		}
	}

	public static CRL[] loadCRLFromURL(String url) throws Exception {
		InputStream is = CertificateUtils.class.getClassLoader().getResourceAsStream(url);
		return CertificateUtils.loadCRLFromStream(is);
	}

	public static CRL[] loadCRLFromStream(InputStream is) throws Exception {
		Properties prop = new Properties();
		prop.load(is);
		return CertificateUtils.loadCRLFromProperties(prop);
	}

	public static CRL[] loadCRLFromProperties(Properties properties) throws Exception {
		return (java.security.cert.CRL[]) CertificateUtils.loadCertificatesFromProperies(properties, true);
	}

	public static boolean checkValidity(Certificate cert) {
		boolean trusted = false;
		try {
			if (cert instanceof X509Certificate) {
				((X509Certificate) cert).checkValidity();
			}
			trusted = true;
		} catch (Exception ex) {
			CertificateUtils.logger.trace(null, ex);
			trusted = false;
		}
		return trusted;
	}

	public static boolean isTrustCA(Certificate cert, java.security.cert.Certificate[] validCA) {
		boolean trusted = false;
		if (validCA == null) {
			return trusted;
		}
		for (int i = 0, a = validCA.length; (i < a) && !trusted; i++) {
			try {
				cert.verify(validCA[i].getPublicKey());
				trusted = true;
			} catch (Exception ex) {
				CertificateUtils.logger.trace(null, ex);
				trusted = false;
			}
		}
		return trusted;
	}

	public static boolean isRevoked(Certificate cert, java.security.cert.CRL[] crls) {
		boolean revoked = false;
		if (crls == null) {
			return revoked;
		}
		for (int j = 0; (j < crls.length) && !revoked; j++) {
			if (crls[j].isRevoked(cert)) {
				CertificateUtils.logger.debug(" -> " + cert + " is Revoked");
				revoked = true;
			}
		}
		return revoked;
	}

	public static java.security.cert.Certificate importCertificate(String filepath) {
		try {
			FileInputStream is = new FileInputStream(new File(filepath));

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			java.security.cert.Certificate cert = cf.generateCertificate(is);
			return cert;
		} catch (CertificateException e) {
			CertificateUtils.logger.error(null, e);
		} catch (IOException e) {
			CertificateUtils.logger.error(null, e);
		}
		return null;
	}

	public static List<Object> getAliasCertPairFromKeyStore(KeyStore ks,
			String pin)
					throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		List<Object> l = new ArrayList<>();
		if (ks == null) {
			return l;
		}
		try {
			ks.load(null, pin.toCharArray());
		} catch (ProviderException e) {
			CertificateUtils.logger.trace(null, e);
			KeyStore renewKs = CertificateUtils.getKeystoreInstance(true);
			if (renewKs != null) {
				renewKs.load(null, pin.toCharArray());
			}
		}

		Enumeration<?> aliasesEnum = ks.aliases();
		while (aliasesEnum.hasMoreElements()) {
			String alias = (String) aliasesEnum.nextElement();
			X509Certificate cert = null;
			try {
				cert = (X509Certificate) ks.getCertificate(alias);
				l.add(new AliasCertPair(alias, cert));
			} catch (Exception ex) {
				CertificateUtils.logger.error(null, ex);
				cert = null;
			}
		}
		return l;
	}

	protected static String getInfo(X509Certificate cert) {
		if (cert == null) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(" getIssuerDN(): " + cert.getIssuerDN().getName());
			sb.append(" SubjectDN(): " + cert.getSubjectDN().getName());
			sb.append(" NotAfter: " + cert.getNotAfter());
			sb.append(" SerialNumber: " + cert.getSerialNumber());
			return sb.toString();
		}
	}

	private static String parseStringForTokenizer(String s, char searchChar, char newChar, char toggleChar) {
		if (s == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean change = false;
		for (int i = 0, a = s.length(); i < a; i++) {
			if ((s.charAt(i) == searchChar) && change) {
				sb.append(newChar);
				continue;
			}
			if (s.charAt(i) == toggleChar) {
				change = !change;
			}
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}

	public static Map<Object, Object> getX509CertificateSubjectDNFields(X509Certificate cert) {
		if (cert == null) {
			return null;
		}
		String s = cert.getSubjectDN().getName();
		if (s == null) {
			return null;
		}
		String parsed = CertificateUtils.parseStringForTokenizer(s, ',', '_', '"');
		Map<Object, Object> h = new HashMap<>();
		StringTokenizer st = new StringTokenizer(parsed, ",", false);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(token, "=", false);
			if (st2.countTokens() == 2) {
				String key = st2.nextToken().trim();
				String value = st2.nextToken().trim();
				h.put(key, CertificateUtils.parseStringForTokenizer(value, '_', ',', '"'));
			}
		}
		return h;
	}

	public static String getX509CertificateSubjectDNFields(X509Certificate cert, String field) {
		Map<Object, Object> h = CertificateUtils.getX509CertificateSubjectDNFields(cert);
		if (h == null) {
			return null;
		}
		if (!h.containsKey(field)) {
			return null;
		}
		return h.get(field).toString();
	}

	public static KeyStore getKeystoreInstance() {
		return CertificateUtils.getKeystoreInstance(false);
	}

	public static KeyStore getKeystoreInstance(boolean forceRenew) {
		if ((CertificateUtils.smcks != null) && !forceRenew) {
			return CertificateUtils.smcks;
		}
		try {
			return CertificateUtils.smcks = KeyStore.getInstance("PKCS11");
		} catch (java.security.KeyStoreException ex) {
			CertificateUtils.smcks = null;
			MessageDialog.showMessage(ApplicationManager.getApplication().getFrame(),
					"abstractlogindialog.pkcs11notfound", JOptionPane.ERROR_MESSAGE,
					ApplicationManager.getApplicationBundle());
			if (ApplicationManager.DEBUG) {
				CertificateUtils.logger.error(null, ex);
			}
			return CertificateUtils.smcks;
		} catch (Exception ex) {
			CertificateUtils.smcks = null;
			MessageDialog.showMessage(ApplicationManager.getApplication().getFrame(), ex.getMessage(),
					JOptionPane.ERROR_MESSAGE, ApplicationManager.getApplicationBundle());
			if (ApplicationManager.DEBUG) {
				CertificateUtils.logger.error(null, ex);
			}
			return CertificateUtils.smcks;
		}
	}

	/**
	 * Loads content into a keystore from file.
	 * @param keystoreFilepath
	 * @param keystorePassword
	 * @param alias
	 */
	public static void addToKeyStore(String keystoreFilepath, char[] keystorePassword, String alias,
			String keystoreType) {
		FileInputStream in = null;
		try {

			in = new FileInputStream(new File(keystoreFilepath));
			CertificateUtils.smcks = java.security.KeyStore.getInstance(keystoreType);
			CertificateUtils.smcks.load(in, keystorePassword);
		} catch (Exception e) {
			CertificateUtils.logger.error(null, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					CertificateUtils.logger.error(null, e);
				}
			}
		}
	}

	/**
	 * It returns in server locator revoked and accepted certificates. This method is called
	 * programmatically twice in remote locator to load revoked and accepted certificates.
	 * @param vCert Contains the list of places where user wants to load certificates: <br>
	 *        <br>
	 *        <ul>
	 *        <li>One unique .properties file included in project (keeps backward compatibility), e.g.
	 *        <i>com/ontimize/quickstart/server /certificate/cert.properties</i> . This properties
	 *        contains a structure like this: <br>
	 *        <br>
	 *        <i> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AC_DNIE_001=com/ontimize/quickstart
	 *        /server/certificate/AC_DNIE_001.crt <br>
	 *        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AC_DNIE_002=com/ontimize/
	 *        quickstart/server/certificate/AC_DNIE_002.crt <br>
	 *        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AC_DNIE_003=com/ontimize/
	 *        quickstart/server/certificate/AC_DNIE_003.crt <br>
	 *        </i> <br>
	 *        <li>Complete path to a directory, e.g. <i>/home/cert</i>. With this format, all files
	 *        contained in this path will be scanned. Non-compatible certificate files will be
	 *        discarded.
	 *        <li>Indivual file path, e.g. <i>/home/cert/AC_DNIE_001.crt</i>. An individual certificate
	 *        will be loaded with this format.
	 *        </ul>
	 * @param crl when <code>true</code> loads and returns revoked certificates, else loads/returns
	 *        accepted certificates.
	 * @return the Certificate[]/CRL[] array with accepted/revoked certificates according to
	 *         <code>crl</code> parameter
	 * @throws Exception when occurs an error loading one of these file paths
	 *
	 * @since 5.2068EN-0.2EN
	 */
	public static Object loadCertificates(List<Object> vCert, boolean crl) throws Exception {
		Certificate[] base = null;
		CRL[] rvk = null;
		for (int i = 0; i < vCert.size(); i++) {
			String url = vCert.get(i).toString();
			if (url.endsWith(".properties")) {
				// Backward compatibility. Certificates are defined into
				// properties in pairs: alias-certX.crt
				vCert.remove(i);
				if (crl) {
					rvk = CertificateUtils.loadCRLFromURL(url);
				} else {
					base = CertificateUtils.loadCerfificatesFromURL(url);
				}
				break;
			}
		}
		if (crl) {
			// revoked certificates loaded from directory or from a single file
			CRL[] rvkDirCert = (CRL[]) CertificateUtils.loadCerfificatesFromDir(vCert, crl);
			CRL[] rvkCert = new CRL[rvk.length + rvkDirCert.length];
			System.arraycopy(rvk, 0, rvkCert, 0, rvk.length);
			System.arraycopy(rvkDirCert, 0, rvkCert, rvk.length, rvkDirCert.length);
			return rvkCert;
		} else {
			// accepted certificates loaded from directory or from a single file
			Certificate[] baseDirCert = (Certificate[]) CertificateUtils.loadCerfificatesFromDir(vCert, crl);
			Certificate[] baseCert = new Certificate[base.length + baseDirCert.length];
			System.arraycopy(base, 0, baseCert, 0, base.length);
			System.arraycopy(baseDirCert, 0, baseCert, base.length, baseDirCert.length);
			return baseCert;
		}
	}

}
