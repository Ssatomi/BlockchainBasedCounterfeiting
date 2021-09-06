package com.dce.blockchain.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.model.Message;
import com.dce.blockchain.web.util.BlockCache;
import com.dce.blockchain.web.util.BlockConstant;

/**
 * 统计请求新区块确认的节点数量
 * 定时查询数量，并做出超时处理
 * 
 *
 */
@DependsOn(value = "p2PService")
@Service
public class NodeCountService {

	@Autowired
	BlockCache blockCache;
	
	@Autowired
	P2PService p2PService;

	/**
	 * 统计请求新区块确认的节点数量
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	@Async("taskExecutor")
	public CompletableFuture<String> run() throws InterruptedException{
		//开始时间
		long start = System.currentTimeMillis();
		while(true){
			System.out.println("statistics in nodecount"+blockCache.getForStatistics());
			//所有节点均发来消息
			if(blockCache.getForStatistics() >= blockCache.getSocketsList().size()){
				p2PService.outputForThread(1);
				//进行成功处理
				//生成新区块仲裁结果并广播
				Message message = new Message();
				message.setType(BlockConstant.DECIDED_NEW_BLOCK);
				message.setData(JSON.toJSONString(blockCache.getNewBlockCache()));
				p2PService.broatcast(JSON.toJSONString(message));
				//标志位，防止主进程修改统计数
				blockCache.getNewBlockCache().setTimestamp(0);
				//统计数清零
				blockCache.setZeroForStatistics();
				//清空新节点列表缓存
				blockCache.getNewSocketsListCache().clear();
				//保存至已成功商品列表
				p2PService.operateSuccessedCommodities(0);
				break;
			}
			//判断是否超时（30秒）
			else if(System.currentTimeMillis()-start > 30000){
				//有不少于一半的节点发来消息
				if(blockCache.getForStatistics() >= blockCache.getSocketsList().size()/2 +
				blockCache.getSocketsList().size()%2){
					p2PService.outputForThread(2);
					//进行成功处理
					//生成新区块仲裁结果并广播
					Message message = new Message();
					message.setType(BlockConstant.DECIDED_NEW_BLOCK);
					message.setData(JSON.toJSONString(blockCache.getNewBlockCache()));
					p2PService.broatcast(JSON.toJSONString(message));
					//标志位，防止主进程修改统计数
					blockCache.getNewBlockCache().setTimestamp(0);
					//统计数清零
					blockCache.setZeroForStatistics();
					//清空新节点列表缓存
					blockCache.getNewSocketsListCache().clear();
					//保存至已成功商品列表
					p2PService.operateSuccessedCommodities(0);
					break;
				}
				else{//未有足够的节点发来消息
					p2PService.outputForThread(3);
					//进行失败处理
					//标志位，防止主进程修改统计数
					blockCache.getNewBlockCache().setTimestamp(0);
					//统计数清零
					blockCache.setZeroForStatistics();
					//替换节点socket列表
					blockCache.getSocketsList().clear();
					blockCache.getSocketsList().addAll(blockCache.getNewSocketsListCache());
					//清空节点列表缓存
					blockCache.getNewSocketsListCache().clear();
					//直接发出开始工作消息
					p2PService.broatcast(p2PService.workStart());
					break;
				}
			}
			//等待1秒钟
			else{
				try {
					p2PService.outputForThread(4);
					Thread.sleep(1000);
				} catch (Exception e) {
					p2PService.outputForThread(5);
				}
			}
		}
		return CompletableFuture.completedFuture("");
	}
	// //统计结果满足要求后的成功处理
	// public void handleSuccess(){
	// 	//生成新区块仲裁结果并广播
	// 	Message message = new Message();
	// 	message.setType(BlockConstant.DECIDED_NEW_BLOCK);
	// 	message.setData(JSON.toJSONString(blockCache.getNewBlockCache()));
	// 	p2PService.broatcast(JSON.toJSONString(message));
	// 	//标志位，防止主进程修改统计数
	// 	blockCache.getNewBlockCache().setTimestamp(0);
	// 	//统计数清零
	// 	blockCache.setZeroForStatistics();
	// 	//清空新节点列表缓存
	// 	blockCache.getNewSocketsListCache().clear();
	// 	//保存至已成功商品列表
	// 	p2PService.operateSuccessedCommodities(0);
	// }
	// //统计结果不满足要求后的失败处理
	// public void handleFailure(){
	// 	//标志位，防止主进程修改统计数
	// 	blockCache.getNewBlockCache().setTimestamp(0);
	// 	//统计数清零
	// 	blockCache.setZeroForStatistics();
	// 	//替换节点socket列表
	// 	blockCache.getSocketsList().clear();
	// 	blockCache.getSocketsList().addAll(blockCache.getNewSocketsListCache());
	// 	//清空节点列表缓存
	// 	blockCache.getNewSocketsListCache().clear();
	// 	//直接发出开始工作消息
	// 	p2PService.broatcast(p2PService.workStart());
	// }
}
