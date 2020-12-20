package lk.ijse.dep.web.model;

public class Order {
    private String orderId;
    private String customerId;
    private String orderTotal;

    public Order() {
    }

    public Order(String id, String customerId, String orderTotal) {
        this.orderId = id;
        this.customerId = customerId;
        this.orderTotal = orderTotal;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + orderId + '\'' +
                ", Date='" + orderTotal + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
