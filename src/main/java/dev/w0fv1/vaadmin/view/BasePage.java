package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;

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

        onGetUrlQueryParameters(queryParams);
        onGetPathParameters(pathParams);
    }

    void onGetUrlQueryParameters(ParameterMap parameters);
    void onGetPathParameters(ParameterMap parameters);


    class ParameterMap {
        private final Map<String, List<String>> parameters;

        public ParameterMap(Map<String, List<String>> parameters) {
            this.parameters = parameters;
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

        @Override
        public String toString() {
            return "UrlParameters{" +
                    "parameters=" + parameters +
                    '}';
        }
    }
}
