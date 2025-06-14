package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;

import java.util.*;
import java.util.stream.Collectors;

public interface BasePage extends BeforeEnterObserver {

    /**
     * 生命周期入口：页面路由进入前执行
     */
    default void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
        RouteParameters routeParameters = event.getRouteParameters();

        ParameterMap queryParams = new ParameterMap(parameters);     // URL ?key=value
        ParameterMap pathParams = new ParameterMap(routeParameters); // 路由 /view/:id

        this.onGetUrlQueryParameters(queryParams, event);
        this.onGetPathParameters(pathParams, event);

        ParameterMap params = new ParameterMap();
        params.addAll(queryParams);
        params.addAll(pathParams);

        this.onGetParameters(params, event);

        // ✅ 新增生命周期：获取所有参数之后执行（用于集中式后处理）
        this.onAfterParameterResolved();
    }

    /**
     * 生命周期：处理 URL 查询参数（?xxx=yyy）
     */
    default void onGetUrlQueryParameters(ParameterMap parameters, BeforeEnterEvent event) {}

    /**
     * 生命周期：处理路径参数（如 /user/:id）
     */
    default void onGetPathParameters(ParameterMap parameters, BeforeEnterEvent event) {}

    /**
     * 生命周期：合并参数后统一处理（路径 + 查询参数）
     */
    default void onGetParameters(ParameterMap parameters, BeforeEnterEvent event) {}

    /**
     * ✅ 新增生命周期：所有参数解析完毕之后执行
     */
    default void onAfterParameterResolved() {}

    /**
     * 参数封装工具类
     */
    class ParameterMap {
        private final Map<String, List<String>> parameters;

        public ParameterMap(Map<String, List<String>> parameters) {
            this.parameters = parameters;
        }

        public ParameterMap() {
            this.parameters = new HashMap<>();
        }

        public ParameterMap(RouteParameters routeParameters) {
            this.parameters = routeParameters.getParameterNames().stream()
                    .collect(Collectors.toMap(
                            name -> name,
                            name -> List.of(routeParameters.get(name).orElse(""))
                    ));
        }

        public Optional<String> getSingle(String key) {
            List<String> values = this.parameters.get(key);
            if (values == null || values.isEmpty()) return Optional.empty();
            return Optional.ofNullable(values.get(0));
        }

        public List<String> get(String key) {
            return this.parameters.getOrDefault(key, List.of());
        }
        public Integer getInt(String key, Integer defaultValue) {
            return getSingle(key).map(Integer::valueOf).orElse(defaultValue);
        }

        public Long getLong(String key, Long defaultValue) {
            return getSingle(key).map(Long::valueOf).orElse(defaultValue);
        }

        public boolean contains(String key) {
            return parameters.containsKey(key);
        }

        public void add(String key, String value) {
            this.parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        public void addAll(ParameterMap other) {
            other.parameters.forEach((key, values) -> {
                this.parameters.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
            });
        }

        public Map<String, List<String>> asMap() {
            return this.parameters;
        }

        @Override
        public String toString() {
            return "UrlParameters{" +
                    "parameters=" + parameters +
                    '}';
        }
    }
}
