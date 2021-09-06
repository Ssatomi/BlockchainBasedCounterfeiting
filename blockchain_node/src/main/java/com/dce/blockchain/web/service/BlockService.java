package com.dce.blockchain.web.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.model.Block;
import com.dce.blockchain.web.model.Transaction;
import com.dce.blockchain.web.util.BlockCache;
import com.dce.blockchain.web.util.CryptoUtil;

/**
 * 区块链相关服务类
 * 
 *
 */
@Service
public class BlockService {

	@Autowired
	BlockCache blockCache;
	@Autowired
	P2PService p2PService;
	/**
	 * 创建创世区块
	 * @return
	 */
	public String createGenesisBlock() {
		Block genesisBlock = new Block();
		//设置创世区块高度为1
		genesisBlock.setIndex(1);
		//genesisBlock.setTimestamp(System.currentTimeMillis());
		genesisBlock.setTimestamp(0);
		genesisBlock.setNonce(1);
		//封装业务数据
		List<Transaction> tsaList = new ArrayList<Transaction>();
		Transaction tsa = new Transaction();
		tsa.setAll("0", "0", "创世商品", "创世工厂", "0", "0");
		tsaList.add(tsa);
		genesisBlock.setTransactions(tsaList);
		//设置创世区块的hash值
		genesisBlock.setHash(calculateHash("",tsaList,1));
		// //添加到区块链索引中
		// blockCache.addtoIndexForBlock("0", 1);
		//添加到区块链中
		blockCache.getBlockChain().add(genesisBlock);
		return JSON.toJSONString(genesisBlock);
	}
	
	/**
	 * 创建新区块
	 * @param nonce
	 * @param previousHash
	 * @param hash
	 * @param blockTxs
	 * @return
	 */
	public Block createNewBlock(int nonce, String previousHash, String hash, List<Transaction> blockTxs) {
		Block block = new Block();
		block.setIndex(blockCache.getBlockChain().size() + 1);
		//时间戳
		block.setTimestamp(System.currentTimeMillis());
		block.setTransactions(blockTxs);
		//工作量证明，计算正确hash值的次数
		block.setNonce(nonce);
		//上一区块的哈希
		block.setPreviousHash(previousHash);
		//当前区块的哈希
		block.setHash(hash);
		return block;
	}

	/**
	 * 添加新区块到当前节点的区块链中
	 * 
	 * @param newBlock
	 */
	public boolean addBlock(Block newBlock) {
		//先对新区块的合法性进行校验
		if (isValidNewBlock(newBlock, blockCache.getLatestBlock())) {
			blockCache.getBlockChain().add(newBlock);
			//建立商品码和区块高度的索引
			for(Transaction transaction : newBlock.getTransactions()){
				blockCache.addtoIndexForBlock(transaction.getId(), newBlock.getIndex());
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 验证新区块是否有效
	 * 
	 * @param newBlock
	 * @param previousBlock
	 * @return
	 */
	public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
		if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
			System.out.println("新区块的前一个区块hash验证不通过");
			return false;
		} else {
			// 验证新区块hash值的正确性
			String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getTransactions(), newBlock.getNonce());
			if (!hash.equals(newBlock.getHash())) {
				System.out.println("新区块的hash无效: " + hash + " " + newBlock.getHash());
				return false;
			}
			if (!isValidHash(newBlock.getHash())) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 验证hash值是否满足系统条件
	 * 
	 * @param hash
	 * @return
	 */
	public boolean isValidHash(String hash) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<blockCache.getDifficulty();i++){
			sb.append('0');
		}
		return hash.startsWith(sb.toString());
	}
	
	/**
	 * 验证整个区块链是否有效
	 * @param chain
	 * @return
	 */
	public boolean isValidChain(List<Block> chain) {
		Block block = null;
		Block lastBlock = chain.get(0);
		int currentIndex = 1;
		while (currentIndex < chain.size()) {
			block = chain.get(currentIndex);

			if (!isValidNewBlock(block, lastBlock)) {
				return false;
			}

			lastBlock = block;
			currentIndex++;
		}
		return true;
	}

	/**
	 * 替换本地区块链
	 * 
	 * @param newBlocks
	 */
	public void replaceChain(List<Block> newBlocks) {
		if (isValidChain(newBlocks) && newBlocks.size() > blockCache.getBlockChain().size()) {
			//更新商品码与区块高度的索引，只需增改而不需删除
			newBlocks.forEach(block -> {
				block.getTransactions().forEach(transaction -> {
					blockCache.addtoIndexForBlock(transaction.getId(), block.getIndex());
				});
			});
			blockCache.setBlockChain(newBlocks);
			System.out.println("替换后的本节点区块链："+JSON.toJSONString(blockCache.getBlockChain()));
		} else {
			System.out.println("接收的区块链无效");
		}
	}

	/**
	 * 计算区块的hash
	 * 
	 * @param previousHash
	 * @param currentTransactions
	 * @param nonce
	 * @return
	 */
	public String calculateHash(String previousHash, List<Transaction> currentTransactions, int nonce) {
		return CryptoUtil.SHA256(previousHash + JSON.toJSONString(currentTransactions) + nonce);
	}

}
