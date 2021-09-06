package com.dce.blockchain.web.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.service.BlockService;
import com.dce.blockchain.web.service.PowService;
import com.dce.blockchain.web.util.BlockCache;

@Controller
public class BlockController {

	@Resource
	BlockService blockService;
	
	@Resource
	PowService powService;
	
	@Autowired
	BlockCache blockCache;
	
	/**
	 * 查看当前节点区块链数据
	 * @return
	 */
	@GetMapping("/scan")
	@ResponseBody
	public String scanBlock() {
		return JSON.toJSONString(blockCache.getBlockChain());
	}
	
}
