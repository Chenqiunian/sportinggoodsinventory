package Demo.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author Chenqiunian
 * Date 2022-05-17 18:18
 * Description
 */

@Component
public class SimpleCorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods","POST,GET,OPTION,DELETE");
        response.setHeader("Access-Control-Max-Age","3600");
        response.setHeader("Access-Control-Allow-Credentials","true");
        response.setHeader("Access-Control-Allow-Headers","Origin,No-Cache,X-Requested-With,If-Modified-Since" +
                "Pragma,Last-Modified,Cache-Control,Expires,Content-type,content-type,X-E4M-With,userId,token,"+
                "Access-Control-Allow-Headers,Authorization,content-disposition,Content-Rages,Accept-Ranges,Etag,Range,user-agent");
        if(request.getMethod().equals(HttpMethod.OPTIONS.name())){
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else{
            chain.doFilter(req,res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig){

    }
}
