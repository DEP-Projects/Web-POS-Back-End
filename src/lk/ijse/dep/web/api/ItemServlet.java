package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonParsingException;
import lk.ijse.dep.web.model.Item;
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

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        response.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setContentType("application/xml");
        try (PrintWriter out = response.getWriter(); Connection con = cp.getConnection();) {
               /* Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/thogakade", "root", "1234");*/
            String code = request.getParameter("code");
            PreparedStatement pstm = con.prepareStatement("SELECT * FROM Item" + ((code != null) ? " WHERE code=?" : ""));
            if (code != null) {
                pstm.setObject(1, code);
            }
            ResultSet rst = pstm.executeQuery();
            List<Item> itemList = new ArrayList<>();
            while (rst.next()) {
                code = rst.getString(1);
                String description = rst.getString(2);
                String unitPrice = rst.getString(3);
                String qtyOnHand = rst.getString(4);
                itemList.add(new Item(code, description, unitPrice, qtyOnHand));
            }
                if (code != null && itemList.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    Jsonb jsonb = JsonbBuilder.create();
                    out.println(jsonb.toJson(itemList));
                    con.close();
                }

            } catch(SQLException exception){
                exception.printStackTrace();
            }

        }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection con = cp.getConnection()) {
            Jsonb jsonb = JsonbBuilder.create();
            Item item = jsonb.fromJson(req.getReader(), Item.class);

            if (item.getCode() == null || item.getDescription() == null || item.getQtyOnHand() == null || item.getUnitPrice() == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (!item.getCode().matches("P\\d{3}") || item.getDescription().isEmpty() || item.getUnitPrice().isEmpty() ||
                    item.getQtyOnHand().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = con.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pstm.setString(1, item.getCode());
            pstm.setString(2, item.getDescription());
            pstm.setString(3, item.getUnitPrice());
            pstm.setString(4, item.getQtyOnHand());
            boolean success = pstm.executeUpdate() > 0;
            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }catch (JsonParsingException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin","http://localhost:3000");

        String code=req.getParameter("code");
        if (code==null || !code.matches("P\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection con = cp.getConnection()) {
            PreparedStatement pstm = con.prepareStatement("SELECT * FROM Item WHERE code=?");
            pstm.setString(1, code);
            if (pstm.executeQuery().next()) {
                pstm = con.prepareStatement("DELETE FROM Item WHERE code=?");
                pstm.setString(1, code);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }catch (SQLIntegrityConstraintViolationException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            throwables.printStackTrace();
        }

    }
}

