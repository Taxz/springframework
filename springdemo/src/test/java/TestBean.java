import com.txz.config.BeanScansConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * description:
 * 测试bean相关属性
 *
 * @author Taxz
 * @create 2019-06-14 11:18
 */
public class TestBean {
    AnnotationConfigApplicationContext context  = new AnnotationConfigApplicationContext(BeanScansConfig.class);

    @Test
    public void testComponetScan() {
        printBeans(context);
    }

    private void printBeans(AnnotationConfigApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        System.out.println(String.join("\n",names));
    }
}
