package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import dev.w0fv1.vaadmin.view.table.BaseRepositoryTablePage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BasePage extends BeforeEnterObserver {

    @Override
    default void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters()
                .getParameters();

        BaseRepositoryTablePage.UrlParameters params = new BaseRepositoryTablePage.UrlParameters(parameters);
        onGetUrlParameters(params);
    }

    void onGetUrlParameters(BaseRepositoryTablePage.UrlParameters parameters);


    public static class UrlParameters {
        private final Map<String, List<String>> parameters;

        public UrlParameters(Map<String, List<String>> parameters) {
            this.parameters = parameters;
        }

        public Optional<String> getSingle(String key) {
            return Optional.ofNullable(parameters.get(key))
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0));
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
