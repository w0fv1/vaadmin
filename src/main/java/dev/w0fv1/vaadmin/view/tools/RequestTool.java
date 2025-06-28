package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.server.VaadinRequest;

public class RequestTool {

    private String getClientIp() {
        VaadinRequest request = VaadinRequest.getCurrent();

        // 尝试从代理中获取
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim(); // 多个 IP 取第一个
        }

        // 否则直接取远端地址
        return request.getRemoteAddr();
    }


}
