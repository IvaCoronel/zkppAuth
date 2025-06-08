package com.github.ivacoronel.server.services;

import com.github.ivacoronel.server.web.dtos.ChallengeDto;

public interface KafkaAgentService {
	
	public void send(String topic, ChallengeDto chal);
	
	public void openTopic(String topic);
	
	public void closeTopic(String topic);

}
