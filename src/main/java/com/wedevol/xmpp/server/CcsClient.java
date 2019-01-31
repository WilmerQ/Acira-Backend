package com.wedevol.xmpp.server;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.base.GeoCodeGooogle;
import co.com.acira.clases.objetoXmpp;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.GrupoFamiliar;
import co.com.acira.modelo.InformadosEventoSucedido;
import co.com.acira.modelo.Usuario;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.wedevol.xmpp.bean.CcsInMessage;
import com.wedevol.xmpp.service.PayloadProcessor;
import com.wedevol.xmpp.util.Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

/**
 * Sample Smack implementation of a client for FCM Cloud Connection Server. Most
 * of it has been taken more or less verbatim from Google's documentation:
 * https://firebase.google.com/docs/cloud-messaging/xmpp-server-ref
 */
public class CcsClient implements StanzaListener {

    private static final Logger logger = Logger.getLogger(CcsClient.class.getName());

    private static CcsClient sInstance = null;
    private XMPPTCPConnection connection;
    private String mApiKey = null;
    private boolean mDebuggable = false;
    private String fcmServerUsername = null;

    List<objetoXmpp> registroEnvio = new ArrayList<>();
    List<String> listAckRecive = new ArrayList<>();

    public static CcsClient getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("You have to prepare the client first");
        }
        return sInstance;
    }

    public static CcsClient prepareClient(String projectId, String apiKey, boolean debuggable) {
        synchronized (CcsClient.class) {
            if (sInstance == null) {
                System.out.println("iniciando prepareClient");
                sInstance = new CcsClient(projectId, apiKey, debuggable);
            }
        }
        return sInstance;
    }

    private CcsClient(String projectId, String apiKey, boolean debuggable) {
        this();
        mApiKey = apiKey;
        mDebuggable = debuggable;
        fcmServerUsername = projectId + "@" + Util.FCM_SERVER_CONNECTION;
    }

    private CcsClient() {
        // Add FCMPacketExtension
        ProviderManager.addExtensionProvider(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE,
                new ExtensionElementProvider<FcmPacketExtension>() {
            @Override
            public FcmPacketExtension parse(XmlPullParser parser, int initialDepth)
                    throws XmlPullParserException, IOException, SmackException {
                String json = parser.nextText();
                return new FcmPacketExtension(json);
            }
        });
    }

    /**
     * Connects to FCM Cloud Connection Server using the supplied credentials
     */
    public void connect() throws XMPPException, SmackException, IOException, InterruptedException {
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setXmppDomain("FCM XMPP Client Connection Server");
        config.setHost(Util.FCM_SERVER);
        config.setPort(Util.FCM_PORT);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());
        // Launch a window with info about packets sent and received
        config.setDebuggerEnabled(mDebuggable);

        // Create the connection
        connection = new XMPPTCPConnection(config.build());

        // Connect
        connection.connect();

        // Enable automatic reconnection
        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();

        // Disable Roster at login
        Roster.getInstanceFor(connection).setRosterLoadedAtLogin(false);

        // Check SASL authentication
        logger.log(Level.INFO, "SASL PLAIN authentication enabled? " + SASLAuthentication.isSaslMechanismRegistered("PLAIN"));
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

        // Handle reconnection and connection errors
        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
                logger.log(Level.INFO, "Reconnection successful ...");
                // TODO: handle the reconnecting successful
            }

            @Override
            public void reconnectionFailed(Exception e) {
                logger.log(Level.INFO, "Reconnection failed: ", e.getMessage());
                // TODO: handle the reconnection failed
            }

            @Override
            public void reconnectingIn(int seconds) {
                logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
                // TODO: handle the reconnecting in
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                logger.log(Level.INFO, "Connection closed on error");
                // TODO: handle the connection closed on error
            }

            @Override
            public void connectionClosed() {
                logger.log(Level.INFO, "Connection closed");
                // TODO: handle the connection closed
            }

            @Override
            public void authenticated(XMPPConnection arg0, boolean arg1) {
                logger.log(Level.INFO, "User authenticated");
                // TODO: handle the authentication
            }

            @Override
            public void connected(XMPPConnection arg0) {
                logger.log(Level.INFO, "Connection established");
                // TODO: handle the connection
            }
        });

        // Handle incoming packets and reject messages that are not from FCM CCS
        connection.addAsyncStanzaListener(this, stanza -> stanza.hasExtension(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE));

        // Log all outgoing packets
        connection.addPacketInterceptor(stanza -> logger.log(Level.INFO, "Sent: " + stanza.toXML()), ForEveryStanza.INSTANCE);

        // Set the ping interval
        /*final PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(100000);
        pingManager.registerPingFailedListener(() -> {
            logger.info("The ping failed, restarting the ping interval again ...");
            pingManager.setPingInterval(100000);
        });*/
        connection.login(fcmServerUsername, mApiKey);
        logger.log(Level.INFO, "Logged in: " + fcmServerUsername);
    }

    public synchronized void reconnect() {
        // Try to connect again using exponential back-off!
    }

    /**
     * Handles incoming messages
     *
     * @param packet
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processStanza(Stanza packet) {
        logger.log(Level.INFO, "Received: " + packet.toXML());
        FcmPacketExtension fcmPacket = (FcmPacketExtension) packet.getExtension(Util.FCM_NAMESPACE);
        String json = fcmPacket.getJson();
        try {
            Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parseWithException(json);
            Object messageType = jsonMap.get("message_type");

            if (messageType == null) {
                CcsInMessage inMessage = MessageHelper.createCcsInMessage(jsonMap);
                handleUpstreamMessage(inMessage); // normal upstream message
                return;
            }

            switch (messageType.toString()) {
                case "ack":
                    handleAckReceipt(jsonMap);
                    break;
                case "nack":
                    handleNackReceipt(jsonMap);
                    break;
                case "receipt":
                    handleDeliveryReceipt(jsonMap);
                    break;
                case "control":
                    handleControlMessage(jsonMap);
                    break;
                default:
                    logger.log(Level.INFO, "Received unknown FCM message type: " + messageType.toString());
            }
        } catch (ParseException e) {
            logger.log(Level.INFO, "Error parsing JSON: " + json, e.getMessage());
        }

    }

    /**
     * Handles an upstream message from a device client through FCM
     */
    private void handleUpstreamMessage(CcsInMessage inMessage) {
        final String action = inMessage.getDataPayload()
                .get(Util.PAYLOAD_ATTRIBUTE_ACTION);
        if (action != null) {
            PayloadProcessor processor = ProcessorFactory.getProcessor(action);
            processor.handleMessage(inMessage);
        }

        // Send ACK to FCM
        String ack = MessageHelper.createJsonAck(inMessage.getFrom(), inMessage.getMessageId());
        send(ack);
    }

    /**
     * Handles an ACK message from FCM
     */
    private void handleAckReceipt(Map<String, Object> jsonMap) {
        // TODO: handle the ACK in the proper way
        System.out.println("aqui en handleAckReceipt");
        System.out.println(jsonMap.get("message_id"));
        listAckRecive.add(jsonMap.get("message_id").toString());
        System.out.println("size acks " + listAckRecive.size());
    }

    /**
     * Handles a NACK message from FCM
     */
    private void handleNackReceipt(Map<String, Object> jsonMap) {
        String errorCode = (String) jsonMap.get("error");

        if (errorCode == null) {
            logger.log(Level.INFO, "Received null FCM Error Code");
            return;
        }

        switch (errorCode) {
            case "INVALID_JSON":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "BAD_REGISTRATION":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "DEVICE_UNREGISTERED":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "BAD_ACK":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "SERVICE_UNAVAILABLE":
                handleServerFailure(jsonMap);
                break;
            case "INTERNAL_SERVER_ERROR":
                handleServerFailure(jsonMap);
                break;
            case "DEVICE_MESSAGE_RATE_EXCEEDED":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "TOPICS_MESSAGE_RATE_EXCEEDED":
                handleUnrecoverableFailure(jsonMap);
                break;
            case "CONNECTION_DRAINING":
                handleConnectionDrainingFailure();
                break;
            default:
                logger.log(Level.INFO, "Received unknown FCM Error Code: " + errorCode);
        }
    }

    /**
     * Handles a Delivery Receipt message from FCM (when a device confirms that
     * it received a particular message)
     */
    private void handleDeliveryReceipt(Map<String, Object> jsonMap) {
        System.out.println("aqui en handleDeliveryReceipt");
        Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
        System.out.println("-data " + data);
        if (data.get("message_status").equals("MESSAGE_SENT_TO_DEVICE")) {
            if (listAckRecive.contains(data.get("original_message_id").toString())) {
                System.out.println("ya se recibio el mensaje de " + data.get("original_message_id").toString());
                System.out.println("remove este ack " + listAckRecive.remove(data.get("original_message_id").toString()));
            }
        }
    }

    /**
     * Handles a Control message from FCM
     */
    private void handleControlMessage(Map<String, Object> jsonMap) {
        // TODO: handle the control message
        String controlType = (String) jsonMap.get("control_type");

        if (controlType.equals("CONNECTION_DRAINING")) {
            handleConnectionDrainingFailure();
        } else {
            logger.log(Level.INFO, "Received unknown FCM Control message: " + controlType);
        }
    }

    private void handleServerFailure(Map<String, Object> jsonMap) {
        // TODO: Resend the message
        logger.log(Level.INFO, "Server error: " + jsonMap.get("error") + " -> " + jsonMap.get("error_description"));

    }

    private void handleUnrecoverableFailure(Map<String, Object> jsonMap) {
        // TODO: handle the unrecoverable failure
        logger.log(Level.INFO, "Unrecoverable error: " + jsonMap.get("error") + " -> " + jsonMap.get("error_description"));
    }

    private void handleConnectionDrainingFailure() {
        // TODO: handle the connection draining failure. Force reconnect?
        logger.log(Level.INFO, "FCM Connection is draining! Initiating reconnection ...");
    }

    /**
     * Sends a downstream message to FCM
     *
     * @param jsonRequest
     */
    public void send(String jsonRequest) {
        Stanza request = new FcmPacketExtension(jsonRequest).toPacket();
        try {
            connection.sendStanza(request);
        } catch (NotConnectedException | InterruptedException e) {
            logger.log(Level.INFO, "The packet could not be sent due to a connection problem. Packet: {}", request.toXML());
        }
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30 * 1000);
                    //connection.disconnect();
                    System.out.println("---------------------------");
                    System.out.println("registro de envio " + registroEnvio.size());
                    System.out.println("list de ack recive " + listAckRecive.size());
                    System.out.println("---------------------------");
                    if (!listAckRecive.isEmpty()) {
                        for (objetoXmpp xmpp : registroEnvio) {
                            GeoCodeGooogle codeGooogle = new GeoCodeGooogle();
                            if (listAckRecive.contains(xmpp.getIdmensaje())) {
                                System.out.println("no se recibe confirmacion- enviar sms");
                                if (xmpp.getEventoPanico() != null) {
                                    String dir = codeGooogle.getLocationName(xmpp.getEventoPanico().getLatitud(), xmpp.getEventoPanico().getLongitud());
                                    SendSMSPanico(xmpp.getEventoPanico(), xmpp.getUsuario(), dir);
                                }
                                if (xmpp.getEventoSospecha() != null) {
                                    String dir = codeGooogle.getLocationName(xmpp.getEventoSospecha().getLatitud(), xmpp.getEventoSospecha().getLongitud());
                                    SendSMSSospecha(xmpp.getEventoSospecha(), xmpp.getUsuario(), dir);
                                }
                            } else {
                                System.out.println("se recibe confirmacion");
                                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                                if (xmpp.getEventoPanico() != null) {
                                    ies.setEventoPanico(xmpp.getEventoPanico());
                                }
                                if (xmpp.getEventoSospecha() != null) {
                                    ies.setEventoSospecha(xmpp.getEventoSospecha());
                                }
                                ies.setCodigoEntrega(xmpp.getIdmensaje());
                                ies.setUsuarioInformado(xmpp.getUsuario());
                                ies.setMedioUtlizado("FCM");
                                ies.setMensajeNativo(xmpp.getMensaje());
                                guardarEnBaseDatos(ies);
                            }
                        }
                    } else {
                        System.out.println("se recibe confirmacion");
                        for (objetoXmpp xmpp : registroEnvio) {
                            InformadosEventoSucedido ies = new InformadosEventoSucedido();
                            if (xmpp.getEventoPanico() != null) {
                                ies.setEventoPanico(xmpp.getEventoPanico());
                            }
                            if (xmpp.getEventoSospecha() != null) {
                                ies.setEventoSospecha(xmpp.getEventoSospecha());
                            }
                            ies.setCodigoEntrega(xmpp.getIdmensaje());
                            ies.setUsuarioInformado(xmpp.getUsuario());
                            ies.setMedioUtlizado("FCM");
                            ies.setMensajeNativo(xmpp.getMensaje());
                            guardarEnBaseDatos(ies);
                        }
                    }
                } catch (InterruptedException ex) {
                    System.out.println("Error: CssClient-disconnect: " + ex.getLocalizedMessage());
                }
            }
        }).start();
    }

    public void sendBroadcast(List<objetoXmpp> xmpps) {
        System.out.println("***********sendBroadcast");
        this.registroEnvio = new ArrayList<>();
        this.listAckRecive = new ArrayList<>();
        this.registroEnvio = xmpps;
        xmpps.forEach((xmpp) -> {
            send(xmpp.getMensaje());
        });
        disconnect();
    }

    public void SendSMSPanico(EventoPanico panico, Usuario usuario, String dir) {
        try {
            String urlServicio = null;
            String nombreCompleto;
            String direccion;
            String barrio = "";
            GrupoFamiliar gf = usuario.getGrupoFamiliar();
            if (gf != null) {
                if (!panico.getAnonimo()) {
                    if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                    } else {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                    }
                } else {
                    nombreCompleto = "Usuario anonimo";
                }

                if (panico.getZonaRespondiente() != null) {
                    barrio = panico.getZonaRespondiente().getBarrio().getNombre();
                }

                if (distanciaCoord(panico.getUsuarioInformante().getGrupoFamiliar().getCodigoAsignado().getLatitud(),
                        panico.getUsuarioInformante().getGrupoFamiliar().getCodigoAsignado().getLongitud(),
                        panico.getLatitud(), panico.getLongitud()) * 1000 <= 25) {
                    if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                        direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21) + " " + barrio;
                    } else {
                        direccion = gf.getCodigoAsignado().getDireccion() + " " + barrio;
                    }
                    urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                            + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento de pánico cerca su lugar de residencia por:"
                            + nombreCompleto
                            + ", Dir:" + direccion
                            + ", Hora:" + panico.getHora().toString().substring(0, 5)
                            + ", Preste Atención" + "&from=seamco";
                } else {
                    if (dir.equals("ninguna")) {
                        if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                            direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                        } else {
                            direccion = gf.getCodigoAsignado().getDireccion();
                        }
                        urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                                + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento de pánico cerca su lugar de residencia por:"
                                + nombreCompleto
                                + ", Dir:" + direccion + " " + barrio
                                + ", Hora:" + panico.getHora().toString().substring(0, 5)
                                + ", Preste Atención" + "&from=seamco";
                    } else {
                        urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                                + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento de pánico cerca su lugar de residencia por:"
                                + nombreCompleto
                                + "Dir:" + dir + " " + barrio
                                + ", Hora:" + panico.getHora().toString().substring(0, 5)
                                + ", Preste Atención" + "&from=seamco";
                    }
                }
            }
            urlServicio = urlServicio.replaceAll(" ", "%20");
            urlServicio = urlServicio.replaceAll("#", "N.");
            System.out.println("url servicio if " + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            if (finalStr.toString().contains("Success")) {
                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                ies.setEventoPanico(panico);
                ies.setCodigoEntrega(finalStr.toString().substring(finalStr.toString().indexOf("\""), finalStr.toString().length()));
                ies.setMedioUtlizado("SMS");
                ies.setUsuarioInformado(usuario);
                ies.setMensajeNativo(urlServicio);
                guardarEnBaseDatos(ies);
            }
        } catch (IOException e) {
            System.out.println("Error: CssClient-SendSMSPanico: " + e.getLocalizedMessage());
        }
    }

    public void SendSMSSospecha(EventoSospecha panico, Usuario usuario, String dir) {
        try {
            String urlServicio = null;
            String nombreCompleto = null;
            String direccion = null;
            GrupoFamiliar gf = usuario.getGrupoFamiliar();

            if (gf != null) {
                System.out.println("gf =! null");
                if (!panico.getAnonimo()) {
                    if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                    } else {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                    }
                } else {
                    nombreCompleto = "Usuario anonimo";
                }
                if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                    direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                } else {
                    direccion = gf.getCodigoAsignado().getDireccion();
                }
                urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento sospechoso cerca su lugar de residencia por:"
                        + nombreCompleto
                        + ", Reside:" + direccion
                        + ", Hora:" + panico.getHora().toString().substring(0, 5)
                        + ", Preste Atención" + "&from=seamco";
                urlServicio = urlServicio.replaceAll(" ", "%20");
                urlServicio = urlServicio.replaceAll("#", "N.");
                System.out.println("url servicio if " + urlServicio);
            }

            System.out.println("url servicio" + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("conexion aqui --------" + connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            if (finalStr.toString().contains("Success")) {
                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                ies.setEventoSospecha(panico);
                ies.setCodigoEntrega(finalStr.toString().substring(finalStr.toString().indexOf("\""), finalStr.toString().length()));
                ies.setMedioUtlizado("SMS");
                ies.setUsuarioInformado(usuario);
                ies.setMensajeNativo(urlServicio);
                guardarEnBaseDatos(ies);
            }
        } catch (IOException e) {
            System.out.println("Error: CssClient-SendSMSSospecha: " + e.getLocalizedMessage());
        }
    }

    public void guardarEnBaseDatos(InformadosEventoSucedido ies) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(ConfiguracionGeneral.URL, ConfiguracionGeneral.USUARIO, ConfiguracionGeneral.PASS);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            if (ies.getEventoPanico() != null) {
                String codigoEntrega2 = ies.getCodigoEntrega();
                if (ies.getCodigoEntrega().length() > 255) {
                    codigoEntrega2 = codigoEntrega2.substring(0, 254);
                }
                String mensajeNativo2 = ies.getMensajeNativo();
                String sql2 = "INSERT INTO public.informadoseventosucedido(\n"
                        + "            estado, fechacreacion, \n"
                        + "            usuariocreacion, codigoentrega, medioutlizado, mensajenativo, \n"
                        + "            eventopanico_id, usuarioinformado_id)\n"
                        + "    VALUES (" + Boolean.TRUE + ", '" + new Date() + "', \n"
                        + "            'manual', '" + codigoEntrega2 + "', '" + ies.getMedioUtlizado() + "', '" + mensajeNativo2 + "', \n"
                        + "            " + ies.getEventoPanico().getId() + ", " + ies.getUsuarioInformado().getId() + ");";

                stmt.executeUpdate(sql2);
            }

            if (ies.getEventoSospecha() != null) {
                String codigoEntrega2 = ies.getCodigoEntrega();
                if (ies.getCodigoEntrega().length() > 255) {
                    codigoEntrega2 = codigoEntrega2.substring(0, 254);
                }
                String mensajeNativo2 = ies.getMensajeNativo();
                String sql2 = "INSERT INTO public.informadoseventosucedido(\n"
                        + "            estado, fechacreacion, \n"
                        + "            usuariocreacion, codigoentrega, medioutlizado, mensajenativo, \n"
                        + "            eventosospecha_id, usuarioinformado_id)\n"
                        + "    VALUES (" + Boolean.TRUE + ", '" + new Date() + "', \n"
                        + "            'manual', '" + codigoEntrega2 + "', '" + ies.getMedioUtlizado() + "', '" + mensajeNativo2 + "', \n"
                        + "            " + ies.getEventoSospecha().getId() + ", " + ies.getUsuarioInformado().getId() + ");";
                stmt.executeUpdate(sql2);
            }

            stmt.close();
            c.commit();
            c.close();
            System.out.println("Guardado Exitoso en la base de datos");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error: CssClient-guardarEnBaseDatos: " + e.getLocalizedMessage());
            System.exit(0);
        }
    }

    public double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {
        double radioTierra = 6371;//en kilómetros  
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;
        return distancia;
    }
}
