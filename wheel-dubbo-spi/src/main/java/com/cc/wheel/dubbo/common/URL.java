package com.cc.wheel.dubbo.common;

import com.cc.wheel.dubbo.utils.Holder;
import com.cc.wheel.dubbo.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * URL - Uniform Resource Locator (Immutable, ThreadSafe) <br>
 * dubbo 的统一资源模型 <br>
 * <br>
 * dubbo 中有协议、用户名、密码等信息作为固定属性，其余附加属性用 Map 存储 <br>
 * 这里灵活一点，不做固定属性，全部用 Map 存储 <br>
 * <br>
 * dubbo 中 URL 的序列化是自己实现的，类似这样：ftp://username:password@192.168.1.7:21 <br>
 * 这里用 Json 来实现 <br>
 *
 * @author cc
 * @date 2023/7/22
 */
public final class URL implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    /**
     * 所有的属性，不可变 Map
     */
    private final Map<String, String> parameters;

    /**
     * 缓存的序列化字符串，因为每次转成 json 的成本比较高，所以缓存一下
     */
    private final transient Holder<String> jsonCache = new Holder<>();

    public URL(Map<String, String> parameters) {
        if (Objects.isNull(parameters)) {
            parameters = Collections.emptyMap();
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public URL(String json) throws JsonProcessingException {
        if (StringUtils.isBlank(json)) {
            this.parameters = Collections.emptyMap();
            return;
        }
        List<Map.Entry<String, String>> list = JsonUtil.OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        this.jsonCache.set(json);
        this.parameters = Collections.unmodifiableMap(list.stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> n)));
    }

    /**
     *
     * @param key 属性 key
     * @param defaultValue 默认值
     * @return 属性 value
     */
    public String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    /**
     * 如果没有此属性返回 null
     *
     * @param key 属性 key
     * @return 属性 value
     */
    public String getParameter(String key) {
        return parameters.getOrDefault(key, null);
    }

    /**
     * @return URL 序列化的字符串
     */
    public String encode() throws JsonProcessingException {
        if (StringUtils.isBlank(jsonCache.get())) {
            synchronized (this.jsonCache) {
                if (StringUtils.isBlank(jsonCache.get())) {
                    // 这里需要排个序，json 不保证对象属性顺序
                    List<Map.Entry<String, String>> list = parameters.entrySet().stream()
                            .sorted((Map.Entry.comparingByKey())).collect(Collectors.toList());
                    jsonCache.set(JsonUtil.OBJECT_MAPPER.writeValueAsString(list));
                }
            }
        }
        return jsonCache.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof URL)) return false;
        URL url = (URL) o;
        return Objects.equals(parameters, url.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }
}
