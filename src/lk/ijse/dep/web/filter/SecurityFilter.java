package lk.ijse.dep.web.filter;

import javax.servlet.*;
import java.io.IOException;

//@WebFilter(filterName = "SecurityFilter") this not need when webxml is coded
public class SecurityFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        System.out.println("security filter incoming");
        chain.doFilter(req, resp);
        System.out.println("security filter outgoing");
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
