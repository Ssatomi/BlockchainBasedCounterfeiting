package com.dce.blockchain.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
	BlockService blockService;

	@Autowired
	BlockCache blockCache;

	@Autowired
	P2PServer p2PServer;

	@Autowired
	P2PClient p2PClient;

	@Autowired
	PowService powService;

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
					+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + JSON.toJSONString(message));
			switch (message.getType()) {
				// producer发来初始握手消息
				case BlockConstant.INIT_HANDSHAKE:
					handleInitHandshake(webSocket, message.getData());
					break;
				// producer发来开始工作消息
				case BlockConstant.WORK_START:
					handleWorkStart();
					break;
				// producer发来商品信息
				case BlockConstant.COMMODITY_INFO:
					handleCommodityInfo(message.getData());
					break;
				// 有节点已算出新区块
				case BlockConstant.CALCULATED_NEW_BLOCK:
					handleCalculatedNewBlock(message.getData());
					break;
				// producer发来新区块的仲裁结果
				case BlockConstant.DECIDED_NEW_BLOCK:
					handleDecidedNewBlockResponse(message.getData());
					break;
				// 客户端请求查询最新的区块:1
				case BlockConstant.QUERY_LATEST_BLOCK:
					write(webSocket, responseLatestBlockMsg());// 服务端调用方法返回最新区块:2
					break;
				// 接收到服务端返回的最新区块:2
				case BlockConstant.RESPONSE_LATEST_BLOCK:
					handleBlockResponse(message.getData(), sockets);
					break;
				// 客户端请求查询整个区块链:3
				case BlockConstant.QUERY_BLOCKCHAIN:
					write(webSocket, responseBlockChainMsg());// 服务端调用方法返回最新区块:4
					break;
				// 直接接收到其他节点发送的整条区块链信息:4
				case BlockConstant.RESPONSE_BLOCKCHAIN:
					handleBlockChainResponse(message.getData(), sockets);
					break;
				case BlockConstant.QUERY_COMMODITY_UPDATE:
					handleCommodityUpdateQuery(webSocket);
					break;
			}
		} catch (Exception e) {
			System.out.println("处理IP地址为：" + webSocket.getRemoteSocketAddress().getAddress().toString() + "，端口号为："
					+ webSocket.getRemoteSocketAddress().getPort() + "的p2p消息错误:" + e.getMessage());
		}
	}

	/**
	 * 处理producer发来的初始握手消息
	 * 
	 * @param webSocket
	 * @param data
	 */
	public synchronized void handleInitHandshake(WebSocket webSocket,String data){
		blockCache.addtoProducersInfoList(webSocket, data);// 保存至producer地址和公钥集合
		if(blockCache.getLatestBlock()==null){//节点尚未启动
			blockCache.setProducerMainWebsocket(webSocket);// 此producer即为主producer
			blockService.createGenesisBlock();
		}
		write(webSocket, responseIsReadyMsg());
	}

	/**
	 * 处理producer发来的开始工作消息
	 * 
	 */
	public synchronized void handleWorkStart(){
		//重置新区块正在仲裁缓存
		blockCache.getNewBlockDeciding().setIndex(0);
		powService.setTsaList(getNewTsaList());;// 每次最多取三个交易信息
		powService.exit = false;
		Thread thread = new Thread(powService);
		thread.start();//开始pow计算线程
	}

	/**
	 * 处理producer发来的商品信息
	 * 
	 * @param data
	 */
	public synchronized void handleCommodityInfo(String data){
		Transaction tsa = JSON.parseObject(data, Transaction.class);
		blockCache.addtoWaitToBlock(tsa);//空白商品不发，故此处无需判断是否为空
	}

	/**
	 * 处理其它节点发送过来的已计算出的新区块消息
	 * 
	 * @param blockData
	 */
	public synchronized void handleCalculatedNewBlock(String blockData){
		Block block = JSON.parseObject(blockData,Block.class);
		//若存在新区块正在仲裁缓存且其时间更早则无需操作
		if((blockCache.getNewBlockDeciding().getIndex()!=0) &&
			blockCache.getNewBlockDeciding().getTimestamp()<block.getTimestamp()){
				return;
		}
		blockCache.setNewBlockDeciding(block);//存入缓存
		powService.exit = true;//停止pow计算线程
		Message message = new Message(BlockConstant.CALCULATED_NEW_BLOCK);
		message.setData(blockData);
		write(blockCache.getProducerMainWebSocket(), JSON.toJSONString(message));
	}

	/**
	 * 处理producer发来的新区块仲裁结果 上链成功返回true，上链失败返回false
	 * 
	 * @param blockData
	 */
	public synchronized void handleDecidedNewBlockResponse(String blockData) {
		// 新区块仲裁结果
		Block decidedNewBlock = JSON.parseObject(blockData, Block.class);
		// 当前节点的最新区块
		Block latestBlock = blockCache.getLatestBlock();

		if (decidedNewBlock != null) {
			if (latestBlock != null) {
				if (latestBlock.getHash().equals(decidedNewBlock.getPreviousHash())) {
					blockService.addBlock(decidedNewBlock);
					System.out.println("将新区块仲裁结果加入到本地的区块链");
					blockCache.getDecidedNewBlockCache().setIndex(0);
					write(blockCache.getProducerMainWebSocket(), responseNewBlockOKMsg());
					return;
				}
			}
			blockCache.setDecidedNewBlockCache(decidedNewBlock);
			broatcast(queryBlockChainMsg());
			System.out.println("重新查询所有节点上的整条区块链");
		}
	}

	/**
	 * 处理其它节点发送过来的区块信息
	 * 
	 * @param blockData
	 * @param sockets
	 */
	public synchronized void handleBlockResponse(String blockData, List<WebSocket> sockets) {
		// 反序列化得到其它节点的最新区块信息
		Block latestBlockReceived = JSON.parseObject(blockData, Block.class);
		// 当前节点的最新区块
		Block latestBlock = blockCache.getLatestBlock();

		if (latestBlockReceived != null) {
			if (latestBlock != null) {
				// 如果接收到的区块高度比本地区块高度大的多
				if (latestBlockReceived.getIndex() > latestBlock.getIndex() + 1) {
					broatcast(queryBlockChainMsg());
					System.out.println("重新查询所有节点上的整条区块链");
				} else if (latestBlockReceived.getIndex() > latestBlock.getIndex()
						&& latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
					blockService.addBlock(latestBlockReceived);
					System.out.println("将新接收到的区块加入到本地的区块链");
				}
			} else if (latestBlock == null) {
				broatcast(queryBlockChainMsg());
				System.out.println("重新查询所有节点上的整条区块链");
			}
		}
	}

	/**
	 * 处理其它节点发送过来的区块链信息
	 * 
	 * @param blockData
	 * @param sockets
	 */
	public synchronized void handleBlockChainResponse(String blockData, List<WebSocket> sockets) {
		// 反序列化得到其它节点的整条区块链信息
		List<Block> receiveBlockchain = JSON.parseArray(blockData, Block.class);
		if (!CollectionUtils.isEmpty(receiveBlockchain) && blockService.isValidChain(receiveBlockchain)) {
			// 根据区块索引先对区块进行排序
			Collections.sort(receiveBlockchain, new Comparator<Block>() {
				public int compare(Block block1, Block block2) {
					return block1.getIndex() - block2.getIndex();
				}
			});

			// 其它节点的最新区块
			Block latestBlockReceived = receiveBlockchain.get(receiveBlockchain.size() - 1);
			// 当前节点的最新区块
			Block latestBlock = blockCache.getLatestBlock();

			if (latestBlock == null) {
				// 替换本地的区块链
				blockService.replaceChain(receiveBlockchain);
			} else {
				// 其它节点区块链如果比当前节点的长，则处理当前节点的区块链
				if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
					if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
						blockService.addBlock(latestBlockReceived);
						System.out.println("将新接收到的区块加入到本地的区块链");
					} else {
						// 用长链替换本地的短链
						blockService.replaceChain(receiveBlockchain);
					}
				}
			}
			// 处理新区块仲裁结果缓存
			if (blockCache.getDecidedNewBlockCache() != null && blockCache.getDecidedNewBlockCache().getIndex() != 0) {// 存在该缓存
				Block decidedNewBlock = blockCache.getDecidedNewBlockCache();
				if (latestBlockReceived.getHash().equals(decidedNewBlock.getPreviousHash())) {//可以上链
					blockService.addBlock(decidedNewBlock);
					System.out.println("将新接收到的区块加入到本地的区块链");
					write(blockCache.getProducerMainWebSocket(), responseNewBlockOKMsg());
					blockCache.getDecidedNewBlockCache().setIndex(0);
				} else if (latestBlockReceived.getHash().equals(decidedNewBlock.getHash())) {//已经上链
					System.out.println("将新接收到的区块加入到本地的区块链");
					write(blockCache.getProducerMainWebSocket(), responseNewBlockOKMsg());
					blockCache.getDecidedNewBlockCache().setIndex(0);
				} else {//等待一秒后重发区块链查询
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println("程序等待出现异常");
					}
					broatcast(queryBlockChainMsg());
				}
			}
		}
	}

	/**
	 * 处理producer发来的更新商品请求
	 * 
	 * @param message
	 */
	public synchronized void handleCommodityUpdateQuery(WebSocket ws){
		Message msg = new Message();
		//先发索引
		msg.setType(BlockConstant.RESPONSE_COMMODITY_INDEX);
		msg.setData(JSON.toJSONString(blockCache.getIndexForBlock()));
		write(ws, JSON.toJSONString(msg));
		//后发商品信息
		msg.setType(BlockConstant.RESPONSE_COMMODITY_DATA);
		msg.setData(JSON.toJSONString(blockCache.getBlockChain()));
		write(ws, JSON.toJSONString(msg));
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
	 * 查询整条区块链
	 * 
	 * @return
	 */
	public String queryBlockChainMsg() {
		return JSON.toJSONString(new Message(BlockConstant.QUERY_BLOCKCHAIN));
	}

	/**
	 * 返回整条区块链数据
	 * 
	 * @return
	 */
	public String responseBlockChainMsg() {
		Message msg = new Message();
		msg.setType(BlockConstant.RESPONSE_BLOCKCHAIN);
		msg.setData(JSON.toJSONString(blockCache.getBlockChain()));
		return JSON.toJSONString(msg);
	}

	/**
	 * 查询最新的区块
	 * 
	 * @return
	 */
	public String queryLatestBlockMsg() {
		return JSON.toJSONString(new Message(BlockConstant.QUERY_LATEST_BLOCK));
	}

	/**
	 * 返回最新的区块
	 * 
	 * @return
	 */
	public String responseLatestBlockMsg() {
		Message msg = new Message();
		msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
		Block b = blockCache.getLatestBlock();
		msg.setData(JSON.toJSONString(b));
		return JSON.toJSONString(msg);
	}

	/**
	 * 返回准备完毕消息
	 * 
	 * @return
	 */
	public String responseIsReadyMsg() {
		return JSON.toJSONString(new Message(BlockConstant.IS_READY));
	}

	/**
	 * 返回上链成功消息
	 * 
	 * @return
	 */
	public String responseNewBlockOKMsg() {
		return JSON.toJSONString(new Message(BlockConstant.NEW_BLOCK_OK));
	}

	public List<WebSocket> getSockets() {
		return blockCache.getSocketsList();
	}

	/**
	 * 返回尚未上链的交易信息，最多三个
	 * 
	 * @return
	 */
	public List<Transaction> getNewTsaList() {
		List<Transaction> tsaList = new ArrayList<Transaction>();
		Transaction tsa = new Transaction();
		for (int i = 0; i < 3; i++) {
			tsa = blockCache.getfromWaitToBlock();
			if (tsa == null) {
				break;
			}
			tsaList.add(tsa);
		}
		return tsaList;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		p2PServer.initP2PServer(blockCache.getP2pport());
		String addrs[] = { blockCache.getAddress(), blockCache.getAddress_2() };
		p2PClient.connectToPeer(addrs);
		System.out.println("*****难度系数******"+blockCache.getDifficulty());
		System.out.println("*****端口号******"+blockCache.getP2pport());
		for(int i=1;i<=addrs.length;i++){
			System.out.println("*****节点地址"+i+"******"+addrs[i-1]);
		}
	}
	
}
