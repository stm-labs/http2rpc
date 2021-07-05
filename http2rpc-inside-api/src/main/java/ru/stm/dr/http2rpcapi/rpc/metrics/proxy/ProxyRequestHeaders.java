package ru.stm.dr.http2rpcapi.rpc.metrics.proxy;

import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

// большая часть скопирована из org.springframework.http.HttpHeaders
@Data
public class ProxyRequestHeaders implements MultiValueMap<String, String> {

    private final MultiValueMap<String, String> headers;

    /**
     * Construct a new, empty instance of the {@code HttpHeaders} object.
     */
    public ProxyRequestHeaders() {
        this(CollectionUtils.toMultiValueMap(new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH)));
    }

    /**
     * Construct a new {@code HttpHeaders} instance backed by an existing map.
     * @since 5.1
     */
    public ProxyRequestHeaders(MultiValueMap<String, String> headers) {
        Assert.notNull(headers, "MultiValueMap must not be null");
        this.headers = headers;
    }

    // MultiValueMap implementation

    /**
     * Return the first header value for the given header name, if any.
     * @param headerName the header name
     * @return the first header value, or {@code null} if none
     */
    @Override
    @Nullable
    public String getFirst(String headerName) {
        return this.headers.getFirst(headerName);
    }

    /**
     * Add the given, single header value under the given name.
     * @param headerName the header name
     * @param headerValue the header value
     * @throws UnsupportedOperationException if adding headers is not supported
     * @see #put(String, List)
     * @see #set(String, String)
     */
    @Override
    public void add(String headerName, @Nullable String headerValue) {
        this.headers.add(headerName, headerValue);
    }

    @Override
    public void addAll(String key, List<? extends String> values) {
        this.headers.addAll(key, values);
    }

    @Override
    public void addAll(MultiValueMap<String, String> values) {
        this.headers.addAll(values);
    }

    /**
     * Set the given, single header value under the given name.
     * @param headerName the header name
     * @param headerValue the header value
     * @throws UnsupportedOperationException if adding headers is not supported
     * @see #put(String, List)
     * @see #add(String, String)
     */
    @Override
    public void set(String headerName, @Nullable String headerValue) {
        this.headers.set(headerName, headerValue);
    }

    @Override
    public void setAll(Map<String, String> values) {
        this.headers.setAll(values);
    }

    @Override
    public Map<String, String> toSingleValueMap() {
        return this.headers.toSingleValueMap();
    }


    // Map implementation

    @Override
    public int size() {
        return this.headers.size();
    }

    @Override
    public boolean isEmpty() {
        return this.headers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.headers.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.headers.containsValue(value);
    }

    @Override
    @Nullable
    public List<String> get(Object key) {
        return this.headers.get(key);
    }

    @Override
    public List<String> put(String key, List<String> value) {
        return this.headers.put(key, value);
    }

    @Override
    public List<String> remove(Object key) {
        return this.headers.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        this.headers.putAll(map);
    }

    @Override
    public void clear() {
        this.headers.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.headers.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return this.headers.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return this.headers.entrySet();
    }

    @Override
    public int hashCode() {
        return this.headers.hashCode();
    }

    @Override
    public String toString() {
        return formatHeaders(this.headers);
    }

    /**
     * Helps to format HTTP header values, as HTTP header values themselves can
     * contain comma-separated values, can become confusing with regular
     * {@link Map} formatting that also uses commas between entries.
     * @param headers the headers to format
     * @return the headers to a String
     * @since 5.1.4
     */
    public static String formatHeaders(MultiValueMap<String, String> headers) {
        return headers.entrySet().stream()
                .map(entry -> {
                    List<String> values = entry.getValue();
                    return entry.getKey() + ":" + (values.size() == 1 ?
                            "\"" + values.get(0) + "\"" :
                            values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
