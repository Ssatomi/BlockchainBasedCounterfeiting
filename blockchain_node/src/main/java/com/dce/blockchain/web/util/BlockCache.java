package com.dce.blockchain.web.util;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.dce.blockchain.web.model.Block;
import com.dce.blockchain.web.model.Transaction;
import com.dce.blockchain.web.model.TQueue;

@ConfigurationProperties(prefix = "block")
@Component
public class BlockCache {

	/**
	 * 当前节点的区块链结构
	 */
	private List<Block> blockChain = new ArrayList<Block>();

	/**
	 * 等待上链的商品信息队列
	 */
	private TQueue waitToBlock = new TQueue(50);

	/**
	 * 商品码查询区块链中对应区块的索引
	 */
	private Map<String,String> indexForBlock = new HashMap<String,String>();
	
	/**
	 * 当前节点的socket对象
	 */
	private List<WebSocket> socketsList = new ArrayList<WebSocket>();

	/**
	 * 当前节点的producer对象的websocket地址及其公钥
	 */
	private Map<WebSocket,String> producers_Info_List = new HashMap<WebSocket,String>();

	/**
	 * 当前节点的主producer对象的websocket地址
	 */
	private WebSocket producerMainWebsocket;

	/**
	 * 正在共识的新区块缓存
	 */
	private Block newBlockDeciding = new Block();

	/**
	 * 新区块共识结果缓存
	 */
	private Block decidedNewBlockCache = new Block();
	
	/**
	 * 挖矿的难度系数
	 */
	@Value("${block.difficulty}")
	private int difficulty;
	
	/**
	 * 当前节点p2pserver端口号
	 */
	@Value("${block.p2pport}")
	private int p2pport;
	
	/**
	 * 要连接的节点地址
	 */
	@Value("${block.address}")
	private String address;
	
	/**
	 * 要连接的节点地址_2
	 */
	@Value("${block.address_2}")
	private String address_2;

	/**
	 * 获取最新的区块，即当前链上最后一个区块
	 * 
	 * @return
	 */
	public Block getLatestBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}

	public List<Block> getBlockChain() {
		return blockChain;
	}

	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}

	public void addtoWaitToBlock(Transaction Transaction){
		waitToBlock.enQueue(Transaction);
	}

	public Transaction getfromWaitToBlock(){
		return waitToBlock.deQueue();
	}

	public Map<String,String> getIndexForBlock(){
		return this.indexForBlock;
	}

	public void addtoIndexForBlock(String key,int index){
		indexForBlock.put(key, Integer.toString(index));
	}

	public int queryfromIndexForBlock(String key){
		String index = null;
		index = indexForBlock.get(key);
		if(index==null){
			return 0;
		}
		return Integer.parseInt(index);
	}

	public Block getNewBlockDeciding(){
		return newBlockDeciding;
	}

	public void setNewBlockDeciding(Block newBlockDeciding){
		this.newBlockDeciding = newBlockDeciding;
	}

	public Block getDecidedNewBlockCache(){
		return decidedNewBlockCache;
	}

	public void setDecidedNewBlockCache(Block decidedNewBlockCache){
		this.decidedNewBlockCache = decidedNewBlockCache;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public List<WebSocket> getSocketsList() {
		return socketsList;
	}

	public void setSocketsList(List<WebSocket> socketsList) {
		this.socketsList = socketsList;
	}

	public void addtoProducersInfoList(WebSocket producerWebsocket,String public_key) {
		producers_Info_List.put(producerWebsocket, public_key);
	}

	public String queryfromProducersInfoList(WebSocket producerWebsocket) {
		return producers_Info_List.get(producerWebsocket);
	}

	public WebSocket getProducerMainWebSocket(){
		return producerMainWebsocket;
	}

	public void setProducerMainWebsocket(WebSocket producerMainWebsocket){
		this.producerMainWebsocket = producerMainWebsocket;
	}

	public int getP2pport() {
		return p2pport;
	}

	public void setP2pport(int p2pport) {
		this.p2pport = p2pport;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress_2() {
		return address_2;
	}

	public void setAddress_2(String address_2) {
		this.address_2 = address_2;
	}
}
