package com.dce.blockchain.web.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.model.Block;
import com.dce.blockchain.web.model.Message;
import com.dce.blockchain.web.model.Transaction;
import com.dce.blockchain.web.util.BlockCache;
import com.dce.blockchain.web.util.BlockConstant;
import com.dce.blockchain.websocket.P2PClient;
import com.dce.blockchain.websocket.P2PServer;

/**
 * p2p网络服务类
 * 
 *
 */
@Service
public class P2PService implements ApplicationRunner {

	@Autowired
	BlockCache blockCache;

	@Autowired
	P2PServer p2PServer;

	@Autowired
	P2PClient p2PClient;

	@Autowired
	CommodityTranService commodityTranService;

	@Autowired
	NodeCountService nodeCountService;

	@Autowired
	CommodityUpdateService commodityUpdateService;

	/**
	 * 客户端和服务端共用的消息处理方法
	 * 
	 * @param webSocket
	 * @param msg
	 * @param sockets
	 */
	public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
		try {
			Message message = JSON.parseObject(msg, Message.class);
			System.out.println("接收到IP地址为：" + webSocket.getRemoteSocketAddress().getAddress().toString() + "，端口号为："
					+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + msg);
			switch (message.getType()) {
				//节点发来准备完毕消息
				case BlockConstant.IS_READY:
					handleIsReady();
					break;
				// 有节点已算出新区块
				case BlockConstant.CALCULATED_NEW_BLOCK:
					handleCalculatedNewBlock(webSocket,message.getData());
					break;
				//节点发来的新区块已上链消息
				case BlockConstant.NEW_BLOCK_OK:
					handleNewBlockOK();
					break;
				//客户端发来的商品信息
				case BlockConstant.COMMODITY_UPLOAD:
					handleCommodityUpload(message.getData());
					break;
				//客户端请求查询已成功商品列表
				case BlockConstant.QUERY_SUCCESSED_COMMODITIES:
					write(webSocket, responseSuccessedCommodities());
					break;
				//消费者客户端请求验证商品信息
				case BlockConstant.COMMODITY_VERIFY:
					handleCommodityVerify(webSocket, message.getData());
					break;
				//消费者客户端发来数字签名
				case BlockConstant.RESPONSE_DIGI_SIG:
					handleDigiSigResponse(webSocket, message.getData());
					break;
				//节点发来商品索引用于更新
				case BlockConstant.RESPONSE_COMMODITY_INDEX:
					handleCommodityIndexResponse(message.getData());
					break;
				//节点发来商品信息用于更新
				case BlockConstant.RESPONSE_COMMODITY_DATA:
					handleCommodityDataResponse(message.getData());
					break;
			}
		} catch (Exception e) {
			System.out.println("处理IP地址为：" + webSocket.getRemoteSocketAddress().getAddress().toString() + "，端口号为："
					+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息错误:" + e.getMessage());
		}
	}

	/**
	 * 处理其它节点发送过来的已准备消息
	 */
	public synchronized void handleIsReady(){
		blockCache.addOneForStatistics();//统计数加一
		if(blockCache.getForStatistics() == blockCache.getSocketsList().size()){//所有节点均准备完毕
			blockCache.setIsReady(true);//所有节点准备
			blockCache.setZeroForStatistics();//统计数清零
			broatcast(workStart());//开始工作
		}
	}

	/**
	 * 处理其它节点发送过来的已计算出的新区块消息
	 * 
	 * @param blockData
	 */
	public synchronized void handleCalculatedNewBlock(WebSocket webSocket,String blockData) throws Exception{
		System.out.println("statistics in p2p: "+blockCache.getForStatistics());
		Block block = JSON.parseObject(blockData,Block.class);
		//开始统计
		if(blockCache.getForStatistics() == 0){
			//依然是前一个区块则不予处理
			if(block.getIndex() == blockCache.getNewBlockCache().getIndex()){
				System.out.println("in handlecaculatednewblock 1");
				return;
			}
			System.out.println("in handlecaculatednewblock 2");
			//保存新区块缓存
			blockCache.setNewBlockCahce(block);
			//保存新节点列表缓存以备后续替换
			blockCache.getNewSocketsListCache().add(webSocket);
			//统计数加一
			blockCache.addOneForStatistics();
			//创建统计线程
			nodeCountService.run();
		}
		else{
			//新区块信息错误
			if(!block.getHash().equals(blockCache.getNewBlockCache().getHash())){
				System.out.println("in handlecaculatednewblock 3");
				return;
			}
			//新区块时间比缓存的新区块要晚
			if(block.getTimestamp() > blockCache.getNewBlockCache().getTimestamp()){
				System.out.println("in handlecaculatednewblock 4");
				return;
			}
			//新区块时间比缓存的新区块要早
			//替换缓存，重新开始统计
			if((block.getTimestamp() < blockCache.getNewBlockCache().getTimestamp()) &&
				(blockCache.getNewBlockCache().getTimestamp()!=0)){
				System.out.println("in handlecaculatednewblock 5");
				blockCache.setNewBlockCahce(block);
				blockCache.getNewSocketsListCache().clear();
				blockCache.getNewSocketsListCache().add(webSocket);
				blockCache.setZeroForStatistics();
				blockCache.addOneForStatistics();
				return;
			}
			System.out.println("in handlecaculatednewblock 6");
			//新区块时间等于缓存的新区块时间
			//收到后续该消息，保存新节点列表缓存
			blockCache.getNewSocketsListCache().add(webSocket);
			//统计数加一
			blockCache.addOneForStatistics();
		}
	}

	/**
	 * 处理其它节点发送过来的新区块上链成功消息
	 */
	public synchronized void handleNewBlockOK(){
		blockCache.addOneForOKStatistics();//统计数加一
		if(blockCache.getForOKStatistics() == blockCache.getSocketsList().size()){//所有节点均上链成功
			blockCache.setZeroForOKStatistics();//统计数清零
			broatcast(workStart());//开始工作
		}
	}

	/**
	 * 处理客户端发来的商品信息
	 * 
	 * @param data
	 */
	public synchronized void handleCommodityUpload(String data){
		operateWaittingCommodities(0, data);
	}

	public String responseSuccessedCommodities(){
		Message msg = new Message();
		msg.setType(BlockConstant.RESPONSE_SUCCESSED_COMMODITIES);
		msg.setData(operateSuccessedCommodities(1));
		return JSON.toJSONString(msg);
	}

	/**
	 * 处理消费者客户端发来的商品验证信息
	 * 
	 * @param webSocket
	 * @param commodity_id
	 */
	public synchronized void handleCommodityVerify(WebSocket webSocket,String commodity_id){
		Message message = new Message();
		String originalText = new String("");
		//查询该商品是否已成功上链
		boolean flag = false;
		for(Transaction transaction : blockCache.getSuccessedCommodities()){
			if(transaction.getId().equals(commodity_id)){
				flag = true;
				blockCache.setVerifyingCommodity(transaction);;//商品信息缓存
				//获得数字签名原文并缓存
				originalText = Long.toString(System.currentTimeMillis());
				blockCache.setOriginalText(originalText);
				System.out.println("Original Text:"+originalText);//输出，方便测试
				break;
			}
		}
		//该商品已成功上链则进行下一步，请求数字签名
		if(flag){
			//生成消息并发送
			message.setType(BlockConstant.QUERY_DIGI_SIG);
			message.setData(blockCache.getOriginalText());
			write(webSocket, JSON.toJSONString(message));
		}
		//区块链上不存在该商品则直接发送假货结果
		else{
			message.setType(BlockConstant.COMMODITY_VERIFY_RESULT);
			message.setData("0");//0代表为假货
			write(webSocket, JSON.toJSONString(message));
		}
	}

	/**
	 * 处理消费者客户端发来的数字签名
	 * 
	 * @param webSocket
	 * @param digi_sig
	 */
	public void handleDigiSigResponse(WebSocket webSocket,String digi_sig){
		Message message = new Message();
		message.setType(BlockConstant.COMMODITY_VERIFY_RESULT);
		//数字签名符合要求则返回真货结果
		//此处省略RSA解密，简化数字签名验证过程
		if(blockCache.getOriginalText().equals(digi_sig)){
			//发送真货结果
			message.setData("1");//1代表真货
			write(webSocket, JSON.toJSONString(message));
			//发送商品的全部信息
			message.setType(BlockConstant.COMMODITY_INFO);
			message.setData(JSON.toJSONString(blockCache.getVerifyingCommodity()));
			write(webSocket, JSON.toJSONString(message));
		}
		//数字签名不符合要求则返回假货结果
		else{
			message.setData("0");//0代表假货
			write(webSocket, JSON.toJSONString(message));
		}
	}

	/**
	 * 处理节点返回的用于更新的索引信息
	 * 
	 * @param data
	 */
	public void handleCommodityIndexResponse(String data){
		//存入缓存
		blockCache.setIndexForBlockCahce(JSON.parseObject(data, blockCache.getIndexForBlockCache().getClass()));
	}

	/**
	 * 处理节点返回的用于更新的商品信息
	 * 
	 * @param data
	 */
	public void handleCommodityDataResponse(String data){
		//存入缓存并调用线程处理
		List<Block> receiveBlockchain = JSON.parseArray(data, Block.class);
		if(!CollectionUtils.isEmpty(receiveBlockchain)){
			//根据索引大小排序
			Collections.sort(receiveBlockchain, new Comparator<Block>() {
				public int compare(Block block1, Block block2) {
					return block1.getIndex() - block2.getIndex();
				}
			});
			//存入缓存
			blockCache.setBlockChainCahce(receiveBlockchain);
			//更新商品信息
			try {
				commodityUpdateService.update();
			} catch (Exception e) {
				System.out.println("CommodityUpdate error");
			}
		}
	}

	/**
	 * 同步操作waitingCommodities的数据
	 * 
	 * @param operation
	 * @param tsaData
	 */
	public synchronized Transaction operateWaittingCommodities(int operation,String tsaData){
		//0为存入
		if(operation == 0){
			Transaction tsa = JSON.parseObject(tsaData,Transaction.class);
			blockCache.addtoWaittingCommodities(tsa);
			return tsa;
		}
		//1为取出
		else if(operation == 1){
			return blockCache.getfromWaittingCommodities();
		}
		return null;
	}

	/**
	 * 同步操作successedCommodities的数据
	 * 
	 * @param operation
	 */
	public synchronized String operateSuccessedCommodities(int operation){
		//0为存入
		if(operation == 0){
			for(Transaction transaction : blockCache.getNewBlockCache().getTransactions()){
				blockCache.getSuccessedCommodities().add(transaction);
			}
			return null;
		}
		//1为取出
		else if(operation == 1){
			return JSON.toJSONString(blockCache.getSuccessedCommodities());
		}
		return null;
	}

	/**
	 * 全网广播消息
	 * 
	 * @param message
	 */
	public void broatcast(String message) {
		List<WebSocket> socketsList = this.getSockets();
		if (CollectionUtils.isEmpty(socketsList)) {
			return;
		}
		System.out.println("======全网广播消息开始：");
		for (WebSocket socket : socketsList) {
			this.write(socket, message);
		}
		System.out.println("======全网广播消息结束");
	}

	/**
	 * 向其它节点发送消息
	 * 
	 * @param ws
	 * @param message
	 */
	public void write(WebSocket ws, String message) {
		System.out.println("发送给IP地址为：" + ws.getRemoteSocketAddress().getAddress().toString() + "，端口号为："
				+ ws.getRemoteSocketAddress().getPort() + " 的p2p消息:" + message);
		ws.send(message);
	}

	/**
	 * 发出初始握手消息
	 * 
	 * @return
	 */
	public String initHandShake(){
		Message msg = new Message();
		msg.setType(BlockConstant.INIT_HANDSHAKE);
		msg.setData(blockCache.getPublicKey());
		return JSON.toJSONString(msg);
	}

	/**
	 * 发出开始工作消息
	 * 
	 * @return
	 */
	public String workStart(){
		return JSON.toJSONString(new Message(BlockConstant.WORK_START));
	}

	public List<WebSocket> getSockets() {
		return blockCache.getSocketsList();
	}

	public List<WebSocket> getClientSockets(){
		return blockCache.getClientSocketsList();
	}
	
	/**
	 * 用于线程输出，利于debug
	 */
	public void outputForThread(int choose){
		switch (choose) {
			case 1:
				System.out.println("NodeCountService handleSuccess 1");
				break;
			case 2:
				System.out.println("NodeCountService handleSuccess 2");
				break;
			case 3:
				System.out.println("NodeCountService handleFailure");
				break;
			case 4:
				System.out.println("NodeCountService Wait");
				break;
			case 5:
				System.out.println("NodeCountService Sleep Error");
				break;	
			case 6:
				System.out.println("CommodityTranService error");
				break;
			case 7:
				System.out.println("CommodityTranService broadcast");
				break;
			case 8:
				System.out.println("CommodityUpdateService error");
				break;
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		p2PServer.initP2PServer(blockCache.getP2pport());
		String addrs[] = { blockCache.getAddress(), blockCache.getAddress_2(), blockCache.getAddress_3()};
		p2PClient.connectToPeer(addrs);
		System.out.println("*****producer端口号******"+blockCache.getP2pport());
		for(int i=1;i<=addrs.length;i++){
			System.out.println("*****节点地址"+i+"******"+addrs[i-1]);
		}
		//开启商品信息传输线程
		commodityTranService.run();
		//开启已成功商品信息更新线程
		commodityUpdateService.query();

	}
	
}
