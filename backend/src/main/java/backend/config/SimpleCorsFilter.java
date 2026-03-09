package backend.config;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.core.order.Ordered;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Filter("/**")
public class SimpleCorsFilter implements HttpServerFilter {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (request.getMethod().name().equals("OPTIONS")) {
            MutableHttpResponse<?> res = HttpResponse.ok();
            addCors(res, request);
            return Mono.just(res);
        }
        return Mono.from(chain.proceed(request)).map(res -> addCors(res, request));
    }

    private MutableHttpResponse<?> addCors(MutableHttpResponse<?> res, HttpRequest<?> request) {
        String origin = request.getHeaders().get("Origin");
        if (origin == null) {
            origin = "*";
        }
        res.header("Access-Control-Allow-Origin", origin);
        res.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        res.header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        res.header("Access-Control-Allow-Credentials", "true");
        res.header("Access-Control-Max-Age", "3600");
        return res;
    }
}
