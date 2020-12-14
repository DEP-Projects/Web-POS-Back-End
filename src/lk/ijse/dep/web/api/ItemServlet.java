package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep.web.model.Item;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        response.addHeader("Access-Control-Allow-Origin","http://localhost:3000");
        response.setContentType("application/xml");
        try (PrintWriter out = response.getWriter()){
            try {
               /* Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/thogakade", "root", "1234");*/
                Connection con = cp.getConnection();
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM Item");
                List<Item> itemList=new ArrayList<>();
                while (rst.next()){
                    String code= rst.getString(1);
                    String description= rst.getString(2);
                    String unitPrice= rst.getString(3);
                    String qtyOnHand= rst.getString(4);
                    itemList.add(new Item(code,description,unitPrice,qtyOnHand));
                }
                    Jsonb jsonb= JsonbBuilder.create();
                    out.println(jsonb.toJson(itemList));
                    con.close();

            } catch (SQLException exception) {
                exception.printStackTrace();
            }

        }
    }
}
