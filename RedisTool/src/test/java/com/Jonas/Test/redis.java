package com.Jonas.Test;



import org.junit.Test;
import com.Jonas.redis.WrapXedisClient;


public class redis {
	
		
	@Test
	public void test(){
		WrapXedisClient.myCacheClient().set("dujunpeng", "hahaha");
	}

}
