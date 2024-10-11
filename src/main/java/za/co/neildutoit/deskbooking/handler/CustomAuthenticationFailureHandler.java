package za.co.neildutoit.deskbooking.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
    // Set the response status to 401 Unauthorized
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    // Set the content type to HTML
    response.setContentType("text/html");

    // Write the HTML error page
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Error</title>");
    out.println("<style>");
    out.println("body { font-family: Arial, sans-serif; }");
    out.println(".container { text-align: center; margin-top: 50px; }");
    out.println(".error-message { font-size: 20px; color: red; margin-bottom: 20px; }");
    out.println(".home-button { padding: 10px 20px; background-color: #007bff; color: #fff; border: none; border-radius: 5px; cursor: pointer; text-decoration: none; }");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
    out.println("<div class=\"container\">");
    out.println("<div class=\"error-message\">Access Denied: " + exception.getMessage() + "</div>");
    out.println("<a href=\"/\" class=\"home-button\">Go Back Home</a>");
    out.println("</div>");
    out.println("</body>");
    out.println("</html>");
  }
}