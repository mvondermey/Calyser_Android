package com.wuala.websocket.httpserver;

import android.content.Context;
import android.util.Log;

import com.wuala.websocket.activity.MainApplication;
import com.wuala.websocket.util.FileHelper;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;

public class WebServer extends Thread {

    static final String SUFFIX_ZIP = "..zip";
    static final String SUFFIX_DEL = "..del";

    static String KEYSTORE_PASSWORD = "1234";
    static String KEY_ALIAS = "SecuredP2P";
    static String COUNTRY_NAME = "US";
    static String FILE_NAME = "server.crt";

    private int port;
    private String webRoot;
    private Context context;

    public WebServer(Context context, int port, final String webRoot) {
        super();
        if (!FileHelper.instance(context).fileIsExist(FILE_NAME)) {
            genSSLKey(context, KEYSTORE_PASSWORD);
        }
        this.port = port;
        this.webRoot = webRoot;
        this.context = context;
    }

    @Override
    public void run() {

        // https
        SslContextFactory sslContextFactory = new SslContextFactory();
        InputStream in = null;
        try {
            in = context.openFileInput(FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("BKS");
        } catch (KeyStoreException e) {
            Log.e(MainApplication.TAG, e.toString(), e);
        }
        try {
            keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            Log.e(MainApplication.TAG, e.toString(), e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
        sslContextFactory.setKeyManagerPassword(KEYSTORE_PASSWORD);
        sslContextFactory.setCertAlias(KEY_ALIAS);

        sslContextFactory.setKeyStoreType("bks");
        // We do not want to speak old SSL and we only want to use strong ciphers
        sslContextFactory.setIncludeProtocols("TLS");
        sslContextFactory.setIncludeCipherSuites("TLS_DHE_RSA_WITH_AES_128_CBC_SHA");

        Server server = new Server();
        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(sslContextFactory);
        sslConnector.setPort(port);
        server.addConnector(sslConnector);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        // resource_handler.setWelcomeFiles(new String[]{"index.html"});

        resource_handler.setResourceBase(webRoot);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, new DefaultHandler()});
        server.setHandler(handlers);

//
//        resource_handler.setResourceBase(webRoot);
//
//        HandlerList handlers = new HandlerList();
//        // handlers.setHandlers(new Handler[]{new HttpsFileHandler(webRoot), resource_handler});
//        handlers.setHandlers(new Handler[]{resource_handler, new HttpsFileHandler(webRoot)});
//        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
//            Log.e(MainApplication.TAG, e.toString(), e);
        }

        // http
//        ServerSocket serverSocket = null;
//        try {
//            // Create socket server
//            serverSocket = new ServerSocket(MainApplication.HTTP_PORT);
//            // Create HTTP processor
//            BasicHttpProcessor httpproc = new BasicHttpProcessor();
//            // add http interceptor
//            httpproc.addInterceptor(new ResponseDate());
//            httpproc.addInterceptor(new ResponseServer());
//            httpproc.addInterceptor(new ResponseContent());
//            httpproc.addInterceptor(new ResponseConnControl());
//            // Create http service
//            HttpService httpService = new HttpService(httpproc,
//                    new DefaultConnectionReuseStrategy(),
//                    new DefaultHttpResponseFactory());
//            // Create http params
//            HttpParams params = new BasicHttpParams();
//            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
//                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
//                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
//                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
//                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");
//            httpService.setParams(params);
//
//            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
//            reqistry.register("*" + SUFFIX_ZIP, new HttpZipHandler(webRoot));
//            reqistry.register("*" + SUFFIX_DEL, new HttpDelHandler(webRoot));
//            reqistry.register("*", new HttpFileHandler(webRoot));
//
//            httpService.setHandlerResolver(reqistry);
//            /* while receive client request */
//            isLoop = true;
//            while (isLoop && !Thread.interrupted()) {
//                // get socket client
//                Socket socket = serverSocket.accept();
//                // bound to http server
//                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
//                conn.bind(socket, params);
//                // start worker thread
//                Thread t = new WorkerThread(httpService, conn);
//                t.setDaemon(true);
//                t.start();
//            }
//        } catch (IOException e) {
//            isLoop = false;
//            e.printStackTrace();
//        } finally {
//            try {
//                if (serverSocket != null) {
//                    serverSocket.close();
//                }
//            } catch (IOException e) {
//            }
//        }
    }

    public void close() {
    }

    /**
     * Creates a new SSL key and certificate and stores them in the app's
     * internal data directory.
     *
     * @param ctx              An Android application context
     * @param keystorePassword The password to be used for the keystore
     * @return boolean indicating success or failure
     */
    public static boolean genSSLKey(Context ctx, String keystorePassword) {
        try {
            // Create a new pair of RSA keys using BouncyCastle classes
            RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
            gen.init(new RSAKeyGenerationParameters(BigInteger.valueOf(3),
                    new SecureRandom(), 1024, 80));
            AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();
            RSAKeyParameters publicKey = (RSAKeyParameters) keyPair.getPublic();
            RSAPrivateCrtKeyParameters privateKey = (RSAPrivateCrtKeyParameters) keyPair
                    .getPrivate();

            // We also need our pair of keys in another format, so we'll convert
            // them using java.security classes
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(
                    new RSAPublicKeySpec(publicKey.getModulus(), publicKey
                            .getExponent()));
            PrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(
                    new RSAPrivateCrtKeySpec(publicKey.getModulus(), publicKey
                            .getExponent(), privateKey.getExponent(),
                            privateKey.getP(), privateKey.getQ(), privateKey
                            .getDP(), privateKey.getDQ(), privateKey
                            .getQInv()));

            // CName or other certificate details do not really matter here
            X509Name x509Name = new X509Name("CN=" + COUNTRY_NAME);

            // We have to sign our public key now. As we do not need or have
            // some kind of CA infrastructure, we are using our new keys
            // to sign themselves

            // Set certificate meta information
            V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
            certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System
                    .currentTimeMillis())));
            certGen.setIssuer(new X509Name("CN=" + COUNTRY_NAME));
            certGen.setSubject(x509Name);
            DERObjectIdentifier sigOID = PKCSObjectIdentifiers.sha1WithRSAEncryption;
            AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID,
                    new DERNull());
            certGen.setSignature(sigAlgId);
            ByteArrayInputStream bai = new ByteArrayInputStream(
                    pubKey.getEncoded());
            ASN1InputStream ais = new ASN1InputStream(bai);
            certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo(
                    (ASN1Sequence) ais.readObject()));
            bai.close();
            ais.close();

            // We want our keys to live long
            Calendar expiry = Calendar.getInstance();
            expiry.add(Calendar.DAY_OF_YEAR, 365 * 30);

            certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
            certGen.setEndDate(new Time(expiry.getTime()));
            TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();

            // The signing: We first build a hash of our certificate, than sign
            // it with our private key
            SHA1Digest digester = new SHA1Digest();
            AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            DEROutputStream dOut = new DEROutputStream(bOut);
            dOut.writeObject(tbsCert);
            byte[] signature;
            byte[] certBlock = bOut.toByteArray();
            // first create digest
            digester.update(certBlock, 0, certBlock.length);
            byte[] hash = new byte[digester.getDigestSize()];
            digester.doFinal(hash, 0);
            // and sign that
            rsa.init(true, privateKey);
            DigestInfo dInfo = new DigestInfo(new AlgorithmIdentifier(
                    X509ObjectIdentifiers.id_SHA1, null), hash);
            byte[] digest = dInfo.getEncoded(ASN1Encodable.DER);
            signature = rsa.processBlock(digest, 0, digest.length);
            dOut.close();

            // We build a certificate chain containing only one certificate
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(tbsCert);
            v.add(sigAlgId);
            v.add(new DERBitString(signature));
            X509CertificateObject clientCert = new X509CertificateObject(
                    new X509CertificateStructure(new DERSequence(v)));
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = clientCert;

            // We add our certificate to a new keystore
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(null);
            keyStore.setKeyEntry(KEY_ALIAS, (Key) privKey, keystorePassword.toCharArray(), chain);

            // We write this keystore to a file
            OutputStream out = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            keyStore.store(out, keystorePassword.toCharArray());
            out.close();
            return true;
        } catch (Exception e) {
            // Do your exception handling here
            // There is a lot which might go wrong
            e.printStackTrace();
        }
        return false;
    }
}