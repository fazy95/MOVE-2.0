package com.mylibrary.xmpp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

import com.move.utils.SessionManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

public class ChatService extends IntentService implements ChatManagerListener, ChatMessageListener {
    private static final String ACTION_FOO = "com.casperon.smackclient.action.FOO";
    private static SessionManager session;
    private static AbstractXMPPConnection connection;
    private static boolean isConnected;
    //Declaration for Chat
    private static Chat chat;
    private static ChatManager chatManager;
    private static boolean isChatEnabled;
    private static Messenger chatMessenger;
    private static ChatHandler chatHandler;
    String hostURL = "", hostName = "";
    Boolean isLogin;
    /**
     */
    public static void startUserAction(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        intent.setAction(ACTION_FOO);
        session = new SessionManager(context);
        context.startService(intent);
    }

    public ChatService() {
        super("ChatService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            handleActionFoo();
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo() {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
        }
        if (session != null && session.getUserDetails() != null) {
            hostURL = session.getXmpp().get(SessionManager.KEY_HOST_URL);
            hostName = session.getXmpp().get(SessionManager.KEY_HOST_NAME);
        }
        isLogin=session.isLoggedIn();
        System.out.println("Xmpp URL and NAME"+hostName+" "+hostURL);
        configBuilder.setHost(hostURL);//http://192.168.1.116/67.219.149.186
        configBuilder.setServiceName(hostName);
        //sec_key
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                isConnected = true;
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
            }

            @Override
            public void connectionClosed() {
                isConnected = false;
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                isConnected = false;
            }

            @Override
            public void reconnectionSuccessful() {
                isConnected = true;
            }

            @Override
            public void reconnectingIn(int seconds) {
            }

            @Override
            public void reconnectionFailed(Exception e) {
                isConnected = false;
            }
        });

        connection.setPacketReplyTimeout(30000);
        try {
            if(isLogin) {
                if (hostName.length() > 0 && hostURL.length() > 0) {
                    connection.connect();
                }

            }
            else
            {
                System.out.println("crash------jai-----------");
            }
        } catch (SmackException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String userName = "";
            String password = "";
            if (session != null && session.getXmppKey() != null) {
                userName = session.getXmppKey().get(SessionManager.KEY_XMPP_USERID);
                password = session.getXmppKey().get(SessionManager.KEY_XMPP_SEC_KEY);
            }
            if (userName.length() > 0 && password.length() > 0) {
                connection.login(userName, password);
                chatManager = ChatManager.getInstanceFor(connection);
                chatManager.addChatListener(this);
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void processMessage(Chat chat, final Message message) {
        if (chatHandler == null) {
            chatHandler = new ChatHandler(getApplicationContext(), this);
        }
        chatHandler.onHandleChatMessage(message);
    }

    public static void setChatMessenger(Messenger messenger) {
        chatMessenger = messenger;
    }

    /*
    Need add chat validation object
    */
    public static Chat createChat(String chatID) {
        if (chatID != null && chatManager != null) {
            chat = chatManager.createChat(chatID);
        }
        return chat;
    }

    public static void enableChat() {
        isChatEnabled = true;
    }

    public static void disableChat() {
        isChatEnabled = false;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        chat.addMessageListener(this);
    }




  /* public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        System.out.println("---------------service destroied--------------------");
    }*/
}
