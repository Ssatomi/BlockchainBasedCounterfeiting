package com.dce.blockchain.web.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.model.Block;
import com.dce.blockchain.web.model.Message;
import com.dce.blockchain.web.model.Transaction;
import com.dce.blockchain.web.util.BlockCache;
import com.dce.blockchain.web.util.BlockConstant;

/**
 * 共识机制
 * 
 *
 */
@Service
public class PowService implements Runnable{

	@Autowired
	BlockCache blockCache;
	
	@Autowired
	BlockService blockService;
	
	@Autowired
	P2PService p2PService;

	private List<Transaction> tsaList = new ArrayList<Transaction>();

	//线程退出标志
	public volatile boolean exit = false;

	public List<Transaction> getTsaList(){
		return tsaList;
	}
	
	public void setTsaList(List<Transaction> tsaList){
		this.tsaList = tsaList;
	}

	/**
	 * 通过“挖矿”进行工作量证明，实现节点间的共识
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public void run(){
		
		// 定义每次哈希函数的结果
		String newBlockHash = "";
		int nonce = 0;
		long start = System.currentTimeMillis();
		System.out.println("开始挖矿");
		while (!exit) {
			// 计算新区块hash值
			newBlockHash = blockService.calculateHash(blockCache.getLatestBlock().getHash(), tsaList, nonce);
			// 校验hash值
			if (blockService.isValidHash(newBlockHash)) {
				System.out.println("挖矿完成，正确的hash值：" + newBlockHash);
				System.out.println("挖矿耗费时间：" + (System.currentTimeMillis() - start) + "ms");
				break;
			}
			System.out.println("第"+(nonce+1)+"次尝试计算的hash值：" + newBlockHash);
			nonce++;
		}
		if(!exit){
			//得到新区块
			Block block = new Block();
			block = blockService.createNewBlock(nonce, blockCache.getLatestBlock().getHash(), newBlockHash, tsaList);
			//新区块正在仲裁存在缓存且其时间更早则舍弃此次挖矿产生的新区块
			if((blockCache.getNewBlockDeciding().getIndex()!=0) && 
				(blockCache.getNewBlockDeciding().getTimestamp()<block.getTimestamp())){
					return;
			}
			//存入缓存
			blockCache.setNewBlockDeciding(block);
			//全网广播
			Message msg = new Message();
			msg.setType(BlockConstant.CALCULATED_NEW_BLOCK);
			msg.setData(JSON.toJSONString(block));
			p2PService.broatcast(JSON.toJSONString(msg));
		}
	}
	
}
