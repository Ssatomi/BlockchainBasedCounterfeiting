package com.dce.blockchain.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.dce.blockchain.web.util.BlockCache;

@Controller
public class BlockController {
	
	@Autowired
	BlockCache blockCache;

	/**
	 * 查看producer已成功商品数据
	 * @return
	 */
	@GetMapping("/scan")
	@ResponseBody
	public String scanSuccessedCommodities() {
		return JSON.toJSONString(blockCache.getSuccessedCommodities());
	}
	
}
