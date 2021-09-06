package com.dce.blockchain.web.service;

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
 * 商品信息传输至节点
 * 定时从已有商品信息中选取一个商品信息传输至所有节点
 *
 */
@DependsOn(value = "p2PService")
@Service
public class CommodityTranService {

	@Autowired
	BlockCache blockCache;
	
	@Autowired
	P2PService p2PService;

	Transaction transaction = new Transaction();

	/**
	 * 定时将商品信息传输至节点
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	@Async("taskExecutor")
	public CompletableFuture<String> run() throws InterruptedException{
		
		while(true){
			try {//等待5秒钟
				Thread.sleep(5000);
			} catch (Exception e) {
				p2PService.outputForThread(6);
			}
			transaction = p2PService.operateWaittingCommodities(1, null);
			if(transaction!=null){
				Message message = new Message();
				message.setType(BlockConstant.COMMODITY_INFO);
				message.setData(JSON.toJSONString(transaction));
				p2PService.broatcast(JSON.toJSONString(message));
				p2PService.outputForThread(7);
			}
		}
	}
	
}
