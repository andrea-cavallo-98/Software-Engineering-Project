package it.polito.ezshop.unitTests;
import static org.junit.Assert.*;
import it.polito.ezshop.model.UserObject;
import org.junit.Test;

public class UserTest {

    @Test
    public void testId(){
        String username ="test_user";
        String password ="test_password";
        String role="test_role";
        Integer id = 132;
        UserObject user = new UserObject(username,password,role,id);

        assertEquals(user.getId(), id);

        user.setId(id+2);

        assertEquals(user.getId(), Integer.valueOf(id+2));
    }

    @Test
    public void testUsername(){
        String username ="test_user";
        String password ="test_password";
        String role="test_role";
        Integer id = 132;
        UserObject user = new UserObject(username,password,role,id);

        assertEquals(user.getUsername(), username);

        username = "username2";

        user.setUsername(username);

        assertEquals(user.getUsername(), username);
    }

    @Test
    public void testPassword(){
        String username ="test_user";
        String password ="test_password";
        String role="test_role";
        Integer id = 132;
        UserObject user = new UserObject(username,password,role,id);

        assertEquals(user.getPassword(), password);

        password = "password2";

        user.setPassword(password);

        assertEquals(user.getPassword(), password);
    }

    @Test
    public void testRole(){
        String username ="test_user";
        String password ="test_password";
        String role="test_role";
        Integer id = 132;
        UserObject user = new UserObject(username,password,role,id);

        assertEquals(user.getRole(), role);

        role = "role2";

        user.setRole(role);

        assertEquals(user.getRole(), role);
    }

}
