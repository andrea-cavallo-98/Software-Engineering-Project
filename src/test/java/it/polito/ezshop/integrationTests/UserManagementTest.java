package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.User;
import it.polito.ezshop.controllers.DB;
import it.polito.ezshop.controllers.UserManagement;
import it.polito.ezshop.exceptions.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("all")
public class UserManagementTest {
    private static final EZShop ezshop = new EZShop();

    @Test
    public void createUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        ezshop.reset();

        assertThrows(InvalidUsernameException.class, () -> UserManagement.createUser(null, null, null));
        assertThrows(InvalidUsernameException.class, () -> UserManagement.createUser("", null, null));
        assertThrows(InvalidPasswordException.class, () -> UserManagement.createUser("username", null, null));
        assertThrows(InvalidPasswordException.class, () -> UserManagement.createUser("username", "", null));
        assertThrows(InvalidRoleException.class, () -> UserManagement.createUser("username", "password", null));
        assertThrows(InvalidRoleException.class, () -> UserManagement.createUser("username", "password", "NotAnAdmissibleRole"));

        DB.alterJDBCUrl();
        assertEquals(Integer.valueOf(-1), UserManagement.createUser("username", "password", "Cashier"));
        DB.restoreJDBCUrl();

        Integer idAdded = UserManagement.createUser("username", "password", "Cashier");
        assertNotEquals(Integer.valueOf(-1), idAdded);

        assertEquals(Integer.valueOf(-1), UserManagement.createUser("username", "password", "Administrator"));

        ezshop.reset();
    }
    @Test
    public void getUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException {
        ezshop.reset();

        assertThrows(InvalidUserIdException.class, () -> UserManagement.getUser(null));
        assertThrows(InvalidUserIdException.class, () -> UserManagement.getUser(0));

        Integer idAdded = UserManagement.createUser("username", "password", "Cashier");

       DB.alterJDBCUrl();
        assertNull(UserManagement.getUser(idAdded));
        DB.restoreJDBCUrl();

        User userRetrieved = UserManagement.getUser(idAdded);
        assertNotNull(userRetrieved);
        assertEquals("username", userRetrieved.getUsername());
        assertEquals("password", userRetrieved.getPassword());
        assertEquals("Cashier", userRetrieved.getRole());
        assertEquals(idAdded, userRetrieved.getId());

        assertNull(UserManagement.getUser(Integer.MAX_VALUE));

        ezshop.reset();
    }
    @Test
    public void getUserByUsernameTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        ezshop.reset();

        assertNull(UserManagement.getUserByUsername("notExistingUsername"));

        Integer idAdded = UserManagement.createUser("username", "password", "Cashier");

       DB.alterJDBCUrl();
        assertNull(UserManagement.getUserByUsername("username"));
        DB.restoreJDBCUrl();

        User userRetrieved = UserManagement.getUserByUsername("username");
        assertNotNull(userRetrieved);
        assertEquals("username", userRetrieved.getUsername());
        assertEquals("password", userRetrieved.getPassword());
        assertEquals("Cashier", userRetrieved.getRole());
        assertEquals(idAdded, userRetrieved.getId());

        ezshop.reset();
    }
    @Test
    public void deleteUserTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException {
        ezshop.reset();

        assertThrows(InvalidUserIdException.class, () -> UserManagement.deleteUser(null));
        assertThrows(InvalidUserIdException.class, () -> UserManagement.deleteUser(0));

        Integer idAdded = UserManagement.createUser("username", "password", "Cashier");
        assertNotEquals(Integer.valueOf(-1), idAdded);

       DB.alterJDBCUrl();
        assertFalse(UserManagement.deleteUser(idAdded));
        DB.restoreJDBCUrl();

        assertTrue(UserManagement.deleteUser(idAdded));
        assertNull(UserManagement.getUser(idAdded));

        ezshop.reset();
    }
    @Test
    public void getAllUsersTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException {
        ezshop.reset();

        Integer[] idsAdded = new Integer[10];
        int i;
        for(i = 0; i < 10; i++) {
            idsAdded[i] = UserManagement.createUser("username" + i, "password" + i, "Cashier");
            assertNotEquals(Integer.valueOf(-1), idsAdded[i]);
        }

       DB.alterJDBCUrl();
        assertNull(UserManagement.getAllUsers());
        DB.restoreJDBCUrl();

        List<User> usersRetrieved = UserManagement.getAllUsers();
        assertNotNull(usersRetrieved);
        assertEquals(10, usersRetrieved.size());
        for(i = 0; i < 10; i++) {
            User userRetrieved = usersRetrieved.get(i);
            assertEquals(idsAdded[i], userRetrieved.getId());
            assertEquals("username" + i, userRetrieved.getUsername());
            assertEquals("password" + i, userRetrieved.getPassword());
            assertEquals("Cashier", userRetrieved.getRole());
        }
        ezshop.reset();
    }
    @Test
    public void updateUserRightsTest() throws SQLException, InvalidPasswordException, InvalidRoleException, InvalidUsernameException, InvalidUserIdException {
        ezshop.reset();

        assertThrows(InvalidUserIdException.class, () -> UserManagement.updateUserRights(null, "Administrator"));
        assertThrows(InvalidUserIdException.class, () -> UserManagement.updateUserRights(0, "Administrator"));
        assertThrows(InvalidRoleException.class, () -> UserManagement.updateUserRights(1, "NotAnAdmissibleRole"));

        Integer idAdded = UserManagement.createUser("username", "password", "Cashier");
        assertNotEquals(Integer.valueOf(-1), idAdded);

       DB.alterJDBCUrl();
        assertFalse(UserManagement.updateUserRights(idAdded, "Administrator"));
        DB.restoreJDBCUrl();

        assertTrue(UserManagement.updateUserRights(idAdded, "Administrator"));
        User userRetrieved = UserManagement.getUser(idAdded);
        assertNotNull(userRetrieved);
        assertEquals("Administrator", userRetrieved.getRole());

        assertFalse(UserManagement.updateUserRights(Integer.MAX_VALUE, "Administrator"));

        ezshop.reset();
    }
}
