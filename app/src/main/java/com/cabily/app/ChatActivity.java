package com.cabily.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cabily.iconstant.Iconstant;
import com.casperon.app.cabily.R;
import com.mylibrary.xmpp.ChatService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;

/**
 * Created by Administrator on 1/13/2016.
 */
public class ChatActivity extends Activity {

    private Chat chat;
    private Button send;
    private EditText message;
    private String toID = "5639be4fe7a1b615318b4568@"+ Iconstant.XMPP_SERVICE_NAME;
    private StringBuilder toTextMessageBuilder;
    private static StringBuilder fromTextMessageBuilder;
    private TextView toTextMessage;
    private static  TextView  fromTextData;
    private Intent intent;
    private String  driverID;
    public static class MessageHandler extends Handler {
        public static final int HIDE = 0;
        public static final int SHOW = 1;
        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;
            String data = (String)message.obj;
            fromTextMessageBuilder.append(data + "\n");
            fromTextData.setText(fromTextMessageBuilder.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlayout);
        intent = getIntent();
        if(intent != null){
                // driverID = (String) intent.getExtras().get("driverID");
                // if(driverID != null){
                // data = driverID + Iconstant.XMPP_SERVICE_NAME;
                toTextMessage = (TextView) findViewById(R.id.updated_text);
                fromTextData = (TextView) findViewById(R.id.updated_text_from);
                message = (EditText) findViewById(R.id.edit_text);
                toTextMessageBuilder = new StringBuilder();
                fromTextMessageBuilder= new StringBuilder();
                send = (Button) findViewById(R.id.button);
                chat = ChatService.createChat(toID);
                ChatService.setChatMessenger(new Messenger(new MessageHandler()));
                ChatService.enableChat();
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String data = message.getText().toString();
                        if(chat == null){
                            chat  = ChatService.createChat(toID);
                        }
                        if (data != null && data.length() > 0 ) {
                            try {
                                chat.sendMessage(data);
                                toTextMessageBuilder.append(data + "\n");
                                message.getText().clear();
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            toTextMessage.setText("" + toTextMessageBuilder.toString());
                        }
                    }
                });
         //   }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatService.enableChat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatService.disableChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatService.disableChat();
    }

}

