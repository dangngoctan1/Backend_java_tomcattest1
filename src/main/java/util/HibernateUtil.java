package util; // Đây là package bạn muốn kiểm tra

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // Khởi tạo SessionFactory một lần duy nhất khi lớp được tải
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Phương thức tĩnh để xây dựng SessionFactory
    private static SessionFactory buildSessionFactory() {
        try {
            // Tải cấu hình từ hibernate.cfg.xml (mặc định) và xây dựng SessionFactory
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Bắt và in ra lỗi nếu SessionFactory không thể khởi tạo
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Phương thức công khai để lấy SessionFactory đã được khởi tạo
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Phương thức để đóng SessionFactory khi ứng dụng tắt
    public static void shutdown() {
        getSessionFactory().close();
    }
}