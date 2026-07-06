package egovframework.example.sample.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import egovframework.com.cmm.util.MapKeyConverter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final MapKeyConverter mapKeyConverter;

    @SuppressWarnings("static-access")
	public Map<String, Object> getUserInfo(Map<String, Object> resultMap) {
        return mapKeyConverter.convertToCamelCase(resultMap);
    }
}