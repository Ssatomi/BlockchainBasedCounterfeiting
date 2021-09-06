package com.dce.blockchain.web.service;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.model.Message;
import com.dce.blockchain.web.model.Transaction;
import com.dce.blockchain.web.util.BlockCache;
import com.dce.blockchain.web.util.BlockConstant;

/**
 * 已成功商品信息更新
 * 定时从一个随机节点获取商品信息并更新已成功商品列表
 *
 */
@DependsOn(value = "p2PService")
@Service
public class CommodityUpdateService {

	@Autowired
	BlockCache blockCache;
	
	@Autowired
	P2PService p2PService;

	/**
	 * 定时向一个随机节点发出商品信息请求
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	@Async("taskExecutor")
	public CompletableFuture<String> query() throws InterruptedException{
		
		while(true){
			try {//等待5分钟
				Thread.sleep(300000);
			} catch (Exception e) {
				p2PService.outputForThread(8);
			}
            //随机节点
			int random = (int)(System.currentTimeMillis()%(blockCache.getSocketsList().size()));
			WebSocket ws = blockCache.getSocketsList().get(random);
            //发出更新请求
            Message message = new Message(BlockConstant.QUERY_COMMODITY_UPDATE);
			p2PService.write(ws, JSON.toJSONString(message));
		}
	}
	
	/**
	 * 收到节点发来的商品信息后触发，进行更新
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	@Async("taskExecutor")
	public CompletableFuture<String> update() throws InterruptedException{
		int index;
		String tempString;
		Transaction transaction = new Transaction();
		//循环遍历已成功商品列表
		for(int i=0;i<blockCache.getSuccessedCommodities().size();i++){
			transaction = blockCache.getSuccessedCommodities().get(i);
			//获得索引
			tempString = blockCache.getIndexForBlockCache().get(transaction.getId());
			//节点发来的信息中不存在该商品
			if(tempString == null){
				continue;
			}
			index = Integer.parseInt(tempString);
			for(Transaction tsa : blockCache.getBlockChainCache().get(index).getTransactions()){
				if(tsa.getId().equals(transaction.getId())){
					//更新商品信息
					blockCache.getSuccessedCommodities().set(i, tsa);
					break;
				}
			}
		}
		return CompletableFuture.completedFuture("");
	}

}
