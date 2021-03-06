package com.allegra.handyuvisa;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allegra.handyuvisa.async.AddChatLines;
import com.allegra.handyuvisa.async.AsyncRestHelperChat;
import com.allegra.handyuvisa.async.AsyncTaskMPosResultEventChat;
import com.allegra.handyuvisa.async.ChatEventsNext;
import com.allegra.handyuvisa.async.ChatInfo;
import com.allegra.handyuvisa.async.ChatRequest;
import com.allegra.handyuvisa.async.ChatResourceEventsInfo;
import com.allegra.handyuvisa.async.EndChat;
import com.allegra.handyuvisa.async.GetBaseResource;
import com.allegra.handyuvisa.async.MyBus;
import com.allegra.handyuvisa.utils.Constants;
import com.allegra.handyuvisa.utils.CustomizedTextView;
import com.allegra.handyuvisa.utils.OnBackCallback;
import com.allegra.handyuvisa.utils.Util;
import com.squareup.otto.Subscribe;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends Fragment implements com.allegra.handyuvisa.BackFragment.MenuSelectListener{

    //*****************GLOBAL ATTRIBUTES*****************

    private final String TAG = "ChatActivity";
    private static SimpleDateFormat CHAT_TIME_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private String agentName, infoUri, nextUri, chatState, eventsUri, chatSessionUri, requestChatUri;
    private ListView chatListView;
    private ArrayList<Message> chatMessages;
    private ChatMsgAdapter chatMsgAdapter;
    private EditText sentText;
    private ImageView animation, iv_header;
    private RelativeLayout relLoader, relHeader;
    private Button btnCancel, btnSend;
    private TextView tv_chat_agent, tv_chat_start;
    private LinearLayout form;

    //*****************INNER CLASSES*****************

    public class Message {

        private String text;
        private boolean fromUser;
        private long timeStamp;

        public Message(String text, boolean fromUser, long timeStamp) {
            this.text = text;
            this.fromUser = fromUser;
            this.timeStamp = timeStamp;
        }

        public String getText() { return text; }
        public boolean isFromUser() { return fromUser; }
        public long getTimeStamp() { return timeStamp; }
    }

    //*****************OVERRIDE METHODS*****************

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /*super.setView(R.layout.fragment_chat_in_progress, R.drawable.load__chat,
                R.string.txt_lbl_setupChat, this);*/
         //setView(R.layout.fragment_chat_in_progress, this);

        initLivePersonService();
        MyBus.getInstance().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_chat_in_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    @Override
    public void onDestroy() {
        MyBus.getInstance().unregister(this);
        super.onDestroy();
    }

    public void initViews(View root) {

        ((FragmentMain) getParentFragment()).configToolbar(true, 0, "");

        chatMessages = new ArrayList<>();
        chatMsgAdapter = new ChatMsgAdapter(getActivity(), chatMessages);
        chatListView = (ListView) root.findViewById(R.id.lv_chat_msg);
        chatListView.setAdapter(chatMsgAdapter);
        sentText = (EditText) root.findViewById(R.id.et_chat_msg);
        relHeader = (RelativeLayout)root.findViewById(R.id.ll_header);
        iv_header = (ImageView)root.findViewById(R.id.iv_header);
        relLoader = (RelativeLayout)root.findViewById(R.id.loader);
        animation = (ImageView)root.findViewById(R.id.load_circle);

        animation.post(new Runnable() {
            @Override
            public void run() {
                ((AnimationDrawable) animation.getBackground()).start();
            }
        });
        btnCancel = (Button)root.findViewById(R.id.cancel_one_touch);
        //Log.d(TAG, "Llega antes de los click");
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "Llega al cancel in chat");
                ((FragmentMain) getParentFragment()).replaceLayout(new FrontFragment(), true);
            }
        });

        tv_chat_agent = (TextView)root.findViewById(R.id.tv_chat_agent);
        tv_chat_start = (TextView)root.findViewById(R.id.tv_chat_start);
        form = (LinearLayout)root.findViewById(R.id.form);

        btnSend = (Button)root.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddLines();
            }
        });
    }

    /*@Override
    public void onBackPressed() {

        //super.onBackPressed();
       // Log.d(TAG, "Back button is pressed");
        if (chatState != null && chatState.equals("chatting")) {
            //endChat();
        } else {
           // Log.d(TAG, "Closing before chat has started");
        }
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    /*@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }*/

    @Override
    public void getStartActivity(Intent intent) {

        if (chatState != null && chatState.equals("chatting")) {
            endChat();
        } else {
            //Log.d(TAG, "Closing before chat has started");
        }
        //super.getStartActivity(intent);
    }


    //*****************PROPER METHODS*****************

    public void onAlertCancelChat(){
        Context cont = getParentFragment().getActivity();
        final Dialog dialog = new Dialog(cont);
        dialog.setContentView(R.layout.dialog_cancel_chat);
        dialog.show();
        //CustomizedTextViews (Cancel and Ok)
        CustomizedTextView textCancel = (CustomizedTextView) dialog.findViewById(R.id.txtCancelDialogchat);
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        CustomizedTextView textOk = (CustomizedTextView) dialog.findViewById(R.id.txtOkCancelChat);
        textOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ((FragmentMain) getParentFragment()).restartStack();
                ((FragmentMain) getParentFragment()).replaceLayout(new FrontFragment(), true);
            }
        });
    }

    public void onMenu(){
        ((MainActivity) getActivity()).animate();
    }

    public void showLayout(){
        //relHeader.setVisibility(View.VISIBLE);
        //iv_header.setVisibility(View.VISIBLE);
        tv_chat_agent.setVisibility(View.VISIBLE);
        tv_chat_start.setVisibility(View.VISIBLE);
        chatListView.setVisibility(View.VISIBLE);
        form.setVisibility(View.VISIBLE);
    }

    /*public void onCloseMenu(View v){
        ((MainActivity) getActivity()).animate();
    }*/

    private void initLivePersonService() {
        GetBaseResource apiInfo = new GetBaseResource();
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
    }

    private void sendChatRequest(String uri) {

        ChatRequest apiInfo = new ChatRequest(uri);
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
    }

    private void getChatResourceEventsInfo() {

        ChatResourceEventsInfo apiInfo = new ChatResourceEventsInfo(chatSessionUri);
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
    }

    private void getChatEventsNext() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ChatEventsNext apiInfo = new ChatEventsNext(nextUri);
                AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
                helper.execute();
            }
        }, 2000);
    }


    private void updateChatHeader(String agentData) {

        agentName = agentData;
        //Log.d(TAG, "Agent name: " + agentName);

        if (agentName != null && agentName.length() > 0) {
            TextView agent = (TextView) getView().findViewById(R.id.tv_chat_agent);
            String strFormat = getResources().getString(R.string.txt_lbl_agent);
            String value = String.format(strFormat, agentName);
            agent.setText(value);

            TextView start = (TextView) getView().findViewById(R.id.tv_chat_start);
            Date dateObj = Calendar.getInstance().getTime();
            start.setText(Util.getFormattedTime(dateObj));

            String user = "";
            if (Util.isAuthenticated(getActivity())) {
                if (Constants.getUser(getActivity()) != null) {
                    user = Constants.getUser(getActivity()).nombre;
                }
            }
            chatMsgAdapter.setSessionDetails(dateObj.getTime(), agentName, user);
        }
    }

    private void handleAgentChatMsg(HashMap<String, String> data) {

        int msgCount = Integer.parseInt(data.get(ChatEventsNext.MSG_COUNT));
        int id = Integer.parseInt(data.get(ChatEventsNext.MSG_START_ID));
        for (int i = 0; i < msgCount; i++, id++) {
            String text = data.get("text" + id);
            if (!TextUtils.isEmpty(text)) {
                if (!data.get("source" + id).equals("visitor")) {
                    String clean = Html.fromHtml(text).toString();
                    Date dateObj = CHAT_TIME_PARSER.parse(data.get("time" + id), new ParsePosition(0));
                    chatMessages.add(new Message(clean, false, dateObj.getTime()));
                   // Log.d(TAG, "chatMessages[" + i + "]:" + text);
                }
            }

        }
        chatMsgAdapter.notifyDataSetChanged();
        chatListView.setSelection(chatMessages.size() - 1);
    }

    public void onHome(View view) {

       // Log.d(TAG, "Llega al onHome");
        //overridePendingTransition(R.animator.back_slide_in, R.animator.front_slide_out);
        if (chatState != null && chatState.equals("chatting")) {
            endChat();
        } else {
           // Log.d(TAG, "Closing before chat has started");
        }
        //super.onHome(view);
    }

    private void endChat() {

        EndChat apiInfo = new EndChat(eventsUri);
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
       // Log.d(TAG, "Llega al endChat");
    }

    private void getChatInfo() {

        ChatInfo apiInfo = new ChatInfo(infoUri);
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
    }

    public void onAddLines() {

        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                    getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        String text = sentText.getText().toString();

        if (text.isEmpty()) {
            return;
        }
        chatMessages.add(new Message(text, true, System.currentTimeMillis()));
        chatMsgAdapter.notifyDataSetChanged();
        sentText.setText("");

        chatListView.setSelection(chatMessages.size() - 1);
        AddChatLines apiInfo = new AddChatLines(eventsUri, text);
        AsyncRestHelperChat helper = new AsyncRestHelperChat(apiInfo);
        helper.execute();
    }

    //*****************SUBSCRIBE METHODS*****************
    @Subscribe
    public void onAsyncTaskResult(AsyncTaskMPosResultEventChat event) {

        boolean errorExists = true;
        HashMap<String, String> data;

        if (event.getResult() != null) {
            data = event.getResult();
            System.out.println("entra al AsyncTaskMPosResultEventChat");
            for(Object objname:data.keySet()) {
                System.out.println("data");
                System.out.println(objname);
                System.out.println(data.get(objname));
            }
            if (event.getApiName().equals(GetBaseResource.APINAME)) {
                if (data.containsKey(GetBaseResource.CHAT_REQUEST)) {
                    requestChatUri = data.get(GetBaseResource.CHAT_REQUEST);
                    sendChatRequest(requestChatUri);
                   // Log.d(TAG, "Es "+requestChatUri);
                    errorExists = false;
                } else {
                   // Log.e(TAG, "GetBaseResource fail");
                }
            } else if (event.getApiName().equalsIgnoreCase(ChatRequest.APINAME)) {
                if (data.containsKey(ChatRequest.LOCATION)) {
                    chatSessionUri = data.get(ChatRequest.LOCATION);
                    errorExists = false;
                    getChatResourceEventsInfo();
                } else {
                   // Log.e(TAG, "ChatRequest fails");
                }
            } else if (event.getApiName().equalsIgnoreCase(ChatResourceEventsInfo.APINAME)) {
                if (data.containsKey(ChatResourceEventsInfo.EVENTS_LINK)) {
                    eventsUri = data.get(ChatResourceEventsInfo.EVENTS_LINK);
                    infoUri = data.get(ChatResourceEventsInfo.INFO_LINK);
                    nextUri = data.get(ChatResourceEventsInfo.EVENT_NEXT);
                    if (data.get(ChatResourceEventsInfo.CHAT_STATE).equals("chatting")) {
                        handleAgentChatMsg(data);
                        updateChatHeader(data.get(ChatResourceEventsInfo.AGENT_NAME));
                        errorExists = false;
                        onMenu();
                        //Hide loader
                        ((MainActivity) getActivity()).statusBarVisibility(false);
                        ((FragmentMain) getParentFragment()).configToolbar(false, Constants.TYPE_ICONCANCEL_MENU,
                                getString(R.string.title_chat));
                        ((FragmentMain) getParentFragment()).setOnBackCallback(new OnBackCallback() {
                            @Override
                            public void onBack() {
                                onAlertCancelChat();
                            }
                        });
                        relLoader.setVisibility(View.GONE);
                        //Show layout
                        showLayout();
                        getChatEventsNext();
                    } else {
                        getChatInfo();
                    }
                   // Log.d(TAG, "Got chat events and info");
                   // Log.d(TAG, "eventsUri: " + eventsUri);
                   // Log.d(TAG, "nextUri: " + nextUri);
                } else {
                   // Log.e(TAG, "ChatResourceEventsInfo fails");
                }
            } else if (event.getApiName().equalsIgnoreCase(ChatInfo.APINAME)) {
                if (data.containsKey(ChatInfo.CHAT_STATE)) {
                    chatState = data.get(ChatInfo.CHAT_STATE);
                    if (chatState.equals("chatting")) {
                        agentName = data.get(ChatInfo.AGENT_NAME);
                      //  Log.d(TAG, "Agent name: " + agentName);
                        updateChatHeader(data.get(ChatInfo.AGENT_NAME));
                        //onMenu();
                        //Hide loader
                        ((MainActivity) getActivity()).statusBarVisibility(false);
                        ((FragmentMain) getParentFragment()).configToolbar(false, Constants.TYPE_ICONCANCEL_MENU,
                                getString(R.string.title_chat));
                        ((FragmentMain) getParentFragment()).setOnBackCallback(new OnBackCallback() {
                            @Override
                            public void onBack() {
                                onAlertCancelChat();
                            }
                        });
                        relLoader.setVisibility(View.GONE);
                        //Show layout
                        showLayout();
                        getChatEventsNext(); // now keep polling for details
                    } else if (chatState.equals("ended")) {
                      //  Log.d(TAG, "info: Chat state ended");
                        ((FragmentMain) getParentFragment()).replaceLayout(new FrontFragment(), true);
                    }  else {
                        getChatInfo();
                       // Log.d(TAG, "Still waiting for agent");
                    }
                    errorExists = false;

                } else {
                  //  Log.e(TAG, "ChatInfo fails");
                }
            } else if (event.getApiName().equalsIgnoreCase(AddChatLines.APINAME)) {
                if (data.containsKey(AddChatLines.RESP_CODE)) {
                    errorExists = false;
                } else {
                  //  Log.e(TAG, "AddChatLines fails");
                }
            } else if (event.getApiName().equalsIgnoreCase(ChatEventsNext.APINAME)) {
                if (data.containsKey(ChatEventsNext.CHAT_STATE)) {
                    String chatState = data.get(ChatEventsNext.CHAT_STATE);
                    if (chatState.equals("chatting")) {
                        nextUri = data.get(ChatEventsNext.EVENT_NEXT);
                        handleAgentChatMsg(data);
                    } else if (chatState.equals("ended")) {
                        ((FragmentMain) getParentFragment()).replaceLayout(new FrontFragment(), true);
                       // Log.d(TAG, "next: chat state ended");
                    }
                    errorExists = false;
                  //  Log.d(TAG, "Got new chat events");
                }
                getChatEventsNext();

            } else if (event.getApiName().equalsIgnoreCase(EndChat.APINAME)) {
                if (data.containsKey(EndChat.RESP_CODE)) {
                    ((FragmentMain) getParentFragment()).replaceLayout(new FrontFragment(), true);
                    errorExists = false;
                } else {
                  //  Log.e(TAG, "EndChat fails");
                }
            }
        }

        if (errorExists) {
            return;
        }

    }

}
