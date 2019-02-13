package de.hhu.propra.sharingplatform.faker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import com.github.javafaker.Faker;
import de.hhu.propra.sharingplatform.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import({ItemFaker.class, DataFaker.class, Faker.class})
public class UserFakerTest {

    private UserFaker userFaker;

    @Before
    public void initDataFaker() {
        long seed = 1337;
        Random rnd = new Random();
        rnd.setSeed(seed);
        Faker faker = new Faker(Locale.ENGLISH, rnd);
        userFaker = new UserFaker(faker);
    }

    @Test
    public void createUserTest() {
        User user = userFaker.create();

        assertEquals("Eloisa O'Kon Sr.", user.getName());
        assertEquals("Suite 347 3803 Collins Land, Valentinaview, UT 56193", user.getAddress());
        assertEquals("torrance.daniel@example.com", user.getEmail());
        assertEquals(3, user.getRating());
        assertFalse(user.isBan());
        assertFalse(user.isDeleted());
        assertNotEquals(null, user.getItems());
        assertNotEquals(null, user.getContracts());
        assertNotEquals(null, user.getOffers());
    }

    @Test
    public void createZeroUser() {

        List<User> user = new ArrayList<>();

        userFaker.createUsers(user, 0);

        assertEquals(0, user.size());
    }


    @Test
    public void createItemsTest() {
        List<User> user1 = new ArrayList<>();
        List<User> user2 = new ArrayList<>();

        userFaker.createUsers(user1, 5);
        userFaker.createUsers(user2, 13);

        assertEquals(5, user1.size());
        assertEquals(13, user2.size());
    }
}
