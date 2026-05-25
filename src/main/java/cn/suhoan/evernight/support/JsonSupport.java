package cn.suhoan.evernight.support;


import cn.suhoan.evernight.exception.ExternalServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        }
        catch (JsonProcessingException ex) {
            throw new ExternalServiceException("解析外部服务 JSON 响应失败", ex);
        }
    }

    public String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException ex) {
            throw new ExternalServiceException("序列化 JSON 请求失败", ex);
        }
    }

}
