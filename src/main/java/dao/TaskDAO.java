package dao;

import entity.Task;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class TaskDAO {
    private final SessionFactory sessionFactory;

    public TaskDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Task findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Task.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find task by ID: " + id, e);
        }
    }

    public List<Task> findByUser(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Task> query = session.createQuery("FROM Task WHERE user.id = :userId", Task.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find tasks for user ID: " + userId, e);
        }
    }

    public List<Task> findByStatus(Long userId, String status) {
        try (Session session = sessionFactory.openSession()) {
            Query<Task> query = session.createQuery("FROM Task WHERE user.id = :userId AND status = :status", Task.class);
            query.setParameter("userId", userId);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find tasks with status: " + status + " for user ID: " + userId, e);
        }
    }

    public List<Task> findByPriority(Long userId, String priority) {
        try (Session session = sessionFactory.openSession()) {
            Query<Task> query = session.createQuery("FROM Task WHERE user.id = :userId AND priority = :priority", Task.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", priority);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find tasks with priority: " + priority + " for user ID: " + userId, e);
        }
    }

    public void save(Task task) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(task);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save task: " + task.getTitle(), e);
        }
    }

    public void update(Task task) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(task);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task: " + task.getTitle(), e);
        }
    }

    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Task task = session.get(Task.class, id);
            if (task != null) {
                session.remove(task);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task with ID: " + id, e);
        }
    }
}