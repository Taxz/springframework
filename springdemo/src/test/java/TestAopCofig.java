import com.txz.bean.Boss;
import com.txz.config.CustomAopConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StringUtils;

/**
 * description:
 * 测试aop配置
 *
 * @author Taxz
 * @create 2019-06-20 16:19
 */
public class TestAopCofig {

    @Test
    public void testAop() {
        AnnotationConfigApplicationContext annotation = new AnnotationConfigApplicationContext(CustomAopConfig.class);

       /* Boss boss = annotation.getBean(Boss.class);
        boss.work("程序猿需要涨工资");*/

        System.out.println(String.join(",",annotation.getBeanDefinitionNames()));
    }

    public static void main(String[] args) {
        System.out.println("heoll");
    }
}
