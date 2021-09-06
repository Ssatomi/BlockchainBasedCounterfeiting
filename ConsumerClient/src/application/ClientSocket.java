package application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	//验证状态，用于判断是否接收商品信息
	//0为不接收，1为接收
	private int status = 0;
	//producer地址
	private String addr = "ws://10.21.203.229:7000";
	//WebSocket对象，保存与producer的连接
	private WebSocket socketProducer;
	//CtrlClient对象，用于更新界面
	private CtrlClient ctrlClient;
	
	//处理producer发来的消息
	private void handleMessage(String msg) {
		Message message = JSON.parseObject(msg,Message.class);
		//请求数字签名
		if(message.getType() == BlockConstant.QUERY_DIGI_SIG) {
    		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
         		"请根据验证原文获取验证密文，输入完毕后再次点击验证按钮",
         		"提示", JOptionPane.INFORMATION_MESSAGE);
    		//显示验证原文
    		System.out.println("original text: "+message.getData());
    		ctrlClient.getTxt_orig_text().setText(message.getData());
    		//进入下一阶段
    		ctrlClient.verifyStatusSetOne();
		}
		//验证结果
		else if(message.getType() == BlockConstant.COMMODITY_VERIFY_RESULT) {
			//重置阶段
			ctrlClient.verifyStatusSetZero();
			//商品为真
			if(message.getData().equals("1")) {
	    		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
	             	"您验证的商品为正品！商品所有信息稍后显示",
	             	"结果", JOptionPane.INFORMATION_MESSAGE);
	    		//设置接收商品信息
	    		status = 1;
			}
			else if(message.getData().equals("0")){
	    		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
	             	"您验证的商品为赝品！",
	             	"结果", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		//商品信息
		else if(message.getType() == BlockConstant.COMMODITY_INFO) {
			//接收商品信息并显示
			if(status  == 1) {
				System.out.println("transaction: "+message.getData());
				ctrlClient.commodityInfoShow(JSON.parseObject(message.getData(),Transaction.class));
				//设置不接收商品信息
				status = 0;
			}
		}
	}
	
	//初始化函数，初始化连接
	public void InitAll(CtrlClient ctrlClient) throws URISyntaxException{
		this.ctrlClient = ctrlClient;
		//WebSocketClient对象，连接producer
		try {
			final WebSocketClient socketClient= new WebSocketClient(new URI(addr)) {
				
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("ConsumerClient socketClient connected");
					socketProducer = this;
				}
				
				@Override
				public void onMessage(String msg) {
					//收到producer发来的消息
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
	
	//验证商品请求函数
	public void verifyCommodity(String message) {
		socketProducer.send(message);
		System.out.println("verify first success");
	}
	
	//数字签名密文发送函数
	public void digitalSigSend(String message) {
		socketProducer.send(message);
		System.out.println("verify second success");
	}
}













