package com.dce.blockchain.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dce.blockchain.web.service.P2PService;

/**
 * p2p客户端
 * 
 *
 */
@Component
public class P2PClient {
	
	@Autowired
	P2PService p2pService;

	public void connectToPeer(String[] addrs) {
				int times=addrs.length;
				final WebSocketClient socketClients[] = new WebSocketClient[times];
				for(int i=0;i<times;i++)
		{	
			try {
					socketClients[i] = new WebSocketClient(new URI(addrs[i])){
						
					@Override
					public void onOpen(ServerHandshake serverHandshake) {
						//客户端发送请求，查询最新区块
						p2pService.write(this, p2pService.initHandShake());
						p2pService.getSockets().add(this);
					}

					/**
					 * 接收到消息时触发
					 * @param msg
					 */
					@Override
					public void onMessage(String msg) {
						p2pService.handleMessage(this, msg, p2pService.getSockets());
					}

					@Override
					public void onClose(int i, String msg, boolean b) {
						p2pService.getSockets().remove(this);
						System.out.println("connection closed");
					}

					@Override
					public void onError(Exception e) {
						p2pService.getSockets().remove(this);
						System.out.println("connection failed");
					}
				};
				socketClients[i].connect();
			} catch (URISyntaxException e) {
				System.out.println("p2p connect is error:" + e.getMessage());
			}
		}
	}

}
