package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BasePage extends BeforeEnterObserver {

    @Override
    default void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters()
                .getParameters();
        RouteParameters routeParameters = event.getRouteParameters();

        ParameterMap queryParams = new ParameterMap(parameters);
        ParameterMap pathParams = new ParameterMap(routeParameters);

        onGetUrlQueryParameters(queryParams, event);
        onGetPathParameters(pathParams, event);

        ParameterMap params = new ParameterMap();
        params.addAll(queryParams);
        params.addAll(pathParams);
        onGetParameters(params, event);
    }

    default void onGetUrlQueryParameters(ParameterMap parameters, BeforeEnterEvent event) {

    }

    default void onGetPathParameters(ParameterMap parameters, BeforeEnterEvent event) {

    }


    default void onGetParameters(ParameterMap parameters, BeforeEnterEvent event) {

    }

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
                    .collect(java.util.stream.Collectors.toMap(
                            name -> name,
                            name -> List.of(routeParameters.get(name).orElse(""))
                    ));
        }

        public Optional<String> getSingle(String key) {
            return Optional.ofNullable(parameters.get(key))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst);
        }

        public List<String> getAll(String key) {
            return parameters.getOrDefault(key, List.of());
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

        /**
         * 将另一个ParameterMap的所有参数合并到当前参数中
         * 如果键重复，则追加到原有列表后面
         */
        public void addAll(ParameterMap other) {
            other.parameters.forEach((key, valueList) -> {
                parameters.merge(key, new java.util.ArrayList<>(valueList), (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                });
            });
        }

        @Override
        public String toString() {
            return "UrlParameters{" +
                    "parameters=" + parameters +
                    '}';
        }
    }

}
