package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HibernateConnectionTest {
    public static void main(String[] args) {
        try {
            // Lấy SessionFactory từ HibernateUtil
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

            // Mở một session để kiểm tra kết nối
            try (Session session = sessionFactory.openSession()) {
                System.out.println("Kết nối thành công tới SQL Server!");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}