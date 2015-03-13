package com.example.chatrobot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ChatListActivity extends Activity {
	private ListView chatListView;
	private ChatListViewAdapter chatListAdapter;
	//�ж����뷨�Ƿ񵯳�
	private int heightDiff = 0;
	//������ʾ�Ự�����ݼ���
	private static LinkedList<Map<String, Object>> listItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_chatlist);
		
		//��ȡxml�ؼ�
		chatListView = (ListView) findViewById(R.id.listview_chatlist_listview);
		
		listItems = new LinkedList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("content", "Hi������ͼ������ˣ�");
		//map1.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.robotimg));
		map1.put("type", "0");
		listItems.add(map1);
		
		/*
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("content", "�������ͷ������¸������ɶ��������������");
		map2.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		map2.put("type", "1");
		listItems.add(map2);
		*/
		
		
		//����������������
		chatListAdapter = new ChatListViewAdapter(getApplicationContext(), listItems);
		chatListView.setAdapter(chatListAdapter);
		
		//ListView��Ϣ��ʾ��ײ�
		chatListView.setSelection(listItems.size() - 1);
		
		//�Ը����ֽ��м������ж�������Ƿ񵯳�����ʾListView�ĵײ�
		final View rootView = findViewById(R.id.listview_chatlist_rootRelativeLayout);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {	
			@Override
			public void onGlobalLayout() {
				//�Ƚ�activity�����ֺ͵�ǰ���ֵĴ�С
				heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
				if (heightDiff > 100) {
					chatListView.setSelection(chatListView.getBottom());
				}
			}
		});
		
		//��ListView�������������������
		chatListView.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					//�ж�������Ƿ񵯳�
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
					if (heightDiff > 100) {
						imm.hideSoftInputFromWindow(findViewById(R.id.listview_chatlist_edittext).getWindowToken(), 0);
					}
				}
				return false;
			}
		});
	}
	
	/*
	 * ������Ϣ�İ�ť
	 */
	public void sendMessageBtn(View v) {
		EditText editText = (EditText) findViewById(R.id.listview_chatlist_edittext);
		final String msg = editText.getText().toString().trim();
		
		//�������ݲ���Ϊ��
		if (!msg.equals("")) {
			//�����Ϣ��listview
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("content", msg);
			//map.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.userimg));
			map.put("type", "1");
			listItems.add(map);
			chatListAdapter.notifyDataSetChanged();
			editText.setText(null);
			
			//�½��߳�����ȡͼ������˻ظ�
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String APIKEY = "59b2528583ddbb3cb23e5604e4742373";
						String INFO = URLEncoder.encode(msg, "utf-8");
						
						String getURL = "http://www.tuling123.com/openapi/api?key=" + APIKEY + "&info=" + INFO; 
						URL getUrl = new URL(getURL);
						HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
						connection.connect();
						
						//ȡ������������ʹ��Reader��ȡ
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
						StringBuffer sb = new StringBuffer();
						String line = "";
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
						reader.close();
						//�Ͽ�����
						connection.disconnect();
						
						//���ͻ�ȡ����Ϣ��UI���߳�
						Message message = new Message();
						message.obj = sb.toString();
						message.what = 0;
						handler.sendMessage(message);
					} catch (Exception e) {
						handler.sendEmptyMessage(110);
					}
				}
			}).start();
		}
	}
	
	/*
	 * �������߳�UI
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				try {
					//��Json�ַ���ת��Ϊjson����
					JSONTokener tokener = new JSONTokener((String) msg.obj);
					JSONObject jobj = (JSONObject) tokener.nextValue();
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("content", jobj.getString("text"));
					//map.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.robotimg));
					map.put("type", "0");
					listItems.add(map);
					chatListAdapter.notifyDataSetChanged();
				}
				catch (Exception e) {
					Toast.makeText(getApplicationContext(), "JSON��������", Toast.LENGTH_SHORT).show();
				}
			}
			else if (msg.what == 110) {
				Toast.makeText(getApplicationContext(), "��ȡʧ�ܣ�", Toast.LENGTH_SHORT).show();
			}
		}
	};
}
