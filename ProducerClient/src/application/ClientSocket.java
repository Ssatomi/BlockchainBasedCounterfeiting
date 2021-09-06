package application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.alibaba.fastjson.JSON;

import org.java_websocket.WebSocket;

import application.CtrlClient;
import application.module.BlockConstant;
import application.module.Message;
import application.module.Transaction;
import application.module.TransactionForShow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientSocket {
	//producer地址
	private String addr = "ws://10.21.203.229:7000";
	//WebSocket对象，保存与producer的连接
	private WebSocket socketProducer;
	//CtrlClient对象，用于更新界面
	private CtrlClient ctrlClient;
	//更新界面用的数据结构
	ObservableList<TransactionForShow> uploaded_table = FXCollections.observableArrayList();
	
	//处理producer发来的已上传商品信息函数
	//渲染至界面
	private void handleMessage(String msg) {
		//更新数据结构
		uploaded_table.clear();
		//获得商品信息
		Message message = JSON.parseObject(msg,Message.class);
		if(message.getType() != BlockConstant.RESPONSE_SUCCESSED_COMMODITIES) {
			return;
		}
		List<Transaction> transactions = JSON.parseArray(message.getData(),Transaction.class);
		for(Transaction transaction : transactions) {
			uploaded_table.add(new TransactionForShow(transaction));
		}
		//渲染
		ctrlClient.get_table_uploaded().setItems(uploaded_table);
	}
	
	//初始化函数，初始化连接
	public void InitAll(CtrlClient ctrlClient) throws URISyntaxException{
		this.ctrlClient = ctrlClient;
		//WebSocketClient对象，连接producer
		try {
			final WebSocketClient socketClient= new WebSocketClient(new URI(addr)) {
				
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("ProducerClient socketClient connected");
					socketProducer = this;
				}
				
				@Override
				public void onMessage(String msg) {
					//收到producer发来的已成功商品信息
					handleMessage(msg);
				}
				
				@Override
				public void onError(Exception e) {
					System.out.println("socketClient error");
				}
				
				@Override
				public void onClose(int i, String msg, boolean b) {
					System.out.println("socketClient closed");
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("socketClient failed");
		}
	}
	
	//商品信息发送函数
	public void SendCommodity(String message) {
		socketProducer.send(message);
		System.out.println("commodity send success");
	}
	
	//已成功商品信息请求函数
	public void QuerySuccessedCommodities() {
		Message message = new Message(BlockConstant.QUERY_SUCCESSED_COMMODITIES);
		socketProducer.send(JSON.toJSONString(message));
		System.out.println("query successed commodities");
	}
}













