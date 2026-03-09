package backend.security;

import io.micronaut.http.HttpRequest;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MicronautWebContext implements WebContext {

    private final HttpRequest<?> request;

    public MicronautWebContext(HttpRequest<?> request) {
        this.request = request;
    }

    @Override
    public Optional<String> getRequestParameter(String name) {
        return Optional.ofNullable(request.getParameters().get(name));
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return request.getParameters().asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toArray(new String[0])
                ));
    }

    @Override
    public Optional<String> getRequestHeader(String name) {
        return Optional.ofNullable(request.getHeaders().get(name));
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public String getFullRequestURL() {
        return request.getUri().toString();
    }

    @Override
    public String getScheme() {
        return request.getUri().getScheme();
    }

    @Override
    public String getServerName() {
        return request.getUri().getHost();
    }

    @Override
    public int getServerPort() {
        int port = request.getUri().getPort();
        return port == -1 ? 80 : port;
    }

    @Override
    public boolean isSecure() {
        return request.getUri().getScheme().equalsIgnoreCase("https");
    }

    @Override
    public String getPath() {
        return request.getPath();
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        return Collections.emptyList();
    }

    @Override
    public void addResponseCookie(Cookie cookie) {
    }

    @Override
    public void setResponseHeader(String name, String value) {
    }

    @Override
    public void setResponseContentType(String content) {
    }

    @Override
    public Optional<String> getResponseHeader(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getRequestAttribute(String name) {
        return request.getAttribute(name);
    }

    @Override
    public void setRequestAttribute(String name, Object value) {
    }

    @Override
    public String getRequestContent() {
        return request.getBody(String.class).orElse("");
    }
}
