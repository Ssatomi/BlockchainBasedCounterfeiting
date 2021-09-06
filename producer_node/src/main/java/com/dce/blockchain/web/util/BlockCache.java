package com.dce.blockchain.web.util;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.dce.blockchain.web.model.Block;
import com.dce.blockchain.web.model.TQueue;
import com.dce.blockchain.web.model.Transaction;

@ConfigurationProperties(prefix = "block")
@Component
public class BlockCache {
	
	/**
	 * 当前producer的节点的socket对象
	 */
	private List<WebSocket> socketsList = new ArrayList<WebSocket>();

	/**
	 * 当前producer的客户端的socket对象
	 */
	private List<WebSocket> clientSocketsList = new ArrayList<WebSocket>();

	/**
	 * 等待发至节点的商品队列
	 */
	private TQueue waittingCommodities = new TQueue(50);

	/**
	 * 已成功上链的商品列表
	 */
	private List<Transaction> successedCommodities = new ArrayList<Transaction>();

	/**
	 * producer的公钥
	 */
	private String publicKey = new String("");

	/**
	 * 正在验证的商品的缓存
	 */
	private Transaction verifyingCommodity;

	/**
	 * 正在验证的商品的数字签名原文
	 */
	private String originalText = new String("");

	/**
	 * 所有节点是否已准备标志
	 */
	private boolean isReady = false;

	/**
	 * 统计用的数字
	 */
	private int forStatistics = 0;

	/**
	 * 上链成功统计用的数字
	 */
	private int forOKStatistics = 0;

	/**
	 * 新区块缓存
	 */
	private Block newBlockCache = new Block();

	/**
	 * 新节点socket列表缓存
	 */
	private List<WebSocket> newSocketsListCache = new ArrayList<WebSocket>();

	/**
	 * 商品码与区块位置的索引缓存
	 */
	private Map<String,String> indexForBlockCache = new HashMap<String,String>();

	/**
	 * 区块链缓存
	 */
	private List<Block> blockChainCache = new ArrayList<Block>();

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
	 * 要连接的节点地址_3
	 */
	@Value("${block.address_3}")
	private String address_3;


	public List<WebSocket> getSocketsList() {
		return socketsList;
	}

	public void setSocketsList(List<WebSocket> socketsList) {
		this.socketsList = socketsList;
	}

	public List<WebSocket> getClientSocketsList(){
		return clientSocketsList;
	}

	public void setClientSocketsList(List<WebSocket> clientSocketsList){
		this.clientSocketsList = clientSocketsList;
	}

	public void addtoWaittingCommodities(Transaction Transaction){
		waittingCommodities.enQueue(Transaction);
	}

	public Transaction getfromWaittingCommodities(){
		return waittingCommodities.deQueue();
	}

	public List<Transaction> getSuccessedCommodities() {
		return successedCommodities;
	}

	public void setSuccessedCommodities(List<Transaction> successedCommodities) {
		this.successedCommodities = successedCommodities;
	}

	public String getPublicKey(){
		return publicKey;
	}
	
	public void setPublicKey(String publicKey){
		this.publicKey = publicKey;
	}

	public Transaction getVerifyingCommodity(){
		return verifyingCommodity;
	}
	
	public void setVerifyingCommodity(Transaction verifyingCommodity){
		this.verifyingCommodity = verifyingCommodity;
	}

	public String getOriginalText(){
		return originalText;
	}
	
	public void setOriginalText(String originalText){
		this.originalText = originalText;
	}

	public boolean getIsReady(){
		return isReady;
	}

	public void setIsReady(boolean isReady){
		this.isReady = isReady;
	}

	public int getForStatistics(){
		return forStatistics;
	}

	public void addOneForStatistics(){
		forStatistics += 1;
	}

	public void setZeroForStatistics(){
		forStatistics = 0;
	}

	public int getForOKStatistics(){
		return forOKStatistics;
	}

	public void addOneForOKStatistics(){
		forOKStatistics += 1;
	}

	public void setZeroForOKStatistics(){
		forOKStatistics = 0;
	}

	public Block getNewBlockCache(){
		return newBlockCache;
	}

	public void setNewBlockCahce(Block newBlockCache){
		this.newBlockCache = newBlockCache;
	}
	
	public List<WebSocket> getNewSocketsListCache() {
		return newSocketsListCache;
	}

	public void setNewSocketsListCahce(List<WebSocket> newSocketsListCache) {
		this.newSocketsListCache = newSocketsListCache;
	}

	public Map<String,String> getIndexForBlockCache() {
		return indexForBlockCache;
	}

	public void setIndexForBlockCahce(Map<String,String> indexForBlockCache) {
		this.indexForBlockCache = indexForBlockCache;
	}

	public List<Block> getBlockChainCache() {
		return blockChainCache;
	}

	public void setBlockChainCahce(List<Block> blockChainCache) {
		this.blockChainCache = blockChainCache;
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

	public String getAddress_3() {
		return address_3;
	}

	public void setAddress_3(String address_3) {
		this.address_3 = address_3;
	}
}
