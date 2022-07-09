package cn.hnit.controller;

import cn.hnit.CloudDiskApplication;
import cn.hnit.Template.HadoopTemplate;
import cn.hnit.entity.Do.FileStatusDo;
import cn.hnit.entity.User;
import cn.hnit.service.UserService;
import cn.hnit.utils.AESUtil;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * UserController Tester.
 *
 * @author fengxian
 * @version 1.0
 * @since <pre>06/09/2022</pre>
 */
@SpringBootTest(classes = CloudDiskApplication.class)
@RunWith(SpringRunner.class)
public class UserControllerTest {
    @Autowired
    private HadoopTemplate hadoopTemplate;
    @Autowired
    private UserService userService;
    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: test()
     */
    Function<Object, Integer> actionTest1 = (object) -> {
        // logic
        return 0;
    };

    public void happy(double money, Consumer<Double> con, Consumer<Double> c2 ) {
        if(money ==1){
            con.accept(money);
        }
    }


    @Test
    public void testFunction() {
    }

    private static int getScore(int num,String str) {
        int score = 0;
        for (int i = 0; i < num; i++) {
            score += str.startsWith("1", i) ? 1 : 0;
        }
        return score;
    }

    Function<Object, Integer> actionTest2 = (object) -> {
        // logic
        return 0;
    };

    @Test
    public void testHdfs() throws IOException {
        List<FileStatusDo> fileStatusDos = hadoopTemplate.listStatus("/");
        System.out.println(fileStatusDos);
    }

    @Test
    public void testUser() {
        String pwd = AESUtil.aesEncrypt("admin");
        System.out.println(pwd);
        userService.save(new User("admin",pwd,User.role.ROLE_ADMIN));
    }
}
