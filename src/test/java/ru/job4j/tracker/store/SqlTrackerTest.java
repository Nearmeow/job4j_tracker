package ru.job4j.tracker.store;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.job4j.tracker.model.Item;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SqlTrackerTest {

    static Connection connection;

    @BeforeClass
    public static void initConnection() {
        try (InputStream in = SqlTrackerTest.class.getClassLoader().getResourceAsStream("test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @After
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertThat(tracker.findById(item.getId()), is(item));
    }

    @Test
    public void whenReplaceItemWithSuccess() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        boolean replaceResult = tracker.replace(item.getId(), new Item("new"));
        assertTrue(replaceResult);
    }

    @Test
    public void whenReplaceItemWithFail() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        boolean replaceResult = tracker.replace(5, new Item("new"));
        assertFalse(replaceResult);
    }

    @Test
    public void whenDeleteItemWithSuccess() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        boolean deleteResult = tracker.delete(item.getId());
        assertTrue(deleteResult);
    }

    @Test
    public void whenDeleteItemWithFail() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        boolean deleteResult = tracker.delete(5);
        assertFalse(deleteResult);
    }

    @Test
    public void whenAddItemsThenFindAll() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item item2 = new Item("item2");
        Item item3 = new Item("item3");
        tracker.add(item);
        tracker.add(item2);
        tracker.add(item3);
        List<Item> items = tracker.findAll();
        assertThat(items.size(), is(3));
    }

    @Test
    public void whenAddItemsThenFindByName() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item item2 = new Item("item");
        Item item3 = new Item("item3");
        tracker.add(item);
        tracker.add(item2);
        tracker.add(item3);
        List<Item> items = tracker.findByName("item");
        assertThat(items.size(), is(2));
    }

    @Test
    public void whenAddItemsThenFindByNameWithNoMatches() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item item2 = new Item("item2");
        Item item3 = new Item("item3");
        tracker.add(item);
        tracker.add(item2);
        tracker.add(item3);
        List<Item> items = tracker.findByName("item5");
        assertTrue(items.isEmpty());
    }

    @Test
    public void whenAddItemsThenFindByIdWithNoMatches() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item item2 = new Item("item2");
        Item item3 = new Item("item3");
        tracker.add(item);
        tracker.add(item2);
        tracker.add(item3);
        Item foundItem = tracker.findById(5);
        assertNull(foundItem);
    }
}