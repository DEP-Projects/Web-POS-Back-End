package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep.web.model.Customer;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       /* resp.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");*/
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        //response.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            try {
               /* Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/thogakade", "root", "1234");*/
                Connection con = cp.getConnection();
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM Customer");
                List<Customer> customerList = new ArrayList<>();
                while (rst.next()) {
                    String id = rst.getString(1);
                    String name = rst.getString(2);
                    String address = rst.getString(3);
                    customerList.add(new Customer(id, name, address));
                }
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(customerList));
                con.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb Jsonb=JsonbBuilder.create();
        Customer customer = Jsonb.fromJson(request.getReader(), Customer.class);
        //response.addHeader("Access-Control-Allow-Origin","http://localhost:3306");
        response.setContentType("application/json");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try {
            Connection connection = cp.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
            pstm.setString(1, customer.getId());
            pstm.setString(2, customer.getName());
            pstm.setString(3, customer.getAddress());
            boolean success=pstm.executeUpdate()>0;

            if(success){
                response.getWriter().println(Jsonb.toJson(true));
            }else {
                response.getWriter().println(Jsonb.toJson(false));
            }
            // Jsonb.toJson(true);
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.getWriter().println(Jsonb.toJson(false));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String id=req.getParameter("id");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try {
            Connection connection = cp.getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
            pstm.setObject(1, id);
            boolean success=pstm.executeUpdate()>0;
            if (success){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}




