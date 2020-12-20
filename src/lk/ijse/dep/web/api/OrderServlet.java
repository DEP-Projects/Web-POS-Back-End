package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Order;
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

@WebServlet(name = "OrderServlet")
public class OrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        resp.setContentType("application/json");

        try(Connection connection = cp.getConnection()){
            PrintWriter out = resp.getWriter();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `Order`"+((orderId != null)? " WHERE orderId=?":""));
            if(orderId != null){
                pstm.setObject(1,orderId);
            }
            ResultSet rst = pstm.executeQuery();
            List<Order> orderList = new ArrayList<>();
            while (rst.next()){
                orderId = rst.getString(1);
                String customerId = rst.getString(2);
                String orderTotal = rst.getString(3);
                orderList.add(new Order(orderId,customerId,orderTotal));
            }

            if(orderId != null && orderList.isEmpty()){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            else{
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(orderList));
            }
        }catch (SQLException e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        String customerId = req.getParameter("customerId");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        try(Connection connection = cp.getConnection()) {

            PreparedStatement pstm = connection.prepareStatement("SELECT SUM(subTotal) FROM OrderItem WHERE orderId=?");
            pstm.setString(1,orderId);
            ResultSet resultSet = pstm.executeQuery();
            resultSet.next();
            String orderTotal = resultSet.getString(1);

            pstm = connection.prepareStatement("INSERT INTO `Order` VALUES (?,?,?)");
            pstm.setString(1,orderId);
            pstm.setString(2,customerId);
            pstm.setString(3,orderTotal);

            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


        }catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (SQLException throwables){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        if (orderId == null || !orderId.matches("O\\d{3}")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `Order` WHERE orderId=?");
            pstm.setObject(1, orderId);
            if (pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM `Order` WHERE orderId=?");
                pstm.setObject(1, orderId);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }
    }
}
