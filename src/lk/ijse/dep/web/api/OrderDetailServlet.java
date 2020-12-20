package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.OrderDetail;
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

@WebServlet(name = "OrderDetailServlet")
public class OrderDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        resp.setContentType("application/json");

        try(Connection connection = cp.getConnection()){
            PrintWriter out = resp.getWriter();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM OrderDetail"+((orderId != null)? " WHERE orderId=?":""));
            if(orderId != null){
                pstm.setObject(1,orderId);
            }
            ResultSet rst = pstm.executeQuery();
            List<OrderDetail> orderDetailList = new ArrayList<>();
            while (rst.next()){
                orderId = rst.getString(1);
                String itemCode = rst.getString(2);
                String qty = rst.getString(3);
                String unitPrice = rst.getString(4);
                orderDetailList.add(new OrderDetail(orderId, itemCode, qty,unitPrice));
            }

            if(orderId != null && orderDetailList.isEmpty()){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            else{
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(orderDetailList));
            }
        }catch (SQLException e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");

        try(Connection connection = cp.getConnection()) {
            Jsonb jsonb = JsonbBuilder.create();
         OrderDetail orderDetail = jsonb.fromJson(req.getReader(),OrderDetail.class);

            if(orderDetail.getOrderId() ==null || orderDetail.getItemCode() == null || orderDetail.getItemCode() == null || orderDetail.getQty() == null || orderDetail.getUnitPrice() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO OrderItem VALUES (?,?,?,?)");
            pstm.setString(1,orderDetail.getOrderId());
            pstm.setString(2,orderDetail.getItemCode());
            pstm.setString(3,orderDetail.getQty());
            pstm.setString(4,orderDetail.getUnitPrice());

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
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String orderId = req.getParameter("orderId");
        String itemCode = req.getParameter("itemCode");
        if(orderId==null || !orderId.matches("O\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if(itemCode==null || !itemCode.matches("I\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection()) {

            Jsonb jsonb = JsonbBuilder.create();
            OrderDetail orderDetail = jsonb.fromJson(req.getReader(), OrderDetail.class);

            if(orderDetail.getOrderId() != null || orderDetail.getItemCode() != null || orderDetail.getQty() == null || orderDetail.getUnitPrice() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

                return;
            }

            if (orderDetail.getItemCode().trim().isEmpty() || orderDetail.getQty().trim().isEmpty() || orderDetail.getUnitPrice().trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }


            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM OrderItem WHERE orderId=?");
            pstm.setObject(1, orderId);

            if (pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE OrderItem SET qty=?, WHERE orderId=? AND itemCode=?");
                pstm.setObject(1,orderDetail.getQty());
                pstm.setObject(2, orderId);
                pstm.setObject(3, itemCode);
                if (pstm.executeUpdate() > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        String itemCode = req.getParameter("itemCode");
        if (orderId == null || !orderId.matches("O\\d{3}")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (itemCode == null || !itemCode.matches("I\\d{3}")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM OrderItem WHERE orderId=?");
            pstm.setObject(1, orderId);
            if (pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM OrderItem WHERE orderId=? AND itemCode=?");
                pstm.setObject(1, orderId);
                pstm.setObject(2, itemCode);
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
        }    }
}

