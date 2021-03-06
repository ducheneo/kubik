package com.cspinformatique.kubik.server.domain.notification.processor;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.cspinformatique.kubik.kos.model.kubik.KubikNotification.Type;

@Component
public class NotificationProcessors {
	@Resource
	private List<NotificationProcessor> notificationProcessorList;

	private HashMap<Type, NotificationProcessor> processorsMap;

	@PostConstruct
	public void init() {
		processorsMap = new HashMap<>();

		notificationProcessorList.forEach(processor -> processorsMap.put(processor.getType(), processor));
	}

	public NotificationProcessor getProcessor(Type type) {
		return processorsMap.get(type);
	}

}
