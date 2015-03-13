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
	//判断输入法是否弹出
	private int heightDiff = 0;
	//用来表示会话的数据集合
	private static LinkedList<Map<String, Object>> listItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_chatlist);
		
		//获取xml控件
		chatListView = (ListView) findViewById(R.id.listview_chatlist_listview);
		
		listItems = new LinkedList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("content", "Hi，我是图灵机器人！");
		//map1.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.robotimg));
		map1.put("type", "0");
		listItems.add(map1);
		
		/*
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("content", "飞洒发送发生独孤给豆腐干豆腐大幅撒旦法机");
		map2.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		map2.put("type", "1");
		listItems.add(map2);
		*/
		
		
		//构建并设置适配器
		chatListAdapter = new ChatListViewAdapter(getApplicationContext(), listItems);
		chatListView.setAdapter(chatListAdapter);
		
		//ListView信息显示最底部
		chatListView.setSelection(listItems.size() - 1);
		
		//对根布局进行监听，判断软键盘是否弹出，显示ListView的底部
		final View rootView = findViewById(R.id.listview_chatlist_rootRelativeLayout);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {	
			@Override
			public void onGlobalLayout() {
				//比较activity根布局和当前布局的大小
				heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
				if (heightDiff > 100) {
					chatListView.setSelection(chatListView.getBottom());
				}
			}
		});
		
		//对ListView触摸监听，隐藏软键盘
		chatListView.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					//判断软键盘是否弹出
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
	 * 发送信息的按钮
	 */
	public void sendMessageBtn(View v) {
		EditText editText = (EditText) findViewById(R.id.listview_chatlist_edittext);
		final String msg = editText.getText().toString().trim();
		
		//发送内容不能为空
		if (!msg.equals("")) {
			//添加信息到listview
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("content", msg);
			//map.put("headimg", BitmapFactory.decodeResource(getResources(), R.drawable.userimg));
			map.put("type", "1");
			listItems.add(map);
			chatListAdapter.notifyDataSetChanged();
			editText.setText(null);
			
			//新建线程来获取图灵机器人回复
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
						
						//取得输入流，并使用Reader读取
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
						StringBuffer sb = new StringBuffer();
						String line = "";
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
						reader.close();
						//断开连接
						connection.disconnect();
						
						//发送获取的信息给UI主线程
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
	 * 更新主线程UI
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				try {
					//把Json字符串转化为json对象
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
					Toast.makeText(getApplicationContext(), "JSON解析出错！", Toast.LENGTH_SHORT).show();
				}
			}
			else if (msg.what == 110) {
				Toast.makeText(getApplicationContext(), "获取失败！", Toast.LENGTH_SHORT).show();
			}
		}
	};
}
