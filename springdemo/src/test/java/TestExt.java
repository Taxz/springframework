import com.txz.config.CustomAopConfig;
import com.txz.config.ExtConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * description:
 * 测试扩展类
 *
 * @author Taxz
 * @create 2019-06-24 11:11
 */
public class TestExt {

    @Test
    public void test() {
        AnnotationConfigApplicationContext annotation = new AnnotationConfigApplicationContext(ExtConfig.class);
        System.out.println("...");
    }
}
