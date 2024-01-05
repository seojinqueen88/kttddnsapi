package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.SetupDao;
import com.kttddnsapi.model.Setup;

@Service
public class SetupService
{
	@Autowired
	private SetupDao setupDao;

	public Map<String, Object> selectSetup()
	{
		List<Setup> setupList = setupDao.selectSetup();
		Map<String, Object> setup = new HashMap<>();

		for(Setup setupItem : setupList)
		{
			switch(setupItem.getSetup_key())
			{
			case "devplaykey_limit_min":
			case "eventtime_limit_day":
				setup.put(setupItem.getSetup_key(), setupItem.getInt_value());
				break;
			default:
				break;
			}
		}

		return setup;
	}
}
